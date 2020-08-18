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

package innexgo;

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
        "SELECT id, host_id, attendee_id, message, creation_time, time, duration, attendance_status WHERE id=?";
    RowMapper<Appt> rowMapper = new ApptRowMapper();
    Appt appt = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return appt;
  }

  public List<Appt> getAll() {
    String sql =
        "SELECT id, host_id, attendee_id, message, creation_time, time, duration, attendance_status FROM appt";
    RowMapper<Appt> rowMapper = new ApptRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  private void syncId(Appt appt) {
    String sql =
        "SELECT id FROM appt_request WHERE host_id=? AND attendee_id=? AND creation_time=? AND time=? AND attendance_status=?";
    appt.id =
        jdbcTemplate.queryForObject(
            sql,
            Long.class,
            appt.hostId,
            appt.attendeeId,
            appt.creationTime,
            appt.time,
            appt.duration,
            appt.attendanceStatus.name());
  }

  public void add(Appt appt) {
    // Add appt
    String sql =
        "INSERT INTO appt(id, host_id, attendee_id, message, creation_time, time, duration, attendance_status) values (?, ?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        appt.id,
        appt.hostId,
        appt.attendeeId,
        appt.message,
        appt.creationTime,
        appt.time,
        appt.duration,
        appt.attendanceStatus.name());
    syncId(appt);
  }

  public void update(Appt appt) {
    String sql =
        "UPDATE appt SET id=?, host_id=?, attendee_id=?, message=?, creation_time=?, time=?, duration=?, attendance_status=? WHERE id=?";
    jdbcTemplate.update(
        sql,
        appt.id,
        appt.hostId,
        appt.attendeeId,
        appt.message,
        appt.creationTime,
        appt.time,
        appt.duration,
        appt.attendanceStatus.name());
  }

  public Appt deleteById(long id) {
    Appt appt = getById(id);
    String sql = "DELETE FROM appt WHERE id=?";
    jdbcTemplate.update(sql, id);
    return appt;
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
      String message,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long time,
      Long minTime, 
      Long maxTime,
      Long duration,
      Long minDuration, 
      Long maxDuration,
      AttendanceStatus attendanceStatus,
      long offset,
      long count) {
    String sql =
        "SELECT a.id, a.host_id, a.attendee_id, a.message, a.creation_time, a.time, a.request_duration, a.reviewed, a.approved, a.response, a.attendance_status FROM appt_request a"
            + " WHERE 1=1 "
            + (id == null ? "" : " AND a.id = " + id)
            + (hostId == null ? "" : " AND a.host_id = " + hostId)
            + (attendeeId == null ? "" : " AND a.attendee_id = " + attendeeId)
            + (message == null ? "" : " AND a.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND a.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND a.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time < " + maxCreationTime)
            + (time == null ? "" : " AND a.time = " + time)
            + (minTime == null ? "" : " AND a.time > " + minTime)
            + (maxTime == null ? "" : " AND a.time < " + maxTime)
            + (duration == null ? "" : " AND a.duration = " + duration)
            + (minDuration == null ? "" : " AND a.duration > " + minDuration)
            + (maxDuration == null ? "" : " AND a.duration < " + maxDuration)
            + (attendanceStatus == null ? "" : " AND a.attendance_status= " + attendanceStatus.name())
            + (" ORDER BY a.id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Appt> rowMapper = new ApptRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}

