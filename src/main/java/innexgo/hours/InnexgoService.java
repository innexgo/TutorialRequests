package innexgo.hours;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InnexgoService {

  @Autowired
  ApiKeyService apiKeyService;
  @Autowired
  LocationService locationService;
  @Autowired
  SubscriptionService subscriptionService;
  @Autowired
  SchoolService schoolService;
  @Autowired
  SchoolDataService schoolDataService;
  @Autowired
  CourseService courseService;
  @Autowired
  CourseDataService courseDataService;
  @Autowired
  CourseKeyService courseKeyService;
  @Autowired
  CourseMembershipService courseMembershipService;
  @Autowired
  AdminshipService adminshipService;
  @Autowired
  UserService userService;
  @Autowired
  SessionService sessionService;
  @Autowired
  SessionDataService sessionDataService;
  @Autowired
  SessionRequestService sessionRequestService;
  @Autowired
  SessionRequestResponseService sessionRequestResponseService;
  @Autowired
  AdminshipRequestService adminshipRequestService;
  @Autowired
  AdminshipRequestResponseService adminshipRequestResponseService;
  @Autowired
  CommittmentService committmentService;
  @Autowired
  CommittmentResponseService committmentResponseService;
  @Autowired
  PasswordService passwordService;
  @Autowired
  PasswordResetService passwordResetService;

  Logger logger = LoggerFactory.getLogger(InnexgoService.class);

  /**
   * Fills in jackson objects (User) for ApiKey
   *
   * @param apiKey - ApiKey object
   * @return apiKey with filled jackson objects
   */
  ApiKey fillApiKey(ApiKey apiKey) {
    apiKey.creator = fillUser(userService.getByUserId(apiKey.creatorUserId));
    return apiKey;
  }

  /**
   * Fills in jackson objects for User
   *
   * @param user - User object
   * @return User object with filled jackson objects
   */
  User fillUser(User user) {
    return user;
  }

  /**
   * Fills in jackson objects for Subscription
   *
   * @param subscription - Subscription object
   * @return Subscription object with filled jackson objects
   */
  Subscription fillSubscription(Subscription subscription) {
    subscription.creator = fillUser(userService.getByUserId(subscription.creatorUserId));
    return subscription;
  }

  /**
   * Fills in jackson objects for School
   *
   * @param school - School object
   * @return School object with filled jackson objects
   */
  School fillSchool(School school) {
    school.creator = fillUser(userService.getByUserId(school.creatorUserId));
    return school;
  }

  /**
   * Fills in jackson objects for SchoolData
   *
   * @param schoolData - SchoolData object
   * @return SchoolData object with filled jackson objects
   */
  SchoolData fillSchoolData(SchoolData schoolData) {
    schoolData.creator = fillUser(userService.getByUserId(schoolData.creatorUserId));
    schoolData.school = fillSchool(schoolService.getBySchoolId(schoolData.schoolId));
    return schoolData;
  }

  /**
   * Fills in jackson objects for PasswordReset
   *
   * @param passwordReset - PasswordReset object
   * @return PasswordReset object with filled jackson objects
   */
  PasswordReset fillPasswordReset(PasswordReset passwordReset) {
    return passwordReset;
  }

  /**
   * Fills in jackson objects for Password
   *
   * @param password - Password object
   * @return Password object with filled jackson objects
   */
  Password fillPassword(Password password) {
    password.creator = fillUser(userService.getByUserId(password.creatorUserId));
    password.user = fillUser(userService.getByUserId(password.userId));
    password.passwordReset = fillPasswordReset(
        passwordResetService.getByPasswordResetKeyHash(password.passwordResetKeyHash));
    return password;
  }

  /**
   * Fills in jackson objects for VerificationChallenge
   *
   * @param emailVerificationChallenge - VerificationChallenge object
   * @return VerificationChallenge object with filled jackson objects
   */
  VerificationChallenge fillVerificationChallenge(VerificationChallenge verificationChallenge) {
    return verificationChallenge;
  }

  /**
   * Fills in jackson objects for Location
   *
   * @param location - Location object
   * @return Location object with filled jackson objects
   */
  Location fillLocation(Location location) {
    location.creator = fillUser(userService.getByUserId(location.creatorUserId));
    location.school = fillSchool(schoolService.getBySchoolId(location.schoolId));
    return location;
  }

  /**
   * Fills in jackson objects for Course
   *
   * @param course - Course object
   * @return Course object with filled jackson objects
   */
  Course fillCourse(Course course) {
    course.creator = fillUser(userService.getByUserId(course.creatorUserId));
    course.school = fillSchool(schoolService.getBySchoolId(course.schoolId));
    return course;
  }

  /**
   * Fills in jackson objects for Course
   *
   * @param course - Course object
   * @return Course object with filled jackson objects
   */
  CourseData fillCourseData(CourseData courseData) {
    courseData.creator = fillUser(userService.getByUserId(courseData.creatorUserId));
    courseData.course = fillCourse(courseService.getByCourseId(courseData.courseId));
    return courseData;
  }

  /**
   * Fills in jackson objects for Course
   *
   * @param course - Course object
   * @return Course object with filled jackson objects
   */
  CourseKey fillCourseKey(CourseKey courseKey) {
    courseKey.creator = fillUser(userService.getByUserId(courseKey.creatorUserId));
    courseKey.course = fillCourse(courseService.getByCourseId(courseKey.courseId));
    if (courseKey.courseKeyKind == CourseKeyKind.CANCEL) {
      courseKey.courseMembershipKind = null;
      courseKey.duration = null;
      courseKey.maxUses = null;
    }
    return courseKey;
  }

  /**
   * Fills in jackson objects for CourseMembership
   *
   * @param courseMembership - CourseMembership object
   * @return CourseMembership object with filled jackson objects
   */
  CourseMembership fillCourseMembership(CourseMembership courseMembership) {
    courseMembership.creator = fillUser(userService.getByUserId(courseMembership.creatorUserId));
    courseMembership.user = fillUser(userService.getByUserId(courseMembership.userId));
    courseMembership.course = fillCourse(courseService.getByCourseId(courseMembership.courseId));
    if (courseMembership.courseMembershipSourceKind == CourseMembershipSourceKind.KEY) {
      courseMembership.courseKey = fillCourseKey(courseKeyService.getByCourseKeyId(courseMembership.courseKeyId));
    } else {
      courseMembership.courseKey = null;
    }
    return courseMembership;
  }

  /**
   * Fills in jackson objects for AdminshipRequest
   *
   * @param adminshipRequest - AdminshipRequest object
   * @return AdminshipRequest object with recursively filled jackson objects
   */
  AdminshipRequest fillAdminshipRequest(AdminshipRequest adminshipRequest) {
    adminshipRequest.creator = fillUser(userService.getByUserId(adminshipRequest.creatorUserId));
    adminshipRequest.school = fillSchool(schoolService.getBySchoolId(adminshipRequest.schoolId));
    return adminshipRequest;
  }

  /**
   * Fills in jackson objects for AdminshipRequestResponse
   *
   * @param adminshipRequestResponse - AdminshipRequestResponse object
   * @return AdminshipRequestResponse object with recursively filled jackson
   *         objects
   */
  AdminshipRequestResponse fillAdminshipRequestResponse(AdminshipRequestResponse adminshipRequestResponse) {
    adminshipRequestResponse.creator = fillUser(userService.getByUserId(adminshipRequestResponse.creatorUserId));
    adminshipRequestResponse.adminshipRequest = fillAdminshipRequest(
        adminshipRequestService.getByAdminshipRequestId(adminshipRequestResponse.adminshipRequestId));

    return adminshipRequestResponse;
  }

  /**
   * Fills in jackson objects for Adminship
   *
   * @param adminship - Adminship object
   * @return Adminship object with filled jackson objects
   */
  Adminship fillAdminship(Adminship adminship) {
    adminship.creator = fillUser(userService.getByUserId(adminship.creatorUserId));
    adminship.user = fillUser(userService.getByUserId(adminship.userId));
    adminship.school = fillSchool(schoolService.getBySchoolId(adminship.schoolId));
    if (adminship.adminshipSourceKind == AdminshipSourceKind.REQUEST) {
      adminship.adminshipRequestResponse = fillAdminshipRequestResponse(
          adminshipRequestResponseService.getByAdminshipRequestId(adminship.adminshipRequestResponseId));
    } else {
      adminship.adminshipRequestResponse = null;
    }
    return adminship;
  }

  /**
   * Fills in jackson objects for SessionRequest
   *
   * @param sessionRequest - SessionRequest object
   * @return SessionRequest object with recursively filled jackson objects
   */
  SessionRequest fillSessionRequest(SessionRequest sessionRequest) {
    sessionRequest.creator = fillUser(userService.getByUserId(sessionRequest.creatorUserId));
    sessionRequest.attendee = fillUser(userService.getByUserId(sessionRequest.attendeeUserId));
    sessionRequest.course = fillCourse(courseService.getByCourseId(sessionRequest.courseId));
    return sessionRequest;
  }

  /**
   * Fills in jackson objects for SessionRequestResponse
   *
   * @param sessionRequestResponse - SessionRequestResponse object
   * @return SessionRequestResponse object with recursively filled jackson objects
   */
  SessionRequestResponse fillSessionRequestResponse(SessionRequestResponse sessionRequestResponse) {
    sessionRequestResponse.creator = fillUser(userService.getByUserId(sessionRequestResponse.creatorUserId));
    sessionRequestResponse.sessionRequest = fillSessionRequest(
        sessionRequestService.getBySessionRequestId(sessionRequestResponse.sessionRequestId));

    // depends on whether it was acceppted or not
    if (sessionRequestResponse.accepted) {
      sessionRequestResponse.committment = fillCommittment(
          committmentService.getByCommittmentId(sessionRequestResponse.committmentId));
    } else {
      sessionRequestResponse.committment = null;
    }
    return sessionRequestResponse;
  }

  /**
   * Fills in jackson objects for Session
   *
   * @param session - Session object
   * @return Session object with recursively filled jackson objects
   */
  Session fillSession(Session session) {
    session.creator = fillUser(userService.getByUserId(session.creatorUserId));
    session.course = fillCourse(courseService.getByCourseId(session.courseId));
    return session;
  }

  /**
   * Fills in jackson objects for SessionData
   *
   * @param sessionData - SessionData object
   * @return SessionData object with recursively filled jackson objects
   */
  SessionData fillSessionData(SessionData sessionData) {
    sessionData.creator = fillUser(userService.getByUserId(sessionData.creatorUserId));
    sessionData.session = fillSession(sessionService.getBySessionId(sessionData.sessionId));
    return sessionData;
  }

  /**
   * Fills in jackson objects for Committment
   *
   * @param committment - Committment object
   * @return Committment object with recursively filled jackson objects
   */
  Committment fillCommittment(Committment committment) {
    committment.creator = fillUser(userService.getByUserId(committment.creatorUserId));
    committment.attendee = fillUser(userService.getByUserId(committment.attendeeUserId));
    committment.session = fillSession(sessionService.getBySessionId(committment.sessionId));
    return committment;
  }

  /**
   * Fills in jackson objects for CommittmentResponse
   *
   * @param committmentResponse - CommittmentResponse object
   * @return CommittmentResponse object with recursively filled jackson objects
   */
  CommittmentResponse fillCommittmentResponse(CommittmentResponse committmentResponse) {
    committmentResponse.creator = fillUser(userService.getByUserId(committmentResponse.creatorUserId));
    committmentResponse.committment = fillCommittment(
        committmentService.getByCommittmentId(committmentResponse.committmentId));

    return committmentResponse;
  }

  /**
   * Returns an apiKey if valid
   *
   * @param key - apikey code of the User
   * @return ApiKey or null if invalid
   */
  ApiKey getApiKeyIfValid(String key) {
    ApiKey apiKey = apiKeyService.getByApiKeyHash(Utils.hashGeneratedKey(key));
    if (apiKey != null //
        && apiKey.creationTime + apiKey.duration > System.currentTimeMillis() //
        && apiKey.apiKeyKind == ApiKeyKind.VALID) {
      return apiKey;
    }
    return null;
  }

  /**
   * Returns true if the passwordPhrase matches the most recent password
   * 
   * @param userId         - valid user id
   * @param passwordPhrase - passwordPhrase to test with user
   * @return returns true if user's most recent password exists, isn't cancelled,
   *         and matches the passwordPhrase
   */
  boolean isValidPassword(long userId, String passwordPhrase) {
    Password password = passwordService.getByUserId(userId);
    return password != null && password.passwordKind != PasswordKind.CANCEL
        && Utils.matchesPassword(passwordPhrase, password.passwordHash);
  }

}
