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
  DATABASE_INITIALIZED("the database already contains some users", HttpStatus.UNAUTHORIZED),
  PASSWORD_INCORRECT("this password is not valid for this user", HttpStatus.UNAUTHORIZED),
  PASSWORD_INSECURE("this password does not meet security requirements", HttpStatus.UNAUTHORIZED),
  USER_NONEXISTENT("the user you are trying to perform this operation on does not exist", HttpStatus.BAD_REQUEST),
  APIKEY_NONEXISTENT("the api key you are trying to perform this operation on does not exist", HttpStatus.BAD_REQUEST),
  USER_EXISTENT("a user with this email already exists", HttpStatus.BAD_REQUEST),
  APPT_REQUEST_NONEXISTENT("the appointment request you are trying to perform this operation on does not exist",
      HttpStatus.BAD_REQUEST),
  STUDENT_NAME_EMPTY("the student name must not be empty", HttpStatus.BAD_REQUEST),
  USER_NAME_EMPTY("the user name must not be empty", HttpStatus.BAD_REQUEST),
  USER_EMAIL_EMPTY("the user email must not be empty", HttpStatus.BAD_REQUEST),
  USERKIND_INVALID("must be one of STUDENT, USER, or ADMIN", HttpStatus.BAD_REQUEST),
  ATTENDANCEKIND_INVALID("must be one of ABSENT, PRESENT, or TARDY", HttpStatus.BAD_REQUEST),
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
