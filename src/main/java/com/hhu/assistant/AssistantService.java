package com.hhu.assistant;

import com.hhu.tool.GraalVmJavaScriptExecutionTool;
import com.hhu.assistant.IAssistant.*;
import com.hhu.tool.EmailTool;
import com.hhu.Service.impl.OrderService;
import com.hhu.tool.Image2TextTool;
import com.hhu.tool.Text2ImageTool;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.searchapi.SearchApiWebSearchEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AssistantService {

    private final ChatLanguageModel chatModel;

    private final StreamingChatLanguageModel streamingChatLanguageModel;

    private final GraalVmJavaScriptExecutionTool graalVmJavaScriptExecutionTool;

    private final SearchApiWebSearchEngine searchApiWebSearchEngine;

    private final EmbeddingStoreContentRetriever retriever;
    
    private final OrderService orderService;

    private final EmailTool emailTool;
    
    private final Text2ImageTool text2ImageTool;
    
    private final Image2TextTool image2TextTool;

    ChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(10);

    /**
     * 创建智能助手实例，配置好Function Calling功能
     */
    @Bean
    public ChatAssistant ChatAssistant() {
        return AiServices.builder(ChatAssistant.class)
                .chatLanguageModel(chatModel)
                .build();
    }

    @Bean
    public StreamingSmartAssistant StreamingAssistant() {
        return AiServices.builder(StreamingSmartAssistant.class)
                .streamingChatLanguageModel(streamingChatLanguageModel)
                .build();
    }


    @Bean
    public RAGAssistant RAGAssistant() {
        return AiServices.builder(RAGAssistant.class)
                .chatLanguageModel(chatModel)
                .contentRetriever(retriever)
                .build();
    }

    @Bean
    public WebSearchAssistant WebSearchAssistant() {
        return AiServices.builder(WebSearchAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(new WebSearchTool(searchApiWebSearchEngine))
                .build();
    }

    @Bean
    public EmailAssistant EmailAssistant() {
        return AiServices.builder(EmailAssistant.class)
                .chatLanguageModel(chatModel)
                .tools()
                .build();
    }

    @Bean
    public OrderAssistant OrderAssistant() {
        return AiServices.builder(OrderAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(orderService)
                .build();
    }

    @Bean
    public Text2ImageAssistant Text2ImageAssistant() {
        return AiServices.builder(Text2ImageAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(text2ImageTool)
                .build();
    }

    @Bean
    public Image2TextAssistant Image2TextAssistant() {
        return AiServices.builder(Image2TextAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(image2TextTool)
                .build();
    }



    @Bean
    public JsCodeAssistant JsCodeAssistant() {
        return AiServices.builder(JsCodeAssistant.class)
                .chatLanguageModel(chatModel)
                .tools(graalVmJavaScriptExecutionTool)
                .build();
    }

    @Bean
    public SuperSmartAssistant SuperSmartAssistant() {
        return AiServices.builder(SuperSmartAssistant.class)
                .chatLanguageModel(chatModel)
//                .chatMemory(chatMemory)                               //会话记忆功能 我用自己实现的
                .contentRetriever(retriever)                            //RAG索引增强生成功能
                .tools(emailTool,                                       //邮件发送功能
                        orderService,                                   //订单查询功能
                        graalVmJavaScriptExecutionTool,                 //Js代码生成执行功能
                        new WebSearchTool(searchApiWebSearchEngine),    //联网搜索功能
                        text2ImageTool,                               //文生图功能
                        image2TextTool                                 //图生文功能
                )
                .build();
    }
}
