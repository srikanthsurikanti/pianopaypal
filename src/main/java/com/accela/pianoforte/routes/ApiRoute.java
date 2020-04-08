package com.accela.pianoforte.routes;

import com.accela.pianoforte.routes.main.AppConfig;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.vavr.control.Try;
import org.apache.camel.builder.RouteBuilder;

import java.net.URL;

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
                    .route()
                    .setBody().simple(getPaymentPage())
                    .setHeader(CONTENT_TYPE, simple(TEXT_HTML.toString()));

        rest(appConfig.getRestBase()).id("api-route")
                .post(appConfig.getRestCheckoutPayment())
                    .to("direct:payment-checkout")
                .post(appConfig.getRestReturnUrl())
                    .to("log:com.accela?level=INFO&showAll=true")
                .post(appConfig.getRestCancelUrl())
                    .to("log:com.accela?level=INFO&showAll=true");
    }

    private static String getPaymentPage() {
        final URL url = Resources.getResource("paymentPage.html");
        return Try.of(() -> Resources.toString(url, Charsets.UTF_8)).get();
    }

}
