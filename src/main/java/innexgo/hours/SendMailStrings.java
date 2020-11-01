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

public class SendMailStrings {
  public static final String emailVerificationSubject = "Innexgo Hours: Email Verification";
  public static final String forgotPasswordSubject = "Innexgo Hours: Reset Password";
  public static final String changedPasswordSubject = "Innexgo Hours: Password Changed";

  public static final String emailVerificationHTMLBlankBody = "<p>Required email verification requested under the name: request_name</p>"
  + "<p>If you did not make this request, then feel free to ignore.</p>"
  + "<p>This link is valid for up to 15 minutes.</p>"
  + "<p>Do not share this link with others.</p>"
  + "<p>Verification link: verification_link</p>";

  public static final String emailVerificationTextBlankBody = "Required email verification requested under the name: request_name"
  + "If you did not make this request, then feel free to ignore."
  + "This link is valid for up to 15 minutes."
  + "Do not share this link with others."
  + "Verification link: verification_link";

  public static final String forgotPasswordHTMLBlankBody = "<p>Requested password reset service.</p>"
  + "<p>If you did not make this request, then feel free to ignore.</p>"
  + "<p>This link is valid for up to 15 minutes.</p>"
  + "<p>Do not share this link with others.</p>"
  + "<p>Password Change link: forgot_password_link</p>";

  public static final String forgotPasswordTextBlankBody = "Requested password reset service."
  + "If you did not make this request, then feel free to ignore."
  + "This link is valid for up to 15 minutes."
  + "Do not share this link with others."
  + "Password Change link: forgot_password_link";

  public static final String changedPasswordHTMLBlankBody = "<p>Your password on Innexgo Hours was changed.</p>"
  + "<p>If you did not change your password, please click the link below and secure your account.</p>"
  + "<p>Password Change link: change_password_link</p>";

  public static final String changedPasswordTextBlankBody = "Your password on Innexgo Hours was changed."
  + "If you did not change your password, please click the link below and secure your account."
  + "Password Change link: change_password_link";

}
