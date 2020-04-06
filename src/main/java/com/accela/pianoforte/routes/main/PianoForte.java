package com.accela.pianoforte.routes.main;

import com.accela.pianoforte.routes.ApiRoute;
import com.accela.pianoforte.routes.PaymentRoute;
import org.apache.camel.main.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PianoForte {
    private static final Logger logger = LoggerFactory.getLogger(PianoForte.class);

    public static void main(final String[] args) {
        final Main routes = new Main();
        routes.addRouteBuilder(PaymentRoute.class, ApiRoute.class);
        routes.start();
        logger.info("app started");
    }


}
