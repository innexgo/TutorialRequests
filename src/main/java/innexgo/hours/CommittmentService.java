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
public class CommittmentService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Committment getByCommittmentId(long committmentId) {
    String sql =
        "SELECT * FROM committment WHERE committment_id=?";
    RowMapper<Committment> rowMapper = new CommittmentRowMapper();
    Committment committment = jdbcTemplate.queryForObject(sql, rowMapper, committmentId);
    return committment;
  }

  public void add(Committment committment) {
    // Add committment
    String sql = "INSERT INTO committment values (?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        committment.committmentId,
        committment.creatorId,
        committment.creationTime,
        committment.attendeeId,
        committment.sessionId,
        committment.cancellable);

  }

  public boolean existsByCommittmentId(long committmentId) {
    String sql = "SELECT count(*) FROM committment WHERE committment_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, committmentId);
    return count != 0;
  }

  public List<Committment> query(
     Long committmentId,
     Long creatorId,
     Long creationTime,
     Long minCreationTime,
     Long maxCreationTime,
     Long attendeeId,
     Long sessionId,
     Boolean cancellable,
     Long hostId,
     Long startTime,
     Long minStartTime,
     Long maxStartTime,
     Long duration,
     Long minDuration,
     Long maxDuration,
     Boolean responded,
     long offset,
     long count)
 {
    boolean nojoin =
       startTime == null && minStartTime == null && maxStartTime == null &&
       duration == null && minDuration == null && maxDuration == null &&
       hostId == null;

    String sql=
      "SELECT c.* FROM committment c"
        + (nojoin ? "" : " LEFT JOIN session s ON s.session_id = c.session_id")
        + (responded == null ? "" : " LEFT JOIN committment_response cr ON cr.committment_id= c.committment_id")
        + " WHERE 1=1 "
        + (committmentId   == null ? "" : " AND c.committment_id = " + committmentId)
        + (creatorId       == null ? "" : " AND c.creator_id = " + creatorId)
        + (creationTime    == null ? "" : " AND c.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND c.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND c.creation_time < " + maxCreationTime)
        + (attendeeId      == null ? "" : " AND c.attendee_id = " + attendeeId)
        + (sessionId       == null ? "" : " AND c.session_id = " + sessionId)
        + (cancellable     == null ? "" : " AND c.cancellable = " + cancellable)
        + (startTime       == null ? "" : " AND s.start_time = " + startTime)
        + (minStartTime    == null ? "" : " AND s.start_time > " + minStartTime)
        + (maxStartTime    == null ? "" : " AND s.start_time < " + maxStartTime)
        + (duration        == null ? "" : " AND s.duration = " + duration)
        + (minDuration     == null ? "" : " AND s.duration > " + minDuration)
        + (maxDuration     == null ? "" : " AND s.duration < " + maxDuration)
        + (hostId          == null ? "" : " AND s.host_id = " + hostId)
        + (responded       == null ? "" : " AND cr.committment_id IS" + (responded ? " NOT NULL" : " NULL"))
        + (" ORDER BY at.committment_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<Committment> rowMapper = new CommittmentRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
