const coverCache = new Map<string, string>();
const pendingTasks = new Map<string, Promise<string>>();
const PRIVATE_NETWORK_PATTERN = /^(10\.|192\.168\.|172\.(1[6-9]|2\d|3[0-1])\.|127\.|localhost$)/;

function clearVideoSource(video: HTMLVideoElement) {
  video.pause();
  video.removeAttribute('src');
  video.load();
}

export function prepareVideoPreview(video: HTMLVideoElement) {
  if (!Number.isFinite(video.duration) || video.duration <= 0) {
    return;
  }
  const targetTime = video.duration > 1 ? 1 : 0;
  const handleSeeked = () => {
    video.pause();
    video.removeEventListener('seeked', handleSeeked);
  };
  video.addEventListener('seeked', handleSeeked);
  try {
    video.currentTime = targetTime;
  } catch {
    video.removeEventListener('seeked', handleSeeked);
  }
}

function parseVideoUrl(playUrl: string) {
  try {
    return new URL(playUrl, window.location.href);
  } catch {
    return null;
  }
}

export function isPrivateNetworkVideo(playUrl: string) {
  const url = parseVideoUrl(playUrl);
  if (!url) {
    return false;
  }
  return PRIVATE_NETWORK_PATTERN.test(url.hostname);
}

export function canExtractVideoCover(playUrl: string) {
  if (!playUrl) {
    return false;
  }
  if (playUrl.startsWith('blob:') || playUrl.startsWith('data:')) {
    return true;
  }
  const url = parseVideoUrl(playUrl);
  if (!url) {
    return false;
  }
  return url.origin === window.location.origin;
}

export function extractVideoCover(playUrl: string): Promise<string> {
  if (!canExtractVideoCover(playUrl)) {
    return Promise.resolve('');
  }
  const cached = coverCache.get(playUrl);
  if (cached) {
    return Promise.resolve(cached);
  }
  const pending = pendingTasks.get(playUrl);
  if (pending) {
    return pending;
  }
  const task = new Promise<string>((resolve) => {
    const video = document.createElement('video');
    let done = false;
    let timer = 0;
    const finish = (result: string) => {
      if (done) {
        return;
      }
      done = true;
      window.clearTimeout(timer);
      clearVideoSource(video);
      if (result) {
        coverCache.set(playUrl, result);
      }
      resolve(result);
    };
    const capture = () => {
      if (!video.videoWidth || !video.videoHeight) {
        finish('');
        return;
      }
      const canvas = document.createElement('canvas');
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      const context = canvas.getContext('2d');
      if (!context) {
        finish('');
        return;
      }
      try {
        context.drawImage(video, 0, 0, canvas.width, canvas.height);
        finish(canvas.toDataURL('image/jpeg', 0.82));
      } catch {
        finish('');
      }
    };
    video.preload = 'metadata';
    video.muted = true;
    video.playsInline = true;
    video.crossOrigin = 'anonymous';
    video.addEventListener('error', () => finish(''), { once: true });
    video.addEventListener(
      'loadeddata',
      () => {
        const targetTime = video.duration > 1 ? 1 : 0;
        if (targetTime <= 0) {
          capture();
          return;
        }
        video.addEventListener('seeked', capture, { once: true });
        try {
          video.currentTime = targetTime;
        } catch {
          capture();
        }
      },
      { once: true }
    );
    timer = window.setTimeout(() => finish(''), 8000);
    video.src = playUrl;
    video.load();
  }).finally(() => {
    pendingTasks.delete(playUrl);
  });
  pendingTasks.set(playUrl, task);
  return task;
}
