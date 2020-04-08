package com.accela.pianoforte.routes.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class Request {
    private String agency;
    private String firstName;
    private String lastName;
    private String transactionId;
    private String continueUrl;
    private BigDecimal amount;
}
