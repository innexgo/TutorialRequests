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
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
  EmailVerificationChallengeService emailVerificationChallengeService;
  @Value("${SCHOOL_NAME}")
  String schoolName;
  @Value("${SCHOOL_DOMAIN}")
  String schoolDomain;

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
  public ResponseEntity<?> newApiKey( //
      @RequestParam String userEmail, //
      @RequestParam String userPassword, //
      @RequestParam long duration) {
    // Ensure user exists
    if (!userService.existsByEmail(userEmail)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }
    // Ensure password is valid
    User u = userService.getByEmail(userEmail);
    if (!Utils.matchesPassword(userPassword, u.passwordHash)) {
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
  public ResponseEntity<?> newUser( //
      @RequestParam String userName, //
      @RequestParam String userEmail, //
      @RequestParam String userPassword, //
      @RequestParam UserKind userKind) {
    if (Utils.isEmpty(userEmail)) {
      return Errors.USER_EMAIL_EMPTY.getResponse();
    }
    if (Utils.isEmpty(userName)) {
      return Errors.USER_NAME_EMPTY.getResponse();
    }
    if (userService.existsByEmail(userEmail)) {
      return Errors.USER_EXISTENT.getResponse();
    }
    User u = new User();
    u.name = userName;
    u.email = userEmail;
    u.passwordHash = Utils.encodePassword(userPassword);
    u.kind = userKind;
    userService.add(u);
    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/new/")
  public ResponseEntity<?> newApptRequest( //
      @RequestParam long targetId, //
      @RequestParam boolean attending, //
      @RequestParam String message, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (!userService.existsById(targetId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
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
    ar.startTime = startTime;
    ar.duration = duration;
    apptRequestService.add(ar);
    return new ResponseEntity<>(innexgoService.fillApptRequest(ar), HttpStatus.OK);
  }

  @RequestMapping("/appt/new/")
  public ResponseEntity<?> newAppt( //
      @RequestParam long apptRequestId, //
      @RequestParam String message, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
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
  public ResponseEntity<?> newAttendance( //
      @RequestParam long apptId, //
      @RequestParam AttendanceKind attendanceKind, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (attendanceService.existsById(apptId)) {
      return Errors.ATTENDANCE_EXISTENT.getResponse();
    }

    Attendance a = new Attendance();
    a.apptId = apptId;
    a.creationTime = System.currentTimeMillis();
    a.kind = attendanceKind;
    attendanceService.add(a);
    return new ResponseEntity<>(innexgoService.fillAttendance(a), HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser( //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) UserKind userKind, //
      @RequestParam(required = false) String userName, //
      @RequestParam(required = false) String partialUserName, //
      @RequestParam(required = false) String userEmail, //
      @RequestParam(required = false) Boolean validated, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<User> list = userService.query( //
        userId, //
        userKind, //
        userName, //
        partialUserName, //
        userEmail, //
        validated, //
        offset, //
        count //
    ).stream().map(x -> innexgoService.fillUser(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/apptRequest/")
  public ResponseEntity<?> viewApptRequest( //
      @RequestParam(required = false) Long apptRequestId, //
      @RequestParam(required = false) Long creatorId, //
      @RequestParam(required = false) Long attendeeId, //
      @RequestParam(required = false) Long hostId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean confirmed, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<ApptRequest> list = apptRequestService.query(//
        apptRequestId, // Long id,
        creatorId, // Long creatorId,
        attendeeId, // Long attendeeId,
        hostId, // Long attendeeId,
        message, // String message,
        creationTime, // Long creationTime,
        minCreationTime, // Long minCreationTime,
        maxCreationTime, // Long maxCreationTime,
        startTime, // Long startTime,
        minStartTime, // Long minStartTime,
        maxStartTime, // Long maxStartTime,
        duration, // Long duration,
        minDuration, // Long minDuration,
        maxDuration, // Long maxDuration,
        confirmed, // Boolean confirmed,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillApptRequest(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/appt/")
  public ResponseEntity<?> viewAppt( //
      @RequestParam(required = false) Long apptRequestId, //
      @RequestParam(required = false) Long creatorId, //
      @RequestParam(required = false) Long attendeeId, //
      @RequestParam(required = false) Long hostId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean attended, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<Appt> list = apptService.query( //
        apptRequestId, // Long apptRequestId,
        attendeeId, // Long attendeeId,
        hostId, // Long hostId,
        message, // String message,
        creationTime, // Long creationTime,
        minCreationTime, // Long minCreationTime,
        maxCreationTime, // Long maxCreationTime,
        startTime, // Long time,
        minStartTime, // Long minTime,
        maxStartTime, // Long maxTime,
        duration, // Long duration,
        minDuration, // Long minDuration,
        maxDuration, // Long maxDuration,
        attended, // Boolean attended,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillAppt(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/attendance/")
  public ResponseEntity<?> viewAttendance( //
      @RequestParam(required = false) Long apptId, //
      @RequestParam(required = false) Long attendeeId, //
      @RequestParam(required = false) Long hostId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) AttendanceKind kind, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    List<Attendance> list = attendanceService.query( //
        apptId, // Long apptId,
        attendeeId, // Long attendeeId
        hostId, // Long hostId,
        creationTime, // Long creationTime,
        minCreationTime, // Long minCreationTime,
        maxCreationTime, // Long maxCreationTime,
        startTime, // Long startTime,
        minStartTime, // Long minStartTime,
        maxStartTime, // Long maxStartTime,
        kind, // AttendanceKind kind,
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillAttendance(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/misc/updatePassword/")
  public ResponseEntity<?> updatePassword( //
      @RequestParam Long userId, //
      @RequestParam String oldPassword, //
      @RequestParam String newPassword //
  ) {

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
    return new ResponseEntity<>(new Object() {
      public final String name = schoolName;
      public final String domain = schoolDomain;
    }, HttpStatus.OK);
  }

  @RequestMapping("/misc/requestEmailVerification")
  public ResponseEntity<?> newEmailVerification( //
    @RequestParam String userName, //
    @RequestParam String userEmail, //
    @RequestParam String userPassword) {

    if (Utils.isEmpty(userEmail)) {
      return Errors.USER_EMAIL_EMPTY.getResponse();
    }
    if (Utils.isEmpty(userName)) {
      return Errors.USER_NAME_EMPTY.getResponse();
    }
    if (userService.existsByEmail(userEmail)) {
      return Errors.USER_EXISTENT.getResponse();
    }

    EmailVerificationChallenge u = new EmailVerificationChallenge();
    u.name = userName;
    u.email = userEmail;
    u.creationTime = System.currentTimeMillis();
    u.verificationKey = Utils.randomString(32);
    u.passwordHash = Utils.encodePassword(userPassword);
    emailVerificationChallengeService.add(u);

    emailVerificationChallengeService.sendVerificationEmail(u);

    return new ResponseEntity<>(innexgoService.fillEmailVerificationChallenge(u), HttpStatus.OK);
  }

  @RequestMapping("/misc/emailVerification")
  public ResponseEntity<?> checkEmailVerification(
    @RequestParam String verificationKey) {

    if (Utils.isEmpty(verificationKey)) {
      return Errors.VERIFICATION_KEY_NONEXISTENT.getResponse();
    }

    if(!emailVerificationChallengeService.existsByVerificationKey(verificationKey)) {
      return Errors.VERIFICATION_KEY_INVALID.getResponse();
    }

    EmailVerificationChallenge verificationUser = emailVerificationChallengeService.getByVerificationKey(verificationKey);

    if (!verificationUser.valid) {
      return Errors.VERIFICATION_KEY_INVALID.getResponse();
    }

    if (verificationUser.creationTime > System.currentTimeMillis() + 15*60*1000) {
      verificationUser.valid = false;
      emailVerificationChallengeService.update(verificationUser);
      return Errors.VERIFICATION_KEY_INVALID.getResponse();
    }

    if (userService.existsByEmail(verificationUser.email)) {
      verificationUser.valid = false;
      emailVerificationChallengeService.update(verificationUser);
      return Errors.USER_EXISTENT.getResponse();
    }

    User u = new User();
    u.name = verificationUser.name;
    u.email = verificationUser.email;
    u.passwordHash = verificationUser.passwordHash;
    u.kind = UserKind.STUDENT; // TODO Defaults to student, decide how to deal with that.
    userService.add(u);

    verificationUser.valid = false;
    emailVerificationChallengeService.update(verificationUser);

    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }
}
