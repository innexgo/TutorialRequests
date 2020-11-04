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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public enum Errors {
  OK("operation completed successfully", HttpStatus.OK),
  NO_CAPABILITY("user is not authorized to create api key with these capabilities", HttpStatus.UNAUTHORIZED),
  APIKEY_UNAUTHORIZED("this api key does not have the capability to access this task", HttpStatus.UNAUTHORIZED),
  DATABASE_INITIALIZED("the database is already initialized", HttpStatus.UNAUTHORIZED),
  PASSWORD_INCORRECT("this password is not valid for this user", HttpStatus.UNAUTHORIZED),
  PASSWORD_INSECURE("this password does not meet security requirements", HttpStatus.UNAUTHORIZED),
  USER_NONEXISTENT("user you are trying to perform this operation on does not exist", HttpStatus.BAD_REQUEST),
  APIKEY_NONEXISTENT("the api key you are trying to perform this operation on does not exist", HttpStatus.BAD_REQUEST),
  USER_EXISTENT("a user with this email already exists", HttpStatus.BAD_REQUEST),
  APPT_REQUEST_NONEXISTENT("the appointment request you are trying to perform this operation on does not exist",
      HttpStatus.BAD_REQUEST),
  USER_NAME_EMPTY("user name must not be empty", HttpStatus.BAD_REQUEST),
  USER_EMAIL_EMPTY("user email must not be empty", HttpStatus.BAD_REQUEST),
  USER_EMAIL_INVALIDATED("this user has not finished account setup", HttpStatus.BAD_REQUEST),
  USERKIND_INVALID("userKind must be one of STUDENT, USER, or ADMIN", HttpStatus.BAD_REQUEST),
  ATTENDANCEKIND_INVALID("attendanceKind must be one of ABSENT, PRESENT, or TARDY", HttpStatus.BAD_REQUEST),
  ATTENDANCE_EXISTENT("already created attendance for this appointment", HttpStatus.BAD_REQUEST),
  NEGATIVE_DURATION("specified a negative duration", HttpStatus.BAD_REQUEST),
  VERIFICATION_KEY_NONEXISTENT("verification key does not exist", HttpStatus.BAD_REQUEST),
  VERIFICATION_KEY_INVALID("verification key is invalid", HttpStatus.BAD_REQUEST),
  VERIFICATION_KEY_TIMED_OUT("verification key has timed out", HttpStatus.BAD_REQUEST),
  ACCESS_KEY_NONEXISTENT("access key does not exist", HttpStatus.BAD_REQUEST),
  ACCESS_KEY_INVALID("access key is invalid", HttpStatus.BAD_REQUEST),
  ACCESS_KEY_TIMED_OUT("access key has timed out", HttpStatus.BAD_REQUEST),
  EMAIL_RATELIMIT("this email is being ratelimited, please wait for five minutes", HttpStatus.TOO_MANY_REQUESTS),
  EMAIL_BLACKLISTED("this email has been blacklisted from bounces or complaints", HttpStatus.FORBIDDEN), //email us at reactivate@innexgo.com to allow usage")
  UNKNOWN("an unknown error has occured", HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus httpStatus;
  final String description;

  private Errors(String description, HttpStatus status) {
    this.httpStatus = status;
    this.description = description;
  }

  public ResponseEntity<?> getResponse() {
    return new ResponseEntity<>(new ApiError(httpStatus, description), httpStatus);
  }
}
