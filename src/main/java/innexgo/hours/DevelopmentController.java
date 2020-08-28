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

package hours;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/dev/"})
public class DevelopmentController {

  @Autowired ApiKeyService apiKeyService;
  @Autowired UserService userService;
  @Autowired SchoolService schoolService;
  @Autowired InnexgoService innexgoService;

  static final String ROOT_EMAIL = "root@example.com";

  @RequestMapping("/initializeRoot/")
  public ResponseEntity<?> populateUsers() {
    if (userService.getAll().size() != 0) {
      return Errors.DATABASE_INITIALIZED.getResponse();
    }

    // create school
    School school = new School();
    school.name = "Squidward Community College";
    schoolService.add(school);

    // create user
    User user = new User();
    user.name = "root";
    user.secondaryId = 0;
    user.schoolId = school.id;
    user.email = ROOT_EMAIL;
    user.kind = UserKind.ADMIN;
    user.passwordHash = Utils.encodePassword("1234");
    userService.add(user);

    // Create apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.userId = user.id;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.duration = Long.MAX_VALUE;
    apiKey.key = "testlmao";
    apiKey.keyHash = Utils.encodeApiKey(apiKey.key);

    apiKey.canChangePassword = true;
    apiKey.canLogIn= true;
    apiKey.canReadUser = true;
    apiKey.canWriteUser = true;
    apiKey.canReadAppt = true;
    apiKey.canWriteAppt = true;
    apiKey.canReadApptRequest= true;
    apiKey.canWriteApptRequest= true;
    apiKey.canReadAttendance = true;
    apiKey.canWriteAttendance = true;

    apiKeyService.add(apiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKey), HttpStatus.OK);
  }
}
