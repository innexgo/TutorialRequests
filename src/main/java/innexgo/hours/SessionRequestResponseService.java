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
import java.util.stream.Stream;
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
        sessionRequestResponse.creatorUserId,
        sessionRequestResponse.creationTime,
        sessionRequestResponse.message,
        sessionRequestResponse.accepted,
        sessionRequestResponse.committmentId);
  }

  public boolean existsBySessionRequestId(long sessionRequestId) {
    String sql = "SELECT count(*) FROM session_request_response WHERE session_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionRequestId);
    return count != 0;
  }

  // Restrict appts by
  public Stream<SessionRequestResponse> query(
      Long sessionRequestId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long creatorUserId,
      String message,
      Boolean accepted,
      Long committmentId,
      Long attendeeUserId,
      Long courseId,
      Long startTime, //
      Long minStartTime, //
      Long maxStartTime, //
      Long duration, //
      Long minDuration, //
      Long maxDuration, //
      long offset,
      long count) {

    if(committmentId != null) {
        accepted = true;
    }

    boolean nojoin = attendeeUserId == null && courseId == null;

    String sql =
        "SELECT srr.* FROM session_request_response srr"
            + (nojoin ? "" : " JOIN session_request sr ON sr.session_request_id = srr.session_request_id")
            + " WHERE 1=1 "
            + (sessionRequestId == null ? "" : " AND srr.session_request_id = " + sessionRequestId)
            + (creatorUserId        == null ? "" : " AND srr.creator_user_id = " + creatorUserId)
            + (creationTime     == null ? "" : " AND srr.creation_time = " + creationTime)
            + (minCreationTime  == null ? "" : " AND srr.creation_time > " + minCreationTime)
            + (maxCreationTime  == null ? "" : " AND srr.creation_time < " + maxCreationTime)
            + (message          == null ? "" : " AND srr.message = " + Utils.escape(message))
            + (accepted         == null ? "" : " AND srr.accepted = " + accepted)
            + (committmentId    == null ? "" : " AND srr.accepted_committment_id= " + committmentId)
            + (attendeeUserId       == null ? "" : " AND sr.attendeee_user_id = " + attendeeUserId)
            + (courseId           == null ? "" : " AND sr.course_id = " + courseId)
            + (startTime        == null ? "" : " AND sr.start_time = " + startTime)
            + (minStartTime     == null ? "" : " AND sr.start_time > " + minStartTime)
            + (maxStartTime     == null ? "" : " AND sr.start_time < " + maxStartTime)
            + (duration         == null ? "" : " AND sr.duration = " + duration)
            + (minDuration      == null ? "" : " AND sr.duration > " + minDuration)
            + (maxDuration      == null ? "" : " AND sr.duration < " + maxDuration)
            + (" ORDER BY srr.session_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<SessionRequestResponse> rowMapper = new SessionRequestResponseRowMapper();
    return this.jdbcTemplate.queryForStream(sql, rowMapper);
  }
}

