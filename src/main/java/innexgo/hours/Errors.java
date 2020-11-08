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
  OK(HttpStatus.OK),
  NO_CAPABILITY(HttpStatus.UNAUTHORIZED),
  APIKEY_UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
  DATABASE_INITIALIZED(HttpStatus.UNAUTHORIZED),
  PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED),
  PASSWORD_INSECURE(HttpStatus.UNAUTHORIZED),
  USER_NONEXISTENT(HttpStatus.BAD_REQUEST),
  APIKEY_NONEXISTENT(HttpStatus.BAD_REQUEST),
  USER_EXISTENT(HttpStatus.BAD_REQUEST),
  APPT_REQUEST_NONEXISTENT(HttpStatus.BAD_REQUEST),
  USER_NAME_EMPTY(HttpStatus.BAD_REQUEST),
  USER_EMAIL_EMPTY(HttpStatus.BAD_REQUEST),
  USER_EMAIL_INVALIDATED(HttpStatus.BAD_REQUEST),
  USERKIND_INVALID(HttpStatus.BAD_REQUEST),
  ATTENDANCEKIND_INVALID(HttpStatus.BAD_REQUEST),
  ATTENDANCE_EXISTENT(HttpStatus.BAD_REQUEST),
  NEGATIVE_DURATION(HttpStatus.BAD_REQUEST),
  VERIFICATIONKEY_NONEXISTENT(HttpStatus.BAD_REQUEST),
  VERIFICATIONKEY_INVALID(HttpStatus.BAD_REQUEST),
  VERIFICATIONKEY_TIMED_OUT(HttpStatus.BAD_REQUEST),
  RESETKEY_NONEXISTENT(HttpStatus.BAD_REQUEST),
  RESETKEY_INVALID(HttpStatus.BAD_REQUEST),
  RESETKEY_TIMED_OUT(HttpStatus.BAD_REQUEST),
  EMAIL_RATELIMIT(HttpStatus.TOO_MANY_REQUESTS),
  EMAIL_BLACKLISTED(HttpStatus.FORBIDDEN), //email us at reactivate@innexgo.com to allow usage
  UNKNOWN(HttpStatus.INTERNAL_SERVER_ERROR);

  private final HttpStatus httpStatus;

  private Errors(HttpStatus status) {
    this.httpStatus = status;
  }

  public ResponseEntity<?> getResponse() {
    return new ResponseEntity<>(this.name(), httpStatus);
  }
}
