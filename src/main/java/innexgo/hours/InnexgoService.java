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
  SchoolService schoolService;
  @Autowired
  CourseService courseService;
  @Autowired
  CourseMembershipService courseMembershipService;
  @Autowired
  AdminshipService adminshipService;
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
  PasswordResetKeyService passwordResetKeyService;

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
   * Fills in jackson objects for PasswordResetKey
   *
   * @param passwordResetKey - PasswordResetKey object
   * @return PasswordResetKey object with filled jackson objects
   */
  PasswordResetKey fillPasswordResetKey(PasswordResetKey passwordResetKey) {
    return passwordResetKey;
  }

  /**
   * Fills in jackson objects for EmailVerificationChallenge
   *
   * @param emailVerificationChallenge - EmailVerificationChallenge object
   * @return EmailVerificationChallenge object with filled jackson objects
   */
  EmailVerificationChallenge fillEmailVerificationChallenge(EmailVerificationChallenge emailVerificationChallenge) {
    return emailVerificationChallenge;
  }

  /**
   * Fills in jackson objects for Location
   *
   * @param location - Location object
   * @return Location object with filled jackson objects
   */
  Location fillLocation(Location location) {
    location.creator = fillUser(userService.getByUserId(location.creatorUserId));
    location.school = fillSchool(schoolService.getBySchoolId(location.schoolId) );
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
    course.school = fillSchool(schoolService.getBySchoolId(course.schoolId) );
    return course;
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
    return courseMembership;
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
    session.location = fillLocation(locationService.getByLocationId(session.locationId));
    return session;
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
    String hash = Utils.hashGeneratedKey(key);
    if (apiKeyService.existsByKeyHash(hash)) {
      ApiKey apiKey = apiKeyService.getByKeyHash(hash);
      if (apiKey.creationTime + apiKey.duration > System.currentTimeMillis()) {
        return apiKey;
      }
    }
    return null;
  }

  /**
   * Returns a user if valid
   *
   * @param key - apikey code of the User
   * @return User or null if invalid
   */
  User getUserIfValid(String key) {
    String hash = Utils.hashGeneratedKey(key);
    if (apiKeyService.existsByKeyHash(hash)) {
      ApiKey apiKey = apiKeyService.getByKeyHash(hash);
      if (apiKey.creationTime + apiKey.duration > System.currentTimeMillis()) {
        if (userService.existsByUserId(apiKey.creatorUserId)) {
          return userService.getByUserId(apiKey.creatorUserId);
        }
      }
    }
    return null;
  }
}
