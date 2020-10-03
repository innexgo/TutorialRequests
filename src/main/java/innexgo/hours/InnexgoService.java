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
  ApptService apptService;
  @Autowired
  ApptRequestService apptRequestService;

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
   * Fills in jackson objects for ApptRequest
   *
   * @param apptRequest - ApptRequest object
   * @return ApptRequest object with recursively filled jackson objects
   */
  ApptRequest fillApptRequest(ApptRequest apptRequest) {
    apptRequest.creator = fillUser(userService.getById(apptRequest.creatorId));
    apptRequest.attendee = fillUser(userService.getById(apptRequest.attendeeId));
    apptRequest.host = fillUser(userService.getById(apptRequest.hostId));
    return apptRequest;
  }

  /**
   * Fills in jackson objects for Appt
   *
   * @param appt - Appt object
   * @return Appt object with recursively filled jackson objects
   */
  Appt fillAppt(Appt appt) {
    appt.apptRequest = fillApptRequest(apptRequestService.getById(appt.apptRequestId));
    return appt;
  }

  /**
   * Fills in jackson objects for Attendance
   *
   * @param attendance - Attendance object
   * @return Attendance object with recursively filled jackson objects
   */
  Attendance fillAttendance(Attendance attendance) {
    attendance.appt = fillAppt(apptService.getById(attendance.apptId));
    return attendance;
  }

  /**
   * Returns an apiKey if valid
   *
   * @param key - apikey code of the User
   * @return ApiKey or null if invalid
   */
  ApiKey getApiKeyIfValid(String key) {
    String hash = Utils.encodeApiKey(key);
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
    String hash = Utils.encodeApiKey(key);
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
