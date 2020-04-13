package com.accela.pianoforte.routes;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.Request;
import com.accela.pianoforte.model.RequestFormBuilder;
import com.accela.pianoforte.model.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.control.Try;
import org.apache.camel.Exchange;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Processors {
    private static final JsonNodeFactory factory = JsonNodeFactory.instance;
    private final RequestFormBuilder formBuilder;
    private final AppConfig appConfig;

    public Processors(final Supplier<OffsetDateTime> timestamper, final AppConfig appConfig) {
        this.appConfig = appConfig;
        formBuilder = new RequestFormBuilder(timestamper, appConfig);
    }

    protected void toJsonNode(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        final List<JsonNode> fields = formBuilder.build(request).entrySet().stream()
                .map(entry -> (ObjectNode) factory.objectNode()
                        .set(entry.getKey(), factory.textNode(entry.getValue())))
                .collect(Collectors.toList());
        final ObjectNode query = factory.objectNode();
        query.set("data", factory.arrayNode().addAll(fields));
        query.set("url", factory.textNode(appConfig.getCheckoutUrl()));
        query.set("method", factory.textNode("POST"));
        query.set("contentType", factory.textNode("application/x-www-form-urlencoded"));
        exchange.getMessage().setBody(query.toString());
    }

    protected void qualifyTXid(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        request.setTransactionId(String.format("urn:%s:transaction-id:%s",
                request.getAgency(), request.getTransactionId()));
    }

    protected void parseResponse(final Exchange exchange) {
        final Map<String, String> headers = exchange.getIn()
                .getHeaders().entrySet().stream()
                    .filter(e -> e.getValue() instanceof String)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> urldecoder.apply(entry.getValue())));
        exchange.getMessage().setBody(new Response(
                new BigDecimal(headers.get("pg_total_amount").replaceAll(",", "")),
                headers.get("pg_response_description"),
                headers.get("pg_response_code"),
                headers.get("pg_response_type"),
                headers.get("pg_trace_number"),
                headers.get("pg_transaction_type"),
                headers.get("pg_authorization_code"),
                headers.get("pg_transaction_order_number"),
                headers.get("pg_billto_postal_name_company"),
                headers.get("pg_billto_postal_name_first"),
                headers.get("pg_billto_postal_name_last"),
                headers.get("pg_billto_postal_street_line1"),
                headers.get("pg_billto_postal_street_line2"),
                headers.get("pg_billto_postal_city"),
                headers.get("pg_billto_postal_stateprov"),
                headers.get("pg_billto_postal_postalcode"),
                headers.get("pg_billto_telecom_phone_number"),
                headers.get("pg_billto_online_email"),
                headers.get("pg_last4"),
                headers.get("pg_payment_card_expdate_year"),
                headers.get("pg_payment_card_expdate_month"),
                headers.get("pg_payment_card_type")
                ), Response.class);
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}