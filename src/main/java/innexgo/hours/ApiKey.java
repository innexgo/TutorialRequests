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
  public long id;
  long userId;

  public long creationTime;
  public long duration;

  // not public
  String keyHash;

  public boolean canLogIn;
  public boolean canReadUser;
  public boolean canWriteUser;
  public boolean canChangePassword;
  public boolean canReadApptRequest;
  public boolean canWriteApptRequest;
  public boolean canReadAppt;
  public boolean canWriteAppt;
  public boolean canReadAttendance;
  public boolean canWriteAttendance;

  // Initialized by jackson during serialization, but not persisted
  public String key;
  public User user;
}
