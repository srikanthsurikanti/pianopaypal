package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.main.Configuration;
import com.accela.pianoforte.routes.model.Request;
import com.accela.pianoforte.routes.model.RequestFormBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConverters;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PaymentRoute extends RouteBuilder {
    private static final JacksonDataFormat jsonFormat = new JacksonDataFormat();
    static {
        jsonFormat.disableFeature(FAIL_ON_UNKNOWN_PROPERTIES);
        jsonFormat.setUnmarshalType(Request.class);
    }

    @Override
    public void configure() {
        getContext().getTypeConverterRegistry()
                .addTypeConverters(new DataConverters());

        from("direct:payment-checkout").routeId("checkout")
                .to("log:com.accela?level=INFO&showAll=true")
                .unmarshal().json(JsonLibrary.Jackson, Request.class)
                .process(PaymentRoute::qualifyTXid)
                .convertBodyTo(JsonNode.class)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));
    }

    private static Configuration configuration = new Configuration("/route.properties");
    private static RequestFormBuilder formBuilder = new RequestFormBuilder(Instant::now,configuration);
    private static JsonNodeFactory factory = JsonNodeFactory.instance;

    private static void qualifyTXid(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        request.setTransactionId(String.format("urn:%s:transaction-id:%s",
                request.getAgency(), request.getTransactionId()));
    }

    public static class DataConverters implements TypeConverters {
        @Converter
        public JsonNode toJsonNode(final Request request) {
            final List<JsonNode> fields = formBuilder.build(request).entrySet().stream()
                    .map(entry -> (ObjectNode)factory.objectNode()
                            .set(entry.getKey(), factory.textNode(entry.getValue())))
                    .collect(Collectors.toList());
            final ObjectNode query = factory.objectNode();
            final ArrayNode dataNode = factory.arrayNode();
            dataNode.addAll(fields);
            query.set("data", dataNode);
            query.set("url", factory.textNode(configuration.getCheckoutUrl()));
            query.set("method", factory.textNode("POST"));
            query.set("contentType", factory.textNode("application/x-www-form-urlencoded"));
            return query;
        }
    }

}