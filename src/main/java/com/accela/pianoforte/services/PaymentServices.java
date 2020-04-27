package com.accela.pianoforte.services;
/**
 * PaymentServices class - encapsulates PayPal payment integration functions.
 * @author Nam Ha Minh
 * @copyright https://codeJava.net
 */
 
import java.util.*;

import com.accela.pianoforte.main.AppConfig;
import com.accela.pianoforte.model.Request;
import com.paypal.api.payments.*;
import com.paypal.base.rest.*;
 
public class PaymentServices {

private APIContext apiContext;

public PaymentServices(AppConfig appConfig){
	  apiContext = new APIContext(appConfig.getClientid(), appConfig.getClientSecert(), appConfig.getMode());
}
	
    public String authorizePayment(AppConfig appConfig,Request request)        
            throws PayPalRESTException {       
 
        RedirectUrls redirectUrls = getRedirectURLs(request,appConfig);
        List<Transaction> listTransaction = getTransactionInformation(request);
         
        Payment requestPayment = new Payment();
        requestPayment.setTransactions(listTransaction);
        requestPayment.setRedirectUrls(redirectUrls);
        requestPayment.setPayer(getPayerInformation(request,appConfig));
        requestPayment.setIntent("SALE");
        
 
       
 
        Payment approvedPayment = requestPayment.create(apiContext);
        
 
        return getApprovalLink(approvedPayment);
 
    }
     
    private Payer getPayerInformation(Request request,AppConfig appConfig) {
        Payer payer = new Payer();
        payer.setPaymentMethod(appConfig.getPaymentProvider());
        PayerInfo payerInfo = new PayerInfo();
        Address address = new Address();
        address.setCity(request.getContact().getCity());
        address.setCountryCode("US");
        address.setLine1(request.getContact().getStreet1());
        address.setLine2(request.getContact().getStreet2());
        address.setPostalCode(request.getContact().getPostCode());
        address.setState(request.getContact().getState());
        address.setPhone(request.getContact().getTelephone());
        payerInfo.setFirstName(request.getPersonalName().getFirstName())
                 .setLastName(request.getPersonalName().getLastName())
                 .setEmail(request.getContact().getEmail())
                 .setBillingAddress(address);
        payer.setPayerInfo(payerInfo);
         
        return payer;
    }
    private RedirectUrls getRedirectURLs(Request request,AppConfig appConfig) {
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(request.getClientLocation().toString().substring(0,request.getClientLocation().toString().lastIndexOf('/'))+   appConfig.getRestBase() + appConfig.getRestReturnUrl());
        redirectUrls.setReturnUrl(request.getClientLocation().toString().substring(0,request.getClientLocation().toString().lastIndexOf('/'))+ appConfig.getRestBase() + appConfig.getRestReturnUrl());
         
        return redirectUrls;
    }
     
    private List<Transaction> getTransactionInformation(Request request) {
        Details details = new Details();
        details.setShipping("0");
        details.setSubtotal(request.getAmount().toString());
        details.setTax("0");
     
        Amount amount = new Amount();
        amount.setCurrency("USD");
        amount.setTotal(request.getAmount().toString());
        amount.setDetails(details);
     
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setDescription("Permit");
         
        ItemList itemList = new ItemList();
        List<Item> items = new ArrayList<>();
         
        Item item = new Item();
        item.setCurrency("USD");
        item.setName("Permit");
        item.setPrice(request.getAmount().toString());
        item.setTax("0");
        item.setQuantity("1");
        
         
        items.add(item);
        itemList.setItems(items);
        transaction.setItemList(itemList);
     
        List<Transaction> listTransaction = new ArrayList<>();
        listTransaction.add(transaction);  
         
        return listTransaction;
    }
     
    private String getApprovalLink(Payment approvedPayment) {
        List<Links> links = approvedPayment.getLinks();
        String approvalLink = null;
         
        for (Links link : links) {
            if (link.getRel().equalsIgnoreCase("approval_url")) {
                approvalLink = link.getHref();
                break;
            }
        }      
         
        return approvalLink;
    }

	/*
	 * public Payment getPaymentDetails(String paymentId) throws PayPalRESTException
	 * { APIContext apiContext = new APIContext(CLIENT_ID, CLIENT_SECRET, MODE);
	 * return Payment.get(apiContext, paymentId); }
	 */
    

	public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
		PaymentExecution paymentExecution = new PaymentExecution();
		paymentExecution.setPayerId(payerId);

		Payment payment = new Payment().setId(paymentId);
	

		return payment.execute(apiContext, paymentExecution);
	}
	 
}