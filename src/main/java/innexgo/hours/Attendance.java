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

public class Attendance {
  long apptId;
  public long creationTime;
  public AttendanceKind kind;

  // for jackson
  public Appt appt;
}


enum AttendanceKind {
  PRESENT(0), TARDY(1), ABSENT(2);

  final int value;

  private AttendanceKind(int value) {
    this.value = value;
  }

  public static AttendanceKind from(int i) {
    for (AttendanceKind attendanceKind : AttendanceKind.values()) {
      if (attendanceKind.value == i) {
        return attendanceKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (AttendanceKind attendanceKind : AttendanceKind.values()) {
      if (attendanceKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}

