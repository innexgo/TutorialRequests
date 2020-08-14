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

public class ApptRequest {
  public long id;
  long studentId;
  long userId;
  public String message;
  public long creationTime;
  // only defined if reviewed is true
  public long requestTime;
  // only defined if reviewed is true
  public long requestDuration;
  public boolean reviewed;
  public boolean approved;
  public String response;
  // only defined if current time is after request time
  public AttendanceStatus attendanceStatus;

  // for jackson
  Student student;
  User user;
}

enum AttendanceStatus {
  ABSENT,
  TARDY,
  PRESENT;

  public static boolean contains(String str) {
    for (AttendanceStatus attendanceStatus : AttendanceStatus.values()) {
      if (attendanceStatus.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
