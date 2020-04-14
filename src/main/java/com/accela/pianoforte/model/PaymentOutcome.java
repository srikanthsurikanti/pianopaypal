package com.accela.pianoforte.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentOutcome {
    private final String authorizationCode;
    private final String traceNumber;
    private final String responseText;
    private final String responseCode;
    private final String responseType;
    private final String description;
}
