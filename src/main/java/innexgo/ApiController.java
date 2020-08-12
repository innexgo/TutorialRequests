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
@RequestMapping(value = {"/api"})
public class ApiController {

  Logger logger = LoggerFactory.getLogger(ApiController.class);

  @Autowired ApiKeyService apiKeyService;
  @Autowired StudentService studentService;
  @Autowired UserService userService;
  @Autowired ApptRequestService apptRequestService;

  @Autowired InnexgoService innexgoService;

  /**
   * Create a new apiKey for a User
   *
   * @param userId the id of the User
   * @param email email of the User
   * @param expirationTime time in milliseconds since 1970 when this key is due to expire
   * @param password User password
   * @return ResponseEntity with ApiKey of User and HttpStatus.OK code if successful
   * @throws ResponseEntity with HttpStatus.UNAUTHORIZED if the User is unauthorized
   * @throws ResponseEntity with HttpStatus.BAD_REQUEST if the process is unsuccessful
   */
  @RequestMapping("/apiKey/new/")
  public ResponseEntity<?> newApiKey(
      @RequestParam(value = "userId", defaultValue = "-1") Long userId,
      @RequestParam(value = "userEmail", defaultValue = "") String email,
      @RequestParam("expirationTime") Long expirationTime,
      @RequestParam("userPassword") String password) {
    // if they gave a username instead of a userId
    if (userId == -1 && !Utils.isEmpty(email)) {
      // if the email is registered
      if (userService.existsByEmail(email)) {
        // get email
        userId = userService.getByEmail(email).id;
      }
    }
    // Ensure user exists
    if(!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    // Ensure password is valid
    User u = userService.getById(userId);
    if (!Utils.matchesPassword(password, u.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }
    // now actually make apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.userId = userId;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.expirationTime = expirationTime;
    apiKey.key = Utils.generateKey();
    apiKey.keyHash = Utils.encodeApiKey(apiKey.key);
    apiKeyService.add(apiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKey), HttpStatus.OK);
  }

  @RequestMapping("/student/new/")
  public ResponseEntity<?> newStudent(
      @RequestParam("studentId") Long studentId,
      @RequestParam("studentName") String name,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.isAdministrator(apiKey)) {
      return Errors.MUST_BE_ADMIN.getResponse();
    }
    if(studentService.existsById(studentId)) {
      return Errors.STUDENT_EXISTENT.getResponse();
    }
    if(Utils.isEmpty(name)) {
      return Errors.STUDENT_NAME_EMPTY.getResponse();
    }
    Student student = new Student();
    student.id = studentId;
    student.name = name.toUpperCase();
    studentService.add(student);
    return new ResponseEntity<>(innexgoService.fillStudent(student), HttpStatus.OK);
  }

  @RequestMapping("/user/new/")
  public ResponseEntity<?> newUser(
      @RequestParam("userName") String name,
      @RequestParam("userEmail") String email,
      @RequestParam("userPassword") String password,
      @RequestParam("userRing") Integer ring,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.isAdministrator(apiKey)) {
      return Errors.MUST_BE_ADMIN.getResponse();
    }
    if(Utils.isEmpty(email)) {
      return Errors.USER_EMAIL_EMPTY.getResponse();
    }
    if(Utils.isEmpty(name)) {
      return Errors.USER_NAME_EMPTY.getResponse();
    }
    if(userService.existsByEmail(email)) {
      return Errors.USER_EXISTENT.getResponse();
    }
    User u = new User();
    u.name = name;
    u.email = email;
    u.passwordHash = Utils.encodePassword(password);
    userService.add(u);
    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/new/")
  public ResponseEntity<?> newApptRequest(
      @RequestParam("userId") Long userId,
      @RequestParam("studentId") Long studentId,
      @RequestParam("message") String message,
      @RequestParam("requestTime") Long requestTime,
      @RequestParam("requestDuration") Long requestDuration,
      @RequestParam("approved") Boolean approved,
      @RequestParam("reviewed") Boolean reviewed,
      @RequestParam("apiKey") String apiKey
    ) {
    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    if(!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    if(!studentService.existsById(studentId)) {
      return Errors.STUDENT_NONEXISTENT.getResponse();
    }

    ApptRequest ar = new ApptRequest();
    ar.userId = userId;
    ar.studentId = studentId;
    ar.message = message;
    ar.creationTime = System.currentTimeMillis();
    ar.requestTime = requestTime;
    ar.requestDuration = requestTime;
    ar.approved = approved;
    ar.reviewed = reviewed;
    ar.response = "";
    ar.attendanceStatus = AttendanceStatus.ABSENT;
    apptRequestService.add(ar);
    return new ResponseEntity<>(innexgoService.fillApptRequest(ar), HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/review/")
  public ResponseEntity<?> reviewApptRequest(
      @RequestParam("apptRequestId") Long apptRequestId, 
      @RequestParam("approved") Boolean approved, 
      @RequestParam("response") String response, 
      @RequestParam("requestTime") Long requestTime,
      @RequestParam("requestDuration") Long requestDuration,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    if(!apptRequestService.existsById(apptRequestId)) {
      return Errors.APPT_REQUEST_NONEXISTENT.getResponse();
    }
    ApptRequest ar = apptRequestService.getById(apptRequestId);
    ar.reviewed = true;
    ar.approved = approved;
    ar.response = response;
    ar.requestTime = requestTime;
    ar.requestDuration = requestDuration;
    apptRequestService.update(ar);
    return new ResponseEntity<>(ar, HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/setAttendance/")
  public ResponseEntity<?> setAttendanceAppt(
      @RequestParam("apptRequestId") Long apptRequestId,
      @RequestParam("attendanceStatus") String attendanceStatusStr,
      @RequestParam("apiKey") String apiKey
  ) {

    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    if(!apptRequestService.existsById(apptRequestId)) {
      return Errors.APPT_REQUEST_NONEXISTENT.getResponse();
    }
    if(AttendanceStatus.contains(attendanceStatusStr)) {
        return Errors.INVALID_ATTENDANCE_STATUS_KIND.getResponse();
    }
    ApptRequest ar = apptRequestService.getById(apptRequestId);
    ar.reviewed = true;
    ar.approved = true;
    ar.attendanceStatus = AttendanceStatus.valueOf(attendanceStatusStr);
    apptRequestService.update(ar);
    return new ResponseEntity<>(ar, HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/user/updatePassword/")
  public ResponseEntity<?> updatePassword(
      @RequestParam("userId") Long userId,
      @RequestParam("userOldPassword") String oldPassword,
      @RequestParam("userNewPassword") String newPassword) {

    if(!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    User user = userService.getById(userId);

    if(!Utils.isEmpty(oldPassword)
        && Utils.matchesPassword(oldPassword, user.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    if(Utils.isEmpty(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    user.passwordHash = Utils.encodePassword(newPassword);
    userService.update(user);
    return new ResponseEntity<>(innexgoService.fillUser(user), HttpStatus.OK);
  }

  @RequestMapping("/apiKey/delete/")
  public ResponseEntity<?> deleteApiKey(
      @RequestParam("apiKeyId") Long apiKeyId,
      @RequestParam("apiKey") String apiKey) {
    if (!innexgoService.isAdministrator(apiKey)) {
      return Errors.MUST_BE_ADMIN.getResponse();
    }
    if(!apiKeyService.existsById(apiKeyId)) {
      return Errors.APIKEY_NONEXISTENT.getResponse();
    }
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKeyService.deleteById(apiKeyId)), HttpStatus.OK);
  }


  @RequestMapping("/apiKey/")
  public ResponseEntity<?> viewApiKey(
      @RequestParam("offset") Long offset,
      @RequestParam("count") Long count,
      @RequestParam Map<String, String> allRequestParam) {
    String apiKey = allRequestParam.get("apiKey");
    if(!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    List<ApiKey> list =
      apiKeyService
      .query(
          Utils.parseLong(allRequestParam.get("apiKeyId")),
          Utils.parseLong(allRequestParam.get("userId")),
          Utils.parseBoolean(allRequestParam.get("administrator")),
          Utils.parseLong(allRequestParam.get("apiKeyMinCreationTime")),
          Utils.parseLong(allRequestParam.get("apiKeyMaxCreationTime")),
          allRequestParam.containsKey("apiKeyData")
            ? Utils.encodeApiKey(allRequestParam.get("apiKeyData"))
            : null,
          offset,
          count)
      .stream()
      .map(x -> innexgoService.fillApiKey(x))
      .collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/student/")
  public ResponseEntity<?> viewStudent(
      @RequestParam("offset") Long offset,
      @RequestParam("count") Long count,
      @RequestParam Map<String, String> allRequestParam) {
    String apiKey = allRequestParam.get("apiKey");
    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    List<Student> list =
      studentService
      .query(
          Utils.parseLong(allRequestParam.get("studentId")),
          allRequestParam.get("studentName"),
          allRequestParam.get("studentNamePartial"),
          offset,
          count
        )
      .stream()
      .map(x -> innexgoService.fillStudent(x))
      .collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser(
      @RequestParam("offset") Long offset,
      @RequestParam("count") Long count,
      @RequestParam Map<String, String> allRequestParam) {
    String apiKey = allRequestParam.get("apiKey");
    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }
    List<User> list =
      userService
      .query(
          Utils.parseLong(allRequestParam.get("userId")),
          allRequestParam.get("userName"),
          allRequestParam.get("userEmail"),
          offset,
          count
        )
      .stream()
      .map(x -> innexgoService.fillUser(x))
      .collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/")
  public ResponseEntity<?> viewApptRequest(
      @RequestParam("offset") Long offset,
      @RequestParam("count") Long count,
      @RequestParam Map<String, String> allRequestParam) {
    String apiKey = allRequestParam.get("apiKey");
    if (!innexgoService.isTrusted(apiKey)) {
      return Errors.MUST_BE_USER.getResponse();
    }

    AttendanceStatus kind = null;
    if(allRequestParam.containsKey("attendanceStatus")) {
      String attendanceStatusStr = allRequestParam.get("attendanceStatus");
      if(AttendanceStatus.contains(attendanceStatusStr)) {
        kind = AttendanceStatus.valueOf(attendanceStatusStr);
      } else {
        return Errors.INVALID_ATTENDANCE_STATUS_KIND.getResponse();
      }
    }

    List<ApptRequest> list = apptRequestService.query(
       Utils.parseLong(allRequestParam.get("id")),// Long id,
       Utils.parseLong(allRequestParam.get("studentId")),// Long studentId, 
       Utils.parseLong(allRequestParam.get("userId")),// Long userId,
       allRequestParam.get("message"),// String message,
       Utils.parseLong(allRequestParam.get("creationTime")),// Long creationTime,
       Utils.parseLong(allRequestParam.get("minCreationTime")),// Long minCreationTime,
       Utils.parseLong(allRequestParam.get("maxCreationTime")),// Long maxCreationTime,
       Utils.parseLong(allRequestParam.get("requestTime")),// Long requestTime,
       Utils.parseLong(allRequestParam.get("minRequestTime")),// Long minRequestTime,
       Utils.parseLong(allRequestParam.get("maxRequestTime")),// Long maxRequestTime,
       Utils.parseBoolean(allRequestParam.get("reviewed")),// Boolean reviewed,
       Utils.parseBoolean(allRequestParam.get("approved")),// Boolean approved,
       allRequestParam.get("response"),// String response,
       allRequestParam.get("all"),// Boolean present,
       offset,// long offset,
       count// long count)
      )
      .stream()
      .map(x -> innexgoService.fillApptRequest(x))
      .collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/misc/validate/")
  public ResponseEntity<?> validateTrusted(@RequestParam("apiKey") String apiKey) {
    return innexgoService.isTrusted(apiKey) ? Errors.OK.getResponse() : Errors.MUST_BE_USER.getResponse();
  }

  @RequestMapping("/misc/validateStudentId/")
  public ResponseEntity<?> validateStudentId(@RequestParam("studentId") Long studentId) {
      if(studentService.existsById(studentId)) {
        Student student = innexgoService.fillStudent(studentService.getById(studentId));
        return new ResponseEntity<>(student, HttpStatus.OK);
      } else {
        return Errors.STUDENT_NONEXISTENT.getResponse();
      }
  }

  @RequestMapping("/misc/studentApptRequest/")
  public ResponseEntity<?> studentApptRequest(
      @RequestParam("studentId") Long studentId,
      @RequestParam("userId") Long userId,
      @RequestParam("requestTime") Long requestTime,
      @RequestParam("message") String message)  {

    if(!studentService.existsById(studentId)) {
      return Errors.STUDENT_NONEXISTENT.getResponse();
    }
    if(!userService.existsById(userId)) {
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
    ar.attendanceStatus = AttendanceStatus.ABSENT
    apptRequestService.add(ar);
    return new ResponseEntity<>(ar, HttpStatus.OK);
  }

}
