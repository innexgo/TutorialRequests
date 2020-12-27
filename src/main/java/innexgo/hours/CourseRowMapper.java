
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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class CourseRowMapper implements RowMapper<Course> {

  @Override
  public Course mapRow(ResultSet row, int rowNum) throws SQLException {
    Course course = new Course();
    course.courseId = row.getLong("course_id");
    course.creationTime = row.getLong("creation_time");
    course.creatorUserId = row.getLong("creator_user_id");
    course.schoolId = row.getLong("school_id");
    course.name = row.getString("name");
    course.description = row.getString("description");
    course.joinable = row.getBoolean("joinable");
    course.joinPasswordHash = row.getString("join_password_hash ");
    return course;
  }
}
