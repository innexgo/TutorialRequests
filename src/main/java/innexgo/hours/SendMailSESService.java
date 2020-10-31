/*
 * Innexgo Website
 * Copyright (C) 2020 Innexgo LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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


            
  public void emailForgotPassword(ForgotPassword user) throws IOException {
      String forgotPasswordHTMLString = SendMailStrings.forgotPasswordHTMLBlankBody
      .replace("forgot_password_link", (websiteLink+"/api/misc/resetPassword/?accessKey="+user.accessKey));
      
      String forgotPasswordTextString = SendMailStrings.forgotPasswordTextBlankBody
      .replace("forgot_password_link", (websiteLink+"/api/misc/resetPassword/?accessKey="+user.accessKey));
      
      send(user.email, forgotPasswordHTMLString, forgotPasswordTextString, SendMailStrings.forgotPasswordSubject);
  }
            
  public void emailChangedPassword(User user) throws IOException {
      String changedPasswordHTMLString = SendMailStrings.changedPasswordHTMLBlankBody
          .replace("change_password_link", (websiteLink+"/api/misc/requestResetPassword/?userEmail="+user.email));
      String changedPasswordTextString = SendMailStrings.changedPasswordTextBlankBody
          .replace("change_password_link", (websiteLink+"/api/misc/requestResetPassword/?userEmail="+user.email));

      send(user.email, changedPasswordHTMLString, changedPasswordTextString, SendMailStrings.changedPasswordSubject);
  }

  public void emailVerification(EmailVerificationChallenge user) throws IOException {
    String emailVerificationHTMLString = SendMailStrings.emailVerificationHTMLBlankBody
        .replace("verification_link", (websiteLink+"/api/misc/emailVerification/?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

    String emailVerificationTextString = SendMailStrings.emailVerificationTextBlankBody
        .replace("verification_link", (websiteLink+"/api/misc/emailVerification/?verificationKey="+user.verificationKey))
        .replace("request_name", user.name);

        send(user.email, emailVerificationHTMLString, emailVerificationTextString, SendMailStrings.emailVerificationSubject);
  }

  private void send(String toEmail, String emailHTMLString, String emailTextString, String emailSubject) throws IOException {
    try {
        SendEmailRequest request = new SendEmailRequest()
            .withDestination(
                new Destination().withToAddresses(toEmail))
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
    } catch (Exception ex) {
        System.out.println("The email was not sent. Error message: " 
            + ex.getMessage());
    }
  }
}
