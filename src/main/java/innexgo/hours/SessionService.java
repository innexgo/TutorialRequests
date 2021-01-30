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

import java.util.stream.Stream;
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
        "INSERT INTO session values (?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        session.sessionId,
        session.creationTime,
        session.creatorUserId,
        session.courseId);
  }

  public boolean existsBySessionId(long sessionId) {
    String sql = "SELECT count(*) FROM session ses WHERE ses.session_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionId);
    return count != 0;
  }

  // Restrict sessions by
  public Stream<Session> query(
      Long sessionId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long creatorUserId,
      Long courseId,
      long offset,
      long count) {
    String sql =
        "SELECT ses.* FROM session ses"
            + " WHERE 1=1 "
            + (sessionId       == null ? "" : " AND ses.session_id = " + sessionId)
            + (creationTime    == null ? "" : " AND ses.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND ses.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND ses.creation_time < " + maxCreationTime)
            + (creatorUserId   == null ? "" : " AND ses.creator_id = " + creatorUserId)
            + (courseId        == null ? "" : " AND ses.course_id = " + courseId)
            + (" ORDER BY ses.session_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Session> rowMapper = new SessionRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
