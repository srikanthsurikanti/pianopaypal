package com.accela.pianoforte.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class Request {
    private String agency;
    private String firstName;
    private String lastName;
    private String transactionId;
    private String clientLocation;
    private BigDecimal amount;
    private Contact contact;

    public String getTransactionId() {
        return String.format("urn:%s:transaction-id:%s", getAgency(), transactionId);
    }
}
