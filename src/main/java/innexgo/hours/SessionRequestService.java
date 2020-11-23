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
public class SessionRequestService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public SessionRequest getBySessionRequestId(long sessionRequestId) {
    String sql =
        "SELECT * FROM session_request WHERE session_request_id=?";
    RowMapper<SessionRequest> rowMapper = new SessionRequestRowMapper();
    SessionRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, sessionRequestId);
    return apptRequest;
  }

  public long nextId() {
    String sql = "SELECT max(sesreq.session_request_id) FROM session_request sesreq";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(SessionRequest apptRequest) {
    apptRequest.sessionRequestId = nextId();
    // Add apptRequest
    String sql =
        "INSERT INTO session_request values (?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        apptRequest.sessionRequestId,
        apptRequest.creatorId,
        apptRequest.creationTime,
        apptRequest.attendeeId,
        apptRequest.hostId,
        apptRequest.message,
        apptRequest.startTime,
        apptRequest.duration
    );
  }

  public boolean existsBySessionRequestId(long sessionRequestId) {
    String sql = "SELECT count(*) FROM session_request sesreq WHERE sesreq.session_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionRequestId);
    return count != 0;
  }

  // Restrict apptRequests by
  public List<SessionRequest> query(
      Long sessionRequestId,
      Long creatorId,
      Long attendeeId,
      Long hostId,
      String message,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long startTime,
      Long minStartTime,
      Long maxStartTime,
      Long duration,
      Long minDuration,
      Long maxDuration,
      Boolean responded,
      long offset,
      long count) {
    String sql =
        "SELECT sesreq.* FROM session_request sesreq"
            + (responded == null ? "" : " LEFT JOIN session_request_response sesreqre ON sesreqre.session_request_id = sesreq.session_request_id")
            + " WHERE 1=1 "
            + (responded == null ? "" : " AND ap.session_request_id IS" + (responded ? " NOT NULL" : " NULL"))
            + (sessionRequestId== null ? "" : " AND sesreq.session_request_id = " + sessionRequestId)
            + (creatorId == null ? "" : " AND sesreq.creator_id = " + creatorId)
            + (attendeeId == null ? "" : " AND sesreq.attendee_id = " + attendeeId)
            + (hostId == null ? "" : " AND sesreq.host_id = " + hostId)
            + (message == null ? "" : " AND sesreq.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND sesreq.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND sesreq.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND sesreq.creation_time < " + maxCreationTime)
            + (startTime == null ? "" : " AND sesreq.start_time = " + startTime)
            + (minStartTime == null ? "" : " AND sesreq.start_time > " + minStartTime)
            + (maxStartTime == null ? "" : " AND sesreq.start_time < " + maxStartTime)
            + (duration == null ? "" : " AND sesreq.duration = " + duration)
            + (minDuration == null ? "" : " AND sesreq.duration > " + minDuration)
            + (maxDuration == null ? "" : " AND sesreq.duration < " + maxDuration)
            + (" ORDER BY sesreq.session_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<SessionRequest> rowMapper = new SessionRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
