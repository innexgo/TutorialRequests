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

import java.util.List;
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
    String sql =
        "SELECT apr.appt_request_id, apr.creator_id, apr.target_id, apr.message, apr.creation_time, apr.suggested_time FROM appt_request apr WHERE apr.appt_request_id=?";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    ApptRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apptRequest;
  }

  public long nextId() {
    String sql = "SELECT max(apr.appt_request_id) FROM appt_request apr";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }



  public void add(ApptRequest apptRequest) {
    apptRequest.apptRequestId = nextId();
    // Add apptRequest
    String sql =
        "INSERT INTO appt_request values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        apptRequest.apptRequestId,
        apptRequest.creatorId,
        apptRequest.targetId,
        apptRequest.message,
        apptRequest.creationTime,
        apptRequest.suggestedTime );
  }

  public void update(ApptRequest apptRequest) {
    String sql =
        "UPDATE appt_request apr SET apr.appt_request_id=?, apr.creator_id=?, apr.target_id=?, apr.message=?, apr.creation_time=?, apr.suggested_time=? WHERE apr.appt_request_id=?";
    jdbcTemplate.update(
        sql,
        apptRequest.apptRequestId,
        apptRequest.creatorId,
        apptRequest.targetId,
        apptRequest.message,
        apptRequest.creationTime,
        apptRequest.suggestedTime);
  }

  public ApptRequest deleteById(long id) {
    ApptRequest apptRequest = getById(id);
    String sql = "DELETE FROM appt_request apr WHERE apr.appt_request_id=?";
    jdbcTemplate.update(sql, id);
    return apptRequest;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM appt_request apr WHERE apr.appt_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  // Restrict apptRequests by
  public List<ApptRequest> query(
      Long apptRequestId,
      Long creatorId,
      Long targetId,
      String message,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long suggestedTime,
      Long minSuggestedTime,
      Long maxSuggestedTime,
      long offset,
      long count) {
    String sql =
        "SELECT apr.appt_request_id, apr.creator_id, apr.target_id, apr.message, apr.creation_time, apr.suggested_time FROM appt_request apr"
            + " WHERE 1=1 "
            + (apptRequestId== null ? "" : " AND apr.appt_request_id = " + apptRequestId)
            + (creatorId == null ? "" : " AND apr.creator_id = " + creatorId)
            + (targetId == null ? "" : " AND apr.target_id = " + targetId)
            + (message == null ? "" : " AND apr.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND apr.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND apr.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND apr.creation_time < " + maxCreationTime)
            + (suggestedTime == null ? "" : " AND apr.suggested_time = " + suggestedTime)
            + (minSuggestedTime == null ? "" : " AND apr.suggested_time > " + minSuggestedTime)
            + (maxSuggestedTime == null ? "" : " AND apr.suggested_time < " + maxSuggestedTime)
            + (" ORDER BY apr.appt_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
