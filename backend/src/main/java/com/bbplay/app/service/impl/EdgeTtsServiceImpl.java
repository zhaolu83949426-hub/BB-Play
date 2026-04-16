package com.bbplay.app.service.impl;

import com.bbplay.app.dto.TtsSynthesizeResponse;
import com.bbplay.app.service.TtsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Edge TTS 服务实现
 * 使用微软 Edge TTS 进行语音合成
 */
@Slf4j
@Service
public class EdgeTtsServiceImpl implements TtsService {

    @Value("${tts.python.path:python}")
    private String pythonPath;

    @Value("${tts.script.path}")
    private String scriptPath;

    @Value("${tts.output.dir}")
    private String outputDir;

    @Value("${tts.audio.url-prefix:/audio/tts}")
    private String audioUrlPrefix;

    @Override
    public TtsSynthesizeResponse synthesize(String text, String voice, Double speed) {
        log.info("Edge TTS 合成请求：text={}, voice={}, speed={}", text, voice, speed);
        
        try {
            // 确保输出目录存在
            Path outputPath = Paths.get(outputDir);
            if (!Files.exists(outputPath)) {
                Files.createDirectories(outputPath);
            }
            
            // 生成唯一文件名
            String fileName = "tts_" + System.currentTimeMillis() + ".mp3";
            String outputFile = outputDir + File.separator + fileName;
            
            // 计算语速参数（Edge TTS 格式：+0%、+10%、-10%）
            String rate = "+0%";
            if (speed != null) {
                int speedPercent = (int) ((speed - 1.0) * 100);
                rate = String.format("%+d%%", speedPercent);
            }
            
            // 设置音色（默认使用晓晓女声）
            String voiceId = (voice != null && !voice.isEmpty()) ? voice : "zh-CN-XiaoxiaoNeural";
            
            // 构建 Python 命令
            ProcessBuilder processBuilder = new ProcessBuilder(
                pythonPath,
                scriptPath,
                text,
                outputFile,
                voiceId,
                rate
            );
            
            // 设置工作目录和环境
            processBuilder.redirectErrorStream(true);
            
            log.info("执行 TTS 命令：{} {} {} {} {} {}", 
                pythonPath, scriptPath, text, outputFile, voiceId, rate);
            
            // 执行命令
            Process process = processBuilder.start();
            
            // 读取输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    log.info("Python 输出: {}", line);
                }
            }
            
            // 等待进程完成
            int exitCode = process.waitFor();
            
            if (exitCode == 0 && new File(outputFile).exists()) {
                // 生成访问 URL
                String audioUrl = audioUrlPrefix + "/" + fileName;
                
                // 估算音频时长（每个字约 0.5 秒）
                int duration = (int) (text.length() * 0.5);
                
                TtsSynthesizeResponse response = new TtsSynthesizeResponse();
                response.setAudioUrl(audioUrl);
                response.setDuration(duration);
                
                log.info("TTS 合成成功：audioUrl={}, duration={}", audioUrl, duration);
                return response;
            } else {
                String errorMsg = "TTS 合成失败，退出码：" + exitCode + "，输出：" + output;
                log.error(errorMsg);
                throw new RuntimeException(errorMsg);
            }
            
        } catch (Exception e) {
            log.error("TTS 合成异常", e);
            throw new RuntimeException("TTS 合成失败: " + e.getMessage(), e);
        }
    }
}
