package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.main.AppConfig;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import static org.apache.camel.Exchange.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.TEXT_HTML;

public class ApiRoute extends RouteBuilder {
    private final AppConfig appConfig;

    public ApiRoute(final AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void configure() {
        restConfiguration()
                .component("jetty")
                .contextPath(appConfig.getRestContextPath())
                .port(appConfig.getRestLocalPort());

        rest("/").id("home")
                .get("/checkout")
                    .to("direct:checkout-page")
                .get("/checkout/failure")
                    .to("direct:failure-page");

        rest(appConfig.getRestBase()).id("api-route")
                .post(appConfig.getRestCheckoutPayment())
                    .to("direct:payment-checkout")
                .post(appConfig.getRestReturnUrl())
                    .to("direct:payment-response");

        from("direct:checkout-page")
                .setProperty("showPage", constant("paymentPage.html"))
                .process(ApiRoute::streamPage)
                .setHeader(CONTENT_TYPE, simple(TEXT_HTML.toString()));

        from("direct:failure-page")
                .setProperty("showPage", constant("failedPage.html"))
                .process(ApiRoute::streamPage)
                .setHeader(CONTENT_TYPE, simple(TEXT_HTML.toString()));
    }

    private static void streamPage(final Exchange exchange) {
        final String page404 = "<div id=\"main\"><div class=\"fof\"><h1>Error 404</h1></div></div>";
        final InputStream pageStream = Optional.ofNullable(exchange.getProperty("showPage", String.class))
                .map(page -> ApiRoute.class.getClassLoader().getResourceAsStream("pages/"+page))
                .orElse(new ByteArrayInputStream(page404.getBytes()));
        exchange.getMessage().setBody(pageStream, InputStream.class);
    }

}
