package com.accela.pianoforte.routes.main;

import io.vavr.control.Try;

import java.util.Properties;

public class Configuration {
    private static final String CHECKOUT_URL = "checkout-url";
    private static final String SECURITY_KEY = "security-key";
    private static final String API_VERSION = "api-version";
    private static final String API_LOGIN_ID = "api-login-id";
    private static final String TRANSACTION_TYPE = "transaction-type";
    private static final String REST_CONTEXT_PATH = "rest-context-path";
    private static final String REST_SERVER_PORT = "rest-server-port";
    private static final String REST_BASE = "rest-base";
    private static final String REST_CHECKOUT_PAYMENT = "rest-checkout-payment";
    private static final String REST_RETURN_URL = "rest-return-url";
    private static final String REST_CONTINUE_URL = "rest-continue-url";
    private static final String REST_CANCEL_URL = "rest-cancel-url";
    private static final Properties appProps = new Properties();

    public Configuration(final String propertyFile) {
        Try.of(() -> {
            appProps.load(this.getClass().getResourceAsStream(propertyFile));
            return null;
        });
    }

    public String getCheckoutUrl() {
        return appProps.getProperty(CHECKOUT_URL, "");
    }

    public String getApiLoginId() {
        return appProps.getProperty(API_LOGIN_ID, "");
    }

    public String getSecurityKey() {
        return appProps.getProperty(SECURITY_KEY, "");
    }

    public String getApiVersion() {
        return appProps.getProperty(API_VERSION, "");
    }

    public String getTransactionType() {
        return appProps.getProperty(TRANSACTION_TYPE, "");
    }

    public String getRestContextPath() {
        return appProps.getProperty(REST_CONTEXT_PATH, "");
    }

    public int getRestServerPort() {
        return Integer.parseInt(appProps.getProperty(REST_SERVER_PORT, "0"));
    }

    public String getBaseUrl() {
        return "http://localhost:9090/pianoforte";
    }

    public String getRestBase() {
        return appProps.getProperty(REST_BASE, "");
    }

    public String getRestCheckoutPayment() {
        return appProps.getProperty(REST_CHECKOUT_PAYMENT, "");
    }

    public String getRestReturnUrl() {
        return appProps.getProperty(REST_RETURN_URL, "");
    }

    public String getRestContinueUrl() {
        return appProps.getProperty(REST_CONTINUE_URL, "");
    }

    public String getRestCancelUrl() {
        return appProps.getProperty(REST_CANCEL_URL, "");
    }
}
