package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.model.Request;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jackson.JacksonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PaymentRoute extends RouteBuilder {
    private static final JacksonDataFormat jsonFormat = new JacksonDataFormat();
    static {
        jsonFormat.disableFeature(FAIL_ON_UNKNOWN_PROPERTIES);
        jsonFormat.setUnmarshalType(Request.class);
    }
    private final Processors processors;

    public PaymentRoute(final Processors processors) {
        this.processors = processors;
    }

    @Override
    public void configure() {

        from("direct:payment-checkout").routeId("checkout")
                .unmarshal().json(JsonLibrary.Jackson, Request.class)
                .process(processors::qualifyTXid)
                .process(processors::toJsonNode)
                .to("log:com.accela?level=DEBUG&showAll=true")
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));
    }

}