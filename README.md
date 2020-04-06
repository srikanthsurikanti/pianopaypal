# PianoForte
[![Build Status](https://travis-ci.org/sothach/pianoforte.svg?branch=master)](https://travis-ci.org/sothach/pianoforte)
[![Coverage Status](https://coveralls.io/repos/github/sothach/pianoforte/badge.svg?branch=master)](https://coveralls.io/github/sothach/pianoforte?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bb4d91d0da86443c85d58bbf225189a8)](https://www.codacy.com/manual/sothach/pianoforte?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sothach/pianoforte&amp;utm_campaign=Badge_Grade)

This project demonstrates a dynamic checkout payment flow, where the calling page does not include any code
specific to a given payment provider.

The service, on receipt of a checkout request, based on the agency id in that request object (for now, could be a header),
will return a query object, similar to an Ajax payload, that the client page can use to redirect the user to the
provider's checkout page (hosted payment page), independent of our services (PCI-compliant)

## Checkout Payment Request
### `POST /pianoforte/api/payment/checkout`
```json
{
  "firstName": "Max",
  "lastName": "Krall",
  "agency": "test-agency",
  "transactionId": "158590114542110",
  "amount": 123.34
}
```

## Checkout Payment Response
```json
{
  "data": [
    {"pg_billto_postal_name_first": "Max"},
    {"pg_billto_postal_name_last": "Krall"},
    {"pg_api_login_id": "9F3FA809B8"},
    {"pg_transaction_type": "10"},
    {"pg_version_number": "1.0"},
    {"pg_total_amount": "1234.54"},
    {"pg_utc_time": "637217008961160000"},
    {"pg_transaction_order_number": "urn:test-agency:transaction-id:158590114542110"},
    {"pg_ts_hash": "7E6EC69499589F2A732CBF69F7E83DDF"},
    {"pg_return_url": "http://localhost:9090/api/payment/return"},
    {"pg_continue_url": "http://localhost:9090/api/payment/continue"},
    {"pg_cancel_url": "http://localhost:9090/api/payment/cancel"}
  ],
  "url": "https://sandbox.paymentsgateway.net/swp/co/default.aspx",
  "method": "POST",
  "contentType": "application/x-www-form-urlencoded"
}
````
## Example Client script
See [example payment page](src/main/resources/paymentPage.html)

```javascript
function doRedirect() {
    return function (response) {
        let form = document.createElement("FORM");
        form.setAttribute('method', response.method);
        form.setAttribute('action', response.url);
        form.setAttribute('enctype', response.contentType);
        response.data.forEach(function (item) {
            let input = document.createElement('input');
            let keys = Object.keys(item);
            input.name = keys[0];
            input.value = item[keys[0]];
            input.type = 'hidden';
            form.appendChild(input);
        });
        $(document.body).append(form);
        form.submit();
    };
}
```
## PayPal
ToDo...
```html
<form action="https://www.paypal.com/cgi-bin/webscr" method="post" target="_top">
    <input type="hidden" name="cmd" value="_xclick">
    <input type="hidden" name="business" value="yourbusinesspaypalaccountemail@mail.com">
    <input type="hidden" name="lc" value="US">
    <input type="hidden" name="item_name" value="Your Item Name">
    <input type="hidden" name="amount" value="YOUR TOTAL AMOUNT">
    <input type="hidden" name="currency_code" value="USD">
    <input type="hidden" name="button_subtype" value="services">
    <input type="hidden" name="no_note" value="0">
    <input type="hidden" name="cn" value="Add special instructions to the seller:">
    <input type="hidden" name="no_shipping" value="2">
    <input type="hidden" name="rm" value="1">
    <input type="hidden" name="landing_page" value="billing"> <!-- This filed redirect to Billing Page -->
    <input type="hidden" name="return" value="Your Success URL">
    <input type="hidden" name="cancel_return" value="Your Cancel Return URL">
    <input type="hidden" name="bn" value="PP-BuyNowBF:btn_buynowCC_LG.gif:NonHostedGuest">
    <input type="image" src="https://www.yourdomain.com/images/paypalpaynowbtn.gif" border="0" name="submit" alt="PayPal - The safer, easier way to pay online!">
    <img alt="" border="0" src="https://www.yourdomain.com/images/paypalpaynowbtn.gif" width="1" height="1">
</form>
```
