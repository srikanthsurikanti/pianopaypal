package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Response {
    public BigDecimal amount;
    public String responseText;
    public String responseCode;
    public String responseType;
    public String traceNumber;
    public String transactionType;
    public String authorizationCode;
    public String transactionId;
    public String firstName;
    public String lastName;
    public Contact contact;
    public String last4Digits;
    public String cardExpDateYear;
    public String cardExpDateMonth;
    public String cardType;
}
