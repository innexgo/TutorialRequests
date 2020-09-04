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
public class ApptService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Appt getById(long id) {
    String sql =
        "SELECT id, host_id, attendee_id, appt_request_id, message, creation_time, start_time, duration FROM appt WHERE id=?";
    RowMapper<Appt> rowMapper = new ApptRowMapper();
    Appt appt = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return appt;
  }

  public List<Appt> getAll() {
    String sql =
        "SELECT id, host_id, attendee_id, appt_request_id, message, creation_time, start_time, duration FROM appt";
    RowMapper<Appt> rowMapper = new ApptRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  private void syncId(Appt appt) {
    String sql =
        "SELECT id FROM appt WHERE host_id=? AND attendee_id=? AND appt_request_id=? AND creation_time=? AND start_time=? AND duration=?";
    appt.id =
        jdbcTemplate.queryForObject(
            sql,
            Long.class,
            appt.hostId,
            appt.attendeeId,
            appt.apptRequestId,
            appt.creationTime,
            appt.startTime,
            appt.duration);
  }

  public void add(Appt appt) {
    // Add appt
    String sql =
        "INSERT INTO appt(id, host_id, attendee_id, appt_request_id, message, creation_time, start_time, duration) values (?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        appt.id,
        appt.hostId,
        appt.attendeeId,
        appt.apptRequestId,
        appt.message,
        appt.creationTime,
        appt.startTime,
        appt.duration);
    syncId(appt);
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM appt WHERE id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  // Restrict appts by
  public List<Appt> query(
      Long id,
      Long hostId,
      Long attendeeId,
      Long apptRequestId,
      String message,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long time,
      Long minStartTime, 
      Long maxStartTime,
      Long duration,
      Long minDuration, 
      Long maxDuration,
      long offset,
      long count) {
    String sql =
        "SELECT a.id, a.host_id, a.attendee_id, a.appt_request_id, a.message, a.creation_time, a.start_time, a.duration FROM appt a"
            + " WHERE 1=1 "
            + (id == null ? "" : " AND a.id = " + id)
            + (hostId == null ? "" : " AND a.host_id = " + hostId)
            + (attendeeId == null ? "" : " AND a.attendee_id = " + attendeeId)
            + (apptRequestId == null ? "" : " AND a.appt_request_id = " + apptRequestId)
            + (message == null ? "" : " AND a.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND a.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND a.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time < " + maxCreationTime)
            + (time == null ? "" : " AND a.start_time = " + time)
            + (minStartTime == null ? "" : " AND a.start_time > " + minStartTime)
            + (maxStartTime == null ? "" : " AND a.start_time < " + maxStartTime)
            + (duration == null ? "" : " AND a.duration = " + duration)
            + (minDuration == null ? "" : " AND a.duration > " + minDuration)
            + (maxDuration == null ? "" : " AND a.duration < " + maxDuration)
            + (" ORDER BY a.id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Appt> rowMapper = new ApptRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}

