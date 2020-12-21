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
  SchoolService schoolService;
  @Autowired
  LocationService locationService;
  @Autowired
  CommittmentResponseService committmentResponseService;
  @Autowired
  EmailVerificationChallengeService emailVerificationChallengeService;
  @Autowired
  MailService mailService;
  @Autowired
  PasswordResetKeyService passwordResetKeyService;
  @Autowired
  AdminshipService adminshipService;
  @Autowired
  CourseService courseService;
  @Autowired
  CourseMembershipService courseMembershipService;
  @Autowired
  InnexgoService innexgoService;

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
    apiKey.creatorUserId = u.userId;
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
    evc.name = userName.toUpperCase();
    evc.email = userEmail;
    evc.creationTime = System.currentTimeMillis();
    String rawKey = Utils.generateKey();
    evc.verificationKey = Utils.hashGeneratedKey(rawKey);
    evc.passwordHash = Utils.encodePassword(userPassword);
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
    u.passwordResetKeyTime = 1;

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

    if ((user.passwordResetKeyTime + fiveMinutes) > now) {
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

    return new ResponseEntity<>(innexgoService.fillPasswordResetKey(fp), HttpStatus.OK);
  }

  @RequestMapping("/courseMembership/new/")
  public ResponseEntity<?> newCourseMembership( //
      @RequestParam long userId, //
      @RequestParam long courseId, //
      @RequestParam CourseMembershipKind courseMembershipKind, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that user exists
    if (!userService.existsByUserId(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // check that course exists
    if (!courseService.existsByCourseId(courseId)) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }
    // if so retrieve course
    boolean creatorAdmin = adminshipService.isAdmin(keyCreator.userId, courseService.getByCourseId(courseId).schoolId);
    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        courseId);
    boolean creatorInstructor = creatorCourseMembershipKind != null
        && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;

    // check if the creator is an admin of this course's school or a teacher of this
    // course
    if (!creatorAdmin && !creatorInstructor) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // prevent teacher from removing themselves from a course
    if (!creatorAdmin && courseMembershipKind == CourseMembershipKind.CANCEL) {
      return Errors.COURSEMEMBERSHIP_CANNOT_REMOVE_SELF.getResponse();
    }

    CourseMembership cm = new CourseMembership();
    cm.creationTime = System.currentTimeMillis();
    cm.creatorUserId = keyCreator.userId;
    cm.courseId = courseId;
    cm.userId = userId;
    cm.courseMembershipKind = courseMembershipKind;
    courseMembershipService.add(cm);
    return new ResponseEntity<>(innexgoService.fillCourseMembership(cm), HttpStatus.OK);
  }

  @RequestMapping("/adminship/new/")
  public ResponseEntity<?> newAdminship( //
      @RequestParam long userId, //
      @RequestParam long schoolId, //
      @RequestParam AdminshipKind adminshipKind, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that user exists
    if (!userService.existsByUserId(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // check that school exists
    if (!schoolService.existsBySchoolId(schoolId)) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // check authorization
    if (!adminshipService.isAdmin(keyCreator.userId, schoolId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // prevent admins from removing themselves
    if (userId == keyCreator.userId && adminshipKind == AdminshipKind.CANCEL) {
      return Errors.COURSEMEMBERSHIP_CANNOT_REMOVE_SELF.getResponse();
    }

    Adminship cm = new Adminship();
    cm.creationTime = System.currentTimeMillis();
    cm.creatorUserId = keyCreator.userId;
    cm.schoolId = schoolId;
    cm.userId = userId;
    cm.adminshipKind = adminshipKind;
    adminshipService.add(cm);
    return new ResponseEntity<>(innexgoService.fillAdminship(cm), HttpStatus.OK);
  }

  @RequestMapping("/session/new/")
  public ResponseEntity<?> newSession( //
      @RequestParam String name, //
      @RequestParam long courseId, //
      @RequestParam long locationId, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam boolean hidden, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (!userService.existsByUserId(courseId)) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    Course course = courseService.getByCourseId(courseId);

    if (!userService.existsByUserId(locationId)) {
      return Errors.LOCATION_NONEXISTENT.getResponse();
    }
    Location location = locationService.getByLocationId(locationId);

    if (location.schoolId != course.schoolId) {
      return Errors.LOCATION_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        courseId);
    boolean creatorInstructor = creatorCourseMembershipKind != null
        && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;

    // creator must be an instructor of this course
    if (!creatorInstructor) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Session s = new Session();
    s.creatorUserId = keyCreator.userId;
    s.creationTime = System.currentTimeMillis();
    s.name = name;
    s.courseId = courseId;
    s.locationId = locationId;
    s.startTime = startTime;
    s.duration = duration;
    s.hidden = hidden;
    sessionService.add(s);
    return new ResponseEntity<>(innexgoService.fillSession(s), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequest/new/")
  public ResponseEntity<?> newSessionRequest( //
      @RequestParam long courseId, //
      @RequestParam String message, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    if (!courseService.existsByCourseId(courseId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // creator must be a student of this course can create a session request for it
    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        courseId);
    if (creatorCourseMembershipKind == null || creatorCourseMembershipKind != CourseMembershipKind.STUDENT) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    SessionRequest sr = new SessionRequest();
    sr.creatorUserId = keyCreator.userId;
    sr.creationTime = System.currentTimeMillis();
    sr.attendeeUserId = keyCreator.userId;
    sr.courseId = courseId;
    sr.message = message;
    sr.startTime = startTime;
    sr.duration = duration;
    sessionRequestService.add(sr);
    return new ResponseEntity<>(innexgoService.fillSessionRequest(sr), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequestResponse/newReject/")
  public ResponseEntity<?> newSessionRequestResponseReject( //
      @RequestParam long sessionRequestId, //
      @RequestParam String message, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (sessionRequestResponseService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_RESPONSE_EXISTENT.getResponse();
    }

    if (!sessionRequestService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_NONEXISTENT.getResponse();
    }

    // check if creator is the sessionRequest's attendee
    // sessionRequest's course's instructor
    SessionRequest sr = sessionRequestService.getBySessionRequestId(sessionRequestId);
    boolean creatorIsAttendee = sr.attendeeUserId == keyCreator.userId;

    if (!creatorIsAttendee) {
      // if the creator isn't the attendee
      CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
          sr.courseId);

      boolean creatorIsInstructor = creatorCourseMembershipKind != null
          && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;
      if (!creatorIsInstructor) {
        return Errors.API_KEY_UNAUTHORIZED.getResponse();
      }
    }

    SessionRequestResponse srr = new SessionRequestResponse();
    srr.creationTime = System.currentTimeMillis();
    srr.sessionRequestId = sessionRequestId;
    srr.creatorUserId = keyCreator.userId;
    srr.message = message;
    srr.accepted = false;
    sessionRequestResponseService.add(srr);
    return new ResponseEntity<>(innexgoService.fillSessionRequestResponse(srr), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequestResponse/newAccept")
  public ResponseEntity<?> newSessionRequestResponseAccept( //
      @RequestParam long sessionRequestId, //
      @RequestParam String message, //
      @RequestParam long committmentId, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (!committmentService.existsByCommittmentId(committmentId)) {
      return Errors.COMMITTMENT_NONEXISTENT.getResponse();
    }

    if (sessionRequestResponseService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_RESPONSE_EXISTENT.getResponse();
    }

    // check if creator is the sessionRequest's attendee
    // sessionRequest's course's instructor
    SessionRequest sr = sessionRequestService.getBySessionRequestId(sessionRequestId);

    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        sr.courseId);

    boolean creatorIsInstructor = creatorCourseMembershipKind != null
        && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;
    if (!creatorIsInstructor) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    SessionRequestResponse srr = new SessionRequestResponse();
    srr.sessionRequestId = sessionRequestId;
    srr.creatorUserId = keyCreator.userId;
    srr.message = message;
    srr.accepted = true;
    srr.committmentId = committmentId;
    sessionRequestResponseService.add(srr);
    return new ResponseEntity<>(innexgoService.fillSessionRequestResponse(srr), HttpStatus.OK);
  }

  @RequestMapping("/committment/new/")
  public ResponseEntity<?> newCommittment( //
      @RequestParam long attendeeUserId, //
      @RequestParam long sessionId, //
      @RequestParam boolean cancellable, //
      @RequestParam String apiKey) {
    User keyCreator = innexgoService.getUserIfValid(apiKey);
    if (keyCreator == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (!userService.existsByUserId(attendeeUserId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if (!sessionService.existsBySessionId(sessionId)) {
      return Errors.SESSION_NONEXISTENT.getResponse();
    }
    Session s = sessionService.getBySessionId(sessionId);

    // creator must be an instructor of the session
    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        s.courseId);

    boolean creatorIsInstructor = creatorCourseMembershipKind != null
        && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;

    if (!creatorIsInstructor) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // TODO for when we develop student stuff
    // // Students may not create committments on others behalf
    // if (keyCreator.userId != attendeeUserId) {
    // return Errors.COMMITTMENT_CANNOT_CREATE_FOR_OTHERS_STUDENT.getResponse();
    // }
    // // Students may not create committments for hidden sessions
    // if (s.hidden) {
    // return Errors.COMMITTMENT_CANNOT_CREATE_HIDDEN_STUDENT.getResponse();
    // }
    // // Students may not create uncancellable committments
    // if (!cancellable) {
    // return Errors.COMMITTMENT_CANNOT_CREATE_UNCANCELLABLE_STUDENT.getResponse();
    // }

    // check that a unresponded committment does not already exist
    List<Committment> preexisting = committmentService.query( //
        null, // Long committmentId,
        null, // Long creationTime,
        null, // Long minCreationTime,
        null, // Long maxCreationTime,
        null, // Long creatorUserId,
        attendeeUserId, // Long attendeeUserId,
        sessionId, // Long sessionId,
        null, // Boolean cancellable,
        null, // Long hostId,
        null, // Long startTime,
        null, // Long minStartTime,
        null, // Long maxStartTime,
        null, // Long duration,
        null, // Long minDuration,
        null, // Long maxDuration,
        null, // Boolean responded,
        0, // long offset,
        1 // long count
    );

    if (preexisting.size() != 0) {
      return Errors.COMMITTMENT_EXISTENT.getResponse();
    }

    Committment c = new Committment();
    c.creatorUserId = keyCreator.userId;
    c.creationTime = System.currentTimeMillis();
    c.attendeeUserId = attendeeUserId;
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

    if (!committmentService.existsByCommittmentId(committmentId)) {
      return Errors.COMMITTMENT_NONEXISTENT.getResponse();
    }

    if (committmentResponseService.existsByCommittmentId(committmentId)) {
      return Errors.COMMITTMENT_RESPONSE_EXISTENT.getResponse();
    }

    // people permitted to accept a committment are the committment's session's
    // course's instructors

    // both the comittment's session's course's instructors and the committment's
    // attendee may cancel committments

    // check if key creator is committment's session's course's instructors
    Committment c = committmentService.getByCommittmentId(committmentId);
    Session s = sessionService.getBySessionId(c.sessionId);
    CourseMembershipKind creatorCourseMembershipKind = courseMembershipService.getCourseMembership(keyCreator.userId,
        s.courseId);

    boolean creatorIsInstructor = creatorCourseMembershipKind != null
        && creatorCourseMembershipKind == CourseMembershipKind.INSTRUCTOR;

    if (!creatorIsInstructor) {
      // ensure that the creator is the committment's attendee
      if (keyCreator.userId != c.attendeeUserId) {
        return Errors.API_KEY_UNAUTHORIZED.getResponse();
      }

      // Students may only cancel if their appointment is cancellable
      if (!c.cancellable) {
        return Errors.COMMITTMENT_RESPONSE_UNCANCELLABLE.getResponse();
      }
    }

    CommittmentResponse a = new CommittmentResponse();
    a.committmentId = committmentId;
    a.creatorUserId = keyCreator.userId;
    a.creationTime = System.currentTimeMillis();
    a.kind = committmentResponseKind;
    committmentResponseService.add(a);
    return new ResponseEntity<>(innexgoService.fillCommittmentResponse(a), HttpStatus.OK);
  }

  @RequestMapping("/school/")
  public ResponseEntity<?> viewSchool(@RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) String abbreviation, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count) //
  {
    List<School> list = schoolService.query( //
        schoolId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        name, //
        partialName, //
        abbreviation, //
        offset, //
        count //
    ).stream().map(x -> innexgoService.fillSchool(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/user/")
  public ResponseEntity<?> viewUser( //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
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

  @RequestMapping("/course/")
  public ResponseEntity<?> viewCourse( //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) String description, //
      @RequestParam(required = false) String passwordHash, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<Course> list = courseService.query( //
        courseId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        schoolId, //
        name, //
        partialName, //
        description, //
        passwordHash, //
        offset, //
        count //
    ).stream().map(x -> innexgoService.fillCourse(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/courseMembership/")
  public ResponseEntity<?> viewCourseMembership( //
      @RequestParam(required = false) Long courseMembershipId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) CourseMembershipKind courseMembershipKind, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, @RequestParam String apiKey) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<CourseMembership> list = courseMembershipService.query( //
        courseMembershipId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        userId, //
        courseId, //
        courseMembershipKind, //
        offset, //
        count).stream().map(x -> innexgoService.fillCourseMembership(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/adminship/")
  public ResponseEntity<?> viewAdminship( //
      @RequestParam(required = false) Long adminshipId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) AdminshipKind adminshipKind, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, @RequestParam String apiKey) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<Adminship> list = adminshipService.query( //
        adminshipId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        userId, //
        schoolId, //
        adminshipKind, //
        offset, //
        count).stream().map(x -> innexgoService.fillAdminship(x)).collect(Collectors.toList());
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/session/")
  public ResponseEntity<?> viewSession( //
      @RequestParam(required = false) Long sessionId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) Long locationId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean hidden, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<Session> list = sessionService.query(//
        sessionId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        courseId, //
        locationId, //
        name, //
        partialName, //
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
      @RequestParam(required = false) Long sessionRequestId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long attendeeUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean responded, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    List<SessionRequest> list = sessionRequestService.query(//
        sessionRequestId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        attendeeUserId, //
        courseId, //
        message, //
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
      @RequestParam(required = false) Long sessionRequestId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Boolean accepted, //
      @RequestParam(required = false) Long committmentId, //
      @RequestParam(required = false) Long attendeeUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
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
        creatorUserId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        message, //
        accepted, //
        committmentId, //
        attendeeUserId, //
        courseId, //
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
      @RequestParam(required = false) Long committmentId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long attendeeUserId, //
      @RequestParam(required = false) Long sessionId, //
      @RequestParam(required = false) Boolean cancellable, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean responded, //
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
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        attendeeUserId, //
        sessionId, //
        cancellable, //
        courseId, //
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
      @RequestParam(required = false) Long committmentId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) CommittmentResponseKind committmentResponseKind, //
      @RequestParam(required = false) Long attendeeUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Long sessionId, //
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
        creatorUserId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        committmentResponseKind, //
        attendeeUserId, //
        courseId, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        sessionId, //
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

    if (!userService.existsByUserId(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    User user = userService.getByUserId(userId);

    if (!Utils.matchesPassword(oldPassword, user.passwordHash)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    if (!Utils.securePassword(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    userService.setPassword(user, newPassword);

    return Errors.OK.getResponse();
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
    userService.setPassword(u, newPassword);

    passwordResetKeyService.use(resetPasswordKey);

    return Errors.OK.getResponse();
  }
}
