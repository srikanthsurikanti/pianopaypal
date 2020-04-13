package com.accela.pianoforte.model;

import java.math.BigDecimal;

public class Response {
    private BigDecimal amount;
    private String responseText;
    private String responseCode;
    private String responseType;
    private String traceNumber;
    private String transactionType;
    private String authorizationCode;
    private String transactionId;
    private String companyName;
    private String firstName;
    private String lastName;
    private String addressStreet1;
    private String addressStreet2;
    private String addressCity;
    private String addressState;
    private String addressPostCode;
    private String telephone;
    private String email;
    private String last4Digits;
    private String cardExpDateYear;
    private String cardExpDateMonth;
    private String cardType;

    public Response(final BigDecimal amount, final String responseText, final String responseCode,
                    final String responseType, final String traceNumber, final String transactionType,
                    final String authorizationCode, final String transactionId,
                    final String companyName, final String firstName, final String lastName,
                    final String addressStreet1, final String addressStreet2, final String addressCity,
                    final String addressState, final String addressPostCode, final String telephone,
                    final String email, final String last4Digits, final String cardExpDateYear,
                    final String cardExpDateMonth, final String cardType) {
        this.amount = amount;
        this.responseText = responseText;
        this.responseCode = responseCode;
        this.responseType = responseType;
        this.traceNumber = traceNumber;
        this.transactionType = transactionType;
        this.authorizationCode = authorizationCode;
        this.transactionId = transactionId;
        this.companyName = companyName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.addressStreet1 = addressStreet1;
        this.addressStreet2 = addressStreet2;
        this.addressCity = addressCity;
        this.addressState = addressState;
        this.addressPostCode = addressPostCode;
        this.telephone = telephone;
        this.email = email;
        this.last4Digits = last4Digits;
        this.cardExpDateYear = cardExpDateYear;
        this.cardExpDateMonth = cardExpDateMonth;
        this.cardType = cardType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getResponseText() {
        return responseText;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseType() {
        return responseType;
    }

    public String getTraceNumber() {
        return traceNumber;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAddressStreet1() {
        return addressStreet1;
    }
    public String getCompanyName() {
        return companyName;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getAddressStreet2() {
        return addressStreet2;
    }

    public String getAddressCity() {
        return addressCity;
    }

    public String getAddressState() {
        return addressState;
    }

    public String getAddressPostCode() {
        return addressPostCode;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getEmail() {
        return email;
    }

    public String getLast4Digits() {
        return last4Digits;
    }

    public String getCardExpDateYear() {
        return cardExpDateYear;
    }

    public String getCardExpDateMonth() {
        return cardExpDateMonth;
    }

    public String getCardType() {
        return cardType;
    }
}
