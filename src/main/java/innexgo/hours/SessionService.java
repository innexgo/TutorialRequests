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
public class SessionService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Session getBySessionId(long sessionId) {
    String sql =
        "SELECT * FROM session WHERE session_id=?";
    RowMapper<Session> rowMapper = new SessionRowMapper();
    Session session = jdbcTemplate.queryForObject(sql, rowMapper, sessionId);
    return session;
  }

  public long nextId() {
    String sql = "SELECT max(ses.session_id) FROM session ses";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }



  public void add(Session session) {
    session.sessionId = nextId();
    // Add session
    String sql =
        "INSERT INTO session values (?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        session.sessionId,
        session.creatorId,
        session.creationTime,
        session.name,
        session.hostId,
        session.startTime,
        session.duration,
        session.hidden
    );
  }

  public Session deleteBySessionId(long sessionId) {
    Session session = getBySessionId(sessionId);
    String sql = "DELETE FROM session ses WHERE ses.session_id=?";
    jdbcTemplate.update(sql, sessionId);
    return session;
  }

  public boolean existsBySessionId(long sessionId) {
    String sql = "SELECT count(*) FROM session ses WHERE ses.session_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
    return count != 0;
  }

  // Restrict sessions by
  public List<Session> query(
      Long sessionId,
      Long creatorId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      String name,
      Long hostId,
      Long startTime,
      Long minStartTime,
      Long maxStartTime,
      Long duration,
      Long minDuration,
      Long maxDuration,
      Boolean hidden,
      long offset,
      long count) {
    String sql =
        "SELECT ses.* FROM session ses"
            + " WHERE 1=1 "
            + (sessionId== null ? "" : " AND ses.session_id = " + sessionId)
            + (creatorId == null ? "" : " AND ses.creator_id = " + creatorId)
            + (hostId == null ? "" : " AND ses.host_id = " + hostId)
            + (name == null ? "" : " AND ses.name = " + Utils.escape(name))
            + (creationTime == null ? "" : " AND ses.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND ses.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND ses.creation_time < " + maxCreationTime)
            + (startTime == null ? "" : " AND ses.start_time = " + startTime)
            + (minStartTime == null ? "" : " AND ses.start_time > " + minStartTime)
            + (maxStartTime == null ? "" : " AND ses.start_time < " + maxStartTime)
            + (duration == null ? "" : " AND ses.duration = " + duration)
            + (minDuration == null ? "" : " AND ses.duration > " + minDuration)
            + (maxDuration == null ? "" : " AND ses.duration < " + maxDuration)
            + (hidden == null ? "" : " AND ses.duration = " + (hidden ? 1 : 0))
            + (" ORDER BY ses.session_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Session> rowMapper = new SessionRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
