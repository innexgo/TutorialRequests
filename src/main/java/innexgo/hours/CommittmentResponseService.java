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
public class CommittmentResponseService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public CommittmentResponse getByCommittmentId(long committmentId) {
    String sql =
        "SELECT * FROM committment_response WHERE committment_id=?";
    RowMapper<CommittmentResponse> rowMapper = new CommittmentResponseRowMapper();
    CommittmentResponse committmentResponse = jdbcTemplate.queryForObject(sql, rowMapper, committmentId);
    return committmentResponse;
  }

  public void add(CommittmentResponse committmentResponse) {
    // Add committmentResponse
    String sql =
        "INSERT INTO committment_response values (?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        committmentResponse.committmentId,
        committmentResponse.creatorId,
        committmentResponse.creationTime,
        committmentResponse.kind.value);
  }

  public boolean existsByCommittmentId(long committmentId) {
    String sql = "SELECT count(*) FROM committment_response WHERE committment_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, committmentId);
    return count != 0;
  }

  public List<CommittmentResponse> query(
     Long committmentId,
     Long creatorId,
     Long creationTime,
     Long minCreationTime,
     Long maxCreationTime,
     CommittmentResponseKind committmentResponseKind,
     Long attendeeId,
     Long hostId,
     Long startTime,
     Long minStartTime,
     Long maxStartTime,
     Long duration,
     Long minDuration,
     Long maxDuration,
     Long sessionId,
     long offset,
     long count)
 {
    // avoid joins to save performance
    boolean joinc = attendeeId != null || sessionId != null;
    boolean joins = joinc ||
      startTime != null || minStartTime != null || maxStartTime != null ||
      duration != null || minDuration != null || maxDuration != null ||
      hostId != null;

    String sql=
      "SELECT cr.* FROM committment_response cr"
        + (!joinc ? "" : " LEFT JOIN committment c ON c.committment_id = cr.committment_id")
        + (!joins ? "" : " LEFT JOIN session s ON s.session_id = c.session_id")
        + " WHERE 1=1 "
        + (committmentId          == null ? "" : " AND cr.committment_id = " + committmentId)
        + (creatorId              == null ? "" : " AND cr.creator_id = " + creatorId)
        + (creationTime           == null ? "" : " AND cr.creation_time = " + creationTime)
        + (minCreationTime        == null ? "" : " AND cr.creation_time > " + minCreationTime)
        + (maxCreationTime        == null ? "" : " AND cr.creation_time < " + maxCreationTime)
        + (committmentResponseKind== null ? "" : " AND cr.committment_response_kind = " + committmentResponseKind.value)
        + (attendeeId             == null ? "" : " AND c.attendee_id = " + attendeeId)
        + (sessionId              == null ? "" : " AND c.session_id = " + sessionId)
        + (hostId                 == null ? "" : " AND s.host_id = " + hostId)
        + (startTime              == null ? "" : " AND s.start_time = " + startTime)
        + (minStartTime           == null ? "" : " AND s.start_time > " + minStartTime)
        + (maxStartTime           == null ? "" : " AND s.start_time < " + maxStartTime)
        + (duration               == null ? "" : " AND s.duration = " + duration)
        + (minDuration            == null ? "" : " AND s.duration > " + minDuration)
        + (maxDuration            == null ? "" : " AND s.duration < " + maxDuration)
        + (" ORDER BY cr.committment_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<CommittmentResponse> rowMapper = new CommittmentResponseRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
