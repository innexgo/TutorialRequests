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
    apiKey.creator = fillUser(userService.getById(apiKey.creatorId));
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
   * Fills in jackson objects for SessionRequest
   *
   * @param sessionRequest - SessionRequest object
   * @return SessionRequest object with recursively filled jackson objects
   */
  SessionRequest fillSessionRequest(SessionRequest sessionRequest) {
    sessionRequest.creator = fillUser(userService.getById(sessionRequest.creatorId));
    sessionRequest.attendee = fillUser(userService.getById(sessionRequest.attendeeId));
    sessionRequest.host = fillUser(userService.getById(sessionRequest.hostId));
    return sessionRequest;
  }

  /**
   * Fills in jackson objects for SessionRequestResponse
   *
   * @param sessionRequestResponse - SessionRequestResponse object
   * @return SessionRequestResponse object with recursively filled jackson objects
   */
  SessionRequestResponse fillSessionRequestResponse(SessionRequestResponse sessionRequestResponse) {
    sessionRequestResponse.creator = fillUser(userService.getById(sessionRequestResponse.creatorId));
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
    session.creator = fillUser(userService.getById(session.creatorId));
    session.host = fillUser(userService.getById(session.hostId));
    return session;
  }

  /**
   * Fills in jackson objects for Committment
   *
   * @param committment - Committment object
   * @return Committment object with recursively filled jackson objects
   */
  Committment fillCommittment(Committment committment) {
    committment.creator = fillUser(userService.getById(committment.creatorId));
    committment.attendee = fillUser(userService.getById(committment.attendeeId));
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
    committmentResponse.creator = fillUser(userService.getById(committmentResponse.creatorId));
    committmentResponse.committment = fillCommittment(
        committmentService.getByCommittmentId(committmentResponse.committmentId));

    return committmentResponse;
  }

  /**
   * Fills in jackson objects for ForgotPassword
   *
   * @param forgotPassword - ForgotPassword object
   * @return ForgotPassword object with recursively filled jackson objects
   */
  ForgotPassword fillForgotPassword(ForgotPassword forgotPassword) {
    return forgotPassword;
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
        if (userService.existsById(apiKey.creatorId)) {
          return userService.getById(apiKey.creatorId);
        }
      }
    }
    return null;
  }

  boolean isAdministrator(String key) {
    User u = getUserIfValid(key);
    return u != null && u.kind == UserKind.ADMIN;
  }

  boolean isUser(String key) {
    User u = getUserIfValid(key);
    return u != null && (u.kind == UserKind.USER || u.kind == UserKind.ADMIN);
  }

  boolean isStudent(String key) {
    User u = getUserIfValid(key);
    return u != null && u.kind == UserKind.STUDENT;
  }
}
