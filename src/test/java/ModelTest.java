import com.accela.pianoforte.model.Contact;
import com.accela.pianoforte.model.Response;
import io.vavr.control.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fixtures.Fixtures.paymentResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTest {

    @Test
    @DisplayName("response is built correctly")
    public void testBuildResponse() {
        final Map<String, String> headers = paymentResponse.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> urldecoder.apply(entry.getValue())));
        final Response.ResponseBuilder builder = Response.builder()
                .amount(new BigDecimal(headers.get("pg_total_amount").replaceAll(",", "")))
                .responseText(headers.get("pg_response_description"))
                .responseCode(headers.get("pg_response_code"))
                .responseType(headers.get("pg_response_type"))
                .traceNumber(headers.get("pg_trace_number"))
                .transactionType(headers.get("pg_transaction_type"))
                .authorizationCode(headers.get("pg_authorization_code"))
                .transactionId(headers.get("pg_transaction_order_number"))
                .firstName(headers.get("pg_billto_postal_name_first"))
                .lastName(headers.get("pg_billto_postal_name_last"))
                .contact(new Contact(
                        headers.get("pg_billto_postal_name_company"),
                        headers.get("pg_billto_postal_street_line1"),
                        headers.get("pg_billto_postal_street_line2"),
                        headers.get("pg_billto_postal_city"),
                        headers.get("pg_billto_postal_stateprov"),
                        headers.get("pg_billto_postal_postalcode"),
                        headers.get("pg_billto_telecom_phone_number"),
                        headers.get("pg_billto_online_email")))
                .last4Digits(headers.get("pg_last4"))
                .cardExpDateYear(headers.get("pg_payment_card_expdate_year"))
                .cardExpDateMonth(headers.get("pg_payment_card_expdate_month"))
                .cardType(headers.get("pg_payment_card_type"));
        assertTrue(builder.toString()
                        .startsWith("Response.ResponseBuilder(amount=1200.00, responseText=TEST APPROVAL"));
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}
