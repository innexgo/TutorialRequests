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

package innexgo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/dev/"})
public class DevelopmentController {

  @Autowired ApiKeyService apiKeyService;
  @Autowired StudentService studentService;
  @Autowired UserService userService;
  @Autowired InnexgoService innexgoService;

  static final String rootEmail = "root@example.com";

  @RequestMapping("/initializeRoot/")
  public ResponseEntity<?> populateUsers() {
    if (userService.getAll().size() != 0) {
      return Errors.DATABASE_INITIALIZED.getResponse();
    }
    User user = new User();
    user.name = "root";
    user.email = rootEmail;
    user.passwordHash = Utils.encodePassword("1234");
    userService.add(user);

    // Create apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.userId = user.id;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.expirationTime = Long.MAX_VALUE;
    apiKey.key = "testlmao";
    apiKey.keyHash = Utils.encodeApiKey(apiKey.key);
    apiKeyService.add(apiKey);
    return new ResponseEntity<>(apiKey, HttpStatus.OK);
  }
}
