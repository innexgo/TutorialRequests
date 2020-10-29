package innexgo.hours;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.*;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

import com.amazonaws.regions.Regions;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest; 

@Service
public class SendMailSESService {

  @Value("${EMAIL_ADDR}")
  private String emailAddr;

  @Value("${SENDGRID_API_KEY}")
  private String sgApiKey;

  @Value("${WEBSITE_LINK}")
  private String websiteLink;

  @Autowired
  private UserService userService;

  private EnvironmentVariableCredentialsProvider awsCredentialProvider = new EnvironmentVariableCredentialsProvider();

  private AmazonSimpleEmailService amazonSESClient = 
            AmazonSimpleEmailServiceClientBuilder.standard()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentialProvider.getCredentials()))
            .withRegion(Regions.US_WEST_1).build();

  private final String emailVerificationSubject = "Innexgo Hours Email Verification";

  private final String emailVerificationHTMLBlankBody = "<p>Email verification service requested under the name: request_name</p>"
  + "<p>If you did not make this request, then feel free to ignore.</p>"
  + "<p>This link is valid for up to 15 minutes.</p>"
  + "<p>Verification link: verification_link</p>";

  private final String emailVerificationTextBlankBody = "Email verification service requested under the name: request_name"
  + "If you did not make this request, then feel free to ignore."
  + "This link is valid for up to 15 minutes."
  + "Verification link: verification_link";

  private Mail buildForgotPasswordTemplate(long ID) {
    User user = userService.getById(ID);
    Mail mail = new Mail();

    Email fromEmail = new Email();
    fromEmail.setName("Reset Password");
    fromEmail.setEmail(emailAddr);
    mail.setFrom(fromEmail);

    mail.setTemplateId("d-62e6fcdd178349ad88646755822224a2"); // TODO make this template actually look nice.

    Personalization personalization = new Personalization();
    personalization.addDynamicTemplateData("request_name", user.name);
    personalization.addDynamicTemplateData("request_email", user.email);
    // personalization.addDynamicTemplateData("resetLink", ); //TODO add verif link
    // generator
    personalization.addTo(new Email("innexgo@gmail.com")); // user.email));
    mail.addPersonalization(personalization);

    return mail;
  }

  public void emailResetPasswordTemplate(long ID) throws IOException {
    final Mail dynamicTemplate = buildForgotPasswordTemplate(ID);
    send(dynamicTemplate);
  }

  public void emailVerificationTemplate(EmailVerificationChallenge emailVerificationChallenge) throws IOException {
    final Mail dynamicTemplate = buildAccVerificationTemplate(emailVerificationChallenge);
    send(dynamicTemplate);
  }

  public void emailVerification(EmailVerificationChallenge emailVerificationChallenge) throws IOException {
    EmailVerificationChallenge user = emailVerificationChallenge;

    String emailVerificationHTMLString = emailVerificationHTMLBlankBody.replace("verification_link", (websiteLink+"/api/misc/emailVerification?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

    String emailVerificationTextString = emailVerificationTextBlankBody.replace("verification_link", (websiteLink+"/api/misc/emailVerification?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

    try {
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(user.email))
            .withMessage(new Message()
                .withBody(new Body()
                    .withHtml(new Content()
                        .withCharset("UTF-8").withData(emailVerificationHTMLString))
                    .withText(new Content()
                        .withCharset("UTF-8").withData(emailVerificationTextString)))
                .withSubject(new Content()
                    .withCharset("UTF-8").withData(emailVerificationSubject)))
            .withSource(emailAddr);
        amazonSESClient.sendEmail(request);
    } catch (IOException ex) {
        System.out.println("The email was not sent. Error message: " 
            + ex.getMessage());
    }
  }
}
