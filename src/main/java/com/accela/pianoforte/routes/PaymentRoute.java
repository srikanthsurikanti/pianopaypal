package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.model.Request;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PaymentRoute extends RouteBuilder {
    private final Processors processors;

    public PaymentRoute(final Processors processors) {
        this.processors = processors;
    }

    @Override
    public void configure() {
        getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");

        from("direct:payment-checkout").routeId("checkout")
                .convertBodyTo(Request.class)
                .process(processors::qualifyTXid)
                .process(processors::toJsonNode)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));

        from("direct:payment-response")
                .process(processors::parseResponse)
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));
    }

}