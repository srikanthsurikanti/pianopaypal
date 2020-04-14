package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class Response {
    private final BigDecimal amount;
    private final Contact contact;
    private final CreditCard creditCard;
    private final PersonalName personalName;
    private final PaymentOutcome paymentOutcome;
    private final String transactionType;
    private final String transactionId;
}
