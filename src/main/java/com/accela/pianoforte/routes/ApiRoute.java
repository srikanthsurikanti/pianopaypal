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
                    .to("direct:checkout-page")
                .get("/checkout/failure")
                    .to("direct:failure-page");

        rest(appConfig.getRestBase()).id("api-route")
                .post(appConfig.getRestCheckoutPayment())
                    .to("direct:payment-checkout")
                .post(appConfig.getRestReturnUrl())
                    .to("direct:payment-response");

        from("direct:checkout-page")
                .setBody().simple(loadPage("paymentPage.html"))
                .setHeader(CONTENT_TYPE, simple(TEXT_HTML.toString()));

        from("direct:failure-page")
                .setBody().simple(loadPage("failedPage.html"))
                .setHeader(CONTENT_TYPE, simple(TEXT_HTML.toString()));
    }

    private static String loadPage(final String pageName) {
        final URL url = Resources.getResource(pageName);
        return Try.of(() -> Resources.toString(url, Charsets.UTF_8)).get();
    }

}
