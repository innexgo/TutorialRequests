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

  private final String emailVerificationSubject = "Innexgo Hours: Email Verification";
  private final String forgotPasswordSubject = "Innexgo Hours: Reset Password";

  private final String emailVerificationHTMLBlankBody = "<p>Required email verification requested under the name: request_name</p>"
  + "<p>If you did not make this request, then feel free to ignore.</p>"
  + "<p>This link is valid for up to 15 minutes.</p>"
  + "<p>Verification link: verification_link</p>";

  private final String emailVerificationTextBlankBody = "Required email verification requested under the name: request_name"
  + "If you did not make this request, then feel free to ignore."
  + "This link is valid for up to 15 minutes."
  + "Verification link: verification_link";

  private final String forgotPasswordHTMLBlankBody = "<p>Requested password reset service.</p>"
  + "<p>If you did not make this request, then feel free to ignore.</p>"
  + "<p>This link is valid for up to 15 minutes.</p>"
  + "<p>Password Change link: forgot_password_link</p>";

  private final String forgotPasswordTextBlankBody = "Requested password reset service."
  + "If you did not make this request, then feel free to ignore."
  + "This link is valid for up to 15 minutes."
  + "Password Change link: forgot_password_link";

  public void emailForgotPassword(User user) throws IOException {
    String forgotPasswordHTMLString = emailVerificationHTMLBlankBody
        .replace("forgot_password_link", (websiteLink+"/api/misc/changePassword?authKey="+user.verificationKey));

    String forgotPasswordTextString = emailVerificationTextBlankBody
        .replace("forgot_password_link", (websiteLink+"/api/misc/changePassword?authKey="+user.verificationKey));

    send(user.email, forgotPasswordHTMLString, forgotPasswordTextString, forgotPasswordSubject);
  }

  public void emailVerification(EmailVerificationChallenge user) throws IOException {
    String emailVerificationHTMLString = emailVerificationHTMLBlankBody
        .replace("verification_link", (websiteLink+"/api/misc/emailVerification?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

    String emailVerificationTextString = emailVerificationTextBlankBody
        .replace("verification_link", (websiteLink+"/api/misc/emailVerification?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

        send(user.email, emailVerificationHTMLString, emailVerificationTextString, emailVerificationSubject);
  }

  private void send(String toEmail, String emailHTMLString, String emailTextString, String emailSubject) throws IOException {
    try {
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(email))
            .withMessage(new Message()
                .withBody(new Body()
                    .withHtml(new Content()
                        .withCharset("UTF-8").withData(emailHTMLString))
                    .withText(new Content()
                        .withCharset("UTF-8").withData(emailTextString)))
                .withSubject(new Content()
                    .withCharset("UTF-8").withData(emailSubject)))
            .withSource(emailAddr);
        amazonSESClient.sendEmail(request);
    } catch (IOException ex) {
        System.out.println("The email was not sent. Error message: " 
            + ex.getMessage());
    }
  }
}
