package com.accela.pianoforte.routes.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Response {
    private BigDecimal amount;
    private String responseText;
    private String responseCode;
    private String responseType;
    private String traceNumber;
    private String transactionType;
    private String authorizationCode;
    private String transactionId;
}
