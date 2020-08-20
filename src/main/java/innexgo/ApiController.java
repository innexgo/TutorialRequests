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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping(value = { "/api" })
public class ApiController {

  Logger logger = LoggerFactory.getLogger(ApiController.class);

  @Autowired
  ApiKeyService apiKeyService;
  @Autowired
  UserService userService;
  @Autowired
  ApptRequestService apptRequestService;
  @Autowired
  ApptService apptService;

  @Autowired
  InnexgoService innexgoService;

  /**
   * Create a new apiKey for a User
   *
   * @param userId         the id of the User
   * @param email          email of the User
   * @param expirationTime time in milliseconds since 1970 when this key is due to
   *                       expire
   * @param password       User password
   * @return ResponseEntity with ApiKey of User and HttpStatus.OK code if
   *         successful
   * @throws ResponseEntity with HttpStatus.UNAUTHORIZED if the User is
   *                        unauthorized
   * @throws ResponseEntity with HttpStatus.BAD_REQUEST if the process is
   *                        unsuccessful
   */
  @RequestMapping("/apiKey/new/")
  public ResponseEntity<?> newApiKey(@RequestParam("userEmail") String userEmail,
      @RequestParam("userPassword") String password, @RequestParam("duration") Long duration,
      @RequestParam("canLogIn") Boolean canLogIn, @RequestParam("canChangePassword") Boolean canChangePassword,
      @RequestParam("canReadUser") Boolean canReadUser, @RequestParam("canWriteUser") Boolean canWriteUser,
      @RequestParam("canReadApptRequest") Boolean canReadApptRequest,
      @RequestParam("canWriteApptRequest") Boolean canWriteApptRequest,
      @RequestParam("canReadAppt") Boolean canReadAppt, @RequestParam("canWriteAppt") Boolean canWriteAppt) {
    // Ensure user exists
    if (!userService.existsByEmail(userEmail)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    // Ensure password is valid
    User u = userService.getByEmail(userEmail);
    if (!Utils.matchesPassword(password, u.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    switch (u.kind) {
      case STUDENT: {
        // students can't
        // write users
        // write apptrequest
        // write appt
        if (canWriteUser) {
          return Errors.NO_CAPABILITY.getResponse();
        }
        if (canWriteApptRequest) {
          return Errors.NO_CAPABILITY.getResponse();
        }
        if (canWriteAppt) {
          return Errors.NO_CAPABILITY.getResponse();
        }
        break;
      }
      case USER: {
        // users can't
        // write users
        if (canWriteUser) {
          return Errors.NO_CAPABILITY.getResponse();
        }
        break;
      }
      case ADMIN: {
        // admins have full access
        break;
      }
    }

    // now actually make apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.userId = u.id;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.duration = duration;
    apiKey.key = Utils.generateKey();
    apiKey.keyHash = Utils.encodeApiKey(apiKey.key);
    apiKey.canLogIn = canLogIn;
    apiKey.canReadUser = canReadUser;
    apiKey.canWriteUser = canWriteUser;
    apiKey.canChangePassword = canChangePassword;
    apiKey.canReadApptRequest = canReadApptRequest;
    apiKey.canWriteApptRequest = canWriteApptRequest;
    apiKey.canReadAppt = canReadAppt;
    apiKey.canWriteAppt = canWriteAppt;
    apiKeyService.add(apiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKey), HttpStatus.OK);
  }

  @RequestMapping("/user/new/")
  public ResponseEntity<?> newUser(@RequestParam("userName") String name, @RequestParam("userEmail") String email,
      @RequestParam("userPassword") String password, @RequestParam("userKind") UserKind kind,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.getApiKeyIfValid(apiKey).canWriteUser) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }
    if (Utils.isEmpty(email)) {
      return Errors.USER_EMAIL_EMPTY.getResponse();
    }
    if (Utils.isEmpty(name)) {
      return Errors.USER_NAME_EMPTY.getResponse();
    }
    if (userService.existsByEmail(email)) {
      return Errors.USER_EXISTENT.getResponse();
    }
    User u = new User();
    u.name = name;
    u.email = email;
    u.passwordHash = Utils.encodePassword(password);
    u.kind = kind;
    userService.add(u);
    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/user/updatePassword/")
  public ResponseEntity<?> updatePassword(@RequestParam("userId") Long userId,
      @RequestParam("userOldPassword") String oldPassword, @RequestParam("userNewPassword") String newPassword) {

    if (!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    User user = userService.getById(userId);

    if (!Utils.isEmpty(oldPassword) && Utils.matchesPassword(oldPassword, user.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    if (Utils.isEmpty(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    user.passwordHash = Utils.encodePassword(newPassword);
    userService.update(user);
    return new ResponseEntity<>(innexgoService.fillUser(user), HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/new/")
  public ResponseEntity<?> newApptRequest(@RequestParam("creatorId") Long creatorId,
      @RequestParam("targetId") Long targetId, @RequestParam("message") String message,
      @RequestParam("suggestedTime") Long suggestedTime, @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.getApiKeyIfValid(apiKey).canWriteApptRequest) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }
    if (!userService.existsById(creatorId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    if (!userService.existsById(targetId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    ApptRequest ar = new ApptRequest();
    ar.creatorId = creatorId;
    ar.targetId = targetId;
    ar.message = message;
    ar.creationTime = System.currentTimeMillis();
    ar.suggestedTime = suggestedTime;
    apptRequestService.add(ar);
    return new ResponseEntity<>(innexgoService.fillApptRequest(ar), HttpStatus.OK);
  }

  @RequestMapping("/appt/new/")
  public ResponseEntity<?> newAppt(@RequestParam("hostId") Long hostId, @RequestParam("attendeeId") Long attendeeId,
      @RequestParam("message") String message, @RequestParam("time") Long time, @RequestParam("duration") Long duration,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.getApiKeyIfValid(apiKey).canWriteAppt) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }
    if (!userService.existsById(hostId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    if (!userService.existsById(attendeeId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    Appt a = new Appt();
    a.hostId = hostId;
    a.attendeeId = attendeeId;
    a.message = message;
    a.creationTime = System.currentTimeMillis();
    a.time = time;
    a.duration = duration;
    apptService.add(a);
    return new ResponseEntity<>(innexgoService.fillAppt(a), HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser(
                                    @RequestParam("offset") Long offset,
                                    @RequestParam("count") Long count,
                                    @RequestParam("apiKey") String apiKey,
                                    @RequestParam Map<String, String> allRequestParam) {
    if (!innexgoService.getApiKeyIfValid(apiKey).canReadUser) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    UserKind kind = null;
    if (allRequestParam.containsKey("userKind")) {
      String userKindStr = allRequestParam.get("userKind");
      if (UserKind.contains(userKindStr)) {
        kind = UserKind.valueOf(userKindStr);
      } else {
        return Errors.INVALID_ATTENDANCE_STATUS_KIND.getResponse();
      }
    }

    List<User> list = userService
        .query(
            Utils.parseLong(allRequestParam.get("userId")),
            Utils.parseLong(allRequestParam.get("userSecondaryId")),
            innexgoService.getUserIfValid(apiKey).schoolId,
            allRequestParam.get("userName"),
            allRequestParam.get("userEmail"),
            kind,
            offset,
            count
        )
        .stream()
        .map(x -> innexgoService.fillUser(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/")
  public ResponseEntity<?> viewApptRequest(
                                    @RequestParam("offset") Long offset,
                                    @RequestParam("count") Long count,
                                    @RequestParam("apiKey") String apiKey,
                                    @RequestParam Map<String, String> allRequestParam) {

    if (!innexgoService.getApiKeyIfValid(apiKey).canReadApptRequest) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<ApptRequest> list = apptRequestService.query(
        Utils.parseLong(allRequestParam.get("id")), // Long id,
        Utils.parseLong(allRequestParam.get("creatorId")), // Long creatorId,
        Utils.parseLong(allRequestParam.get("targetId")), // Long targetId,
        allRequestParam.get("message"), // String message,
        Utils.parseLong(allRequestParam.get("creationTime")), // Long creationTime,
        Utils.parseLong(allRequestParam.get("minCreationTime")), // Long minCreationTime,
        Utils.parseLong(allRequestParam.get("maxCreationTime")), // Long maxCreationTime,
        Utils.parseLong(allRequestParam.get("suggestedTime")), // Long suggestedTime,
        Utils.parseLong(allRequestParam.get("minSuggestedTime")), // Long minSuggestedTime,
        Utils.parseLong(allRequestParam.get("maxSuggestedTime")), // Long maxSuggestedTime,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillApptRequest(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/appt/")
  public ResponseEntity<?> viewAppt(
                                    @RequestParam("offset") Long offset,
                                    @RequestParam("count") Long count,
                                    @RequestParam("apiKey") String apiKey,
                                    @RequestParam Map<String, String> allRequestParam) {
    if (!innexgoService.getApiKeyIfValid(apiKey).canReadApptRequest) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    AttendanceStatus kind = null;
    if (allRequestParam.containsKey("attendanceStatus")) {
      String attendanceStatusStr = allRequestParam.get("attendanceStatus");
      if (AttendanceStatus.contains(attendanceStatusStr)) {
        kind = AttendanceStatus.valueOf(attendanceStatusStr);
      } else {
        return Errors.INVALID_ATTENDANCE_STATUS_KIND.getResponse();
      }
    }

    List<Appt> list = apptService.query(
        Utils.parseLong(allRequestParam.get("id")), // Long id,
        Utils.parseLong(allRequestParam.get("hostId")), // Long hostId,
        Utils.parseLong(allRequestParam.get("attendeeId")), // Long attendeeId,
        allRequestParam.get("message"), // String message,
        Utils.parseLong(allRequestParam.get("creationTime")), // Long creationTime,
        Utils.parseLong(allRequestParam.get("minCreationTime")), // Long minCreationTime,
        Utils.parseLong(allRequestParam.get("maxCreationTime")), // Long maxCreationTime,
        Utils.parseLong(allRequestParam.get("time")), // Long time,
        Utils.parseLong(allRequestParam.get("minTime")), // Long minTime,
        Utils.parseLong(allRequestParam.get("maxTime")), // Long maxTime,
        Utils.parseLong(allRequestParam.get("duration")), // Long duration,
        Utils.parseLong(allRequestParam.get("minDuration")), // Long minDuration,
        Utils.parseLong(allRequestParam.get("maxDuration")), // Long maxDuration,
        kind, // AttendanceStatus attendanceStatus,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillAppt(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/misc/validateStudentId/")
  public ResponseEntity<?> validateStudentId(@RequestParam("studentId") Long studentId) {
    if (studentService.existsById(studentId)) {
      Student student = innexgoService.fillStudent(studentService.getById(studentId));
      return new ResponseEntity<>(student, HttpStatus.OK);
    } else {
      return Errors.STUDENT_NONEXISTENT.getResponse();
    }
  }

  @RequestMapping("/misc/studentApptRequest/")
  public ResponseEntity<?> studentApptRequest(@RequestParam("studentId") Long studentId,
      @RequestParam("userId") Long userId, @RequestParam("requestTime") Long requestTime,
      @RequestParam("message") String message) {

    if (!studentService.existsById(studentId)) {
      return Errors.STUDENT_NONEXISTENT.getResponse();
    }
    if (!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    ApptRequest ar = new ApptRequest();
    ar.userId = userId;
    ar.studentId = studentId;
    ar.message = message;
    ar.creationTime = System.currentTimeMillis();
    ar.requestTime = requestTime;
    ar.requestDuration = 0;
    ar.approved = false;
    ar.reviewed = false;
    ar.response = "";
    ar.attendanceStatus = AttendanceStatus.ABSENT;
    apptRequestService.add(ar);
    return new ResponseEntity<>(ar, HttpStatus.OK);
  }
}
