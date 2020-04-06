import com.accela.pianoforte.routes.ApiRoute;
import com.accela.pianoforte.routes.PaymentRoute;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

public class PaymentLiveTest extends CamelTestSupport {
    private static final Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    private static final String txOrderNb = Long.toString(timestamp.getTime());
    private static final String jsonRequest =
            "{\"firstName\":\"Karl\",\"lastName\":\"Marx\"," +
                    "\"agency\":\"test-agency\"," +
                    "\"transactionId\":\""+txOrderNb+"\",\"amount\":123.34}";

    @Test
    @DisplayName("API call succeeds")
    public void testPaymentRequestProviderNotConfigured() {
        final String result = template.requestBodyAndHeader("http://localhost:9090/pianoforte/api/payment/checkout", jsonRequest,
                Exchange.CONTENT_TYPE, "application/json", String.class);
        System.out.println("result="+result);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
                context.addRoutes(new PaymentRoute());
                context.addRoutes(new ApiRoute());
            }
        };
    }

}