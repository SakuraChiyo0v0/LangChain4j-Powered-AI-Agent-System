package com.hhu.tool;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;


@Component
@Slf4j
@RequiredArgsConstructor
public class Image2TextTool {
    @Tool("分析图片内容并生成文字描述，支持图片URL或Base64编码")
    public String analyzeImage(
            @P("图片的URL地址或Base64编码数据") String imageInput,
            @P(value = "用户对图片的具体问题或指令，例如'描述这张图片的内容'或'图中有什么动物'", required = false) String question) {

        log.info("开始分析图片，输入：{}，问题：{}", imageInput, question);

        try {
            // 1. 构建多模态消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            Collections.singletonMap("image", imageInput),
                            Collections.singletonMap("text", question != null ? question : "请描述这张图片")
                    ))
                    .build();

            // 2. 配置参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                    .model("qwen-vl-plus") // qvq模型
                    .messages(Collections.singletonList(userMessage))
                    .build();

            // 3. 调用API
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalConversationResult result = conv.call(param);

            // 4. 提取并返回文本结果
            String description = extractTextContent(result);
            log.info("图片分析完成，结果：{}", description);
            return "图片分析完成，结果:" + description;

        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            String error = "图片分析失败：" + e.getMessage();
            log.error(error, e);
            return error;
        }
    }

    private String extractTextContent(MultiModalConversationResult result) {
        // 从复杂响应结构中提取文本内容
        return result.getOutput().getChoices().get(0).getMessage().getContent()
                .stream()
                .filter(item -> item.containsKey("text"))
                .map(item -> (String) item.get("text"))
                .findFirst()
                .orElse("未能提取有效描述");
    }
}
