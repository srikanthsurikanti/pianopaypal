package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class CreditCard {
    private final long number;
    private final YearMonth expiryDate;
    private final String issuer;

    public String getNumber() {
        return "************"+number;
    }
}
