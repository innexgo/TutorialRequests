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

public class Password {
    public long passwordId;
    public long creationTime;
    long creatorUserId;
    long userId;
    public PasswordKind passwordKind;
    String passwordHash;
    String passwordResetKeyHash;

    // for jackson
    public User creator;
    public User user;
    public PasswordReset passwordReset;
}

enum PasswordKind {
  CHANGE(0), RESET(1), CANCEL(2);

  final int value;

  private PasswordKind(int value) {
    this.value = value;
  }

  public static PasswordKind from(int i) {
    for (PasswordKind passwordKind : PasswordKind.values()) {
      if (passwordKind.value == i) {
        return passwordKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (PasswordKind passwordKind : PasswordKind.values()) {
      if (passwordKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
