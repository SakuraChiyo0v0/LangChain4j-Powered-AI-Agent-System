package com.hhu.Service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhu.Service.IMemoryService;
import com.hhu.entity.Message;
import com.hhu.mapper.MessageMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@Slf4j
public class MemoryServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMemoryService {
    final StringRedisTemplate redisTemplate;

    final Integer MAX_MESSAGE_MEMORY = 10;

    public MemoryServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public List<ChatMessage> getContext(String sessionId) {
        //系统提示词
        List<ChatMessage> context = new ArrayList<>();
        context.add(SystemMessage.systemMessage("你是一个专业客服,需要为用户解决问题,以下是历史对话,可以据此生成"));
        context.add(SystemMessage.systemMessage("请注意!只需要对usermessage进行处理,其他信息不要返回给用户,如我给你提供的tool工具"));

        // 1. 查Redis（按createdTime排序）
        Set<String> messageIds = redisTemplate.opsForZSet()
                .reverseRange(buildRedisKey(sessionId), 0, MAX_MESSAGE_MEMORY - 1);
        List<Message> messages;
        if (messageIds == null || messageIds.isEmpty()) {
            //五缓存则查表
            messages = lambdaQuery().eq(Message::getSessionId, sessionId)
                    .orderByDesc(Message::getCreatedTime)
                    .last("LIMIT " + MAX_MESSAGE_MEMORY)
                    .list();
            if(messageIds == null || messageIds.isEmpty()){
                return context;
            }
            Set<ZSetOperations.TypedTuple<String>> tuples = new HashSet<>();

            messages.stream().forEach(
                    msg -> tuples.add(new DefaultTypedTuple<>(String.valueOf(msg.getId()), Double.valueOf(msg.getCreatedTime())))
            );
            Long add = redisTemplate.opsForZSet().add(buildRedisKey(sessionId), tuples);
            log.info("添加到Redis: {}", add);
            redisTemplate.expire(buildRedisKey(sessionId), 1, TimeUnit.DAYS);
        }

        //拿到整体的数据
        messages = getBaseMapper().selectByIds(messageIds);
        context.addAll(convertToChatMessages(messages));
        return context;
    }

    @Override
    public void SaveMessage(Message message) {
        save(message);

        redisTemplate.opsForZSet().add(buildRedisKey(message.getSessionId()),
                String.valueOf(message.getId()), System.currentTimeMillis());
        redisTemplate.expire(buildRedisKey(message.getSessionId()), 1, TimeUnit.DAYS);
    }


    // Redis Key格式: chat:session:{sessionId}:messages
    private String buildRedisKey(Object memoryId) {
        return "chat:session:" + memoryId;
    }

    private List<ChatMessage> convertToChatMessages(Collection<Message> messages) {
        //一条message 分别创建AIMessage和UserMessage
        return messages.stream()
                .sorted(Comparator.comparing(Message::getCreatedTime))
                .limit(MAX_MESSAGE_MEMORY)
                .flatMap(
                        msg-> Stream.of(
                                new UserMessage(msg.getUserMsg()),
                                new AiMessage(msg.getAIMsg())
                        )
                ).toList();

    }
}
