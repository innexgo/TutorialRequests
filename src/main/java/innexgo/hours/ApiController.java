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
  AttendanceService attendanceService;
  @Autowired
  SchoolInfoService schoolInfoService;

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
      @RequestParam("userPassword") String password, @RequestParam("duration") Long duration) {
    // Ensure user exists
    if (!userService.existsByEmail(userEmail)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    // Ensure password is valid
    User u = userService.getByEmail(userEmail);
    if (!Utils.matchesPassword(password, u.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    // now actually make apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.creatorId = u.id;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.duration = duration;
    apiKey.key = Utils.generateKey();
    apiKey.keyHash = Utils.encodeApiKey(apiKey.key);
    apiKeyService.add(apiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKey), HttpStatus.OK);
  }

  @RequestMapping("/user/new/")
  public ResponseEntity<?> newUser(@RequestParam("userName") String name, @RequestParam("userEmail") String email,
      @RequestParam("userPassword") String password, @RequestParam("userKind") UserKind kind) {
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

  @RequestMapping("/apptRequest/new/")
  public ResponseEntity<?> newApptRequest(@RequestParam("targetId") Long targetId,
      @RequestParam("attending") Boolean attending, @RequestParam("message") String message,
      @RequestParam("suggestedTime") Long suggestedTime, @RequestParam("apiKey") String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (!userService.existsById(targetId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    long hostId;
    long attendeeId;
    if (attending) {
      hostId = targetId;
      attendeeId = key.creatorId;
    } else {
      hostId = key.creatorId;
      attendeeId = targetId;
    }

    ApptRequest ar = new ApptRequest();
    ar.creatorId = key.creatorId;
    ar.hostId = hostId;
    ar.attendeeId = attendeeId;
    ar.message = message;
    ar.creationTime = System.currentTimeMillis();
    ar.suggestedTime = suggestedTime;
    apptRequestService.add(ar);
    return new ResponseEntity<>(innexgoService.fillApptRequest(ar), HttpStatus.OK);
  }

  @RequestMapping("/appt/new/")
  public ResponseEntity<?> newAppt(@RequestParam("apptRequestId") Long apptRequestId,
      @RequestParam("message") String message, @RequestParam("startTime") Long startTime,
      @RequestParam("duration") Long duration, @RequestParam("apiKey") String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    Appt a = new Appt();
    a.apptRequestId = apptRequestId;
    a.message = message;
    a.creationTime = System.currentTimeMillis();
    a.startTime = startTime;
    a.duration = duration;
    apptService.add(a);
    return new ResponseEntity<>(innexgoService.fillAppt(a), HttpStatus.OK);
  }

  @RequestMapping("/attendance/new/")
  public ResponseEntity<?> newAttendance(@RequestParam("apptId") Long apptId,
      @RequestParam("kind") AttendanceKind attendanceKind, @RequestParam("apiKey") String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    Attendance a = new Attendance();
    a.apptId = apptId;
    a.creationTime = System.currentTimeMillis();
    a.kind = attendanceKind;
    attendanceService.add(a);
    return new ResponseEntity<>(innexgoService.fillAttendance(a), HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser(@RequestParam(value = "offset", defaultValue = "0") Long offset,
      @RequestParam(value = "count", defaultValue = "100") Long count, @RequestParam("apiKey") String apiKey,
      @RequestParam Map<String, String> allRequestParam) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    UserKind kind = null;
    if (allRequestParam.containsKey("userKind")) {
      String userKindStr = allRequestParam.get("userKind");
      if (UserKind.contains(userKindStr)) {
        kind = UserKind.valueOf(userKindStr);
      } else {
        return Errors.USERKIND_INVALID.getResponse();
      }
    }

    List<User> list = userService
        .query(Utils.parseLong(allRequestParam.get("userId")), kind, allRequestParam.get("userName"),
            allRequestParam.get("partialUserName"), allRequestParam.get("userEmail"),
            Utils.parseBoolean(allRequestParam.get("validated")), offset, count)
        .stream().map(x -> innexgoService.fillUser(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/")
  public ResponseEntity<?> viewApptRequest(@RequestParam(value = "offset", defaultValue = "0") long offset,
      @RequestParam(value = "count", defaultValue = "100") long count, @RequestParam("apiKey") String apiKey,
      @RequestParam Map<String, String> allRequestParam) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<ApptRequest> list = apptRequestService.query(Utils.parseLong(allRequestParam.get("id")), // Long id,
        Utils.parseLong(allRequestParam.get("creatorId")), // Long creatorId,
        Utils.parseLong(allRequestParam.get("attendeeId")), // Long attendeeId,
        Utils.parseLong(allRequestParam.get("hostId")), // Long attendeeId,
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
  public ResponseEntity<?> viewAppt(@RequestParam(value = "offset", defaultValue = "0") long offset,
      @RequestParam(value = "count", defaultValue = "100") long count, @RequestParam("apiKey") String apiKey,
      @RequestParam Map<String, String> allRequestParam) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<Appt> list = apptService.query(Utils.parseLong(allRequestParam.get("apptRequestId")), // Long apptRequestId,
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
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillAppt(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/attendance/")
  public ResponseEntity<?> viewAttendance(@RequestParam(value = "offset", defaultValue = "0") long offset,
      @RequestParam(value = "count", defaultValue = "100") long count, @RequestParam("apiKey") String apiKey,
      @RequestParam Map<String, String> allRequestParam) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    AttendanceKind kind = null;
    if (allRequestParam.containsKey("attendanceKind")) {
      String attendanceKindStr = allRequestParam.get("attendanceKind");
      if (AttendanceKind.contains(attendanceKindStr)) {
        kind = AttendanceKind.valueOf(attendanceKindStr);
      } else {
        return Errors.ATTENDANCEKIND_INVALID.getResponse();
      }
    }

    List<Attendance> list = attendanceService.query(Utils.parseLong(allRequestParam.get("apptId")), // Long apptId,
        Utils.parseLong(allRequestParam.get("creationTime")), // Long creationTime,
        Utils.parseLong(allRequestParam.get("minCreationTime")), // Long minCreationTime,
        Utils.parseLong(allRequestParam.get("maxCreationTime")), // Long maxCreationTime,
        kind, // AttendanceKind kind,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillAttendance(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/misc/updatePassword/")
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

  @RequestMapping("/misc/info/school/")
  public ResponseEntity<?> viewSchool() {
    return new ResponseEntity<>(schoolInfoService.get(), HttpStatus.OK);
  }
}
