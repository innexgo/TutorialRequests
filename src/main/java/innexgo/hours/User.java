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

public class User {
  public long id;
  public long creationTime;
  public UserKind kind;
  public String name;
  public String email;
  // not public so it doesn't get serialized to jackson
  long passwordSetTime;
  String passwordHash;
}

enum UserKind {
  STUDENT(0), USER(1), ADMIN(2);

  final int value;

  private UserKind(int value) {
    this.value = value;
  }

  public static UserKind from(int i) {
    for (UserKind userKind : UserKind.values()) {
      if (userKind.value == i) {
        return userKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (UserKind userKind : UserKind.values()) {
      if (userKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
