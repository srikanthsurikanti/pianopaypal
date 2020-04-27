package com.accela.pianoforte.routes;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.*;
import com.accela.pianoforte.services.PaymentServices;
import com.accela.pianoforte.services.TransactionStore;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import io.vavr.Tuple2;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.camel.Exchange;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URLDecoder;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.databind.node.JsonNodeFactory.instance;
import static io.vavr.API.For;
import static java.lang.Integer.parseInt;

public class Processors {
    private static final JsonNodeFactory factory = instance;
    private final RequestFormBuilder formBuilder;
    private final AppConfig appConfig;
    private final TransactionStore store;

    public Processors(final TransactionStore store,
                      final Supplier<OffsetDateTime> timestamper, final AppConfig appConfig) {
        this.store = store;
        this.appConfig = appConfig;
        formBuilder = new RequestFormBuilder(timestamper, appConfig);
    }

    protected void toRedirectQuery(final Exchange exchange)throws PayPalRESTException {
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
        if(appConfig.getPaymentProvider().equalsIgnoreCase("PAYPAL")) {
        	PaymentServices paymentServices = new PaymentServices(appConfig);
        	 query.set("url", factory.textNode(paymentServices.authorizePayment(appConfig, request)));
        }
        
        exchange.getMessage().setBody(query.toString());
    }

    protected void parseResponse(final Exchange exchange) throws  JsonProcessingException{
    	
    	if(appConfig.getPaymentProvider().equalsIgnoreCase("PAYPAL")) {
    		
    		Map<String, Object> heasders = exchange.getIn().getHeaders();
    		String paymentId = exchange.getMessage().getHeader("paymentId", String.class);
		    String PayerID = exchange.getMessage().getHeader("PayerID", String.class);
		    PaymentServices paymentServices = new PaymentServices(appConfig);
		    Payment payment=new Payment();
		    try {
		    	payment= paymentServices.executePayment(paymentId,  PayerID);
	    	Contact contact = new Contact("", payment.getPayer().getPayerInfo().getShippingAddress().getLine1() ,
	    			payment.getPayer().getPayerInfo().getShippingAddress().getLine2() , payment.getPayer().getPayerInfo().getShippingAddress().getCity(),
	    			payment.getPayer().getPayerInfo().getShippingAddress().getState(), payment.getPayer().getPayerInfo().getShippingAddress().getPostalCode(),
	    			payment.getPayer().getPayerInfo().getShippingAddress().getPhone(), payment.getPayer().getPayerInfo().getEmail());
			Instrument instrument = Instrument.builder().type("CC").number(parseInt("0002")).issuer("Visa").expiryDate(YearMonth.of(parseInt("2021"), parseInt("12"))).build();
			PersonalName personalName = new PersonalName(payment.getPayer().getPayerInfo().getFirstName(), payment.getPayer().getPayerInfo().getLastName());
			PaymentOutcome paymentOutcome = PaymentOutcome.builder().authorizationCode(payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getId())
					.description(payment.getState()).responseCode("A01").responseText(payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getState())
					.responseType("Post").traceNumber(payment.getTransactions().get(0).getRelatedResources().get(0).getSale().getReceiptId()).build();
			exchange.getMessage()
			.setBody(Response.builder().amount(new BigDecimal(payment.getTransactions().get(0).getAmount().getTotal() ))
					.transactionType("POST")
					.transactionId(Try.of(() -> new URI(paymentId)).getOrNull())
					.personalName(personalName)
					.contact(contact)
					.paymentOutcome(paymentOutcome)
					.instrument(instrument).build(), Response.class);
		    }catch (PayPalRESTException e) {
		    	PaymentOutcome paymentOutcome = PaymentOutcome.builder().authorizationCode(null)
						.description(e.getDetails().getName()).responseCode(String.valueOf(e.getResponsecode())).responseText(e.getDetails().getMessage())
						.responseType("Post").traceNumber(e.getDetails().getDebugId()).build();
		    	exchange.getMessage()
				.setBody(Response.builder()
						.transactionType("POST")
						.transactionId(Try.of(() -> new URI(paymentId)).getOrNull())
						.paymentOutcome(paymentOutcome)
						.build(), Response.class);
			}
	    
    	}else {
		
		final Map<String, String> fields = exchange.getIn().getHeaders().entrySet().stream()
				.filter(e -> e.getKey().startsWith("pg_"))
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> urldecoder.apply(entry.getValue())));

		exchange.getMessage()
				.setBody(Response.builder().amount(new BigDecimal(fields.get("pg_total_amount").replaceAll(",", "")))
						.transactionType(fields.get("pg_transaction_type"))
						.transactionId(Try.of(() -> new URI(fields.get("pg_transaction_order_number"))).getOrNull())
						.personalName(new PersonalName(fields.get("pg_billto_postal_name_first"),
								fields.get("pg_billto_postal_name_last")))
						.contact(new Contact(fields.get("pg_billto_postal_name_company"),
								fields.get("pg_billto_postal_street_line1"),
								fields.get("pg_billto_postal_street_line2"), fields.get("pg_billto_postal_city"),
								fields.get("pg_billto_postal_stateprov"), fields.get("pg_billto_postal_postalcode"),
								fields.get("pg_billto_telecom_phone_number"), fields.get("pg_billto_online_email")))
						.paymentOutcome(PaymentOutcome.builder().responseText(fields.get("pg_response_description"))
								.responseCode(fields.get("pg_response_code"))
								.description(appConfig.getResponseDescription(fields.get("pg_response_code")))
								.responseType(fields.get("pg_response_type"))
								.authorizationCode(fields.get("pg_authorization_code"))
								.traceNumber(fields.get("pg_trace_number")).build())
						.instrument(buildInstrument(fields)).build(), Response.class);
    	}
		 
    }

    private static Instrument buildInstrument(final Map<String, String> fields) {
        final Instrument.InstrumentBuilder builder = Instrument.builder()
                .number(parseInt(fields.get("pg_last4")))
                .type("EC");
        final Option<Instrument> instrument = For(
                Option.of(fields.get("pg_payment_card_expdate_year")),
                Option.of(fields.get("pg_payment_card_expdate_month")),
                Option.of(fields.get("pg_payment_card_type"))
        ).yield((year, month, issuer) ->
                builder.expiryDate(YearMonth.of(parseInt(year), parseInt(month)))
                        .issuer(issuer)
                        .type("CC")
                        .build()
        );
        return instrument.getOrElse(builder.build());
    }

	
	 
    
    protected void storeResponse(final Exchange exchange) {
        store.add(Option.of(exchange.getIn().getBody(Response.class)));
    }

    private static final Map<String, String> notFound =
            ImmutableMap.<String, String>builder()
                    .put("error", "Transaction not found").build();

    protected void lookupResponse(final Exchange exchange) {
        final Tuple2<Object,Integer> result = store.get(exchange.getIn().getHeader("id", URI.class))
                .map(response -> new Tuple2<Object,Integer>(response, 200))
                .getOrElse(() -> new Tuple2<>(notFound, 404));
        exchange.getMessage().setBody(result._1);
        exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, result._2);
    }

    private static final Function<Object,String> urldecoder = encoded ->
            Try.of(() -> URLDecoder.decode(encoded.toString(), "UTF-8")).getOrElse(encoded.toString());
}