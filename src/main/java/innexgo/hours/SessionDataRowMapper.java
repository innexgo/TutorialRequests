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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class SessionDataRowMapper implements RowMapper<SessionData> {

  @Override
  public SessionData mapRow(ResultSet row, int rowNum) throws SQLException {
    SessionData sessionData = new SessionData();
    sessionData.sessionDataId = row.getLong("session_data_id");
    sessionData.creationTime = row.getLong("creation_time");
    sessionData.creatorUserId = row.getLong("creator_user_id");
    sessionData.sessionId = row.getLong("session_id");
    sessionData.name = row.getString("name");
    sessionData.startTime = row.getLong("start_time");
    sessionData.duration = row.getLong("duration");
    sessionData.hidden= row.getBoolean("hidden");
    sessionData.active= row.getBoolean("active");
    return sessionData;
  }
}
