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
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ApptRequestService {

  @Autowired private JdbcTemplate jdbcTemplate;

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
    jdbcTemplate.update(sql, apptRequest.id, apptRequest.studentId,
        apptRequest.userId, apptRequest.message, apptRequest.creationTime, apptRequest.requestTime, 
        apptRequest.reviewed, apptRequest.approved, apptRequest.response, apptRequest.present);
  }

  public void update(ApptRequest apptRequest) {
    String sql = "UPDATE apptRequest SET id=?, student_id=?, user_id=?, message=?, creation_time=?, request_time=?, reviewed=?. approved=?, response=?, present=? WHERE id=?";
    jdbcTemplate.update(sql,
        apptRequest.id,
        apptRequest.studentId,
        apptRequest.userId,
        apptRequest.message, 
        apptRequest.creationTime, 
        apptRequest.requestTime,
        apptRequest.reviewed, 
        apptRequest.approved, 
        apptRequest.response, 
        apptRequest.present);
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
      Long id, // Exact match to id
      String name, // Exact match to name
      String partialName, // Partial match to name
      long offset,
      long count
      ) {
    String sql =
        "SELECT st.id, st.name FROM apptRequest st"
            + " WHERE 1=1 "
            + (id == null ? "" : " AND st.id = " + id)
            + (name == null ? "" : " AND st.name = " + Utils.escape(name))
            + (partialName == null ? "" : " AND st.name LIKE " + Utils.escape("%"+partialName+"%"))
            + (" ORDER BY st.id")
            + (" LIMIT " + offset + ", "  + count)
            + ";";

    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
