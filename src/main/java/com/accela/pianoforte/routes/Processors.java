package com.accela.pianoforte.routes;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.Request;
import com.accela.pianoforte.model.RequestFormBuilder;
import com.accela.pianoforte.model.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.control.Try;
import org.apache.camel.Exchange;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Processors {
    private static final JsonNodeFactory factory = JsonNodeFactory.instance;
    private final RequestFormBuilder formBuilder;
    private final AppConfig appConfig;

    public Processors(final Supplier<OffsetDateTime> timestamper, final AppConfig appConfig) {
        this.appConfig = appConfig;
        formBuilder = new RequestFormBuilder(timestamper, appConfig);
    }

    void toJsonNode(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        final List<JsonNode> fields = formBuilder.build(request).entrySet().stream()
                .map(entry -> (ObjectNode) factory.objectNode()
                        .set(entry.getKey(), factory.textNode(entry.getValue())))
                .collect(Collectors.toList());
        final ObjectNode query = factory.objectNode();
        query.set("data", factory.arrayNode().addAll(fields));
        query.set("url", factory.textNode(appConfig.getCheckoutUrl()));
        query.set("method", factory.textNode("POST"));
        query.set("contentType", factory.textNode("application/x-www-form-urlencoded"));
        exchange.getMessage().setBody(query.toString());
    }

    void qualifyTXid(final Exchange exchange) {
        final Request request = exchange.getIn().getBody(Request.class);
        request.setTransactionId(String.format("urn:%s:transaction-id:%s",
                request.getAgency(), request.getTransactionId()));
    }
/*
pg_authorization_code=4OT598,
pg_billto_online_email=test@gmail.com,
pg_billto_postal_city=Landford,
pg_billto_postal_name_company=Landford County,
pg_billto_postal_name_first=Booker,
pg_billto_postal_name_last=Brooks,
pg_billto_postal_postalcode=60060,
pg_billto_postal_stateprov=IL,
pg_billto_postal_street_line1=123 Main Street,
pg_billto_postal_street_line2=123 Main Street,
pg_billto_telecom_phone_number=555-555-5555,
pg_last4=1111, pg_payment_card_expdate_month=04,
pg_payment_card_expdate_year=2020,
pg_payment_card_type=visa,
pg_response_code=A01,
pg_response_description=TEST APPROVAL,
pg_response_type=A,
pg_shipto_postal_city=Landford,
pg_shipto_postal_name=Booker Brooks, pg_shipto_postal_postalcode=60060, pg_shipto_postal_stateprov=IL, pg_shipto_postal_street_line1=123 Main Street, pg_shipto_postal_street_line2=123 Main Street,
pg_total_amount=1,200.00,
pg_trace_number=40b91ca9-aa9a-4d5d-b4bf-60320f4cd740,
pg_transaction_order_number=urn:test-agency:transaction-id:1586714344019,
pg_transaction_type=10,
pg_ts_hash_response=34821a9f0e293fef8af68e05c79306ba,
pg_utc_time=637223111645305847
 */
    void parseResponse(final Exchange exchange) {
        final Map<String, String> headers = exchange.getIn()
                .getHeaders().entrySet().stream()
                    .filter(e -> e.getValue() instanceof String)
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> urldecoder.apply(entry.getValue())));
        exchange.getMessage().setBody(new Response(
                new BigDecimal(headers.get("pg_total_amount").replaceAll(",", "")),
                headers.get("pg_response_description"),
                headers.get("pg_response_code"),
                headers.get("pg_response_type"),
                headers.get("pg_trace_number"),
                headers.get("pg_transaction_type"),
                headers.get("pg_authorization_code"),
                headers.get("pg_transaction_order_number"),
                headers.get("pg_billto_postal_name_company"),
                headers.get("pg_billto_postal_name_first"),
                headers.get("pg_billto_postal_name_last"),
                headers.get("pg_billto_postal_street_line1"),
                headers.get("pg_billto_postal_street_line2"),
                headers.get("pg_billto_postal_city"),
                headers.get("pg_billto_postal_stateprov"),
                headers.get("pg_billto_postal_postalcode"),
                headers.get("pg_billto_telecom_phone_number"),
                headers.get("pg_billto_online_email"),
                headers.get("pg_last4"),
                headers.get("pg_payment_card_expdate_year"),
                headers.get("pg_payment_card_expdate_month"),
                headers.get("pg_payment_card_type")
                ), Response.class);
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}