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

public class ApptRowMapper implements RowMapper<Appt> {

  @Override
  public Appt mapRow(ResultSet row, int rowNum) throws SQLException {
    Appt appt = new Appt();
    appt.id = row.getLong("id");
    appt.hostId = row.getLong("host_id");
    appt.attendeeId = row.getLong("attendee_id");
    appt.message = row.getString("message");
    appt.creationTime = row.getLong("creation_time");
    appt.startTime= row.getLong("start_time");
    appt.duration = row.getLong("duration");
    appt.apptRequestId = row.getLong("appt_request_id");
    return appt;
  }
}
