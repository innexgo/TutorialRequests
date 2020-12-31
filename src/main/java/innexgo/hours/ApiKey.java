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

public class ApiKey {

  public long apiKeyId;
  public long creationTime;
  long creatorUserId;

  String apiKeyHash;
  public long duration;
  public ApiKeyKind apiKeyKind;

  // Initialized by jackson during serialization, but not persisted
  public String key;
  public User creator;
}

enum ApiKeyKind {
  VALID(0), CANCEL(1);

  final int value;

  private ApiKeyKind(int value) {
    this.value = value;
  }

  public static ApiKeyKind from(int i) {
    for (ApiKeyKind apiKeyKind : ApiKeyKind.values()) {
      if (apiKeyKind.value == i) {
        return apiKeyKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (ApiKeyKind apiKeyKind : ApiKeyKind.values()) {
      if (apiKeyKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
