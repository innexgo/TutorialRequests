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

  @Autowired private JdbcTemplate jdbcTemplate;

  public ApptRequest getById(long id) {
    String sql =
        "SELECT id, creator_id, target_id, message, creation_time, suggested_time WHERE id=?";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    ApptRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apptRequest;
  }

  public List<ApptRequest> getAll() {
    String sql =
        "SELECT id, creator_id, target_id, message, creation_time, suggested_time FROM apptRequest";
    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  private void syncId(ApptRequest apptRequest) {
    String sql =
        "SELECT id FROM appt_request WHERE creator_id=? AND target_id=? AND creation_time=? AND suggested_time=?";
    apptRequest.id =
        jdbcTemplate.queryForObject(
            sql,
            Long.class,
            apptRequest.creatorId,
            apptRequest.targetId,
            apptRequest.creationTime,
            apptRequest.suggestedTime);
  }

  public void add(ApptRequest apptRequest) {
    // Add apptRequest
    String sql =
        "INSERT INTO apptRequest(id, creator_id, target_id, message, creation_time, suggested_time ) values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        apptRequest.id,
        apptRequest.creatorId,
        apptRequest.targetId,
        apptRequest.message,
        apptRequest.creationTime,
        apptRequest.suggestedTime );
    syncId(apptRequest);
  }

  public void update(ApptRequest apptRequest) {
    String sql =
        "UPDATE apptRequest SET id=?, creator_id=?, target_id=?, message=?, creation_time=?, suggested_time=? WHERE id=?";
    jdbcTemplate.update(
        sql,
        apptRequest.id,
        apptRequest.creatorId,
        apptRequest.targetId,
        apptRequest.message,
        apptRequest.creationTime,
        apptRequest.suggestedTime);
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
        "SELECT ar.id, ar.creator_id, ar.target_id, ar.message, ar.creation_time, ar.suggested_time, ar.request_duration, ar.reviewed, ar.approved, ar.response, ar.attendance_status FROM appt_request ar"
            + " WHERE 1=1 "
            + (id == null ? "" : " AND ar.id = " + id)
            + (creatorId == null ? "" : " AND ar.creator_id = " + creatorId)
            + (targetId == null ? "" : " AND ar.target_id = " + targetId)
            + (message == null ? "" : " AND ar.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND ar.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND ar.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND ar.creation_time < " + maxCreationTime)
            + (suggestedTime == null ? "" : " AND ar.suggested_time = " + suggestedTime)
            + (minSuggestedTime == null ? "" : " AND ar.suggested_time > " + minSuggestedTime)
            + (maxSuggestedTime == null ? "" : " AND ar.suggested_time < " + maxSuggestedTime)
            + (" ORDER BY ar.id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<ApptRequest> rowMapper = new ApptRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
