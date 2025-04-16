package com.hhu.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhu.entity.Message;
import dev.langchain4j.data.message.ChatMessage;

import java.util.List;

public interface IMemoryService extends IService<Message> {
    List<ChatMessage> getContext(String sessionId);

    void SaveMessage(Message message);
}
