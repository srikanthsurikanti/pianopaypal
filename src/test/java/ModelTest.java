import com.accela.pianoforte.model.*;
import io.vavr.control.Try;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.time.YearMonth;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fixtures.Fixtures.paymentResponse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTest {

    @Test
    @DisplayName("response is transformed correctly")
    public void testBuildResponse() {
        final Map<String, String> headers = paymentResponse.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> urldecoder.apply(entry.getValue())));
        final CreditCard.CreditCardBuilder ccBuilder = CreditCard.builder()
                .number(headers.get("pg_last4"))
                .expiryDate(YearMonth.of(
                        Integer.parseInt(headers.get("pg_payment_card_expdate_year")),
                        Integer.parseInt(headers.get("pg_payment_card_expdate_month"))))
                .issuer(headers.get("pg_payment_card_type"));
        final PaymentOutcome.PaymentOutcomeBuilder outcomeBuilder = PaymentOutcome.builder()
                .responseText(headers.get("pg_response_description"))
                .responseCode(headers.get("pg_response_code"))
                .responseType(headers.get("pg_response_type"))
                .authorizationCode(headers.get("pg_authorization_code"))
                .traceNumber(headers.get("pg_trace_number"));
        final Response.ResponseBuilder builder = Response.builder()
                .amount(new BigDecimal(headers.get("pg_total_amount").replaceAll(",", "")))
                .paymentOutcome(outcomeBuilder.build())
                .transactionType(headers.get("pg_transaction_type"))
                .transactionId(headers.get("pg_transaction_order_number"))
                .personalName(new PersonalName(
                        headers.get("pg_billto_postal_name_first"),
                        headers.get("pg_billto_postal_name_last")))
                .contact(new Contact(
                        headers.get("pg_billto_postal_name_company"),
                        headers.get("pg_billto_postal_street_line1"),
                        headers.get("pg_billto_postal_street_line2"),
                        headers.get("pg_billto_postal_city"),
                        headers.get("pg_billto_postal_stateprov"),
                        headers.get("pg_billto_postal_postalcode"),
                        headers.get("pg_billto_telecom_phone_number"),
                        headers.get("pg_billto_online_email")))
                .creditCard(ccBuilder.build());
        assertTrue(ccBuilder.toString().contains("expiryDate=2020-04"));
        assertTrue(outcomeBuilder.toString().contains("authorizationCode=6RW586"));
        assertTrue(builder.toString().contains("amount=1200.00"));
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}
