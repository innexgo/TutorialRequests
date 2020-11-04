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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

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
public class MailService {
  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Value("${NOREPLY_EMAIL_ADDR}")
  private String emailAddr;

  private EnvironmentVariableCredentialsProvider awsCredentialProvider = new EnvironmentVariableCredentialsProvider();

  private AmazonSimpleEmailService amazonSESClient = //
      AmazonSimpleEmailServiceClientBuilder.standard() //
          .withCredentials(new AWSStaticCredentialsProvider(awsCredentialProvider.getCredentials())) //
          .withRegion(Regions.US_WEST_1).build(); //

  public void send(String destination, String subject, String content) {
    SendEmailRequest request = new SendEmailRequest() //
        .withDestination( //
            new Destination().withToAddresses(destination)) //
        .withMessage(new Message() //
            .withBody(new Body() //
                .withHtml(new Content() //
                    .withCharset("UTF-8").withData(content))) //
            .withSubject(new Content() //
                .withCharset("UTF-8").withData(subject))) //
        .withSource(emailAddr); //

    amazonSESClient.sendEmail(request);
  }

  public boolean emailExistsInBlacklist(String email) {
    String sql = "SELECT count(*) FROM email_blacklist WHERE email=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, email);
    return count != 0;
  }
}
