import com.accela.pianoforte.routes.PaymentRoute;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

public class PaymentApiTest extends CamelTestSupport {
    private static final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private static final String txOrderNb = Long.toString(timestamp.getTime());
    private static final String jsonRequest =
            "{\"firstName\":\"Karl\",\"lastName\":\"Marx\"," +
                    "\"agency\":\"test-agency\"," +
                    "\"transactionId\":\""+txOrderNb+"\",\"amount\":123.34}";
    @Test
    @DisplayName("Payment initialize request should redirect payment page URL from provider")
    public void testPaymentInitialize() {
        final JsonNode result = template.requestBodyAndHeader(
                "direct:payment-checkout", jsonRequest,
                Exchange.CONTENT_TYPE, "application/json", JsonNode.class);

        System.out.println("result="+result.toPrettyString());
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                context.addRoutes(new PaymentRoute());
            }
        };
    }

}