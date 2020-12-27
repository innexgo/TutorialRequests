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
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class CoursePasswordService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public void add(CoursePassword user) {
    user.creationTime = System.currentTimeMillis();
    // Add user
    String sql = "INSERT INTO course_password values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, //
        user.coursePasswordId, //
        user.creationTime, //
        user.creatorUserId, //
        user.courseId, //
        user.coursePasswordKind.value, //
        user.passwordHash); //
  }

  public CoursePassword getByCoursePasswordId(long coursePasswordId) {
    String sql = "SELECT * FROM course_password WHERE course_password_id=?";
    RowMapper<CoursePassword> rowMapper = new CoursePasswordRowMapper();
    List<CoursePassword> passwords = jdbcTemplate.query(sql, rowMapper, coursePasswordId);
    return passwords.size() > 0 ? passwords.get(0) : null;
  }

  public List<CoursePassword> query( //
      Long coursePasswordId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long courseId, //
      CoursePasswordKind coursePasswordKind, //
      long offset, //
      long count) {

    String sql = "SELECT p.* FROM course_password p" //
        + " WHERE 1=1 " //
        + (coursePasswordId   == null ? "" : " AND p.course_password_id = " + coursePasswordId) //
        + (creationTime       == null ? "" : " AND p.creation_time = " + creationTime) //
        + (minCreationTime    == null ? "" : " AND p.creation_time > " + minCreationTime) //
        + (maxCreationTime    == null ? "" : " AND p.creation_time < " + maxCreationTime) //
        + (creatorUserId      == null ? "" : " AND p.creator_user_id = " + creatorUserId) //
        + (courseId           == null ? "" : " AND p.course_id = " + courseId) //
        + (coursePasswordKind == null ? "" : " AND p.course_password_kind = " + coursePasswordKind.value) //
        + (" ORDER BY p.course_password_id") //
        + (" LIMIT " + offset + ", " + count) //
        + ";"; //

    RowMapper<CoursePassword> rowMapper = new CoursePasswordRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

}
