package com.accela.pianoforte.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;

@Getter
@NoArgsConstructor
public class Request {
    private PersonalName personalName;
    private URI transactionId;
    private URL clientLocation;
    private BigDecimal amount;
    private String transactionType;
    private Contact contact;
}
