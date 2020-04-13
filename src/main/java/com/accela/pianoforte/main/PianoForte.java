package com.accela.pianoforte.main;

import com.accela.pianoforte.routes.ApiRoute;
import com.accela.pianoforte.routes.PaymentRoute;
import com.accela.pianoforte.routes.Processors;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class PianoForte {
    private static final Logger logger = LoggerFactory.getLogger(PianoForte.class);

    public static void main(final String[] args) {
        final Main routes = new Main();
        final AppConfig appConfig = new AppConfig("/route.properties");
        final Processors processors = new Processors(() -> OffsetDateTime.now(ZoneOffset.UTC), appConfig);
        routes.addRoutesBuilder(new PaymentRoute(processors));
        routes.addRoutesBuilder(new ApiRoute(appConfig));
        routes.start();
        logger.info("app started");
    }


}
