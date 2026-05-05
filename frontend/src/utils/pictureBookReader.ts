import { computed, ref, type ComputedRef, type Ref } from 'vue'
import type { PictureBookPage } from '../types/pictureBook'

export type TurnDirection = 'prev' | 'next' | 'none'

const SPREAD_SIZE = 2
const TURN_DURATION_MS = 360

interface TurnOptions {
  index: number
  autoPlay: boolean
}

interface ReaderOptions {
  spreads: ComputedRef<PictureBookPage[][]>
  onAudioError: () => void
  onNoAudio: () => void
}

interface AudioOptions {
  getSpread: () => PictureBookPage[]
  onSpreadFinished: () => void
  onAudioError: () => void
  onNoAudio: () => void
}

interface TurnControllerOptions {
  spreads: ComputedRef<PictureBookPage[][]>
  currentSpreadIndex: Ref<number>
  stopPlayback: () => void
  startPlayback: () => void
}

type AudioController = ReturnType<typeof createAudioController>
type TurnController = ReturnType<typeof createTurnController>

interface ReaderResultOptions {
  currentSpreadIndex: Ref<number>
  currentSpread: ComputedRef<PictureBookPage[]>
  audio: AudioController
  turner: TurnController
  resetReader: () => void
  destroyReader: () => void
}

export function buildPictureBookSpreads(pages: PictureBookPage[]) {
  const ordered = [...pages].sort((left, right) => left.pageNumber - right.pageNumber)
  const result: PictureBookPage[][] = []
  for (let index = 0; index < ordered.length; index += SPREAD_SIZE) {
    result.push(ordered.slice(index, index + SPREAD_SIZE))
  }
  return result
}

function getAudioQueue(spread: PictureBookPage[]) {
  return spread.map((page) => page.audioUrl?.trim()).filter(Boolean) as string[]
}

function createAudioController(options: AudioOptions) {
  const isPlaying = ref(false)
  let currentAudio: HTMLAudioElement | null = null
  let currentQueue: string[] = []
  let currentQueueIndex = 0

  function stopPlayback() {
    if (currentAudio) {
      currentAudio.pause()
      currentAudio.src = ''
      currentAudio = null
    }
    currentQueue = []
    currentQueueIndex = 0
    isPlaying.value = false
  }

  function handleAudioError() {
    stopPlayback()
    options.onAudioError()
  }

  function playAudio(url: string) {
    currentAudio = new Audio(url)
    currentAudio.onended = handleAudioEnded
    currentAudio.onerror = handleAudioError
    currentAudio.play().then(() => {
      isPlaying.value = true
    }).catch(handleAudioError)
  }

  function handleAudioEnded() {
    if (currentQueueIndex < currentQueue.length - 1) {
      currentQueueIndex += 1
      playAudio(currentQueue[currentQueueIndex])
      return
    }
    options.onSpreadFinished()
  }

  function startPlayback() {
    const queue = getAudioQueue(options.getSpread())
    if (!queue.length) {
      options.onNoAudio()
      return
    }
    stopPlayback()
    currentQueue = queue
    playAudio(currentQueue[0])
  }

  return { isPlaying, startPlayback, stopPlayback }
}

function createTurnController(options: TurnControllerOptions) {
  const isTurning = ref(false)
  const nextSpreadIndex = ref<number>()
  const turningDirection = ref<TurnDirection>('none')
  let turnTimer: number | undefined

  function resetTurnState() {
    isTurning.value = false
    nextSpreadIndex.value = undefined
    turningDirection.value = 'none'
  }

  function clearTurnTimer() {
    if (!turnTimer) {
      return
    }
    window.clearTimeout(turnTimer)
    turnTimer = undefined
  }

  function finishTurn(targetIndex: number, autoPlay: boolean) {
    options.currentSpreadIndex.value = targetIndex
    resetTurnState()
    if (autoPlay) {
      options.startPlayback()
    }
  }

  function turnToSpread({ index, autoPlay }: TurnOptions) {
    if (index < 0 || index >= options.spreads.value.length || isTurning.value) {
      return
    }
    options.stopPlayback()
    clearTurnTimer()
    nextSpreadIndex.value = index
    turningDirection.value = index > options.currentSpreadIndex.value ? 'next' : 'prev'
    isTurning.value = true
    turnTimer = window.setTimeout(() => finishTurn(index, autoPlay), getTurnDuration())
  }

  function resetTurner() {
    clearTurnTimer()
    options.currentSpreadIndex.value = 0
    resetTurnState()
  }

  return { isTurning, nextSpreadIndex, turningDirection, turnToSpread, resetTurner, clearTurnTimer }
}

function getTurnDuration() {
  return window.matchMedia('(prefers-reduced-motion: reduce)').matches ? 0 : TURN_DURATION_MS
}

function buildReaderResult(options: ReaderResultOptions) {
  const { currentSpreadIndex, currentSpread, audio, turner } = options
  return {
    currentSpreadIndex,
    currentSpread,
    isPlaying: audio.isPlaying,
    isTurning: turner.isTurning,
    nextSpreadIndex: turner.nextSpreadIndex,
    turningDirection: turner.turningDirection,
    canPlayCurrentSpread: computed(() => currentSpread.value.some((page) => Boolean(page.audioUrl))),
    startPlayback: audio.startPlayback,
    stopPlayback: audio.stopPlayback,
    togglePlayback: () => audio.isPlaying.value ? audio.stopPlayback() : audio.startPlayback(),
    turnToSpread: turner.turnToSpread,
    goPrevSpread: () => turner.turnToSpread({ index: currentSpreadIndex.value - 1, autoPlay: true }),
    goNextSpread: () => turner.turnToSpread({ index: currentSpreadIndex.value + 1, autoPlay: true }),
    resetReader: options.resetReader,
    destroyReader: options.destroyReader
  }
}

export function usePictureBookReader(options: ReaderOptions) {
  const currentSpreadIndex = ref(0)
  const currentSpread = computed(() => options.spreads.value[currentSpreadIndex.value] ?? [])
  let turnToNextAfterAudio = () => undefined
  const audio = createAudioController({
    getSpread: () => currentSpread.value,
    onSpreadFinished: () => turnToNextAfterAudio(),
    onAudioError: options.onAudioError,
    onNoAudio: options.onNoAudio
  })
  const turner = createTurnController({
    spreads: options.spreads,
    currentSpreadIndex,
    stopPlayback: audio.stopPlayback,
    startPlayback: audio.startPlayback
  })

  turnToNextAfterAudio = () => {
    const nextIndex = currentSpreadIndex.value + 1
    if (nextIndex >= options.spreads.value.length) {
      audio.stopPlayback()
      return
    }
    turner.turnToSpread({ index: nextIndex, autoPlay: true })
  }

  function resetReader() {
    audio.stopPlayback()
    turner.resetTurner()
  }

  function destroyReader() {
    audio.stopPlayback()
    turner.clearTurnTimer()
  }

  return buildReaderResult({
    currentSpreadIndex,
    currentSpread,
    audio,
    turner,
    resetReader,
    destroyReader
  })
}
