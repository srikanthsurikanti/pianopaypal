package com.accela.pianoforte.routes.model;

import com.accela.pianoforte.routes.common.HMacMD5;
import com.accela.pianoforte.routes.common.UTCTicks;
import com.accela.pianoforte.routes.main.AppConfig;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.function.Supplier;

public class RequestFormBuilder {
    private final Supplier<OffsetDateTime> timestamper;
    private final String securityKey;
    private final String apiLoginId;
    private final String transactionType;
    private final String apiVersion;
    private final String returnUrl;

    public RequestFormBuilder(final Supplier<OffsetDateTime> timestamper,
                              final AppConfig config) {
        this.timestamper = timestamper;
        this.securityKey = config.getSecurityKey();
        this.apiLoginId = config.getApiLoginId();
        this.transactionType = config.getTransactionType();
        this.apiVersion = config.getApiVersion();
        this.returnUrl =
                config.getBaseUrl() + config.getRestBase() + config.getRestReturnUrl();
    }

    public Map<String,String> build(final Request request) {
        final String utcTime = UTCTicks.getUtcTime(timestamper.get()).toString();
        final String pgTsHash = calculateHash(
                apiLoginId, transactionType, request.getAmount().toString(), utcTime,
                request.getTransactionId(), apiVersion);
        return ImmutableMap.<String, String>builder()
                .put("pg_billto_postal_name_first", request.getFirstName())
                .put("pg_billto_postal_name_last", request.getLastName())
                .put("pg_api_login_id",apiLoginId)
                .put("pg_transaction_type", transactionType)
                .put("pg_version_number", apiVersion)
                .put("pg_total_amount", request.getAmount().toString())
                .put("pg_utc_time", utcTime)
                .put("pg_transaction_order_number", request.getTransactionId())
                .put("pg_ts_hash", pgTsHash)
                .put("pg_return_url", returnUrl)
                .put("pg_continue_url", request.getClientLocation())
                .put("pg_cancel_url", request.getClientLocation()+"/failure")
                .put("pg_return_method", "AsyncPost")
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