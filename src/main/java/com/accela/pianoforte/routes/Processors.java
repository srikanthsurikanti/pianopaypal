package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.main.AppConfig;
import com.accela.pianoforte.routes.model.Request;
import com.accela.pianoforte.routes.model.RequestFormBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Exchange;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Processors {
    private static JsonNodeFactory factory = JsonNodeFactory.instance;
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
        final ArrayNode dataNode = factory.arrayNode();
        dataNode.addAll(fields);
        query.set("data", dataNode);
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
}