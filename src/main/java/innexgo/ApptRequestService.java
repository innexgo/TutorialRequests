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
    String sql = "SELECT id, student_id, user_id, message, creation_time, request_time, reviewed, approved, response, present FROM apptRequest WHERE id=?";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    ApptRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apptRequest;
  }

  public List<ApptRequest> getAll() {
    String sql = "SELECT id, student_id, user_id, message, creation_time, request_time, reviewed, approved, response, present FROM apptRequest";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  public void add(ApptRequest apptRequest) {
    // Add apptRequest
    String sql = "INSERT INTO apptRequest(id, student_id, user_id, message, creation_time, request_time, reviewed, approved, response, present) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, apptRequest.id, apptRequest.studentId, apptRequest.userId, apptRequest.message,
        apptRequest.creationTime, apptRequest.requestTime, apptRequest.reviewed, apptRequest.approved,
        apptRequest.response, apptRequest.present);
  }

  public void update(ApptRequest apptRequest) {
    String sql = "UPDATE apptRequest SET id=?, student_id=?, user_id=?, message=?, creation_time=?, request_time=?, reviewed=?. approved=?, response=?, present=? WHERE id=?";
    jdbcTemplate.update(sql, apptRequest.id, apptRequest.studentId, apptRequest.userId, apptRequest.message,
        apptRequest.creationTime, apptRequest.requestTime, apptRequest.reviewed, apptRequest.approved,
        apptRequest.response, apptRequest.present);
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
      Long requestTime,
      Boolean reviewed,
      Boolean approved,
      String response,
      Boolean present,
      long offset,
      long count)
  {
    String sql = "SELECT ar.id, ar.student_id, ar.user_id, ar.message, ar.creation_time, ar.request_time, ar.reviewed, ar.approved, ar.response, ar.present FROM appt_request ar"
      + " WHERE 1=1 " + (id == null ? "" : " AND ar.id = " + id)
      + (studentId == null ? "" : " AND ar.student_id = " + studentId)
      + (userId == null ? "" : " AND ar.user_id = " + userId)
      + (message == null ? "" : " AND ar.message = " + Utils.escape(message))
      + (creationTime == null ? "" : " AND ar.creation_time = " + creationTime)
      + (requestTime == null ? "" : " AND ar.request_time = " + requestTime)
      + (reviewed == null ? "" : " AND ar.reviewed = " + reviewed)
      + (approved == null ? "" : " AND ar.approved = " + approved)
      + (response == null ? "" : " AND ar.response = " + Utils.escape(message))
      + (present == null ? "" : " AND ar.present = " + present) + (" ORDER BY ar.id")
      + (" LIMIT " + offset + ", " + count) + ";";

    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
