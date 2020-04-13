package com.accela.pianoforte.model;

import java.math.BigDecimal;

public class Request {
    private String agency;
    private String firstName;
    private String lastName;
    private String transactionId;
    private String clientLocation;
    private BigDecimal amount;
    private String company;
    private String addressStreet1;
    private String addressStreet2;
    private String addressCity;
    private String addressState;
    private String addressPostCode;
    private String telephone;
    private String email;

    public void setTransactionId(final String transactionId) {
        this.transactionId = transactionId;
    }

    public String getAgency() {
        return agency;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getClientLocation() {
        return clientLocation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCompany() {
        return company;
    }

    public String getAddressStreet1() {
        return addressStreet1;
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
}
