import com.accela.pianoforte.routes.PaymentRoute;
import com.accela.pianoforte.routes.Processors;
import com.accela.pianoforte.routes.main.AppConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple2;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentRouteTest extends CamelTestSupport {
    private static final String jsonRequest =
            "{\"firstName\":\"Karl\",\"lastName\":\"Marx\"," +
                    "\"agency\":\"test-agency\"," +
                    "\"continueUrl\":\"http://localhost:9090/pianoforte/checkout\"," +
                    "\"transactionId\":\"1586197589861\",\"amount\":123.34}";
    @Test
    @DisplayName("Payment request should return redirect query document")
    public void testPaymentRequest() throws JsonProcessingException {
        final String response = template.requestBodyAndHeader("direct:payment-checkout",
                jsonRequest, Exchange.CONTENT_TYPE, "application/json", String.class);

        final JsonNode result = (new ObjectMapper()).readTree(response);
        assertEquals("POST", result.get("method").asText());
        assertEquals("application/x-www-form-urlencoded", result.get("contentType").asText());
        assertEquals("https://sandbox.paymentsgateway.net/swp/co/default.aspx", result.get("url").asText());
        final ArrayList<JsonNode> data = new ObjectMapper().convertValue(result.get("data"), new TypeReference<ArrayList<JsonNode>>(){});
        final Map<String,String> fieldMap = data.stream()
                .map(object -> object.fields().next())
                .map(node -> new Tuple2<>(node.getKey(), node.getValue().asText()))
                .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2));

        assertEquals("Karl", fieldMap.get("pg_billto_postal_name_first"));
        assertEquals("Marx", fieldMap.get("pg_billto_postal_name_last"));
        assertEquals("9F3FA809B8", fieldMap.get("pg_api_login_id"));
        assertEquals("10", fieldMap.get("pg_transaction_type"));
        assertEquals("1.0", fieldMap.get("pg_version_number"));
        assertEquals("123.34", fieldMap.get("pg_total_amount"));
        assertEquals("637134786000000000", fieldMap.get("pg_utc_time"));
        assertEquals("3A98C930C5620F4BF1BABEEEA46C7DC9", fieldMap.get("pg_ts_hash"));
        assertTrue(fieldMap.containsKey("pg_utc_time"));
        assertEquals("urn:test-agency:transaction-id:1586197589861",fieldMap.get("pg_transaction_order_number"));
        assertTrue(fieldMap.containsKey("pg_ts_hash"));
        assertEquals("http://localhost:9090/pianoforte/checkout",fieldMap.get("pg_continue_url"));
        assertEquals("http://localhost:9090/pianoforte/api/payment/return",fieldMap.get("pg_return_url"));
        assertEquals("http://localhost:9090/pianoforte/api/payment/cancel",fieldMap.get("pg_cancel_url"));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                final Processors processors = new Processors(
                        () -> OffsetDateTime.parse("2020-01-01T12:30Z"), new AppConfig("/route.properties"));
                context.addRoutes(new PaymentRoute(processors));
            }
        };
    }

}