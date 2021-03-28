package innexgo.hours;

import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;



public class StripeCreateCheckout {

  @Autowired
  InnexgoService innexgoService;

  @Value("${STRIPE_SECRET_KEY}")
  private String stripe_secret_key;
  @RequestMapping("/create-checkout-session")
  public ResponseEntity<?> newCheckout( //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }
    SessionCreateParams params =
        SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl("/paymentsuccess")
            .setCancelUrl("/paymentfailure")
            .addLineItem(
            SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(
                SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount(1000L)
                .setProductData(
                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName("Innexgo Hours Subscription")
                    .build())
                .build())
            .build())
            .build();

    Session session = Session.create(params);

    Map<String, String> responseData = new HashMap();
    responseData.put("id", session.getId());
       
    return new ResponseEntity<>(responseData, HttpStatus.OK);
  }

}