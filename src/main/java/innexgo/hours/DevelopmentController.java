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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = {"/dev/"})
public class DevelopmentController {

  @Autowired ApiKeyService apiKeyService;
  @Autowired UserService userService;
  @Autowired SchoolInfoService schoolInfoService;
  @Autowired InnexgoService innexgoService;


  @RequestMapping("/initializeRoot/")
  public ResponseEntity<?> populateUsers(
      @RequestParam("name") String name,
      @RequestParam("domain") String domain,
      @RequestParam("adminEmail") String adminEmail,
      @RequestParam("adminName") String adminName,
      @RequestParam("adminPassword") String adminPassword
  ) {
    if (userService.getAll().size() != 0) {
      return Errors.DATABASE_INITIALIZED.getResponse();
    }

    schoolInfoService.inintialize(name, domain); 

    // create user
    User user = new User();
    user.name = adminName;
    user.email = adminEmail;
    user.kind = UserKind.ADMIN;
    user.passwordHash = Utils.encodePassword(adminPassword);
    userService.add(user);

    return new ResponseEntity<>(user, HttpStatus.OK);
  }
}
