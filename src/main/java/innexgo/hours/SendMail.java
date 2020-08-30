package hours;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class SendMail {
  @Autowired UserService userService;

  public Mail buildAccVerificationTemplate(long ID) {
    User user = userService.getById(ID);
    Mail mail = new Mail();

    Email fromEmail = new Email();
    fromEmail.setName("Email Authenticator");
    fromEmail.setEmail("email-authenticator@hours.innexgo.com");
    mail.setFrom(fromEmail);

    mail.setTemplateId("d-deadbeefdeadbeefdeadbeefdeadbeef"); //TODO set template

    Personalization personalization = new Personalization();
    personalization.addDynamicTemplateData("name", user.name);
    //personalization.addDynamicTemplateData("verifLink", ); //TODO add verif link generator 
    personalization.addTo(new Email(user.email));
    mail.addPersonalization(personalization);

    return mail;
  }

  public Mail buildForgotPasswordTemplate(long ID) {
    User user = userService.getById(ID);
    Mail mail = new Mail();

    Email fromEmail = new Email();
    fromEmail.setName("Reset Password");
    fromEmail.setEmail("reset-password@hours.innexgo.com");
    mail.setFrom(fromEmail);

    mail.setTemplateId("d-deadbeefdeadbeefdeadbeefdeadbeef"); //TODO set template

    Personalization personalization = new Personalization();
    personalization.addDynamicTemplateData("name", user.name);
    //personalization.addDynamicTemplateData("resetLink", ); //TODO add verif link generator 
    personalization.addTo(new Email(user.email));
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
    final SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY")); //TODO add .env API KEY

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