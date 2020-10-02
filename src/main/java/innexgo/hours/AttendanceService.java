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
public class AttendanceService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Attendance getById(long id) {
    String sql =
        "SELECT at.appt_id, at.creation_time, at.kind FROM attendance at WHERE appt_id=?";
    RowMapper<Attendance> rowMapper = new AttendanceRowMapper();
    Attendance attendance = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return attendance;
  }

  public void add(Attendance attendance) {
    // Add attendance
    String sql =
        "INSERT INTO attendance  values (?, ?, ?)";
    jdbcTemplate.update(
        sql,
        attendance.apptId,
        attendance.creationTime,
        attendance.kind.value);
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM attendance at WHERE at.appt_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  public List<Attendance> query(
      Long apptId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      AttendanceKind kind,
      long offset,
      long count) {
    String sql =
        "SELECT at.appt_id, at.creation_time, at.kind FROM attendance at"
            + " WHERE 1=1 "
            + (apptId == null ? "" : " AND at.appt_id = " + apptId)
            + (kind == null ? "" : " AND at.kind = " + kind.value)
            + (creationTime == null ? "" : " AND at.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND at.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND at.creation_time < " + maxCreationTime)
            + (" ORDER BY at.appt_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Attendance> rowMapper = new AttendanceRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
