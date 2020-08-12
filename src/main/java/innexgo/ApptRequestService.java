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

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ApptRequestService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public ApptRequest getById(long id) {
    String sql = "SELECT id, student_id, user_id, message, creation_time, request_time, request_duration, reviewed, approved, response, attendance_status FROM appt_request WHERE id=?";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    ApptRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apptRequest;
  }

  public List<ApptRequest> getAll() {
    String sql = "SELECT id, student_id, user_id, message, creation_time, request_time, request_duration, reviewed, approved, response, attendance_status FROM apptRequest";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  private void syncId(ApptRequest apptRequest) {
    String sql = "SELECT id FROM appt_request WHERE student_id=? AND user_id=? AND creation_time=? AND request_time=?";
    apptRequest. id = jdbcTemplate.queryForObject(sql, Long.class, apptRequest.studentId, apptRequest.userId, apptRequest.creationTime, apptRequest.requestTime);
  }

  public void add(ApptRequest apptRequest) {
    // Add apptRequest
    String sql = "INSERT INTO apptRequest(id, student_id, user_id, message, creation_time, request_time, request_duration, reviewed, approved, response, attendance_status) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, apptRequest.id, apptRequest.studentId, apptRequest.userId, apptRequest.message,
        apptRequest.creationTime, apptRequest.requestTime, apptRequest.reviewed, apptRequest.approved,
        apptRequest.response, apptRequest.attendanceStatus.name());
    syncId(apptRequest);
  }

  public void update(ApptRequest apptRequest) {
    String sql = "UPDATE apptRequest SET id=?, student_id=?, user_id=?, message=?, creation_time=?, request_time=?, request_duration=?, reviewed=?. approved=?, response=?, attendance_status=? WHERE id=?";
    jdbcTemplate.update(sql, apptRequest.id, apptRequest.studentId, apptRequest.userId, apptRequest.message,
        apptRequest.creationTime, apptRequest.requestTime, apptRequest.requestDuration, apptRequest.reviewed, apptRequest.approved,
        apptRequest.response, apptRequest.attendanceStatus.name());
  }

  public ApptRequest deleteById(long id) {
    ApptRequest apptRequest = getById(id);
    String sql = "DELETE FROM apptRequest WHERE id=?";
    jdbcTemplate.update(sql, id);
    return apptRequest;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM apptRequest WHERE id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  // Restrict apptRequests by
  public List<ApptRequest> query(
      Long id,
      Long studentId, 
      Long userId,
      String message,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long requestTime,
      Long minRequestTime,
      Long maxRequestTime,
      Long requestDuration,
      Boolean reviewed,
      Boolean approved,
      String response,
      AttendanceStatus attendanceStatus,
      long offset,
      long count)
  {
    String sql = "SELECT ar.id, ar.student_id, ar.user_id, ar.message, ar.creation_time, ar.request_time, ar.request_duration, ar.reviewed, ar.approved, ar.response, ar.attendance_status FROM appt_request ar"
      + " WHERE 1=1 " + (id == null ? "" : " AND ar.id = " + id)
      + (studentId == null ? "" : " AND ar.student_id = " + studentId)
      + (userId == null ? "" : " AND ar.user_id = " + userId)
      + (message == null ? "" : " AND ar.message = " + Utils.escape(message))
      + (creationTime == null ? "" : " AND ar.creation_time = " + creationTime)
      + (minCreationTime == null ? "" : " AND ar.creation_time > " + minCreationTime)
      + (maxCreationTime == null ? "" : " AND ar.creation_time < " + maxCreationTime)
      + (requestTime == null ? "" : " AND ar.request_time = " + requestTime)
      + (minRequestTime == null ? "" : " AND ar.request_time > " + minRequestTime)
      + (maxRequestTime == null ? "" : " AND ar.request_time < " + maxRequestTime)
      + (requestDuration == null ? "" : " AND ar.request_duration = " + requestDuration)
      + (reviewed == null ? "" : " AND ar.reviewed = " + reviewed)
      + (approved == null ? "" : " AND ar.approved = " + approved)
      + (response == null ? "" : " AND ar.response = " + Utils.escape(message))
      + (attendanceStatus == null ? "" : " AND ar.attendance_status = " + attendanceStatus.name())
      + (" ORDER BY ar.id")
      + (" LIMIT " + offset + ", " + count) + ";";

    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
