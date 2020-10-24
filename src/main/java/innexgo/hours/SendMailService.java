package innexgo.hours;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class SendMailService {
  @Autowired UserService userService;

  private Mail buildAccVerificationTemplate(long ID) {
    User user = userService.getById(ID);
    Mail mail = new Mail();

    Email fromEmail = new Email();
    fromEmail.setName("Email Authenticator");
    fromEmail.setEmail("email-authenticator@innexgo.com");
    mail.setFrom(fromEmail);

    mail.setTemplateId("d-deadbeefdeadbeefdeadbeefdeadbeef"); //TODO set template

    Personalization personalization = new Personalization();
    personalization.addDynamicTemplateData("request_name", user.name);
    personalization.addDynamicTemplateData("request_email", user.email);
    //personalization.addDynamicTemplateData("verifLink", ); //TODO add verif link generator 
    personalization.addTo(new Email("innexgo@gmail.com")); //user.email));
    mail.addPersonalization(personalization);

    return mail;
  }

  private Mail buildForgotPasswordTemplate(long ID) {
    User user = userService.getById(ID);
    Mail mail = new Mail();

    Email fromEmail = new Email();
    fromEmail.setName("Reset Password");
    fromEmail.setEmail("reset-password@innexgo.com");
    mail.setFrom(fromEmail);

    mail.setTemplateId("d-62e6fcdd178349ad88646755822224a2"); //TODO make this template actually look nice.

    Personalization personalization = new Personalization();
    personalization.addDynamicTemplateData("request_name", user.name);
    personalization.addDynamicTemplateData("request_email", user.email);
    //personalization.addDynamicTemplateData("resetLink", ); //TODO add verif link generator 
    personalization.addTo(new Email("innexgo@gmail.com")); //user.email));
    mail.addPersonalization(personalization);

    return mail;
  }

  public void emailResetPasswordTemplate(long ID) throws IOException {
    final Mail dynamicTemplate = buildForgotPasswordTemplate(ID);
    send(dynamicTemplate);
  }

  public void emailVerificationTemplate(long ID) throws IOException {
    final Mail dynamicTemplate = buildAccVerificationTemplate(ID);
    send(dynamicTemplate);
  }

  private static void send(final Mail mail) throws IOException {
    final SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

    final Request request = new Request();
    request.setMethod(Method.POST);
    request.setEndpoint("mail/send");
    request.setBody(mail.build());

    final Response response = sg.api(request);
    //System.out.println(response.getStatusCode());
    //System.out.println(response.getBody());
    //System.out.println(response.getHeaders());
  }
}
