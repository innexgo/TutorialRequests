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
        "SELECT ap.appt_request_id, ap.message, ap.creation_time, ap.start_time, ap.duration FROM appt ap WHERE ap.appt_request_id=?";
    RowMapper<Appt> rowMapper = new ApptRowMapper();
    Appt appt = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return appt;
  }

  public void add(Appt appt) {
    String sql =
        "INSERT INTO appt values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        appt.apptRequestId,
        appt.message,
        appt.creationTime,
        appt.startTime,
        appt.duration);
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM appt ap WHERE ap.appt_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  // Restrict appts by
  public List<Appt> query(
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
        "SELECT ap.appt_request_id, ap.message, ap.creation_time, ap.start_time, ap.duration FROM appt ap"
            + " WHERE 1=1 "
            + (apptRequestId == null ? "" : " AND ap.appt_request_id = " + apptRequestId)
            + (message == null ? "" : " AND ap.message = " + Utils.escape(message))
            + (creationTime == null ? "" : " AND ap.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND ap.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND ap.creation_time < " + maxCreationTime)
            + (time == null ? "" : " AND ap.start_time = " + time)
            + (minStartTime == null ? "" : " AND ap.start_time > " + minStartTime)
            + (maxStartTime == null ? "" : " AND ap.start_time < " + maxStartTime)
            + (duration == null ? "" : " AND ap.duration = " + duration)
            + (minDuration == null ? "" : " AND ap.duration > " + minDuration)
            + (maxDuration == null ? "" : " AND ap.duration < " + maxDuration)
            + (" ORDER BY ap.appt_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Appt> rowMapper = new ApptRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}

