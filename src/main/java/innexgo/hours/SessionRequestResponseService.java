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
public class SessionRequestResponseService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public SessionRequestResponse getBySessionRequestId(long sessionRequestId) {
    String sql = "SELECT * FROM session_request_response WHERE session_request_id=?";
    RowMapper<SessionRequestResponse> rowMapper = new SessionRequestResponseRowMapper();
    SessionRequestResponse sessionRequestResponse = jdbcTemplate.queryForObject(sql, rowMapper, sessionRequestId);
    return sessionRequestResponse;
  }

  public void add(SessionRequestResponse sessionRequestResponse) {
    String sql =
        "INSERT INTO session_request_response values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        sessionRequestResponse.sessionRequestId,
        sessionRequestResponse.creatorId,
        sessionRequestResponse.creationTime,
        sessionRequestResponse.message,
        sessionRequestResponse.accepted,
        sessionRequestResponse.committmentId);
  }

  public boolean existsBySessionRequestResponseRequestId(long sessionRequestId) {
    String sql = "SELECT count(*) FROM session_request_response WHERE session_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionRequestId);
    return count != 0;
  }

  // Restrict appts by
  public List<SessionRequestResponse> query(
      Long sessionRequestId,
      Long creatorId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      String message,
      Boolean accepted,
      Long committmentId,
      long offset,
      long count) {

    if(committmentId != null) {
        accepted = true;
    }

    String sql =
        "SELECT sesreqre.* FROM session_request_response sesreqre"
            + " WHERE 1=1 "
            + (sessionRequestId == null ? "" : " AND sesreqre.session_request_id = " + sessionRequestId)
            + (creatorId == null ? "" : " AND sesreqre.creator_id = " + creatorId)
            + (creationTime == null ? "" : " AND sesreqre.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND sesreqre.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND sesreqre.creation_time < " + maxCreationTime)
            + (message == null ? "" : " AND sesreqre.message = " + Utils.escape(message))
            + (accepted == null ? "" : " AND sesreqre.accepted = " + (accepted ? 1 : 0))
            + (committmentId == null ? "" : " AND sesreqre.accepted_committment_id= " + committmentId)
            + (" ORDER BY sesreqre.session_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<SessionRequestResponse> rowMapper = new SessionRequestResponseRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}

