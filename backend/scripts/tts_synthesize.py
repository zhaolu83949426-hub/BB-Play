#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Edge TTS 语音合成脚本
用于将文本转换为语音文件
"""

import asyncio
import edge_tts
import sys
import os


async def synthesize(text, output_file, voice="zh-CN-XiaoxiaoNeural", rate="+0%"):
    """
    使用 Edge TTS 合成语音
    
    Args:
        text: 要合成的文本
        output_file: 输出文件路径
        voice: 音色ID，默认为晓晓女声
        rate: 语速调整，格式如 "+0%"、"+10%"、"-10%"
    """
    try:
        # 确保输出目录存在
        output_dir = os.path.dirname(output_file)
        if output_dir and not os.path.exists(output_dir):
            os.makedirs(output_dir, exist_ok=True)
        
        # 创建通信对象并合成
        communicate = edge_tts.Communicate(text, voice, rate=rate)
        await communicate.save(output_file)
        
        print(f"SUCCESS: {output_file}")
        return 0
    except Exception as e:
        print(f"ERROR: {str(e)}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python tts_synthesize.py <text> <output_file> [voice] [rate]", file=sys.stderr)
        sys.exit(1)
    
    text = sys.argv[1]
    output_file = sys.argv[2]
    voice = sys.argv[3] if len(sys.argv) > 3 else "zh-CN-XiaoxiaoNeural"
    rate = sys.argv[4] if len(sys.argv) > 4 else "+0%"
    
    exit_code = asyncio.run(synthesize(text, output_file, voice, rate))
    sys.exit(exit_code)
