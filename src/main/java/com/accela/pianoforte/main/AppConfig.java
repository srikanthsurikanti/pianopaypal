package com.accela.pianoforte.main;

import io.vavr.control.Try;

import java.util.Properties;

public class AppConfig {
	 public static final String PAYMENT_PROVIDER = "payment.provider";
    public static String CHECKOUT_URL = "checkout-url";
    public static final String SECURITY_KEY = "security-key";
    public static final String API_VERSION = "api-version";
    public static final String API_LOGIN_ID = "api-login-id";
    public static final String TRANSACTION_CCARD_SALE = "transaction-ccard-sale";
    public static final String REST_CONTEXT_PATH = "rest-context-path";
    public static final String REST_SERVER_HOST = "rest-server-host";
    public static final String REST_LOCAL_PORT = "rest-local-port";
    public static final String REST_BASE = "rest-base";
    public static final String REST_CHECKOUT_PAYMENT = "rest-checkout-payment";
    public static final String REST_RETURN_URL = "rest-return-url";
    //paypal properties
    public static final String CLIENTID = "clientId";
    public static final String CLIENT_SECERT = "client.secert";
    public static final String MODE = "mode";
    
    private final Properties appProps;

    public AppConfig(final String propertyFile) {
        appProps = new Properties();
        Try.of(() -> {
            appProps.load(this.getClass().getResourceAsStream(propertyFile));
            return null;
        });
    }
    

    public String getPaymentProvider() {
    	return appProps.getProperty(PAYMENT_PROVIDER, "");
	}


	public void update(final String key, final String value) {
        appProps.put(key, value);
    }

    public String getCheckoutUrl() {
        return appProps.getProperty(CHECKOUT_URL, "");
    }
    
    public String setCheckoutUrl(String checkoutURL) {
		return this.CHECKOUT_URL = checkoutURL;
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

    public String mapTransactionType(final String code) {
        return appProps.getProperty("forte.transaction.code."+code,"");
    }

    public String getRestContextPath() {
        return appProps.getProperty(REST_CONTEXT_PATH, "");
    }

    public String getRestServerHost() {
        return appProps.getProperty(REST_SERVER_HOST, "");
    }

    public int getRestLocalPort() {
        return Integer.parseInt(appProps.getProperty(REST_LOCAL_PORT, "0"));
    }

    public String getBaseUrl() {
        return String.format("%s/%s", getRestServerHost(), getRestContextPath());
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

    public String getResponseDescription(final String code) {
        return appProps.getProperty("forte.response.code."+code, "("+code+" is undefined)");
    }

	public  String getClientid() {
		 return appProps.getProperty(CLIENTID, "");
	}

	public  String getClientSecert() {
		return appProps.getProperty(CLIENT_SECERT, "");
	}

	public  String getMode() {
		return appProps.getProperty(MODE, "");
	}
    
    
}
