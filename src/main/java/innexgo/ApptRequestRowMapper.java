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

public class ApptRequestRowMapper implements RowMapper<ApptRequest> {

  @Override
  public ApptRequest mapRow(ResultSet row, int rowNum) throws SQLException {
    ApptRequest apptRequest = new ApptRequest();
    apptRequest.id = row.getLong("id");
    apptRequest.studentId = row.getLong("student_id");
    apptRequest.userId = row.getLong("user_id");
    apptRequest.message = row.getString("message");
    apptRequest.creationTime = row.getLong("creation_time");
    apptRequest.requestTime = row.getLong("request_time");
    apptRequest.requestDuration = row.getLong("request_duration");
    apptRequest.reviewed = row.getBoolean("reviewed");
    apptRequest.approved = row.getBoolean("approved");
    apptRequest.response = row.getString("response");
    apptRequest.attendanceStatus = AttendanceStatus.valueOf(row.getString("attendance_status"));
    return apptRequest;
  }
}
