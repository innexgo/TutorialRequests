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

public class Adminship {
  public long adminshipId;
  public long creationTime;
  long creatorUserId;
  long userId;
  long schoolId;
  public AdminshipKind adminshipKind;
  long subscriptionId; // only valid if adminshipKind is ADMIN
  public AdminshipSourceKind adminshipSourceKind;
  long adminshipRequestResponseId; // only valid if adminshipKind is ADMIN

  public User creator;
  public User user;
  public School school;
  public Subscription subscription;
  public AdminshipRequestResponse adminshipRequestResponse;
}

enum AdminshipKind {
  ADMIN(0), CANCEL(1);

  final int value;

  private AdminshipKind(int value) {
    this.value = value;
  }

  public static AdminshipKind from(int i) {
    for (AdminshipKind adminshipKind : AdminshipKind.values()) {
      if (adminshipKind.value == i) {
        return adminshipKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (AdminshipKind adminshipKind : AdminshipKind.values()) {
      if (adminshipKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}

enum AdminshipSourceKind {
  REQUEST(0), SET(1);

  final int value;

  private AdminshipSourceKind(int value) {
    this.value = value;
  }

  public static AdminshipSourceKind from(int i) {
    for (AdminshipSourceKind adminshipSourceKind : AdminshipSourceKind.values()) {
      if (adminshipSourceKind.value == i) {
        return adminshipSourceKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (AdminshipSourceKind adminshipSourceKind : AdminshipSourceKind.values()) {
      if (adminshipSourceKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}

