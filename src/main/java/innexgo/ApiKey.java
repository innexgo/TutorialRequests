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

package innexgo;

public class ApiKey {
  public long id;
  long userId;

  public long creationTime;
  public long expirationTime;

  // not public
  String keyHash;

  public CapabilityKind readUser;
  public CapabilityKind writeUser;

  public CapabilityKind readApiKey;
  public CapabilityKind writeApiKey;

  public CapabilityKind readApptRequest;
  public CapabilityKind writeApptRequest;

  public CapabilityKind readAppt;
  public CapabilityKind writeAppt;

  // Initialized by jackson during serialization, but not persisted
  public String key;
  public User user;
}

enum CapabilityKind {
  GLOBAL,
  MY,
  NONE;

  public static boolean contains(String str) {
    for (CapabilityKind capabilityKind : CapabilityKind.values()) {
      if (capabilityKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
