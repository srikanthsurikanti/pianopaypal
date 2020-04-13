package com.accela.pianoforte.routes;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.Contact;
import com.accela.pianoforte.model.Request;
import com.accela.pianoforte.model.RequestFormBuilder;
import com.accela.pianoforte.model.Response;
import com.accela.pianoforte.services.TransactionStore;
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
    private final TransactionStore store;

    public Processors(final TransactionStore store,
                      final Supplier<OffsetDateTime> timestamper, final AppConfig appConfig) {
        this.store = store;
        this.appConfig = appConfig;
        formBuilder = new RequestFormBuilder(timestamper, appConfig);
    }

    protected void toRedirectQuery(final Exchange exchange) {
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

    protected void parseResponse(final Exchange exchange) {
        final Map<String, String> headers = exchange.getIn()
                .getHeaders().entrySet().stream()
                    .filter(e -> e.getValue() instanceof String)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> urldecoder.apply(entry.getValue())));

        exchange.getMessage().setBody(Response.builder()
                .amount(new BigDecimal(headers.get("pg_total_amount").replaceAll(",", "")))
                .responseText(headers.get("pg_response_description"))
                .responseCode(headers.get("pg_response_code"))
                .responseType(headers.get("pg_response_type"))
                .traceNumber(headers.get("pg_trace_number"))
                .transactionType(headers.get("pg_transaction_type"))
                .authorizationCode(headers.get("pg_authorization_code"))
                .transactionId(headers.get("pg_transaction_order_number"))
                .firstName(headers.get("pg_billto_postal_name_first"))
                .lastName(headers.get("pg_billto_postal_name_last"))
                .contact(new Contact(
                        headers.get("pg_billto_postal_name_company"),
                        headers.get("pg_billto_postal_street_line1"),
                        headers.get("pg_billto_postal_street_line2"),
                        headers.get("pg_billto_postal_city"),
                        headers.get("pg_billto_postal_stateprov"),
                        headers.get("pg_billto_postal_postalcode"),
                        headers.get("pg_billto_telecom_phone_number"),
                        headers.get("pg_billto_online_email")))
                .last4Digits(headers.get("pg_last4"))
                .cardExpDateYear(headers.get("pg_payment_card_expdate_year"))
                .cardExpDateMonth(headers.get("pg_payment_card_expdate_month"))
                .cardType(headers.get("pg_payment_card_type")).build(), Response.class);
    }


    protected void storeResponse(final Exchange exchange) {
        store.add(exchange.getIn().getBody(Response.class));
    }

    protected void lookupResponse(final Exchange exchange) {
        store.get(exchange.getIn().getHeader("id", String.class)).map(response -> {
            exchange.getMessage().setBody(response, Response.class);
            return null;
        });
    }


    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}