package com.hhu.controller;


import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.tokenizers.TokenizationUsage;
import com.hhu.Service.IMemoryService;
import com.hhu.assistant.AssistantService;
import com.hhu.assistant.IAssistant.StreamingSmartAssistant;
import com.hhu.entity.AgentRequest;
import com.hhu.entity.Message;
import com.hhu.tool.Image2TextTool;
import com.hhu.tool.Text2ImageTool;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
@Slf4j
public class AssistantController {

    final AssistantService assistantService;

    final IMemoryService memoryService;

    final Snowflake snowflake = new Snowflake(1, 1);
    private final Text2ImageTool text2ImageTool;
    private final Image2TextTool image2TextTool;


    @GetMapping("/ez")
    public String ezChat(@RequestParam(value = "message") String message) {
        log.info("用户发起普通聊天: {}", message);
        return assistantService.ChatAssistant().chat(message);
    }

    final StreamingChatLanguageModel streamingChatLanguageModel;

    @GetMapping("/stream")
    public Flux<String> stream(@RequestParam("message") String message
    ) {
        StreamingSmartAssistant streamingAssistant = assistantService.StreamingAssistant();
        return streamingAssistant.chat(message);
    }

    @GetMapping("/stream/ez")
    public Flux<String> streamEz(@RequestParam("message") String message,
                                 @RequestParam(value = "sessionId") String sessionId,
                                 HttpServletResponse response
    ) {
        response.setCharacterEncoding("UTF-8");

        // 1. 初始化或获取记忆（每个sessionId独立）
        List<ChatMessage> context = memoryService.getContext(sessionId);
        context.add(UserMessage.userMessage(message));

        for (ChatMessage chatMessage : context) {
            System.out.println(chatMessage);
        }

        StringBuilder stringBuilder = new StringBuilder();

        return Flux.create(sink -> streamingChatLanguageModel.chat(message, new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        stringBuilder.append(partialResponse);
                        System.out.print(partialResponse);
                        sink.next(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(ChatResponse completeResponse) {
                        System.out.println("==========================================");
                        String complete = stringBuilder.toString();
                        Message newMessage = Message.builder()
                                .AIMsg(complete)
                                .userMsg(message)
                                .tokens(completeResponse.tokenUsage().inputTokenCount())
                                .createdTime(System.currentTimeMillis())
                                .sessionId(Long.valueOf(sessionId))
                                .id(snowflake.nextId())
                                .build();
                        memoryService.SaveMessage(newMessage);
                        sink.complete();
                    }

                    @Override
                    public void onError(Throwable error) {
                        log.error("流式响应接口出现问题,请及时解决!");
                        sink.error(error);
                    }
                }
        ));
    }

    @GetMapping("/webSearch")
    public String webSearchChat(@RequestParam(value = "message") String message) {
        log.info("用户发起网页搜索: {}", message);
        return assistantService.WebSearchAssistant().chat(message);
    }

    @GetMapping("/email")
    public String emailChat(@RequestParam(value = "message") String message) {
        log.info("用户使用邮件发送模型: {}", message);
        return assistantService.EmailAssistant().chat(message);
    }

    private final EmbeddingStoreContentRetriever embeddingStoreContentRetriever;

    @GetMapping("/rag")
    public String ragChat(@RequestParam(value = "message") String message) {
        log.info("用户发起RAG: {}", message);

        List<Content> retrieve = embeddingStoreContentRetriever.retrieve(Query.from(message));

        log.warn("RAG参数测试");
        for (Content content : retrieve) {
            System.out.println(content.textSegment().text());
            System.out.println(content.metadata());
            System.out.println("=============");
        }

        return assistantService.RAGAssistant().chat(message);
    }

    @GetMapping("/order")
    public String orderChat(@RequestParam(value = "message") String message) {
        log.info("用户发起订单查询: {}", message);
        return assistantService.OrderAssistant().chat(message);
    }

    @GetMapping("/js")
    public String jsChat(@RequestParam(value = "message") String message) {
        return assistantService.JsCodeAssistant().chat(message);
    }

    @GetMapping("/image")
    public String imageChat(@RequestParam(value = "message") String message) {
        log.info("用户发起文生图请求: {}", message);
        return assistantService.Text2ImageAssistant().generate(message);
    }

    @GetMapping("/text")
    public String textChat(@RequestParam(value = "message") String message) {
        log.info("用户发起图生文请求: {}", message);
        return assistantService.Image2TextAssistant().generate(message);
    }

    @PostMapping("/agent")
    public String agentChat(@RequestParam(value = "message") String message,
                            @RequestParam(value = "sessionId") String sessionId,
                            @RequestParam(value = "base64", required = false)String base64,
                            @RequestParam(value = "imageUrl", required = false)String imageUrl
    ) {

        log.info("用户发起智能代理: {}", message);
        // 1. 初始化或获取记忆（每个sessionId独立）
        log.warn("获取上文记忆中...");
        //太吃token了 先关掉了
        //List<ChatMessage> context = memoryService.getContext(sessionId);
        List<ChatMessage> context = new ArrayList<>();
        context.add(UserMessage.userMessage(message));

        for (ChatMessage chatMessage : context) {
            System.out.println(chatMessage);
        }

        //2.尝试使用图生文的model
        // 如果是base64由于太大 只能先直接交给图片大模型
        if (!StrUtil.isBlankIfStr(base64)) {
            log.warn("进行图生文中...");
            String text = "用户提供的图片信息:" + image2TextTool.analyzeImage(base64, "请返回图片中可用的信息:");
            context.add(UserMessage.userMessage(text));
        }
        // 如果是url 就可以让ai自己去调用
        if(!StrUtil.isBlankIfStr(imageUrl)){
            String text = "用户提供的图片链接:" + imageUrl;
            context.add(UserMessage.userMessage(text));
        }

        List<Content> retrieve = embeddingStoreContentRetriever.retrieve(Query.from(message));

        log.warn("RAG参数测试...");
        if (retrieve.isEmpty()) {
            log.warn("没有相匹配的结果(Score>0.8的结果)");
        } else {
            for (Content content : retrieve) {
                System.out.println(content.textSegment().text());
                System.out.println(content.metadata());
                System.out.println("=============");
            }
        }

        String complete = assistantService.SuperSmartAssistant().chat(context.toString());
        Message newMessage = Message.builder()
                .AIMsg(complete)
                .userMsg(message)
                .tokens(complete.length())
                .createdTime(System.currentTimeMillis())
                .sessionId(Long.valueOf(sessionId))
                .id(snowflake.nextId())
                .build();
        memoryService.SaveMessage(newMessage);
        return complete;
    }
}
