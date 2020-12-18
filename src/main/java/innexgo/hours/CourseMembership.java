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

public class CourseMembership {
  public long courseMembershipId;
  public long creationTime;
  public long creatorUserId;
  public long userId;
  public long courseId;

  User creator;
  User user;
  User course;
}

enum CourseMembershipKind {
  STUDENT(0), INSTRUCTOR(1);

  final int value;

  private CourseMembershipKind(int value) {
    this.value = value;
  }

  public static CourseMembershipKind from(int i) {
    for (CourseMembershipKind courseMembershipKind : CourseMembershipKind.values()) {
      if (courseMembershipKind.value == i) {
        return courseMembershipKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (CourseMembershipKind courseMembershipKind : CourseMembershipKind.values()) {
      if (courseMembershipKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
