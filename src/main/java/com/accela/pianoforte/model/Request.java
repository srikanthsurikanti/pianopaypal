package com.accela.pianoforte.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class Request {
    public String agency;
    public String firstName;
    public String lastName;
    public String transactionId;
    public String clientLocation;
    public BigDecimal amount;
    public Contact contact;

    public String getTransactionId() {
        return String.format("urn:%s:transaction-id:%s", getAgency(), transactionId);
    }
}
