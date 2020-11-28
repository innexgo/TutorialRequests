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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ErrorHandler {

  Logger logger = LoggerFactory.getLogger(ApiController.class);

  @ExceptionHandler(value = { NoHandlerFoundException.class })
  public ResponseEntity<?> notFoundHandler() {
    return Errors.NOT_FOUND.getResponse();
  }


  @ExceptionHandler(value = { Exception.class })
  public ResponseEntity<?> generalHandler(Exception e) {
    logger.error("Internal Server Error:", e);
    return Errors.UNKNOWN.getResponse();
  }
}
