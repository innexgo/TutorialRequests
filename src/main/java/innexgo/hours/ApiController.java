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

import java.util.stream.Stream;
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
  SubscriptionService subscriptionService;
  @Autowired
  SessionService sessionService;
  @Autowired
  SessionDataService sessionDataService;
  @Autowired
  SessionRequestService sessionRequestService;
  @Autowired
  SessionRequestResponseService sessionRequestResponseService;
  @Autowired
  CommittmentService committmentService;
  @Autowired
  SchoolService schoolService;
  @Autowired
  SchoolDataService schoolDataService;
  @Autowired
  LocationService locationService;
  @Autowired
  CommittmentResponseService committmentResponseService;
  @Autowired
  VerificationChallengeService verificationChallengeService;
  @Autowired
  MailService mailService;
  @Autowired
  PasswordService passwordService;
  @Autowired
  PasswordResetService passwordResetService;
  @Autowired
  AdminshipRequestService adminshipRequestService;
  @Autowired
  AdminshipRequestResponseService adminshipRequestResponseService;
  @Autowired
  AdminshipService adminshipService;
  @Autowired
  CourseKeyService courseKeyService;
  @Autowired
  CourseService courseService;
  @Autowired
  CourseDataService courseDataService;
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
  @RequestMapping("/apiKey/newValid/")
  public ResponseEntity<?> newValidApiKey( //
      @RequestParam String userEmail, //
      @RequestParam String userPassword, //
      @RequestParam long duration) {
    // Ensure user is valid
    User u = userService.getByEmail(userEmail);
    if (u == null) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    if (!innexgoService.isValidPassword(u.userId, userPassword)) {
      return Errors.PASSWORD_INCORRECT.getResponse();
    }

    // randomly generate key
    String key = Utils.generateKey();

    // now actually make apiKey
    ApiKey apiKey = new ApiKey();
    apiKey.apiKeyHash = Utils.hashGeneratedKey(key);
    apiKey.creatorUserId = u.userId;
    apiKey.creationTime = System.currentTimeMillis();
    apiKey.duration = duration;
    apiKey.key = key;
    apiKey.apiKeyKind = ApiKeyKind.VALID;
    apiKeyService.add(apiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(apiKey), HttpStatus.OK);
  }

  @RequestMapping("/apiKey/newCancel/")
  public ResponseEntity<?> newCancelApiKey( //
      @RequestParam String apiKeyToCancel, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // check if api key to cancel is valid
    ApiKey toCancel = innexgoService.getApiKeyIfValid(apiKeyToCancel);
    if (toCancel == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that both creators are the same
    if (key.creatorUserId != toCancel.creatorUserId) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // now actually make apiKey
    ApiKey newApiKey = new ApiKey();
    newApiKey.apiKeyHash = Utils.hashGeneratedKey(apiKeyToCancel);
    newApiKey.creatorUserId = key.creatorUserId;
    newApiKey.creationTime = System.currentTimeMillis();
    newApiKey.key = apiKeyToCancel;
    newApiKey.apiKeyKind = ApiKeyKind.VALID;
    newApiKey.duration = 0;

    apiKeyService.add(newApiKey);
    return new ResponseEntity<>(innexgoService.fillApiKey(newApiKey), HttpStatus.OK);
  }

  @RequestMapping("/verificationChallenge/new/")
  public ResponseEntity<?> newVerificationChallenge( //
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

    Long lastEmailSent = verificationChallengeService.getLastCreationTimeByEmail(userEmail);
    if (lastEmailSent != null && System.currentTimeMillis() < (lastEmailSent + fiveMinutes)) {
      return Errors.EMAIL_RATELIMIT.getResponse();
    }

    if (mailService.emailExistsInBlacklist(userEmail)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    // randomly generate key
    String rawKey = Utils.generateKey();

    VerificationChallenge evc = new VerificationChallenge();
    evc.name = userName.toUpperCase();
    evc.email = userEmail;
    evc.creationTime = System.currentTimeMillis();
    evc.verificationChallengeKeyHash = Utils.hashGeneratedKey(rawKey);
    evc.passwordHash = Utils.encodePassword(userPassword);
    verificationChallengeService.add(evc);

    mailService.send(userEmail, "Innexgo Hours: Email Verification",
        "<p>Required email verification requested under the name: " + evc.name + "</p>" //
            + "<p>If you did not make this request, then feel free to ignore.</p>" //
            + "<p>This link is valid for up to 15 minutes.</p>" //
            + "<p>Do not share this link with others.</p>" //
            + "<p>Verification link: " //
            + innexgoHoursSite + "/register_confirm?verificationChallengeKey=" + rawKey //
            + "</p>"); //

    return new ResponseEntity<>(innexgoService.fillVerificationChallenge(evc), HttpStatus.OK);
  }

  @RequestMapping("/user/new/")
  public ResponseEntity<?> newUser(@RequestParam String verificationChallengeKey) {
    VerificationChallenge evc = verificationChallengeService
        .getByVerificationChallengeKeyHash(Utils.hashGeneratedKey(verificationChallengeKey));

    if (evc == null) {
      return Errors.VERIFICATION_CHALLENGE_NONEXISTENT.getResponse();
    }

    if (userService.existsByVerificationChallengeKeyHash(evc.verificationChallengeKeyHash)) {
      return Errors.USER_EXISTENT.getResponse();
    }

    if (userService.existsByEmail(evc.email)) {
      return Errors.USER_EXISTENT.getResponse();
    }

    final long now = System.currentTimeMillis();

    if ((evc.creationTime + fifteenMinutes) < now) {
      return Errors.VERIFICATION_CHALLENGE_TIMED_OUT.getResponse();
    }

    User u = new User();
    u.creationTime = System.currentTimeMillis();
    u.name = evc.name;
    u.email = evc.email;
    u.verificationChallengeKeyHash = evc.verificationChallengeKeyHash;
    userService.add(u);

    Password p = new Password();
    p.creationTime = System.currentTimeMillis();
    p.creatorUserId = u.userId;
    p.userId = u.userId;
    p.passwordHash = evc.passwordHash;
    p.passwordKind = PasswordKind.CHANGE;
    p.passwordResetKeyHash = "";
    passwordService.add(p);
    return new ResponseEntity<>(innexgoService.fillUser(u), HttpStatus.OK);
  }

  @RequestMapping("/passwordReset/new/")
  public ResponseEntity<?> newPasswordReset(@RequestParam String userEmail) {
    if (mailService.emailExistsInBlacklist(userEmail)) {
      return Errors.EMAIL_BLACKLISTED.getResponse();
    }

    User user = userService.getByEmail(userEmail);
    if (user == null) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // generate raw random key
    String rawKey = Utils.generateKey();

    PasswordReset pr = new PasswordReset();
    pr.passwordResetKeyHash = Utils.hashGeneratedKey(rawKey);
    pr.creationTime = System.currentTimeMillis();
    pr.creatorUserId = user.userId;

    mailService.send(user.email, "Innexgo Hours: Password Reset", //
        "<p>Requested password reset service.</p>" + //
            "<p>If you did not make this request, then feel free to ignore.</p>" + //
            "<p>This link is valid for up to 15 minutes.</p>" + //
            "<p>Do not share this link with others.</p>" + //
            "<p>Password Change link: " + //
            innexgoHoursSite + "/reset_password?resetKey=" + rawKey + "</p>" //
    ); //

    passwordResetService.add(pr);
    return new ResponseEntity<>(innexgoService.fillPasswordReset(pr), HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/password/newChange/")
  public ResponseEntity<?> newPasswordChange( //
      @RequestParam long userId, //
      @RequestParam String newPassword, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // TODO later we'd like admins / mods to be able to set user's passwords

    if (key.creatorUserId != userId) {
      return Errors.PASSWORD_CANNOT_CREATE_FOR_OTHERS.getResponse();
    }

    if (!Utils.securePassword(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    Password password = new Password();
    password.creationTime = System.currentTimeMillis();
    password.creatorUserId = key.creatorUserId;
    password.userId = key.creatorUserId;
    password.passwordKind = PasswordKind.CHANGE;
    password.passwordHash = Utils.encodePassword(newPassword);
    password.passwordResetKeyHash = "";

    passwordService.add(password);
    return new ResponseEntity<>(innexgoService.fillPassword(password), HttpStatus.OK);
  }

  // This method updates the password for same user only
  @RequestMapping("/password/newCancel/")
  public ResponseEntity<?> newPasswordCancel( //
      @RequestParam long userId, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // TODO later we'd like admins / mods to be able to set user's passwords

    if (key.creatorUserId != userId) {
      return Errors.PASSWORD_CANNOT_CREATE_FOR_OTHERS.getResponse();
    }

    Password password = new Password();
    password.creationTime = System.currentTimeMillis();
    password.creatorUserId = key.creatorUserId;
    password.userId = key.creatorUserId;
    password.passwordKind = PasswordKind.CANCEL;
    password.passwordHash = "";
    password.passwordResetKeyHash = "";

    passwordService.add(password);
    return new ResponseEntity<>(innexgoService.fillPassword(password), HttpStatus.OK);
  }

  @RequestMapping("/password/newReset/")
  public ResponseEntity<?> newPasswordReset( //
      @RequestParam String passwordResetKey, //
      @RequestParam String newPassword //
  ) {

    PasswordReset psr = passwordResetService.getByPasswordResetKeyHash(Utils.hashGeneratedKey(passwordResetKey));

    if (psr == null) {
      return Errors.PASSWORD_RESET_NONEXISTENT.getResponse();
    }

    // deny if timed out
    if (System.currentTimeMillis() > (psr.creationTime + fifteenMinutes)) {
      return Errors.PASSWORD_RESET_TIMED_OUT.getResponse();
    }

    // deny if password already exists created from this psr
    if (passwordService.existsByPasswordResetKeyHash(psr.passwordResetKeyHash)) {
      return Errors.PASSWORD_EXISTENT.getResponse();
    }

    // reject insecure passwords
    if (!Utils.securePassword(newPassword)) {
      return Errors.PASSWORD_INSECURE.getResponse();
    }

    Password password = new Password();
    password.creationTime = System.currentTimeMillis();
    password.creatorUserId = psr.creatorUserId;
    password.userId = psr.creatorUserId;
    password.passwordKind = PasswordKind.RESET;
    password.passwordHash = Utils.encodePassword(newPassword);
    password.passwordResetKeyHash = psr.passwordResetKeyHash;

    passwordService.add(password);
    return new ResponseEntity<>(innexgoService.fillPassword(password), HttpStatus.OK);
  }

  @RequestMapping("/subscription/new/")
  public ResponseEntity<?> newSubscription( //
      @RequestParam SubscriptionKind subscriptionKind, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    Subscription subscription = new Subscription();
    subscription.creationTime = System.currentTimeMillis();
    subscription.creatorUserId = key.creatorUserId;
    subscription.subscriptionKind = subscriptionKind;
    subscription.maxUses = 1;
    subscriptionService.add(subscription);

    return new ResponseEntity<>(innexgoService.fillSubscription(subscription), HttpStatus.OK);
  }

  @RequestMapping("/course/new/")
  public ResponseEntity<?> newCourse( //
      @RequestParam long schoolId, //
      @RequestParam String name, //
      @RequestParam String description, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that school exists
    if (!schoolService.existsBySchoolId(schoolId)) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // if so check if key creator is admin
    boolean creatorAdmin = adminshipService.isAdmin(key.creatorUserId, schoolId);
    if (!creatorAdmin) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Course course = new Course();
    course.creationTime = System.currentTimeMillis();
    course.creatorUserId = key.creatorUserId;
    course.schoolId = schoolId;
    courseService.add(course);

    CourseData courseData = new CourseData();
    courseData.creationTime = System.currentTimeMillis();
    courseData.creatorUserId = key.creatorUserId;
    courseData.courseId = course.courseId;
    courseData.name = name;
    courseData.description = description;
    courseData.active = true;
    courseDataService.add(courseData);

    // creating the course should give the creator a course membership, which they
    // can later discharge if necessary
    CourseMembership courseMembership = new CourseMembership();
    courseMembership.creationTime = System.currentTimeMillis();
    courseMembership.creatorUserId = key.creatorUserId;
    courseMembership.userId = key.creatorUserId;
    courseMembership.courseId = course.courseId;
    courseMembership.courseMembershipKind = CourseMembershipKind.INSTRUCTOR;
    courseMembership.courseMembershipSourceKind = CourseMembershipSourceKind.SET;
    courseMembership.courseKeyId = 0;
    courseMembershipService.add(courseMembership);

    return new ResponseEntity<>(innexgoService.fillCourse(course), HttpStatus.OK);
  }

  @RequestMapping("/courseData/new/")
  public ResponseEntity<?> newCourseData( //
      @RequestParam long courseId, //
      @RequestParam String name, //
      @RequestParam String description, //
      @RequestParam boolean active, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that course exists
    Course course = courseService.getByCourseId(courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // if so check if key creator is admin
    if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)
        && !courseMembershipService.isInstructor(key.creatorUserId, courseId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    CourseData courseData = new CourseData();
    courseData.creationTime = System.currentTimeMillis();
    courseData.creatorUserId = key.creatorUserId;
    courseData.courseId = courseId;
    courseData.name = name;
    courseData.description = description;
    courseData.active = active;
    courseDataService.add(courseData);

    return new ResponseEntity<>(innexgoService.fillCourseData(courseData), HttpStatus.OK);
  }

  // This method enables signing in with a password on courses
  @RequestMapping("/courseKey/newValid/")
  public ResponseEntity<?> newCourseKeyValid( //
      @RequestParam long courseId, //
      @RequestParam CourseMembershipKind courseMembershipKind, //
      @RequestParam long duration, //
      @RequestParam long maxUses, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // check parameters make sense
    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }
    if (maxUses < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    // check that course exists
    Course course = courseService.getByCourseId(courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }


    // check if the creator is an admin of this course's school or a teacher of this
    // course
    if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)
        && !courseMembershipService.isInstructor(key.creatorUserId, courseId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // now actually make courseKey
    CourseKey ck = new CourseKey();
    ck.creatorUserId = key.creatorUserId;
    ck.creationTime = System.currentTimeMillis();
    ck.courseId = courseId;
    ck.key = Utils.generateKey();
    ck.courseKeyKind = CourseKeyKind.VALID;
    ck.courseMembershipKind = courseMembershipKind;
    ck.duration = duration;
    ck.maxUses = maxUses;
    courseKeyService.add(ck);

    return new ResponseEntity<>(innexgoService.fillCourseKey(ck), HttpStatus.OK);
  }

  // This method disables a previous course key
  @RequestMapping("/courseKey/newCancel/")
  public ResponseEntity<?> newCourseKeyCancel( //
      @RequestParam String courseKey, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // check that course key exists
    CourseKey oldCourseKey = courseKeyService.getByCourseKey(courseKey);
    if (oldCourseKey == null) {
      return Errors.COURSE_KEY_NONEXISTENT.getResponse();
    }

    // check that course exists
    Course course = courseService.getByCourseId(oldCourseKey.courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(course.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    if (courseMembershipService.isInstructor(key.creatorUserId, oldCourseKey.courseId)) {
      // this means the creator isnt an instructor
      // they could still be authorized to create the key if they are an admin, so
      // let's check that
      if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)) {
        // if they're not an admin either, return unauthorized
        return Errors.API_KEY_UNAUTHORIZED.getResponse();
      }
    }

    // now actually make courseKey
    CourseKey ck = new CourseKey();
    ck.creatorUserId = key.creatorUserId;
    ck.creationTime = System.currentTimeMillis();
    ck.courseId = oldCourseKey.courseId;
    ck.key = oldCourseKey.key;
    ck.courseKeyKind = CourseKeyKind.CANCEL;
    ck.courseMembershipKind = CourseMembershipKind.CANCEL;
    ck.duration = 0l;
    ck.maxUses = 0l;
    courseKeyService.add(ck);

    return new ResponseEntity<>(innexgoService.fillCourseKey(ck), HttpStatus.OK);
  }

  // lets you cancel an existing course membership
  @RequestMapping("/courseMembership/newCancel/")
  public ResponseEntity<?> newCourseMembershipCancel( //
      @RequestParam long userId, //
      @RequestParam long courseId, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that user exists
    if (!userService.existsByUserId(userId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    // check that course exists
    Course course = courseService.getByCourseId(courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // check if the creator is an admin of this course's school
    // or an instructor of this course
    // or student trying to remoe themself
    CourseMembership ucm = courseMembershipService.getByUserIdCourseId(userId, courseId);
    if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)
       && !(ucm != null && ucm.courseMembershipKind == CourseMembershipKind.INSTRUCTOR)
       && !(ucm != null && ucm.courseMembershipKind == CourseMembershipKind.STUDENT && ucm.userId == key.creatorUserId)
    ) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // if they already have a non cancelled membership in this school:
    if (ucm != null && ucm.courseMembershipKind != CourseMembershipKind.CANCEL) {
        // prevent removing the last instructor of a course (and orphaning it)
        if (ucm.courseMembershipKind == CourseMembershipKind.INSTRUCTOR
            && courseMembershipService.numInstructors(courseId) <= 1) {
          return Errors.COURSE_MEMBERSHIP_CANNOT_LEAVE_EMPTY.getResponse();
        }
    }

    CourseMembership cm = new CourseMembership();
    cm.creationTime = System.currentTimeMillis();
    cm.creatorUserId = key.creatorUserId;
    cm.courseId = courseId;
    cm.userId = userId;
    cm.courseMembershipKind = CourseMembershipKind.CANCEL;
    cm.courseMembershipSourceKind = CourseMembershipSourceKind.SET;
    courseMembershipService.add(cm);
    return new ResponseEntity<>(innexgoService.fillCourseMembership(cm), HttpStatus.OK);
  }

  @RequestMapping("/courseMembership/newKey/")
  public ResponseEntity<?> newCourseMembershipKey( //
      @RequestParam String courseKey, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that course key exists
    CourseKey ck = courseKeyService.getByCourseKey(courseKey);
    if (ck == null) {
      return Errors.COURSE_KEY_NONEXISTENT.getResponse();
    }

    // check that course exists
    Course course = courseService.getByCourseId(ck.courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(course.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // check that course key isn't expired
    if (ck.duration + ck.creationTime < System.currentTimeMillis()) {
      return Errors.COURSE_KEY_EXPIRED.getResponse();
    }

    // check that course key has less that max number of uses
    if (courseMembershipService.numCourseKeyUses(ck.courseKeyId) >= ck.maxUses) {
      return Errors.COURSE_KEY_USED.getResponse();
    }

    // user course membership
    CourseMembership ucm = courseMembershipService.getByUserIdCourseId(key.creatorUserId, ck.courseId);
    if (ucm != null && ucm.courseMembershipKind != CourseMembershipKind.CANCEL) {
      switch (ck.courseMembershipKind) {
        case STUDENT:
        case INSTRUCTOR: {
          return Errors.COURSE_MEMBERSHIP_EXISTENT.getResponse();
        }
        case CANCEL: {
          // prevent removing the last instructor of a course (and orphaning it)
          if (ucm.courseMembershipKind == CourseMembershipKind.INSTRUCTOR
              && courseMembershipService.numInstructors(ck.courseId) <= 1) {
            return Errors.COURSE_MEMBERSHIP_CANNOT_LEAVE_EMPTY.getResponse();
          }
        }
      }
    }

    CourseMembership cm = new CourseMembership();
    cm.creationTime = System.currentTimeMillis();
    cm.creatorUserId = key.creatorUserId;
    cm.courseId = ck.courseId;
    cm.userId = key.creatorUserId;
    cm.courseMembershipKind = ck.courseMembershipKind;
    cm.courseMembershipSourceKind = CourseMembershipSourceKind.KEY;
    cm.courseKeyId = ck.courseKeyId;
    courseMembershipService.add(cm);
    return new ResponseEntity<>(innexgoService.fillCourseMembership(cm), HttpStatus.OK);
  }

  @RequestMapping("/school/new/")
  public ResponseEntity<?> newSchool( //
      @RequestParam String name, //
      @RequestParam String description, //
      @RequestParam boolean whole, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check if user has a valid subscription perms
    Subscription subscription = subscriptionService.getSubscriptionByUserId(key.creatorUserId);
    if (subscription == null || subscription.subscriptionKind == SubscriptionKind.CANCEL) {
      return Errors.SUBSCRIPTION_NONEXISTENT.getResponse();
    }

    // check number of schools in subscription to verify that they have perms
    if (adminshipService.numSubscriptionUses(key.creatorUserId) >= subscription.maxUses) {
      return Errors.SUBSCRIPTION_LIMITED.getResponse();
    }

    School school = new School();
    school.creationTime = System.currentTimeMillis();
    school.creatorUserId = key.creatorUserId;
    school.whole = whole;
    schoolService.add(school);

    SchoolData schoolData = new SchoolData();
    schoolData.creationTime = System.currentTimeMillis();
    schoolData.creatorUserId = key.creatorUserId;
    schoolData.schoolId = school.schoolId;
    schoolData.name = name;
    schoolData.description = description;
    schoolData.active = true;
    schoolDataService.add(schoolData);

    // creating the school should give the creator an adminship, which they
    // can later discharge if necessary
    Adminship adminship = new Adminship();
    adminship.creationTime = System.currentTimeMillis();
    adminship.creatorUserId = key.creatorUserId;
    adminship.userId = key.creatorUserId;
    adminship.schoolId = school.schoolId;
    adminship.adminshipKind = AdminshipKind.ADMIN;
    adminship.adminshipSourceKind = AdminshipSourceKind.SET;
    adminship.adminshipRequestResponseId = 55555555; // doesnt matter
    adminshipService.add(adminship);
    return new ResponseEntity<>(innexgoService.fillSchool(school), HttpStatus.OK);
  }

  @RequestMapping("/schoolData/new/")
  public ResponseEntity<?> newSchoolData( //
      @RequestParam long schoolId, //
      @RequestParam String name, //
      @RequestParam String description, //
      @RequestParam boolean active, //
      @RequestParam String apiKey //
  ) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that school exists
    if (!schoolService.existsBySchoolId(schoolId)) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // if so check if key creator is admin
    boolean creatorAdmin = adminshipService.isAdmin(key.creatorUserId, schoolId);
    if (!creatorAdmin) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    SchoolData schoolData = new SchoolData();
    schoolData.creationTime = System.currentTimeMillis();
    schoolData.creatorUserId = key.creatorUserId;
    schoolData.schoolId = schoolId;
    schoolData.name = name;
    schoolData.description = description;
    schoolData.active = active;
    schoolDataService.add(schoolData);

    return new ResponseEntity<>(innexgoService.fillSchoolData(schoolData), HttpStatus.OK);
  }

  @RequestMapping("/adminshipRequest/new/")
  public ResponseEntity<?> newAdminshipRequest( //
      @RequestParam long schoolId, //
      @RequestParam String message, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (schoolService.getBySchoolId(schoolId) == null) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // check that school is unarchived
    if (!schoolDataService.getBySchoolId(schoolId).active) {
      return Errors.SCHOOL_ARCHIVED.getResponse();
    }


    // creator must be an active subscriber in order to create a adminship request
    // for a school
    if (!subscriptionService.isSubscriber(key.creatorUserId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    AdminshipRequest sr = new AdminshipRequest();
    sr.creatorUserId = key.creatorUserId;
    sr.creationTime = System.currentTimeMillis();
    sr.schoolId = schoolId;
    sr.message = message;
    adminshipRequestService.add(sr);
    return new ResponseEntity<>(innexgoService.fillAdminshipRequest(sr), HttpStatus.OK);
  }

  @RequestMapping("/adminshipRequestResponse/new/")
  public ResponseEntity<?> newAdminshipRequestResponse( //
      @RequestParam long adminshipRequestId, //
      @RequestParam String message, //
      @RequestParam boolean accept, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (adminshipRequestResponseService.existsByAdminshipRequestId(adminshipRequestId)) {
      return Errors.ADMINSHIP_REQUEST_RESPONSE_EXISTENT.getResponse();
    }

    AdminshipRequest ar = adminshipRequestService.getByAdminshipRequestId(adminshipRequestId);
    if (ar == null) {
      return Errors.ADMINSHIP_REQUEST_NONEXISTENT.getResponse();
    }

    if (schoolService.getBySchoolId(ar.schoolId) == null) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // check that school is unarchived
    if (!schoolDataService.getBySchoolId(ar.schoolId).active) {
      return Errors.SCHOOL_ARCHIVED.getResponse();
    }

    // a adminship may be only be dealt with by an Administrator at that school
    if (!adminshipService.isAdmin(key.creatorUserId, ar.schoolId)) { //
      // an exception is if the creator is rejecting his own appointment (cancelling
      // the request)
      if (ar.creatorUserId == key.creatorUserId && !accept) {
        // don't exit early
      } else {
        // unauthorized
        return Errors.API_KEY_UNAUTHORIZED.getResponse();
      }
    }

    AdminshipRequestResponse arr = new AdminshipRequestResponse();
    arr.creationTime = System.currentTimeMillis();
    arr.adminshipRequestId = adminshipRequestId;
    arr.creatorUserId = key.creatorUserId;
    arr.message = message;
    arr.accepted = accept;
    adminshipRequestResponseService.add(arr);
    return new ResponseEntity<>(innexgoService.fillAdminshipRequestResponse(arr), HttpStatus.OK);
  }

  @RequestMapping("/adminship/newCancel/")
  public ResponseEntity<?> newCancelAdminship( //
      @RequestParam long userId, //
      @RequestParam long schoolId, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
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

    // check that school is unarchived
    if (!schoolDataService.getBySchoolId(schoolId).active) {
      return Errors.SCHOOL_ARCHIVED.getResponse();
    }

    // check authorization
    if (!adminshipService.isAdmin(key.creatorUserId, schoolId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    // prevent admins from removing the last member of a group (and orphaning it)
    if (adminshipService.numAdmins(schoolId) <= 1) {
      return Errors.ADMINSHIP_CANNOT_LEAVE_EMPTY.getResponse();
    }

    Adminship cm = new Adminship();
    cm.creationTime = System.currentTimeMillis();
    cm.creatorUserId = key.creatorUserId;
    cm.userId = userId;
    cm.schoolId = schoolId;
    cm.adminshipKind = AdminshipKind.CANCEL;
    cm.adminshipSourceKind = AdminshipSourceKind.SET;
    adminshipService.add(cm);
    return new ResponseEntity<>(innexgoService.fillAdminship(cm), HttpStatus.OK);
  }

  @RequestMapping("/adminship/newValid/")
  public ResponseEntity<?> newValidAdminship( //
      @RequestParam long adminshipRequestResponseId, //
      @RequestParam String apiKey) {
    // check that key valid
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    // check that adminship request response exists
    AdminshipRequestResponse arr = adminshipRequestResponseService.getByAdminshipRequestId(adminshipRequestResponseId);
    if (arr == null) {
      return Errors.ADMINSHIP_REQUEST_RESPONSE_NONEXISTENT.getResponse();
    }

    if (!arr.accepted) {
      return Errors.ADMINSHIP_REQUEST_RESPONSE_INVALID.getResponse();
    }

    // check that it was the same person who made it
    AdminshipRequest ar = adminshipRequestService.getByAdminshipRequestId(adminshipRequestResponseId);
    if (ar.creatorUserId != key.creatorUserId) {
      return Errors.ADMINSHIP_REQUEST_RESPONSE_CANNOT_USE_OTHERS.getResponse();
    }

    // check that school exists
    if (!schoolService.existsBySchoolId(ar.schoolId)) {
      return Errors.SCHOOL_NONEXISTENT.getResponse();
    }

    // check that school is unarchived
    if (!schoolDataService.getBySchoolId(ar.schoolId).active) {
      return Errors.SCHOOL_ARCHIVED.getResponse();
    }

    // check creator's subscription perms
    Subscription subscription = subscriptionService.getSubscriptionByUserId(key.creatorUserId);
    if (subscription == null || subscription.subscriptionKind == SubscriptionKind.CANCEL) {
      return Errors.SUBSCRIPTION_NONEXISTENT.getResponse();
    }

    // check number of schools in subscription to verify that they have perms
    if (adminshipService.numSubscriptionUses(key.creatorUserId) >= subscription.maxUses) {
      return Errors.SUBSCRIPTION_LIMITED.getResponse();
    }

    // give adminship
    Adminship adminship = new Adminship();
    adminship.creationTime = System.currentTimeMillis();
    adminship.creatorUserId = key.creatorUserId;
    adminship.userId = key.creatorUserId;
    adminship.schoolId = ar.schoolId;
    adminship.adminshipKind = AdminshipKind.ADMIN;
    adminship.adminshipSourceKind = AdminshipSourceKind.REQUEST;
    adminship.adminshipRequestResponseId = adminshipRequestResponseId;
    adminshipService.add(adminship);
    return new ResponseEntity<>(innexgoService.fillAdminship(adminship), HttpStatus.OK);
  }

  @RequestMapping("/session/new/")
  public ResponseEntity<?> newSession( //
      @RequestParam String name, //
      @RequestParam long courseId, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam boolean hidden, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    Course course = courseService.getByCourseId(courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(course.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // Location location = locationService.getByLocationId(locationId);
    // if (location == null) {
    //   return Errors.LOCATION_NONEXISTENT.getResponse();
    // }

    // // TODO a working permissioning system + figure out what to do with location
    // if (location.schoolId != course.schoolId) {
    //   return Errors.LOCATION_NONEXISTENT.getResponse();
    // }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    // check if the creator is an admin of this course's school or a teacher of this
    // course
    if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)
        && !courseMembershipService.isInstructor(key.creatorUserId, courseId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Session s = new Session();
    s.creationTime = System.currentTimeMillis();
    s.creatorUserId = key.creatorUserId;
    s.courseId = courseId;
    sessionService.add(s);

    SessionData sessionData = new SessionData();
    sessionData.creationTime = System.currentTimeMillis();
    sessionData.creatorUserId = key.creatorUserId;
    sessionData.sessionId = s.sessionId;
    sessionData.name = name;
    sessionData.startTime = startTime;
    sessionData.duration = duration;
    sessionData.hidden = hidden;
    sessionData.active = true;
    sessionDataService.add(sessionData);

    return new ResponseEntity<>(innexgoService.fillSession(s), HttpStatus.OK);
  }

  @RequestMapping("/sessionData/new/")
  public ResponseEntity<?> newSessionData( //
      @RequestParam long sessionId, //
      @RequestParam String name, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam boolean hidden, //
      @RequestParam boolean active, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    Session session = sessionService.getBySessionId(sessionId);
    if (session == null) {
      return Errors.SESSION_NONEXISTENT.getResponse();
    }

    Course course = courseService.getByCourseId(session.courseId);
    if (course == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check if the creator is an admin of this course's school or a teacher of this
    // course
    if (!adminshipService.isAdmin(key.creatorUserId, course.schoolId)
        && !courseMembershipService.isInstructor(key.creatorUserId, session.courseId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    SessionData sessionData = new SessionData();
    sessionData.creationTime = System.currentTimeMillis();
    sessionData.creatorUserId = key.creatorUserId;
    sessionData.sessionId = sessionId;
    sessionData.name = name;
    sessionData.startTime = startTime;
    sessionData.duration = duration;
    sessionData.hidden = hidden;
    sessionData.active = active;
    sessionDataService.add(sessionData);

    return new ResponseEntity<>(innexgoService.fillSessionData(sessionData), HttpStatus.OK);
  }

  @RequestMapping("/sessionRequest/new/")
  public ResponseEntity<?> newSessionRequest( //
      @RequestParam long courseId, //
      @RequestParam String message, //
      @RequestParam long startTime, //
      @RequestParam long duration, //
      @RequestParam String apiKey) {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (duration < 0) {
      return Errors.NEGATIVE_DURATION.getResponse();
    }

    if (courseService.getByCourseId(courseId) == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    if (!courseDataService.getByCourseId(courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // creator must be a student of this course in order to create a session request
    // for it
    if (!courseMembershipService.isStudent(key.creatorUserId, courseId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    SessionRequest sr = new SessionRequest();
    sr.creatorUserId = key.creatorUserId;
    sr.creationTime = System.currentTimeMillis();
    sr.attendeeUserId = key.creatorUserId;
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
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (sessionRequestResponseService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_RESPONSE_EXISTENT.getResponse();
    }


    if (!sessionRequestService.existsBySessionRequestId(sessionRequestId)) {
      return Errors.SESSION_REQUEST_NONEXISTENT.getResponse();
    }

    // a session may be rejected by an Administrator, an Instructor, or the student
    // who created it
    SessionRequest sr = sessionRequestService.getBySessionRequestId(sessionRequestId);
    if (sr.attendeeUserId != key.creatorUserId //
        && !courseMembershipService.isInstructor(key.creatorUserId, sr.courseId) //
        && !adminshipService.isAdmin(key.creatorUserId, courseService.getByCourseId(sr.courseId).schoolId)) { //
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    if (courseService.getByCourseId(sr.courseId) == null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    if (!courseDataService.getByCourseId(sr.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    SessionRequestResponse srr = new SessionRequestResponse();
    srr.creationTime = System.currentTimeMillis();
    srr.sessionRequestId = sessionRequestId;
    srr.creatorUserId = key.creatorUserId;
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
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
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
    if (sr == null) {
      return Errors.SESSION_REQUEST_NONEXISTENT.getResponse();
    }

    // check that course exists
    if (courseService.getByCourseId(sr.courseId)== null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (! courseDataService.getByCourseId(sr.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }


    // check if the creator is an admin of request's course's school or a teacher of
    // request's
    // course
    if (!courseMembershipService.isInstructor(key.creatorUserId, sr.courseId)
        && !adminshipService.isAdmin(key.creatorUserId, courseService.getByCourseId(sr.courseId).schoolId)) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }
    SessionRequestResponse srr = new SessionRequestResponse();
    srr.sessionRequestId = sessionRequestId;
    srr.creatorUserId = key.creatorUserId;
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
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (!userService.existsByUserId(attendeeUserId)) {
      return Errors.USER_NONEXISTENT.getResponse();
    }

    Session s = sessionService.getBySessionId(sessionId);
    if (s == null) {
      return Errors.SESSION_NONEXISTENT.getResponse();
    }

    if (courseService.getByCourseId(s.courseId)== null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(s.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // check if the creator is an admin of request's course's school or a teacher of
    // request's
    // course
    if (!courseMembershipService.isInstructor(key.creatorUserId, s.courseId)
        && !adminshipService.isAdmin(key.creatorUserId, courseService.getByCourseId(s.courseId).schoolId)) {
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
    boolean preexisting = committmentService.unrespondedExistsByAttendeeIdSessionId( //
        attendeeUserId, // Long attendeeUserId,
        sessionId// Long sessionId,
    );

    if (preexisting) {
      return Errors.COMMITTMENT_EXISTENT.getResponse();
    }

    Committment c = new Committment();
    c.creatorUserId = key.creatorUserId;
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
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_NONEXISTENT.getResponse();
    }

    if (committmentResponseService.existsByCommittmentId(committmentId)) {
      return Errors.COMMITTMENT_RESPONSE_EXISTENT.getResponse();
    }

    Committment committment = committmentService.getByCommittmentId(committmentId);
    if (committment == null) {
      return Errors.COMMITTMENT_NONEXISTENT.getResponse();
    }
    Session s = sessionService.getBySessionId(committment.sessionId);

    if (courseService.getByCourseId(s.courseId)== null) {
      return Errors.COURSE_NONEXISTENT.getResponse();
    }

    // check that course isn't archived
    if (!courseDataService.getByCourseId(s.courseId).active) {
      return Errors.COURSE_ARCHIVED.getResponse();
    }

    // check if the creator is an admin of request's course's school or a teacher of
    // request's
    // course
    if (!courseMembershipService.isInstructor(key.creatorUserId, s.courseId)
        && !adminshipService.isAdmin(key.creatorUserId, courseService.getByCourseId(s.courseId).schoolId)) {

      // this means that the creator is neither an instructor nor an admin
      if (key.creatorUserId == committment.attendeeUserId && committment.cancellable
          && committmentResponseKind == CommittmentResponseKind.CANCELLED) {
        // cancelling is only allowed if the creator is the one cancelling
        // otherwise, return unauthorized
        return Errors.API_KEY_UNAUTHORIZED.getResponse();
      }
    }

    CommittmentResponse a = new CommittmentResponse();
    a.committmentId = committmentId;
    a.creatorUserId = key.creatorUserId;
    a.creationTime = System.currentTimeMillis();
    a.kind = committmentResponseKind;
    committmentResponseService.add(a);
    return new ResponseEntity<>(innexgoService.fillCommittmentResponse(a), HttpStatus.OK);
  }

  @RequestMapping("/subscription/")
  public ResponseEntity<?> viewSubscription( //
      @RequestParam(required = false) Long subscriptionId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) SubscriptionKind subscriptionKind, //
      @RequestParam(required = false) Long maxUses, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Subscription> list = subscriptionService.query( //
        subscriptionId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        subscriptionKind, //
        maxUses, //
        onlyRecent, //
        offset, //
        count //
    ).map(x -> innexgoService.fillSubscription(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/school/")
  public ResponseEntity<?> viewSchool( //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Boolean whole, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<School> list = schoolService.query( //
        schoolId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        whole, //
        offset, //
        count //
    ).map(x -> innexgoService.fillSchool(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/schoolData/")
  public ResponseEntity<?> viewSchoolData( //
      @RequestParam(required = false) Long schoolDataId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) String description, //
      @RequestParam(required = false) String partialDescription, //
      @RequestParam(required = false) Boolean active, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(required = false) Long recentAdminUserId, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<SchoolData> list = schoolDataService.query( //
        schoolDataId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        schoolId, //
        name, //
        partialName, //
        description, //
        partialDescription, //
        active, //
        onlyRecent, //
        recentAdminUserId, //
        offset, //
        count //
    ).map(x -> innexgoService.fillSchoolData(x));
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

    Stream<User> list = userService.query( //
        userId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        userName, //
        partialUserName, //
        userEmail, //
        offset, //
        count //
    ).map(x -> innexgoService.fillUser(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/password/")
  public ResponseEntity<?> viewPassword( //
      @RequestParam(required = false) Long passwordId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long userId, //
      @RequestParam(required = false) PasswordKind passwordKind, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Password> list = passwordService.query( //
        passwordId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        userId, //
        passwordKind, //
        onlyRecent, //
        offset, //
        count //
    ).map(x -> innexgoService.fillPassword(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  public ResponseEntity<?> viewApiKey( //
      @RequestParam(required = false) Long apiKeyId, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) ApiKeyKind apiKeyKind, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<ApiKey> list = apiKeyService.query( //
        apiKeyId, //
        creatorUserId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        duration, //
        minDuration, //
        maxDuration, //
        apiKeyKind, //
        onlyRecent, //
        offset, //
        count //
    ).map(x -> innexgoService.fillApiKey(x));
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
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Course> list = courseService.query( //
        courseId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        schoolId, //
        offset, //
        count //
    ).map(x -> innexgoService.fillCourse(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/courseData/")
  public ResponseEntity<?> viewCourseData( //
      @RequestParam(required = false) Long courseDataId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) String description, //
      @RequestParam(required = false) String partialDescription, //
      @RequestParam(required = false) Boolean active, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) Long recentMemberUserId, //
      @RequestParam(required = false) Long recentInstructorUserId, //
      @RequestParam(required = false) Long recentStudentUserId, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<CourseData> list = courseDataService.query( //
        courseDataId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        courseId, //
        name, //
        partialName, //
        description, //
        partialDescription, //
        active, //
        onlyRecent, //
        schoolId, //
        recentMemberUserId, //
        recentInstructorUserId, //
        recentStudentUserId, //
        offset, //
        count //
    ).map(x -> innexgoService.fillCourseData(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/courseKey/")
  public ResponseEntity<?> viewCourseKey( //
      @RequestParam(required = false) Long courseKeyId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(required = false) CourseKeyKind courseKeyKind, //
      @RequestParam(required = false) CourseMembershipKind courseMembershipKind, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Long maxUses, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<CourseKey> list = courseKeyService.query( //
        courseKeyId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        courseId, //
        courseKeyKind, //
        courseMembershipKind, //
        duration, //
        minDuration, //
        maxDuration, //
        maxUses, //
        onlyRecent, //
        offset, //
        count //
    ).map(x -> innexgoService.fillCourseKey(x));
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
      @RequestParam(required = false) CourseMembershipSourceKind courseMembershipSourceKind, //
      @RequestParam(required = false) Long courseKeyId, //
      @RequestParam(required = false) String userName, //
      @RequestParam(required = false) String partialUserName, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<CourseMembership> list = courseMembershipService.query( //
        courseMembershipId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        userId, //
        courseId, //
        courseMembershipKind, //
        courseMembershipSourceKind, //
        courseKeyId, //
        userName, //
        partialUserName, //
        onlyRecent, //
        offset, //
        count).map(x -> innexgoService.fillCourseMembership(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/adminshipRequest/")
  public ResponseEntity<?> viewAdminshipRequest( //
      @RequestParam(required = false) Long adminshipRequestId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Boolean responded, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<AdminshipRequest> list = adminshipRequestService.query( //
        adminshipRequestId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        schoolId, //
        message, //
        responded, //
        offset, //
        count).map(x -> innexgoService.fillAdminshipRequest(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/adminshipRequestResponse/")
  public ResponseEntity<?> viewAdminshipRequestResponse( //
      @RequestParam(required = false) Long adminshipRequestId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) String message, //
      @RequestParam(required = false) Boolean accepted, //
      @RequestParam(required = false) Boolean responded, //
      @RequestParam(required = false) Long requesterUserId, //
      @RequestParam(required = false) Long schoolId, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<AdminshipRequestResponse> list = adminshipRequestResponseService.query( //
        adminshipRequestId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        message, //
        accepted, //
        responded, //
        requesterUserId, //
        schoolId, //
        offset, // long offset,
        count // long count)
    ).map(x -> innexgoService.fillAdminshipRequestResponse(x));
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
      @RequestParam(required = false) AdminshipSourceKind adminshipSourceKind, //
      @RequestParam(required = false) Long adminshipRequestResponseId, //
      @RequestParam(defaultValue = "false") boolean onlyRecent, //
      @RequestParam(required = false) String userName, //
      @RequestParam(required = false) String partialUserName, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) //
  {
    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Adminship> list = adminshipService.query( //
        adminshipId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        userId, //
        schoolId, //
        adminshipKind, //
        adminshipSourceKind, //
        adminshipRequestResponseId, //
        onlyRecent, //
        userName, //
        partialUserName, //
        offset, //
        count).map(x -> innexgoService.fillAdminship(x));
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
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Session> list = sessionService.query(//
        sessionId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        courseId, //
        offset, // long offset,
        count // long count)
    ).map(x -> innexgoService.fillSession(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

  @RequestMapping("/sessionData/")
  public ResponseEntity<?> viewSessionData( //
      @RequestParam(required = false) Long sessionDataId, //
      @RequestParam(required = false) Long creationTime, //
      @RequestParam(required = false) Long minCreationTime, //
      @RequestParam(required = false) Long maxCreationTime, //
      @RequestParam(required = false) Long creatorUserId, //
      @RequestParam(required = false) Long sessionId, //
      @RequestParam(required = false) String name, //
      @RequestParam(required = false) String partialName, //
      @RequestParam(required = false) Long startTime, //
      @RequestParam(required = false) Long minStartTime, //
      @RequestParam(required = false) Long maxStartTime, //
      @RequestParam(required = false) Long duration, //
      @RequestParam(required = false) Long minDuration, //
      @RequestParam(required = false) Long maxDuration, //
      @RequestParam(required = false) Boolean hidden, //
      @RequestParam(required = false) Boolean active, //
      @RequestParam(required = false) boolean onlyRecent, //
      @RequestParam(required = false) Long courseId, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);

    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<SessionData> list = sessionDataService.query(//
        sessionDataId, //
        creationTime, //
        minCreationTime, //
        maxCreationTime, //
        creatorUserId, //
        sessionId, //
        name, //
        partialName, //
        startTime, //
        minStartTime, //
        maxStartTime, //
        duration, //
        minDuration, //
        maxDuration, //
        hidden, //
        active, //
        onlyRecent, //
        courseId, //
        offset, // long offset,
        count // long count)
    ).map(x -> innexgoService.fillSessionData(x));
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

    Stream<SessionRequest> list = sessionRequestService.query(//
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
    ).map(x -> innexgoService.fillSessionRequest(x));
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
      @RequestParam(required = false) Boolean responded, //
      @RequestParam(required = false) Long sessionId, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<SessionRequestResponse> list = sessionRequestResponseService.query( //
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
        responded, //
        sessionId, //
        offset, // long offset,
        count // long count)
    ).map(x -> innexgoService.fillSessionRequestResponse(x));
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
      @RequestParam(required = false) Boolean fromRequestResponse, //
      @RequestParam(defaultValue = "0") long offset, //
      @RequestParam(defaultValue = "100") long count, //
      @RequestParam String apiKey //
  ) {

    ApiKey key = innexgoService.getApiKeyIfValid(apiKey);
    if (key == null) {
      return Errors.API_KEY_UNAUTHORIZED.getResponse();
    }

    Stream<Committment> list = committmentService.query( //
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
        fromRequestResponse, //
        offset, // long offset,
        count // long count)
    ).map(x -> innexgoService.fillCommittment(x));
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

    Stream<CommittmentResponse> list = committmentResponseService.query( //
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
    ).map(x -> innexgoService.fillCommittmentResponse(x));
    return new ResponseEntity<>(list, HttpStatus.OK);
  }

}
