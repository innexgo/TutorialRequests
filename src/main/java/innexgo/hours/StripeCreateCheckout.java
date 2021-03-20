import java.util.HashMap;
import java.util.Map;
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
import com.google.gson.Gson;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

@Value("${STRIPE_SECRET_KEY}")
String stripe_secret_key;

public class Server {
  private static Gson gson = new Gson();

  public static void main(String[] args) {
    port(8080);
    Stripe.apiKey = stripe_secret_key;

    post("/create-checkout-session", (request, response) -> {
      response.type("application/json");

      SessionCreateParams params =
        SessionCreateParams.builder()
          .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
          .setMode(SessionCreateParams.Mode.PAYMENT)
          .setSuccessUrl("https://localhost:8080/paymentsuccess")
          .setCancelUrl("https://localhost:8080/paymentfailure")
          .addLineItem(
          SessionCreateParams.LineItem.builder()
            .setQuantity(1L)
            .setPriceData(
              SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency("usd")
                .setUnitAmount(10L)
                .setProductData(
                  SessionCreateParams.LineItem.PriceData.ProductData.builder()
                    .setName("Innexgo Subscription")
                    .build())
                .build())
            .build())
          .build();

      Session session = Session.create(params);

      Map<String, String> responseData = new HashMap();
      responseData.put("id", session.getId());

      return gson.toJson(responseData);
    });
  }
}