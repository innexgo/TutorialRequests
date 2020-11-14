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
import java.io.IOException;
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
  @Autowired
  MailService mailService;
  @Autowired
  ForgotPasswordService forgotPasswordService;
  @Autowired
  InnexgoService innexgoService;

  // Human friendly name of school
  @Value("${SCHOOL_NAME}")
  String schoolName;

  // the school's domain (used for teacher validation)
  @Value("${SCHOOL_DOMAIN}")
  String schoolDomain;

  // the school's prefix used in their innexgo hours subdomain
  @Value("${SCHOOL_INNEXGO_PREFIX}")
  String schoolInnexgoPrefix;

  // The website where this application is hosted
  @Value("${INNEXGO_HOURS_SITE}")
  String innexgoHoursSite;

  final static int fiveMinutes = 5 * 60 * 1000;
  final static int fifteenMinutes = 15 * 60 * 1000;

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

  @RequestMapping("/emailVerificationChallenge/new/")
  public ResponseEntity<?> newEmailVerificationChallenge( //
      @RequestParam String userName, //
      @RequestParam String userEmail, //
      @RequestParam UserKind userKind, //
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

    if (!Utils.securePassword(userPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    Long lastEmailSent = emailVerificationChallengeService.getLastEmailCreationTimeByEmail(userEmail);
    if (lastEmailSent != null && System.currentTimeMillis() < (lastEmailSent + fiveMinutes)) {
      return Errors.EMAIL_RATELIMIT.getResponse();
    }

    if (mailService.emailExistsInBlacklist(userEmail)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    EmailVerificationChallenge evc = new EmailVerificationChallenge();
    evc.name = userName;
    evc.email = userEmail;
    evc.creationTime = System.currentTimeMillis();
    evc.verificationKey = Utils.generateKey();
    evc.passwordHash = Utils.encodePassword(userPassword);
    evc.kind = userKind;
    emailVerificationChallengeService.add(evc);
    mailService.send(userEmail, "Innexgo Hours: Email Verification",
      "<p>Required email verification requested under the name: " + evc.name
      + "</p>"
      + "<p>If you did not make this request, then feel free to ignore.</p>"
      + "<p>This link is valid for up to 15 minutes.</p>"
      + "<p>Do not share this link with others.</p>"
      + "<p>Verification link: "
      + innexgoHoursSite + "/api/user/new/?verificationKey=" + evc.verificationKey
      + "</p>");

    return new ResponseEntity<>(HttpStatus.OK);
  }



  @RequestMapping("/user/new/")
  public ResponseEntity<?> checkEmailVerification(@RequestParam String verificationKey) {

    if (Utils.isEmpty(verificationKey)) {
      return Errors.VERIFICATIONKEY_NONEXISTENT.getResponse();
    }

    if (!emailVerificationChallengeService.existsByVerificationKey(verificationKey)) {
      return Errors.VERIFICATIONKEY_NONEXISTENT.getResponse();
    }

    EmailVerificationChallenge evc = emailVerificationChallengeService.getByVerificationKey(verificationKey);

    final long now = System.currentTimeMillis();

    if ((evc.creationTime + fifteenMinutes) < now) {
      return Errors.VERIFICATIONKEY_TIMED_OUT.getResponse();
    }

    if (userService.existsByEmail(evc.email)) {
      return Errors.USER_EXISTENT.getResponse();
    }

    User u = new User();
    u.name = evc.name;
    u.email = evc.email;
    u.passwordHash = evc.passwordHash;
    u.passwordSetTime = now;
    u.kind = evc.kind;

    userService.add(u);

    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }

  @RequestMapping("/forgotPassword/new/")
  public ResponseEntity<?> newForgotPassword(@RequestParam String userEmail) {

    if (!userService.existsByEmail(userEmail)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    User user = userService.getByEmail(userEmail);

    final long now = System.currentTimeMillis();

    if ((user.passwordSetTime + fiveMinutes) > now) {
      return Errors.EMAIL_RATELIMIT.getResponse();
    }

    if (mailService.emailExistsInBlacklist(userEmail)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    ForgotPassword fp = new ForgotPassword();
    fp.email = userEmail;
    fp.creationTime = now;
    fp.used = false;
    fp.resetKey = Utils.generateKey();

    forgotPasswordService.add(fp);


    mailService.send(
      fp.email,
      "Innexgo Hours: Password Reset",
      "<p>Requested password reset service.</p>"
      + "<p>If you did not make this request, then feel free to ignore.</p>"
      + "<p>This link is valid for up to 15 minutes.</p>"
      + "<p>Do not share this link with others.</p>"
      + "<p>Password Change link: "
      + innexgoHoursSite + "/misc/resetPassword/?resetKey=" + fp.resetKey 
      + "</p>");

    return new ResponseEntity<>(HttpStatus.OK);
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
      return Errors.APIKEY_NONEXISTENT.getResponse();
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
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.APIKEY_NONEXISTENT.getResponse();
    }

    if(keyCreator.kind == UserKind.STUDENT) {
        return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    if(apptService.existsByApptRequestId(apptRequestId)) {
      return Errors.APPT_EXISTENT.getResponse();
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
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.APIKEY_NONEXISTENT.getResponse();
    }

    if(keyCreator.kind == UserKind.STUDENT) {
        return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (attendanceService.existsByApptId(apptId)) {
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
        null, //
        null, // Don't expose password reset times to external Api
        null, //
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
      @RequestParam long userId, //
      @RequestParam String oldPassword, //
      @RequestParam String newPassword, // 
      @RequestParam String apiKey  // 
  ) throws IOException {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.APIKEY_UNAUTHORIZED.getResponse();
    }

    if (!userService.existsById(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    User user = userService.getById(userId);

    if (!Utils.matchesPassword(oldPassword, user.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    if (!Utils.securePassword(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    if (mailService.emailExistsInBlacklist(user.email)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    user.passwordHash = Utils.encodePassword(newPassword);
    user.passwordSetTime = System.currentTimeMillis();
    userService.update(user);

    mailService.send(
        user.email,
        "Innexgo Hours: Password Changed",
        "Your password on Innexgo Hours was changed. If you did not change your password, please secure your account."
    );

    return Errors.OK.getResponse();
  }

  @RequestMapping("/misc/info/school/")
  public ResponseEntity<?> viewSchool() {
    return new ResponseEntity<>(new Object() {
      public final String name = schoolName;
      public final String domain = schoolDomain;
    }, HttpStatus.OK);
  }

  

  @RequestMapping("/misc/resetPassword/")
  public ResponseEntity<?> checkResetPassword( //
      @RequestParam String resetKey, //
      @RequestParam String newUserPassword //
  ) throws IOException {

    if (Utils.isEmpty(resetKey)) {
      return Errors.RESETKEY_INVALID.getResponse();
    }

    if (!forgotPasswordService.existsByResetKey(resetKey)) {
      return Errors.RESETKEY_NONEXISTENT.getResponse();
    }

    ForgotPassword forgotPassword = forgotPasswordService.getByResetKey(resetKey);

    // deny if timed out
    if (System.currentTimeMillis() > (forgotPassword.creationTime + fifteenMinutes)) {
      return Errors.RESETKEY_TIMED_OUT.getResponse();
    } 

    // deny if already used
    if (forgotPassword.used) {
      return Errors.RESETKEY_INVALID.getResponse();
    }

    // deny if email blacklisted
    if (mailService.emailExistsInBlacklist(forgotPassword.email)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    User u = userService.getByEmail(forgotPassword.email);
    u.passwordHash = Utils.encodePassword(newUserPassword);
    u.passwordSetTime = System.currentTimeMillis();
    userService.update(u);
    
    mailService.send(
      u.email,
      "Innexgo Hours: Password Changed",
      "Your password on Innexgo Hours was changed. If you did not change your password, please secure your account."
    );

    forgotPassword.used = true;
    forgotPasswordService.update(forgotPassword);

    return Errors.OK.getResponse();
  }
}
