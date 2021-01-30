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
public class SchoolService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public School getBySchoolId(long schoolId) {
    String sql =
        "SELECT * FROM school WHERE school_id=?";
    RowMapper<School> rowMapper = new SchoolRowMapper();
    School school = jdbcTemplate.queryForObject(sql, rowMapper, schoolId);
    return school;
  }

  public long nextId() {
    String sql = "SELECT max(school_id) FROM school";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(School school) {
    school.schoolId = nextId();
    school.creationTime = System.currentTimeMillis();
    // Add school
    String sql = "INSERT INTO school values (?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        school.schoolId,
        school.creationTime,
        school.creatorUserId,
        school.whole);
  }

  public boolean existsBySchoolId(long schoolId) {
    String sql = "SELECT count(*) FROM school WHERE school_id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, schoolId);
    return count != 0;
  }

  public Stream<School> query( //
     Long schoolId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     String name, //
     String partialName, //
     Boolean whole, //
     long offset, //
     long count) //
 {

    String sql=
      "SELECT s.* FROM school s"
        + " WHERE 1=1 "
        + (schoolId        == null ? "" : " AND s.school_id = " + schoolId)
        + (creatorUserId   == null ? "" : " AND s.creator_user_id = " + creatorUserId)
        + (creationTime    == null ? "" : " AND s.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND s.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND s.creation_time < " + maxCreationTime)
        + (name            == null ? "" : " AND s.name = " + Utils.escape(name))
        + (partialName     == null ? "" : " AND s.name LIKE " + Utils.escape("%"+partialName+"%"))
        + (whole           == null ? "" : " AND s.whole = " + whole)
        + (" ORDER BY s.school_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<School> rowMapper = new SchoolRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
