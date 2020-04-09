package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.model.Request;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PaymentRoute extends RouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRoute.class);
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
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
                .to("direct:platform");

        from("direct:platform")
                .to("log:com.accela?level=INFO&showAll=true")
                .process(exch -> {
                    final String body = exch.getIn().getBody(String.class);
                    logger.info("POST to platform => "+body);
                });
    }

}