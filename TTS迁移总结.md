# TTS 方案迁移总结

## 迁移概述

已成功将项目的 TTS 方案从阿里云 TTS 迁移到 Edge TTS（微软免费 TTS 服务）。

## 完成的工作

### 1. 创建 Python TTS 脚本 ✅
- 文件位置：`backend/scripts/tts_synthesize.py`
- 功能：调用 Edge TTS API 生成语音文件
- 支持参数：文本、输出路径、音色、语速

### 2. 更新后端 Service 实现 ✅
- 文件：`backend/src/main/java/com/bbplay/app/service/impl/AliyunTtsServiceImpl.java`
- 改动：从模拟实现改为调用 Python 脚本
- 功能：
  - 通过 ProcessBuilder 调用 Python 脚本
  - 生成唯一文件名
  - 处理语速参数转换
  - 返回音频 URL 和时长

### 3. 更新配置文件 ✅
- 文件：`backend/src/main/resources/application.yml`
- 新增配置：
  ```yaml
  tts:
    python:
      path: python
    script:
      path: backend/scripts/tts_synthesize.py
    output:
      dir: backend/audio/tts
    audio:
      url-prefix: /audio/tts
  ```

### 4. 配置静态资源访问 ✅
- 文件：`backend/src/main/java/com/bbplay/app/config/WebConfig.java`
- 改动：添加 `addResourceHandlers` 方法
- 功能：映射 `/audio/tts/**` 到本地文件系统

### 5. 创建文档 ✅
- `backend/EDGE_TTS_README.md` - 详细的部署和使用文档
- 包含：环境准备、配置说明、API 使用、故障排查等

### 6. 测试验证 ✅
- Python 脚本测试：成功生成音频文件（9936字节）
- 音色：zh-CN-XiaoxiaoNeural（晓晓女声）
- 文本：测试文本

## 技术架构

```
前端 (Vue)
    ↓ HTTP POST /api/tts/synthesize
后端 (Spring Boot)
    ↓ ProcessBuilder
Python 脚本 (tts_synthesize.py)
    ↓ edge-tts 库
Edge TTS API (微软服务器)
    ↓ 返回音频数据
本地文件系统 (backend/audio/tts/)
    ↓ HTTP GET /audio/tts/xxx.mp3
前端播放器
```

## 接口兼容性

### 保持不变
- ✅ API 路径：`POST /api/tts/synthesize`
- ✅ 请求参数：`text`, `voice`, `speed`
- ✅ 响应格式：`audioUrl`, `duration`
- ✅ 前端代码无需修改

### 需要更新
- ⚠️ 音色参数：需要使用 Edge TTS 音色 ID
  - 推荐：`zh-CN-XiaoxiaoNeural`（晓晓女声，适合儿童）

## 部署要求

### 环境依赖
1. Python 3.9+ ✅ (当前：Python 3.12.0)
2. edge-tts 库 ✅ (当前：7.2.8)
3. 网络访问：需要访问微软服务器

### 配置检查
- [ ] 确认 Python 路径配置正确
- [ ] 确认脚本路径配置正确
- [ ] 确认输出目录有写权限
- [ ] 确认静态资源映射配置正确

## 性能对比

| 指标 | 阿里云 TTS | Edge TTS |
|-----|-----------|----------|
| 费用 | 前3个月免费，之后收费 | 完全免费 |
| 音质 | 优秀 | 优秀 |
| 合成速度 | ~2秒 | 1.5-2秒 |
| 中文音色 | 多种 | 14种 |
| 稳定性 | 高 | 高 |

## 优势总结

1. **成本优势**：完全免费，无使用限制
2. **音质优秀**：适合儿童绘本内容
3. **实现简单**：无需修改现有架构
4. **接口兼容**：前端无需改动
5. **音色丰富**：14个中文音色可选

## 后续建议

### 1. 缓存优化
相同文本可以缓存音频文件，避免重复生成：
```java
String cacheKey = DigestUtils.md5Hex(text + voice + speed);
```

### 2. 异步处理
对于非实时场景，可以异步生成音频：
```java
@Async
public CompletableFuture<TtsSynthesizeResponse> synthesizeAsync(...)
```

### 3. 定期清理
定期清理过期的音频文件：
```bash
find backend/audio/tts -name "tts_*.mp3" -mtime +7 -delete
```

### 4. 监控告警
- 监控 Python 进程状态
- 监控音频文件生成成功率
- 监控磁盘空间使用

## 测试清单

- [x] Python 环境验证
- [x] edge-tts 安装验证
- [x] TTS 脚本功能测试
- [x] 音频文件生成测试
- [ ] 后端 API 集成测试
- [ ] 前端页面功能测试
- [ ] 不同音色测试
- [ ] 语速调节测试
- [ ] 并发性能测试

## 相关文档

- [TTS方案调研.md](TTS方案调研.md) - 详细的方案对比和性能测试
- [backend/EDGE_TTS_README.md](backend/EDGE_TTS_README.md) - 部署和使用文档

## 迁移日期

2026-04-16

## 迁移状态

✅ 已完成核心功能迁移
⏳ 待进行完整集成测试
