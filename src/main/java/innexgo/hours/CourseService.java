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
public class CourseService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Course getByCourseId(long courseId) {
    String sql =
        "SELECT * FROM course WHERE course_id=?";
    RowMapper<Course> rowMapper = new CourseRowMapper();
    Course course = jdbcTemplate.queryForObject(sql, rowMapper, courseId);
    return course;
  }

  public long nextId() {
    String sql = "SELECT max(course_id) FROM course";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Course course) {
    course.courseId = nextId();
    // Add course
    String sql = "INSERT INTO course values (?,?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        course.courseId,
        course.creationTime,
        course.creatorUserId,
        course.schoolId,
        course.name,
        course.description,
        course.passwordHash);
  }

  public boolean existsByCourseId(long courseId) {
    String sql = "SELECT count(*) FROM course WHERE course_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, courseId);
    return count != 0;
  }

 public List<Course> query( //
     Long courseId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     Long schoolId, //
     String name, //
     String partialName, //
     String description, //
     String passwordHash, //
     long offset, //
     long count) //
 {

    String sql =
      "SELECT s.* FROM course s"
        + " WHERE 1=1 "
        + (courseId      == null ? "" : " AND s.course_id = " + courseId)
        + (creationTime    == null ? "" : " AND s.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND s.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND s.creation_time < " + maxCreationTime)
        + (creatorUserId   == null ? "" : " AND s.creator_user_id = " + creatorUserId)
        + (schoolId        == null ? "" : " AND s.school_id = " + schoolId)
        + (name            == null ? "" : " AND s.name = " + Utils.escape(name))
        + (partialName     == null ? "" : " AND s.name LIKE " + Utils.escape("%"+partialName+"%"))
        + (description     == null ? "" : " AND s.description = " + Utils.escape(description))
        + (passwordHash           == null ? "" : " AND s.password_hash = " + Utils.escape(passwordHash))
        + (" ORDER BY s.course_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<Course> rowMapper = new CourseRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
