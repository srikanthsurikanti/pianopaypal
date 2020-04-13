package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Response {
    private final BigDecimal amount;
    private final String responseText;
    private final String responseCode;
    private final String responseType;
    private final String traceNumber;
    private final String transactionType;
    private final String authorizationCode;
    private final String transactionId;
    private final String firstName;
    private final String lastName;
    private final Contact contact;
    private final String last4Digits;
    private final String cardExpDateYear;
    private final String cardExpDateMonth;
    private final String cardType;
}
