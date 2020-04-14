package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class CreditCard {
    private final String number;
    private final YearMonth expiryDate;
    private final String issuer;

    public String getNumber() {
        return "************"+number;
    }
}
