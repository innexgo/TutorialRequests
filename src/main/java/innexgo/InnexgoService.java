package innexgo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InnexgoService {

  @Autowired ApiKeyService apiKeyService;
  @Autowired UserService userService;
  @Autowired ApptService apptService;
  @Autowired ApptRequestService apptRequestService;

  Logger logger = LoggerFactory.getLogger(InnexgoService.class);

  /**
   * Fills in jackson objects (User) for ApiKey
   *
   * @param apiKey - ApiKey object
   * @return apiKey with filled jackson objects
   */
  ApiKey fillApiKey(ApiKey apiKey) {
    apiKey.user = fillUser(userService.getById(apiKey.userId));
    return apiKey;
  }

  /**
   * Fills in jackson objects (none at the moment) for User
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
    apptRequest.creator = userService.getById(apptRequest.creatorId);
    apptRequest.target = userService.getById(apptRequest.targetId);
    return apptRequest;
  }

  /**
   * Fills in jackson objects for Appt
   *
   * @param appt - Appt object
   * @return Appt object with recursively filled jackson objects
   */
  Appt fillAppt(Appt appt) {
    appt.host = userService.getById(appt.hostId);
    appt.attendee = userService.getById(appt.attendeeId);
    return appt;
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
        if (userService.existsById(apiKey.userId)) {
          return userService.getById(apiKey.userId);
        }
      }
    }
    return null;
  }
}
