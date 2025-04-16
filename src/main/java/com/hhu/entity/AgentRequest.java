package com.hhu.entity;

import lombok.Data;

@Data
public class AgentRequest {
    private String message;
    private String sessionId;
    private String base64;
}
