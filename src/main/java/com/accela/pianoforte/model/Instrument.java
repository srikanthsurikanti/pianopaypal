package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;

@Getter
@Builder
public class Instrument {
    private final String type;
    private final long number;
    private final YearMonth expiryDate;
    private final String issuer;

    public String getNumber() {
        return String.format("************%04d",number);
    }
}
