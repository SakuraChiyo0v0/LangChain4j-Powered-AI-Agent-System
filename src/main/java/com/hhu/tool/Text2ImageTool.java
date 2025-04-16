package com.hhu.tool;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisListResult;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.task.AsyncTaskListParam;
import com.alibaba.dashscope.tools.DashScopePluginBase;
import com.alibaba.dashscope.utils.JsonUtils;
import com.hhu.assistant.AssistantService;
import com.hhu.assistant.IAssistant.Text2ImageAssistant;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
@Slf4j
@RequiredArgsConstructor
public class Text2ImageTool {

    @Tool("根据文本描述生成图片，可以指定图片风格")
    public String generateImage(
            @P("图片的详细描述，例如'一只可爱的橘猫在阳光下玩耍',也要推断用户可能想要的风格,生成一段提示词") String prompt) {
        
        log.info("开始生成图片，描述：{}", prompt);
        
        try {
            // 构建系统提示词
            String systemPrompt = "你是一个专业的AI绘画助手，请根据用户的描述生成合适的图片:\n";

            ImageSynthesisParam param =
                    ImageSynthesisParam.builder()
                            .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                            .model("wanx2.1-t2i-turbo")
                            .prompt(systemPrompt+prompt)
                            .n(1)
                            .size("1024*1024")
                            .build();

            ImageSynthesis imageSynthesis = new ImageSynthesis();
            ImageSynthesisResult result = null;

            log.info("正在生成图片......");
            result = imageSynthesis.call(param);
            System.out.println(JsonUtils.toJson(result));
            return JsonUtils.toJson(result);

        } catch (Exception e) {
            String error = "图片生成过程中发生错误：" + e.getMessage();
            log.error(error, e);
            return error;
        }
    }

}
