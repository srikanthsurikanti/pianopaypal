package com.accela.pianoforte.routes;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.*;
import com.accela.pianoforte.services.TransactionStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.camel.Exchange;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;

public class Processors {
    private static final JsonNodeFactory factory = instance;
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
                .transactionType(headers.get("pg_transaction_type"))
                .transactionId(
                        Try.of(() -> new URI(headers.get("pg_transaction_order_number"))).getOrNull())
                .personalName(new PersonalName(
                        headers.get("pg_billto_postal_name_first"),
                        headers.get("pg_billto_postal_name_last")))
                .contact(new Contact(
                        headers.get("pg_billto_postal_name_company"),
                        headers.get("pg_billto_postal_street_line1"),
                        headers.get("pg_billto_postal_street_line2"),
                        headers.get("pg_billto_postal_city"),
                        headers.get("pg_billto_postal_stateprov"),
                        headers.get("pg_billto_postal_postalcode"),
                        headers.get("pg_billto_telecom_phone_number"),
                        headers.get("pg_billto_online_email")))
                .paymentOutcome(PaymentOutcome.builder()
                        .responseText(headers.get("pg_response_description"))
                        .responseCode(headers.get("pg_response_code"))
                        .description(
                                appConfig.getResponseDescription(headers.get("pg_response_code")))
                        .responseType(headers.get("pg_response_type"))
                        .authorizationCode(headers.get("pg_authorization_code"))
                        .traceNumber(headers.get("pg_trace_number")).build())
                .creditCard(CreditCard.builder()
                        .number(Integer.parseInt(headers.get("pg_last4")))
                        .expiryDate(YearMonth.of(
                                Integer.parseInt(headers.get("pg_payment_card_expdate_year")),
                                Integer.parseInt(headers.get("pg_payment_card_expdate_month"))))
                        .issuer(headers.get("pg_payment_card_type")).build()).build(), Response.class);
    }

    protected void storeResponse(final Exchange exchange) {
        store.add(Option.of(exchange.getIn().getBody(Response.class)));
    }

    private static final ObjectNode notFound =
            instance.objectNode().set("error", instance.textNode("Transaction not found"));

    protected void lookupResponse(final Exchange exchange) {
        final Tuple2<Object,Integer> result = store.get(exchange.getIn().getHeader("id", URI.class))
                .map(response -> new Tuple2<Object,Integer>(response, 200))
                .getOrElse(() -> new Tuple2<>(notFound, 404));
        exchange.getMessage().setBody(result._1, result._1.getClass());
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, result._2);
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}