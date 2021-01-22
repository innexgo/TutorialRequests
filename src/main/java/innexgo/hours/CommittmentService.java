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
public class CommittmentService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public Committment getByCommittmentId(long committmentId) {
    String sql = "SELECT * FROM committment WHERE committment_id=?";
    RowMapper<Committment> rowMapper = new CommittmentRowMapper();
    List<Committment> committments = jdbcTemplate.query(sql, rowMapper, committmentId);
    return committments.size() > 0 ? committments.get(0) : null;
  }

  public long nextId() {
    String sql = "SELECT max(committment_id) FROM committment";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if (maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Committment committment) {
    committment.committmentId = nextId();
    // Add committment
    String sql = "INSERT INTO committment values (?,?,?,?,?,?)";
    jdbcTemplate.update( //
        sql, //
        committment.committmentId, //
        committment.creationTime, //
        committment.creatorUserId, //
        committment.attendeeUserId, //
        committment.sessionId, //
        committment.cancellable); //
  }

  public boolean existsByCommittmentId(long committmentId) {
    String sql = "SELECT count(*) FROM committment WHERE committment_id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, committmentId);
    return count != 0;
  }

  public boolean unrespondedExistsByAttendeeIdSessionId(long attendeeId, long sessionId) {
    String sql = "SELECT count(c.committment_id) FROM committment c" //
        + " LEFT JOIN committment_response cr ON cr.committment_id = c.committment_id" //
        + " WHERE cr.committment_id IS NULL" //
        + " AND c.attendee_user_id = ? AND c.session_id = ?"; //
    long count = jdbcTemplate.queryForObject(sql, Long.class, attendeeId, sessionId);
    return count != 0;
  }

  public Stream<Committment> query( //
      Long committmentId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long attendeeUserId, //
      Long sessionId, //
      Boolean cancellable, //
      Long courseId, //
      Long startTime, //
      Long minStartTime, //
      Long maxStartTime, //
      Long duration, //
      Long minDuration, //
      Long maxDuration, //
      Boolean responded, //
      Boolean fromRequestResponse, //
      long offset, //
      long count) //
  {
    boolean nojoinSession = //
        startTime == null && minStartTime == null && maxStartTime == null && //
            duration == null && minDuration == null && maxDuration == null && //
            courseId == null; //

    String sql = "SELECT c.* FROM committment c" //
        + (nojoinSession ? "" : " LEFT JOIN session s ON s.session_id = c.session_id") //
        + (responded == null ? "" : " LEFT JOIN committment_response cr ON cr.committment_id = c.committment_id") //
        + (fromRequestResponse == null ? "" : " LEFT JOIN session_request_response srr ON srr.accepted AND srr.accepted_committment_id = c.committment_id") //
        + " WHERE 1=1 " //
        + (committmentId == null ? "" : " AND c.committment_id = " + committmentId) //
        + (creatorUserId == null ? "" : " AND c.creator_user_id = " + creatorUserId) //
        + (creationTime == null ? "" : " AND c.creation_time = " + creationTime) //
        + (minCreationTime == null ? "" : " AND c.creation_time > " + minCreationTime) //
        + (maxCreationTime == null ? "" : " AND c.creation_time < " + maxCreationTime) //
        + (attendeeUserId == null ? "" : " AND c.attendee_user_id = " + attendeeUserId) //
        + (sessionId == null ? "" : " AND c.session_id = " + sessionId) //
        + (cancellable == null ? "" : " AND c.cancellable = " + cancellable) //
        + (startTime == null ? "" : " AND s.start_time = " + startTime) //
        + (minStartTime == null ? "" : " AND s.start_time > " + minStartTime) //
        + (maxStartTime == null ? "" : " AND s.start_time < " + maxStartTime) //
        + (duration == null ? "" : " AND s.duration = " + duration) //
        + (minDuration == null ? "" : " AND s.duration > " + minDuration) //
        + (maxDuration == null ? "" : " AND s.duration < " + maxDuration) //
        + (courseId == null ? "" : " AND s.course_id = " + courseId) //
        + (responded == null ? "" : " AND cr.committment_id IS" + (responded ? " NOT NULL" : " NULL")) //
        + (fromRequestResponse == null ? "" : " AND srr.accepted_committment_id IS" + (fromRequestResponse ? " NOT NULL" : " NULL")) //
        + (" ORDER BY c.committment_id") //
        + (" LIMIT " + offset + ", " + count) //
        + ";"; //

    RowMapper<Committment> rowMapper = new CommittmentRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
