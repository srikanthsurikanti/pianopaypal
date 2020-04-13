package com.accela.pianoforte.routes;

import com.accela.pianoforte.model.Response;
import com.accela.pianoforte.services.TransactionStore;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PaymentRoute extends RouteBuilder {
    private static final Logger logger = LoggerFactory.getLogger(PaymentRoute.class);
    private final Processors processors;
    private final TransactionStore store;

    public PaymentRoute(final Processors processors) {
        this.processors = processors;
        store = new TransactionStore();
    }

    @Override
    public void configure() {
        getContext().getGlobalOptions().put("CamelJacksonEnableTypeConverter", "true");

        from("direct:payment-checkout").routeId("checkout")
                .process(processors::toRedirectQuery)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));

        from("direct:payment-response")
                .process(processors::parseResponse)
                .process(this::storeResponse)
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON))
                .to("direct:platform");

        from("direct:platform")
                .process(exch ->
                    logger.info("POST to platform => "+exch.getIn().getBody(String.class)));

        from("direct:transaction-query")
                .process(this::lookupResponse)
                .marshal().json(JsonLibrary.Jackson)
                .setHeader(CONTENT_TYPE, constant(APPLICATION_JSON));
    }

    private void storeResponse(final Exchange exchange) {
        store.add(exchange.getIn().getBody(Response.class));
    }

    private void lookupResponse(final Exchange exchange) {
        store.get(exchange.getIn().getHeader("id", String.class)).map(response -> {
            exchange.getMessage().setBody(response, Response.class);
            return null;
        });
    }

}