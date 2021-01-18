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

public class CourseKey {
  public long courseKeyId;
  public long creationTime;
  long creatorUserId;
  long courseId;
  public String key;
  public CourseKeyKind courseKeyKind;
  public CourseMembershipKind courseMembershipKind; // valid if courseKeyKind != CANCEL
  public Long duration; // valid if courseKeyKind != CANCEL
  public Long maxUses; // valid if courseKeyKind != CANCEL

  // Jackson
  User creator;
  Course course;
}

enum CourseKeyKind {
  VALID(0), CANCEL(1);

  final int value;

  private CourseKeyKind(int value) {
    this.value = value;
  }

  public static CourseKeyKind from(int i) {
    for (CourseKeyKind courseKeyKind : CourseKeyKind.values()) {
      if (courseKeyKind.value == i) {
        return courseKeyKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (CourseKeyKind courseKeyKind : CourseKeyKind.values()) {
      if (courseKeyKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
