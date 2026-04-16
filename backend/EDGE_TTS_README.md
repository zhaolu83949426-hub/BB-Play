# Edge TTS 集成说明

## 概述

项目已从阿里云 TTS 切换到 Edge TTS（微软 Edge 浏览器的免费 TTS 服务）。

## 优势

- ✅ 完全免费，无使用限制
- ✅ 音质优秀，适合儿童绘本内容
- ✅ 14个中文音色可选
- ✅ 合成速度快（1.5-2秒）
- ✅ 支持语速调节

## 环境准备

### 1. 安装 Python 3.9+

确保服务器已安装 Python 3.9 或更高版本：

```bash
python --version
# 或
python3 --version
```

### 2. 安装 Edge TTS

```bash
pip install edge-tts
# 或
pip3 install edge-tts
```

### 3. 验证安装

```bash
edge-tts --list-voices | grep zh-CN
```

应该能看到中文音色列表。

## 配置说明

### application.yml 配置项

```yaml
tts:
  python:
    path: python  # Python 可执行文件路径，默认为 python
  script:
    path: backend/scripts/tts_synthesize.py  # TTS 脚本路径
  output:
    dir: backend/audio/tts  # 音频文件输出目录
  audio:
    url-prefix: /audio/tts  # 音频文件访问 URL 前缀
```

### 环境变量（可选）

可以通过环境变量覆盖默认配置：

- `TTS_PYTHON_PATH`: Python 可执行文件路径
- `TTS_SCRIPT_PATH`: TTS 脚本路径
- `TTS_OUTPUT_DIR`: 音频输出目录
- `TTS_AUDIO_URL_PREFIX`: 音频访问 URL 前缀

## 可用音色

### 推荐音色（儿童绘本）

- **zh-CN-XiaoxiaoNeural** (女声) - 晓晓 ⭐ 默认音色，最适合儿童内容

### 其他普通话音色

- zh-CN-XiaoyiNeural (女声) - 晓伊
- zh-CN-YunjianNeural (男声) - 云健
- zh-CN-YunxiNeural (男声) - 云希
- zh-CN-YunxiaNeural (男声) - 云夏
- zh-CN-YunyangNeural (男声) - 云扬

### 方言音色

- zh-CN-liaoning-XiaobeiNeural (女声) - 东北话
- zh-CN-shaanxi-XiaoniNeural (女声) - 陕西话

## API 使用

### 请求示例

```bash
POST /api/tts/synthesize
Content-Type: application/json

{
  "text": "这是一个测试文本",
  "voice": "zh-CN-XiaoxiaoNeural",
  "speed": 1.0
}
```

### 参数说明

- `text` (必填): 要合成的文本内容
- `voice` (可选): 音色ID，默认为 zh-CN-XiaoxiaoNeural
- `speed` (可选): 语速倍数，默认为 1.0
  - 0.5 = 慢速（-50%）
  - 1.0 = 正常速度
  - 1.5 = 快速（+50%）

### 响应示例

```json
{
  "audioUrl": "/audio/tts/tts_1234567890.mp3",
  "duration": 5
}
```

## 文件结构

```
backend/
├── scripts/
│   └── tts_synthesize.py          # Edge TTS Python 脚本
├── audio/
│   └── tts/                       # 音频文件输出目录（自动创建）
│       └── tts_*.mp3              # 生成的音频文件
└── src/main/java/com/bbplay/app/
    ├── service/
    │   ├── AliyunTtsService.java  # TTS 服务接口
    │   └── impl/
    │       └── AliyunTtsServiceImpl.java  # Edge TTS 实现
    └── config/
        └── WebConfig.java         # 静态资源映射配置
```

## 故障排查

### 1. Python 命令找不到

**错误**: `Cannot run program "python"`

**解决方案**:
- 确认 Python 已安装并在 PATH 中
- 或在配置中指定完整路径：
  ```yaml
  tts:
    python:
      path: /usr/bin/python3  # Linux/Mac
      # 或
      path: C:\Python39\python.exe  # Windows
  ```

### 2. edge-tts 模块找不到

**错误**: `ModuleNotFoundError: No module named 'edge_tts'`

**解决方案**:
```bash
pip install edge-tts
```

### 3. 音频文件无法访问

**错误**: 404 Not Found

**解决方案**:
- 确认 `tts.output.dir` 目录存在且有写权限
- 确认 WebConfig 中的静态资源映射配置正确
- 检查生成的音频文件是否存在

### 4. 合成超时

**解决方案**:
- 检查网络连接（Edge TTS 需要访问微软服务器）
- 减少文本长度
- 检查 Python 进程是否正常

## 性能优化建议

### 1. 缓存机制

相同文本可以缓存音频文件，避免重复生成：

```java
// 可以基于文本内容的 MD5 生成文件名
String cacheKey = DigestUtils.md5Hex(text + voice + speed);
String cachedFile = outputDir + "/" + cacheKey + ".mp3";
if (new File(cachedFile).exists()) {
    return cachedAudioUrl;
}
```

### 2. 异步处理

对于非实时场景，可以异步生成音频：

```java
@Async
public CompletableFuture<TtsSynthesizeResponse> synthesizeAsync(...)
```

### 3. 定期清理

定期清理过期的音频文件，节省存储空间：

```bash
# 删除 7 天前的文件
find backend/audio/tts -name "tts_*.mp3" -mtime +7 -delete
```

## 迁移说明

### 从阿里云 TTS 迁移

1. ✅ 已删除阿里云 TTS SDK 依赖
2. ✅ 已更新 Service 实现为 Edge TTS
3. ✅ 接口保持兼容，前端无需修改
4. ✅ 音色参数需要更新为 Edge TTS 音色ID

### 音色映射参考

如果之前使用阿里云音色，可以映射到 Edge TTS：

| 阿里云音色 | Edge TTS 音色 | 说明 |
|----------|--------------|------|
| Aixia (女声) | zh-CN-XiaoxiaoNeural | 晓晓女声 |
| Aitong (女声) | zh-CN-XiaoyiNeural | 晓伊女声 |
| Aiyue (男声) | zh-CN-YunxiNeural | 云希男声 |

## 测试

### 手动测试 Python 脚本

```bash
cd backend
python scripts/tts_synthesize.py "测试文本" "test.mp3" "zh-CN-XiaoxiaoNeural" "+0%"
```

### 测试 API 接口

```bash
curl -X POST http://localhost:18180/api/tts/synthesize \
  -H "Content-Type: application/json" \
  -d '{"text":"这是一个测试","voice":"zh-CN-XiaoxiaoNeural","speed":1.0}'
```

## 注意事项

1. **网络要求**: Edge TTS 需要访问微软服务器，确保服务器可以访问外网
2. **文本长度**: 建议单次合成文本不超过 500 字
3. **并发限制**: 虽然支持并发，但建议控制并发数量避免过载
4. **文件清理**: 定期清理生成的音频文件，避免占用过多存储空间
5. **错误处理**: 做好异常处理和降级方案

## 相关文档

- [Edge TTS GitHub](https://github.com/rany2/edge-tts)
- [TTS方案调研.md](../TTS方案调研.md) - 详细的方案对比和性能测试
