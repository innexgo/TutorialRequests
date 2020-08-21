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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ApiKeyRowMapper implements RowMapper<ApiKey> {
  @Override
  public ApiKey mapRow(ResultSet row, int rowNum) throws SQLException {
    ApiKey apiKey = new ApiKey();
    apiKey.id = row.getLong("id");
    apiKey.userId = row.getLong("user_id");
    apiKey.creationTime = row.getLong("creation_time");
    apiKey.duration= row.getLong("duration");

    apiKey.canLogIn = row.getBoolean("can_log_in");
    apiKey.canChangePassword = row.getBoolean("can_change_password");
    apiKey.canReadUser = row.getBoolean("can_read_user");
    apiKey.canWriteUser = row.getBoolean("can_write_user");
    apiKey.canReadApptRequest = row.getBoolean("can_read_appt_request");
    apiKey.canWriteApptRequest = row.getBoolean("can_write_appt_request");
    apiKey.canReadAppt = row.getBoolean("can_read_appt");
    apiKey.canWriteAppt = row.getBoolean("can_write_appt");
    apiKey.canReadAttendance = row.getBoolean("can_read_attendance");
    apiKey.canWriteAttendance = row.getBoolean("can_write_attendance");

    apiKey.keyHash = row.getString("key_hash");
    return apiKey;
  }
}
