package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.main.AppConfig;
import com.accela.pianoforte.routes.model.Request;
import com.accela.pianoforte.routes.model.RequestFormBuilder;
import com.accela.pianoforte.routes.model.Response;
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

    void toJsonNode(final Exchange exchange) {
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

    void qualifyTXid(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        request.setTransactionId(String.format("urn:%s:transaction-id:%s",
                request.getAgency(), request.getTransactionId()));
    }

    void parseResponse(final Exchange exchange) {
        final Map<String, String> headers = exchange.getIn()
                .getHeaders().entrySet().stream()
                    .filter(e -> e.getValue() instanceof String)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> urldecoder.apply(entry.getValue())));
        exchange.getMessage().setBody(Response.builder()
                .amount(new BigDecimal(
                        headers.get("pg_total_amount").replaceAll(",", "")))
                .responseCode(headers.get("pg_response_code"))
                .responseText(headers.get("pg_response_description"))
                .responseType(headers.get("pg_response_type"))
                .traceNumber(headers.get("pg_trace_number"))
                .transactionType(headers.get("pg_transaction_type"))
                .authorizationCode(headers.get("pg_authorization_code"))
                .transactionId(headers.get("pg_transaction_order_number"))
                .build(), Response.class);
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}