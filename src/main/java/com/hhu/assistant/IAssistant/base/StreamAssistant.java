package com.hhu.assistant.IAssistant.base;

import reactor.core.publisher.Flux;

public interface StreamAssistant {
    Flux<String> chat(String userMessage);
}
