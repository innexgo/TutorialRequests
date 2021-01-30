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
public class SessionDataService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public SessionData getBySessionDataId(long sessionDataId) {
    String sql =
        "SELECT * FROM session_data WHERE session_data_id=?";
    RowMapper<SessionData> rowMapper = new SessionDataRowMapper();
    SessionData sessionData = jdbcTemplate.queryForObject(sql, rowMapper, sessionDataId);
    return sessionData;
  }

  public long nextId() {
    String sql = "SELECT max(sesd.session_data_id) FROM session_data sesd";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }



  public void add(SessionData sessionData) {
    sessionData.sessionDataId = nextId();
    // Add sessionData
    String sql =
        "INSERT INTO session_data values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        sessionData.sessionDataId,
        sessionData.creationTime,
        sessionData.creatorUserId,
        sessionData.sessionId,
        sessionData.name,
        sessionData.startTime,
        sessionData.duration,
        sessionData.hidden,
        sessionData.active
    );
  }

  public boolean existsBySessionDataId(long sessionDataId) {
    String sql = "SELECT count(*) FROM session_data sesd WHERE sesd.session_data_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, sessionDataId);
    return count != 0;
  }

  // Restrict sessionDatas by
  public Stream<SessionData> query(
      Long sessionDataId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long creatorUserId,
      Long sessionId,
      String name,
      String partialName,
      Long startTime,
      Long minStartTime,
      Long maxStartTime,
      Long duration,
      Long minDuration,
      Long maxDuration,
      Boolean hidden,
      Boolean active,
      boolean onlyRecent,
      long offset,
      long count) {
    String sql =
        "SELECT sesd.* FROM session_data sesd"
            + (!onlyRecent ? "" : " INNER JOIN (SELECT max(session_data_id) id FROM session_data GROUP BY session_id) maxids ON maxids.id = sesd.session_data_id")
            + " WHERE 1=1 "
            + (sessionDataId   == null ? "" : " AND sesd.session_data_id = " + sessionDataId)
            + (creationTime    == null ? "" : " AND sesd.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND sesd.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND sesd.creation_time < " + maxCreationTime)
            + (creatorUserId   == null ? "" : " AND sesd.creator_id = " + creatorUserId)
            + (sessionId       == null ? "" : " AND sesd.session_id = " + sessionId)
            + (name            == null ? "" : " AND sesd.name = " + Utils.escape(name))
            + (partialName     == null ? "" : " AND sesd.name LIKE " + Utils.escape("%"+partialName+"%"))
            + (startTime       == null ? "" : " AND sesd.start_time = " + startTime)
            + (minStartTime    == null ? "" : " AND sesd.start_time > " + minStartTime)
            + (maxStartTime    == null ? "" : " AND sesd.start_time < " + maxStartTime)
            + (duration        == null ? "" : " AND sesd.duration = " + duration)
            + (minDuration     == null ? "" : " AND sesd.duration > " + minDuration)
            + (maxDuration     == null ? "" : " AND sesd.duration < " + maxDuration)
            + (hidden          == null ? "" : " AND sesd.hidden = " + hidden)
            + (active          == null ? "" : " AND sesd.active = " + active)
            + (" ORDER BY sesd.session_data_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<SessionData> rowMapper = new SessionDataRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
