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
  SessionService sessionService;
  @Autowired
  SessionRequestService sessionRequestService;
  @Autowired
  SessionRequestResponseService sessionRequestResponseService;
  @Autowired
  CommittmentService committmentService;
  @Autowired
  CommittmentResponseService committmentResponseService;
  @Autowired
  EmailVerificationChallengeService emailVerificationChallengeService;
  @Autowired
  MailService mailService;
  @Autowired
  PasswordResetKeyService passwordResetKeyService;
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

  final static int fiveMinutes =     5 * 60 * 1000;
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
    apiKey.keyHash = Utils.hashGeneratedKey(apiKey.key);
    apiKey.valid = true;
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
    String rawKey = Utils.generateKey();
    evc.verificationKey = Utils.hashGeneratedKey(rawKey);
    evc.passwordHash = Utils.encodePassword(userPassword);
    evc.kind = userKind;
    emailVerificationChallengeService.add(evc);
    mailService.send(userEmail, "Innexgo Hours: Email Verification",
        "<p>Required email verification requested under the name: " + evc.name + "</p>" //
            + "<p>If you did not make this request, then feel free to ignore.</p>" //
            + "<p>This link is valid for up to 15 minutes.</p>" //
            + "<p>Do not share this link with others.</p>" //
            + "<p>Verification link: " //
            + innexgoHoursSite + "/register_confirm?verificationKey=" + rawKey //
            + "</p>"); //

    return new ResponseEntity<>(innexgoService.fillEmailVerificationChallenge(evc), HttpStatus.OK);
  }

  @RequestMapping("/user/new/")
  public ResponseEntity<?> newUser(@RequestParam String verificationKey) {
    String hashedVerificationKey = Utils.hashGeneratedKey(verificationKey);

    if (!emailVerificationChallengeService.existsByVerificationKey(hashedVerificationKey)) {
      return Errors.EMAIL_VERIFICATION_CHALLENGE_KEY_NONEXISTENT.getResponse();
    }

    EmailVerificationChallenge evc = emailVerificationChallengeService.getByVerificationKey(hashedVerificationKey);

    final long now = System.currentTimeMillis();

    if ((evc.creationTime + fifteenMinutes) < now) {
      return Errors.EMAIL_VERIFICATION_CHALLENGE_KEY_TIMED_OUT.getResponse();
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

  @RequestMapping("/passwordResetKey/new/")
  public ResponseEntity<?> newPasswordResetKey(@RequestParam String userEmail) {

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

    PasswordResetKey fp = new PasswordResetKey();
    fp.email = userEmail;
    fp.creationTime = now;
    fp.used = false;
    String rawKey = Utils.generateKey();
    fp.resetKey = Utils.hashGeneratedKey(rawKey);

    passwordResetKeyService.add(fp);

    mailService.send(fp.email, "Innexgo Hours: Password Reset", //
        "<p>Requested password reset service.</p>" + //
            "<p>If you did not make this request, then feel free to ignore.</p>" + //
            "<p>This link is valid for up to 15 minutes.</p>" + //
            "<p>Do not share this link with others.</p>" + //
            "<p>Password Change link: " + //
            innexgoHoursSite + "/reset_password?resetKey=" + rawKey + "</p>" //
    ); //

    user.passwordSetTime = System.currentTimeMillis();
    userService.update(user);

    return new ResponseEntity<>(innexgoService.fillPasswordResetKey(fp), HttpStatus.OK);
  }

  @RequestMapping("/session/new/")
  public ResponseEntity<?> newSession( //
      @RequestParam String name, //
      @RequestParam long hostId, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam boolean hidden, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (keyCreator.kind == UserKind.STUDENT) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    if (!userService.existsById(hostId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    Session s = new Session();
    s.creatorId = keyCreator.id;
    s.creationTime = System.currentTimeMillis();
    s.name = name;
    s.hostId = hostId;
    s.startTime = startTime;
    s.duration = duration;
    s.hidden = hidden;
    sessionService.add(s);
    return new ResponseEntity<>(innexgoService.fillSession(s), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequest/new/")
  public ResponseEntity<?> newSessionRequest( //
      @RequestParam long attendeeId, //
      @RequestParam long hostId, //
      @RequestParam String message, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (!userService.existsById(hostId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if (!userService.existsById(attendeeId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // Students can only create appointments for themselves (may want to change this
    // as we develop a more fine grained permissioning system)
    if ((keyCreator.kind == UserKind.STUDENT) && attendeeId != keyCreator.id) {
      return Errors.SESSION_CANNOT_CREATE_FOR_OTHERS_STUDENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    SessionRequest sr = new SessionRequest();
    sr.creatorId = keyCreator.id;
    sr.creationTime = System.currentTimeMillis();
    sr.attendeeId = attendeeId;
    sr.hostId = hostId;
    sr.message = message;
    sr.startTime = startTime;
    sr.duration = duration;
    sessionRequestService.add(sr);
    return new ResponseEntity<>(innexgoService.fillSessionRequest(sr), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequestResponse/new/")
  public ResponseEntity<?> newSessionRequestResponse( //
      @RequestParam long sessionRequestId, //
      @RequestParam String message, //
      @RequestParam boolean accepted, //
      @RequestParam(required = false) Long committmentId, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (sessionRequestService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_RESPONSE_EXISTENT.getResponse();
    }

    // students are not allowed to accept their own appointments
    if (keyCreator.kind == UserKind.STUDENT && accepted) {
      return Errors.SESSION_REQUEST_RESPONSE_CANNOT_CANCEL_STUDENT.getResponse();
    }

    if (accepted && committmentId == null) {
      return Errors.COMMITTMENT_NONEXISTENT.getResponse();
    }

    SessionRequestResponse srr = new SessionRequestResponse();
    srr.sessionRequestId = sessionRequestId;
    srr.creatorId = keyCreator.id;
    srr.message = message;
    srr.accepted = accepted;
    if (accepted) {
      srr.committmentId = committmentId;
    }
    sessionRequestResponseService.add(srr);
    return new ResponseEntity<>(innexgoService.fillSessionRequestResponse(srr), HttpStatus.OK);
  }


  @RequestMapping("/committment/new/")
  public ResponseEntity<?> newCommittment( //
      @RequestParam long attendeeId, //
      @RequestParam long sessionId, //
      @RequestParam boolean cancellable, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if(!userService.existsById(attendeeId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if(!sessionService.existsBySessionId(sessionId)) {
      return Errors.SESSION_NONEXISTENT.getResponse();
    }


    if(keyCreator.kind == UserKind.STUDENT) {
      // Students may not create committments on others behalf
      if(keyCreator.id != attendeeId) {
        return Errors.COMMITTMENT_CANNOT_CREATE_FOR_OTHERS_STUDENT.getResponse();
      }
      Session s = sessionService.getBySessionId(sessionId);
      // Students may not create committments for hidden sessions
      if(s.hidden) {
        return Errors.COMMITTMENT_CANNOT_CREATE_HIDDEN_STUDENT.getResponse();
      }
      // Students may not create uncancellable committments
      if(!cancellable) {
        return Errors.COMMITTMENT_CANNOT_CREATE_UNCANCELLABLE_STUDENT.getResponse();
      }
    }

    Committment c = new Committment();
    c.creatorId = keyCreator.id;
    c.creationTime = System.currentTimeMillis();
    c.attendeeId = attendeeId;
    c.sessionId = sessionId;
    c.cancellable = cancellable;
    committmentService.add(c);
    return new ResponseEntity<>(innexgoService.fillCommittment(c), HttpStatus.OK);
  }

  @RequestMapping("/committmentResponse/new/")
  public ResponseEntity<?> newCommittmentResponse( //
      @RequestParam long committmentId, //
      @RequestParam CommittmentResponseKind committmentResponseKind, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if(!committmentService.existsByCommittmentId(committmentId) ) {
      return Errors.COMMITTMENT_NONEXISTENT.getResponse();
    }

    if (committmentResponseService.existsByCommittmentId(committmentId)) {
      return Errors.COMMITTMENT_RESPONSE_EXISTENT.getResponse();
    }

    if (keyCreator.kind == UserKind.STUDENT) {
      Committment c = committmentService.getByCommittmentId(committmentId) ;
      // Students may only cancel their own committment
      if(keyCreator.id != c.attendeeId) {
        return Errors.COMMITTMENT_RESPONSE_CANNOT_CREATE_FOR_OTHERS_STUDENT.getResponse();
      }

      // Students may only cancel if their appointment is cancellable
      if(!c.cancellable) {
        return Errors.COMMITTMENT_RESPONSE_UNCANCELLABLE.getResponse();
      }
    }

    CommittmentResponse a = new CommittmentResponse();
    a.committmentId = committmentId;
    a.creatorId = keyCreator.id;
    a.creationTime = System.currentTimeMillis();
    a.kind = committmentResponseKind;
    committmentResponseService.add(a);
    return new ResponseEntity<>(innexgoService.fillCommittmentResponse(a), HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser( //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
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
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<User> list = userService.query( //
        userId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
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


  @RequestMapping("/session/")
  public ResponseEntity<?> viewSession( //
      @RequestParam(required=false) Long sessionId, //
      @RequestParam(required=false) Long creatorId, //
      @RequestParam(required=false) Long creationTime, //
      @RequestParam(required=false) Long minCreationTime, //
      @RequestParam(required=false) Long maxCreationTime, //
      @RequestParam(required=false) String name, //
      @RequestParam(required=false) Long hostId, //
      @RequestParam(required=false) Long startTime, //
      @RequestParam(required=false) Long minStartTime, //
      @RequestParam(required=false) Long maxStartTime, //
      @RequestParam(required=false) Long duration, //
      @RequestParam(required=false) Long minDuration, //
      @RequestParam(required=false) Long maxDuration, //
      @RequestParam(required=false) Boolean hidden, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<Session> list = sessionService.query(//
        sessionId, //
        creatorId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        name, //
        hostId, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        hidden, //
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillSession(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }


  @RequestMapping("/sessionRequest/")
  public ResponseEntity<?> viewSessionRequest( //
      @RequestParam(required=false) Long sessionRequestId, //
      @RequestParam(required=false) Long creatorId, //
      @RequestParam(required=false) Long attendeeId, //
      @RequestParam(required=false) Long hostId, //
      @RequestParam(required=false) String message, //
      @RequestParam(required=false) Long creationTime, //
      @RequestParam(required=false) Long minCreationTime, //
      @RequestParam(required=false) Long maxCreationTime, //
      @RequestParam(required=false) Long startTime, //
      @RequestParam(required=false) Long minStartTime, //
      @RequestParam(required=false) Long maxStartTime, //
      @RequestParam(required=false) Long duration, //
      @RequestParam(required=false) Long minDuration, //
      @RequestParam(required=false) Long maxDuration, //
      @RequestParam(required=false) Boolean responded, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<SessionRequest> list = sessionRequestService.query(//
        sessionRequestId, //
        creatorId, //
        attendeeId, //
        hostId, //
        message, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        responded, //
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillSessionRequest(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/sessionRequestResponse/")
  public ResponseEntity<?> viewSessionRequestResponse( //
      @RequestParam(required=false) Long sessionRequestId, //
      @RequestParam(required=false) Long creatorId, //
      @RequestParam(required=false) Long creationTime, //
      @RequestParam(required=false) Long minCreationTime, //
      @RequestParam(required=false) Long maxCreationTime, //
      @RequestParam(required=false) String message, //
      @RequestParam(required=false) Boolean accepted, //
      @RequestParam(required=false) Long committmentId, //
      @RequestParam(required=false) Long attendeeId, //
      @RequestParam(required=false) Long hostId, //
      @RequestParam(required=false) Long startTime, //
      @RequestParam(required=false) Long minStartTime, //
      @RequestParam(required=false) Long maxStartTime, //
      @RequestParam(required=false) Long duration, //
      @RequestParam(required=false) Long minDuration, //
      @RequestParam(required=false) Long maxDuration, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<SessionRequestResponse> list = sessionRequestResponseService.query( //
        sessionRequestId, //
        creatorId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        message, //
        accepted, //
        committmentId, //
        attendeeId, //
        hostId, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillSessionRequestResponse(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/committment/")
  public ResponseEntity<?> viewCommittment( //
      @RequestParam(required=false) Long committmentId, //
      @RequestParam(required=false) Long creatorId, //
      @RequestParam(required=false) Long creationTime, //
      @RequestParam(required=false) Long minCreationTime, //
      @RequestParam(required=false) Long maxCreationTime, //
      @RequestParam(required=false) Long attendeeId, //
      @RequestParam(required=false) Long sessionId, //
      @RequestParam(required=false) Boolean cancellable, //
      @RequestParam(required=false) Long hostId, //
      @RequestParam(required=false) Long startTime, //
      @RequestParam(required=false) Long minStartTime, //
      @RequestParam(required=false) Long maxStartTime, //
      @RequestParam(required=false) Long duration, //
      @RequestParam(required=false) Long minDuration, //
      @RequestParam(required=false) Long maxDuration, //
      @RequestParam(required=false) Boolean responded, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<Committment> list = committmentService.query( //
        committmentId, //
        creatorId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        attendeeId, //
        sessionId, //
        cancellable, //
        hostId, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        responded, //
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillCommittment(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/committmentResponse/")
  public ResponseEntity<?> viewCommittmentResponse( //
      @RequestParam(required=false) Long committmentId, //
      @RequestParam(required=false) Long creatorId, //
      @RequestParam(required=false) Long creationTime, //
      @RequestParam(required=false) Long minCreationTime, //
      @RequestParam(required=false) Long maxCreationTime, //
      @RequestParam(required=false) CommittmentResponseKind committmentResponseKind, //
      @RequestParam(required=false) Long attendeeId, //
      @RequestParam(required=false) Long hostId, //
      @RequestParam(required=false) Long startTime, //
      @RequestParam(required=false) Long minStartTime, //
      @RequestParam(required=false) Long maxStartTime, //
      @RequestParam(required=false) Long duration, //
      @RequestParam(required=false) Long minDuration, //
      @RequestParam(required=false) Long maxDuration, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<CommittmentResponse> list = committmentResponseService.query( //
        committmentId, //
        creatorId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        committmentResponseKind, //
        attendeeId, //
        hostId, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        offset, // long offset,
        count // long count)
    ).stream().map(x -> innexgoService.fillCommittmentResponse(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/misc/updatePassword/")
  public ResponseEntity<?> updatePassword( //
      @RequestParam long userId, //
      @RequestParam String oldPassword, //
      @RequestParam String newPassword, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
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

    user.passwordHash = Utils.encodePassword(newPassword);
    user.passwordSetTime = System.currentTimeMillis();
    userService.update(user);

    return Errors.OK.getResponse();
  }

  @RequestMapping("/misc/info/school/")
  public ResponseEntity<?> viewSchool() {
    return new ResponseEntity<>(new Object() {
      public final String name = schoolName;
      public final String domain = schoolDomain;
    }, HttpStatus.OK);
  }

  @RequestMapping("/misc/usePasswordResetKey/")
  public ResponseEntity<?> usePasswordResetKey( //
      @RequestParam String resetKey, //
      @RequestParam String newPassword //
  ) {

    String hashedResetKey = Utils.hashGeneratedKey(resetKey);
    if (!passwordResetKeyService.existsByResetKey(hashedResetKey)) {
      return Errors.PASSWORD_RESET_KEY_NONEXISTENT.getResponse();
    }

    PasswordResetKey resetPasswordKey = passwordResetKeyService.getByResetKey(hashedResetKey);

    // deny if timed out
    if (System.currentTimeMillis() > (resetPasswordKey.creationTime + fifteenMinutes)) {
      return Errors.PASSWORD_RESET_KEY_TIMED_OUT.getResponse();
    }

    // deny if already used
    if (resetPasswordKey.used) {
      return Errors.PASSWORD_RESET_KEY_INVALID.getResponse();
    }

    if (!Utils.securePassword(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    User u = userService.getByEmail(resetPasswordKey.email);
    u.passwordHash = Utils.encodePassword(newPassword);
    u.passwordSetTime = System.currentTimeMillis();
    userService.update(u);

    passwordResetKeyService.use(resetPasswordKey);

    return Errors.OK.getResponse();
  }

  @RequestMapping("/misc/initializeRoot/")
  public ResponseEntity<?> populateUsers( //
      @RequestParam("adminEmail") String adminEmail, //
      @RequestParam("adminName") String adminName, //
      @RequestParam("adminPassword") String adminPassword //
  ) {

    if (userService.getAll().size() > 0) {
      return Errors.DATABASE_INITIALIZED.getResponse();
    }

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
