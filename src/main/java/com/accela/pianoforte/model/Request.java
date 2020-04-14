package com.accela.pianoforte.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.net.URL;

@Getter
@NoArgsConstructor
public class Request {
    private String agency;
    private PersonalName personalName;
    private String transactionId;
    private URL clientLocation;
    private BigDecimal amount;
    private Contact contact;

    public String getTransactionId() {
        return String.format("urn:%s:transaction-id:%s", getAgency(), transactionId);
    }
}
