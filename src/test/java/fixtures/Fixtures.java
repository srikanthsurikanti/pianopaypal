package fixtures;

import com.google.common.collect.ImmutableMap;
import org.apache.camel.Exchange;

import java.util.Map;
import java.util.stream.Collectors;

public class Fixtures {
    public static final Map<String, Object> paymentResponse =
            ImmutableMap.<String, Object>builder()
                    .put("pg_billto_postal_name_company", "Accela+Ireland+Ltd.")
                    .put("pg_billto_postal_name_first", "Max")
                    .put("pg_billto_postal_name_last", "Krall")
                    .put("pg_billto_postal_street_line1", "Beaux+Lane+House%2c+Mercer+Street+Lower")
                    .put("pg_billto_postal_state", "Dublin")
                    .put("pg_shipto_postal_name", "Max+Krall")
                    .put("pg_shipto_postal_city", "Dublin")
                    .put("pg_total_amount", "1%2c200.00")
                    .put("pg_response_description", "TEST+APPROVAL")
                    .put("pg_response_code", "A01")
                    .put("pg_response_type", "A")
                    .put("pg_trace_number", "965fc3ec-f221-49ac-b54e-4e3fb4f3ce20")
                    .put("pg_ts_hash_response", "9e3745626b5457d7fb9c882fd3ffbc92")
                    .put("pg_utc_time", "637219349419991905")
                    .put("pg_transaction_order_number", "urn%3atest-agency%3atransaction-id%3a1586338120514")
                    .put("pg_transaction_type", "10")
                    .put("pg_authorization_code", "6RW586")
                    .put("pg_last4", "1111")
                    .put("pg_payment_card_type", "visa")
                    .put("pg_payment_card_expdate_month", "04")
                    .put("pg_payment_card_expdate_year", "2020")
                    .put(Exchange.CONTENT_TYPE, "application/x-www-form-urlencoded")
                    .build();

    public static final String paymentResponseUrlencoded = paymentResponse.entrySet().stream()
            .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("&"));
}
