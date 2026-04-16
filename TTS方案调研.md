# TTS 方案调研报告

## 方案对比

### 阿里云 TTS
- **费用**：前三个月免费，之后收费
- **音质**：优秀
- **稳定性**：高
- **结论**：不适合长期使用

### Edge TTS（推荐方案）⭐
- **费用**：完全免费，无使用限制
- **音质**：优秀，适合儿童内容
- **稳定性**：高（使用微软 Edge 浏览器的在线 TTS 服务）
- **延迟**：1.5-2 秒合成时间，页面总延迟 2-3 秒
- **结论**：最佳选择

## Edge TTS 详细信息

### 可用音色（14个中文音色）

#### 普通话（中国大陆）- 6个
- **zh-CN-XiaoxiaoNeural** (女声) - 晓晓 ⭐推荐用于儿童绘本
- **zh-CN-XiaoyiNeural** (女声) - 晓伊
- **zh-CN-YunjianNeural** (男声) - 云健
- **zh-CN-YunxiNeural** (男声) - 云希
- **zh-CN-YunxiaNeural** (男声) - 云夏
- **zh-CN-YunyangNeural** (男声) - 云扬

#### 方言
- **zh-CN-liaoning-XiaobeiNeural** (女声) - 东北话
- **zh-CN-shaanxi-XiaoniNeural** (女声) - 陕西话

#### 粤语（香港）- 3个
- zh-HK-HiuGaaiNeural (女声)
- zh-HK-HiuMaanNeural (女声)
- zh-HK-WanLungNeural (男声)

#### 国语（台湾）- 3个
- zh-TW-HsiaoChenNeural (女声)
- zh-TW-YunJheNeural (男声)
- zh-TW-HsiaoYuNeural (女声)

### 性能测试结果

#### 转换效率
- **2字文本**：1.44秒
- **16字文本**：1.43秒
- **45字文本**：2.00秒
- **124字文本**：1.98秒

#### 不同音色性能
- 晓晓(女)：1.33秒
- 云希(男)：1.73秒
- 晓伊(女)：1.49秒
- 云健(男)：1.65秒

#### 并发性能
- 5个请求并发执行总耗时：1.77秒
- 平均每个请求：0.35秒

### 页面延迟估算

**纯合成时间**：
- 短文本(10字内)：约 1.4秒
- 中等文本(30-50字)：约 2.0秒
- 长文本(100字)：约 2.0秒

**实际页面延迟** = 合成时间 + 网络传输 + 文件上传/存储

**预计总延迟**：
- 短文本：1-2秒
- 中等文本：2-3秒
- 长文本：3-5秒

## 集成方案

### 技术架构
当前项目是 **Spring Boot (Java) 后端 + Vue 前端**，Edge TTS 是 Python 库。

### 集成方式：Java 调用 Python 脚本

通过 Java 的 `ProcessBuilder` 调用 Python 脚本生成音频文件。

#### 优点
- 利用 Edge TTS 的免费优势
- 实现相对简单
- 无需修改现有架构
- 音质好，适合儿童内容

#### 实现步骤

1. **服务器环境准备**
   ```bash
   # 安装 Python 3.9+
   # 安装 edge-tts
   pip install edge-tts
   ```

2. **创建 Python TTS 脚本**
   ```python
   # tts_synthesize.py
   import asyncio
   import edge_tts
   import sys
   
   async def synthesize(text, output_file, voice="zh-CN-XiaoxiaoNeural", rate="+0%"):
       communicate = edge_tts.Communicate(text, voice, rate=rate)
       await communicate.save(output_file)
   
   if __name__ == "__main__":
       text = sys.argv[1]
       output_file = sys.argv[2]
       voice = sys.argv[3] if len(sys.argv) > 3 else "zh-CN-XiaoxiaoNeural"
       rate = sys.argv[4] if len(sys.argv) > 4 else "+0%"
       
       asyncio.run(synthesize(text, output_file, voice, rate))
   ```

3. **Java 服务实现**
   ```java
   @Service
   public class EdgeTtsServiceImpl implements AliyunTtsService {
       
       @Value("${tts.python.path:python}")
       private String pythonPath;
       
       @Value("${tts.script.path}")
       private String scriptPath;
       
       @Value("${tts.output.dir}")
       private String outputDir;
       
       @Override
       public TtsSynthesizeResponse synthesize(String text, String voice, Double speed) {
           try {
               // 生成临时文件路径
               String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
               String outputPath = outputDir + File.separator + fileName;
               
               // 计算语速参数
               String rate = speed != null ? 
                   String.format("%+d%%", (int)((speed - 1) * 100)) : "+0%";
               
               // 设置音色
               String voiceId = voice != null ? voice : "zh-CN-XiaoxiaoNeural";
               
               // 构建命令
               ProcessBuilder pb = new ProcessBuilder(
                   pythonPath,
                   scriptPath,
                   text,
                   outputPath,
                   voiceId,
                   rate
               );
               
               // 执行命令
               Process process = pb.start();
               int exitCode = process.waitFor();
               
               if (exitCode == 0 && new File(outputPath).exists()) {
                   // 上传到对象存储或返回本地路径
                   String audioUrl = uploadToStorage(outputPath);
                   
                   TtsSynthesizeResponse response = new TtsSynthesizeResponse();
                   response.setAudioUrl(audioUrl);
                   response.setDuration(estimateDuration(text));
                   
                   return response;
               } else {
                   throw new RuntimeException("TTS 合成失败");
               }
           } catch (Exception e) {
               log.error("TTS 合成失败", e);
               throw new RuntimeException("TTS 合成失败: " + e.getMessage());
           }
       }
       
       private int estimateDuration(String text) {
           // 简单估算：每个字约 0.5 秒
           return text.length() / 2;
       }
       
       private String uploadToStorage(String filePath) {
           // TODO: 上传到对象存储（OSS/COS）或返回本地访问路径
           // 这里简化处理，返回相对路径
           return "/audio/" + new File(filePath).getName();
       }
   }
   ```

4. **配置文件**
   ```yaml
   # application.yml
   tts:
     python:
       path: python  # 或指定完整路径，如 /usr/bin/python3
     script:
       path: /path/to/tts_synthesize.py
     output:
       dir: /tmp/tts_output
   ```

### 注意事项

1. **服务器环境**
   - 确保服务器安装 Python 3.9+
   - 安装 edge-tts：`pip install edge-tts`
   - 确保 Python 可执行文件在 PATH 中

2. **文件存储**
   - 生成的音频文件需要存储（本地或对象存储）
   - 建议使用对象存储（阿里云 OSS、腾讯云 COS）
   - 设置音频文件的访问权限和过期时间

3. **性能优化**
   - 考虑使用缓存，相同文本不重复生成
   - 可以异步处理 TTS 请求
   - 监控 Python 进程，避免僵尸进程

4. **错误处理**
   - 捕获 Python 脚本执行错误
   - 处理网络超时
   - 提供降级方案

## 测试验证

已在本地环境完成测试：
- ✅ Edge TTS 安装成功
- ✅ 中文语音合成正常
- ✅ 支持多种音色
- ✅ 支持语速调节
- ✅ 性能满足要求
- ✅ 并发处理正常

测试文件位置：
- `test_edge_tts.py` - 基本功能测试
- `test_edge_tts_performance.py` - 性能测试

## 推荐配置

**默认音色**：zh-CN-XiaoxiaoNeural（晓晓女声）
**默认语速**：1.0（正常速度）
**音频格式**：MP3
**采样率**：24kHz

## 总结

Edge TTS 是当前最佳的免费 TTS 方案：
1. ✅ 完全免费，无使用限制
2. ✅ 音质优秀，适合儿童绘本
3. ✅ 14个中文音色可选
4. ✅ 合成速度快（1.5-2秒）
5. ✅ 支持并发处理
6. ✅ 集成简单，无需修改架构
7. ✅ 页面延迟可接受（2-3秒）

建议立即采用此方案替代阿里云 TTS。
