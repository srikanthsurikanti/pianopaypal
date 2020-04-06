package com.accela.pianoforte.routes.model;

import com.accela.pianoforte.routes.common.HMacMD5;
import com.accela.pianoforte.routes.common.UTCTicks;
import com.accela.pianoforte.routes.main.Configuration;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.time.Instant;
import java.util.Map;
import java.util.function.Supplier;

public class RequestFormBuilder {
    private final Supplier<Instant> timestamper;
    private final String apiLoginId;
    private final String transactionType;
    private final String apiVersion;
    private final String securityKey;
    private final String returnUrl;
    private final String continueUrl;
    private final String cancelUrl;

    public RequestFormBuilder(final Supplier<Instant> timestamper,
                              final Configuration configuration) {
        this.timestamper = timestamper;
        this.apiLoginId = configuration.getApiLoginId();
        this.transactionType = configuration.getTransactionType();
        this.apiVersion = configuration.getApiVersion();
        this.securityKey = configuration.getSecurityKey();
        final String baseUrl = configuration.getBaseUrl();
        this.returnUrl = baseUrl+configuration.getRestReturnUrl();
        this.continueUrl = baseUrl+configuration.getRestContinueUrl();
        this.cancelUrl = baseUrl+configuration.getRestCancelUrl();
    }

    public Map<String,String> build(final Request request) {
        return formData(
                request.getFirstName(), request.getLastName(),
                UTCTicks.getUtcTime(timestamper.get()).toString(),
                request.getTransactionId(),
                request.getAmount().toString());
    }

    private Map<String, String> formData(final String firstName, final String lastName,
                                         final String utcTime, final String txOrderNb, final String amount) {
        final String pgTsHash = calculateHash(
                apiLoginId, transactionType, amount, utcTime, txOrderNb, apiVersion);
        return ImmutableMap.<String, String>builder()
                .put("pg_billto_postal_name_first", firstName)
                .put("pg_billto_postal_name_last", lastName)
                .put("pg_api_login_id", apiLoginId)
                .put("pg_transaction_type", transactionType)
                .put("pg_version_number", apiVersion)
                .put("pg_total_amount", amount)
                .put("pg_utc_time", utcTime)
                .put("pg_transaction_order_number", txOrderNb)
                .put("pg_ts_hash", pgTsHash)
                .put("pg_return_url", returnUrl)
                .put("pg_continue_url", continueUrl)
                .put("pg_cancel_url", cancelUrl)
                .build();
    }

    private String calculateHash(final String userId, final String txType, final String amount,
                                 final String utcTimestamp, final String txOrderNb, final String version) {
        final String hash = String.join("|", ImmutableList.<String>builder()
                .add(userId, txType, version, amount, utcTimestamp, txOrderNb)
                .build());
        return HMacMD5.getHmacMD5(hash, securityKey);
    }
}
