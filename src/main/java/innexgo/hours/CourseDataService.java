
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
public class CourseDataService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public CourseData getByCourseDataId(long courseDataId) {
    String sql =
        "SELECT * FROM course_data WHERE course_data_id =?";
    RowMapper<CourseData> rowMapper = new CourseDataRowMapper();
    List<CourseData> courseDatas = jdbcTemplate.query(sql, rowMapper, courseDataId);
    return courseDatas.size() > 0 ? courseDatas.get(0) : null;
  }

  public long nextId() {
    String sql = "SELECT max(course_data_id) FROM course_data";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(CourseData courseData) {
    courseData.courseDataId = nextId();
    courseData.creationTime= System.currentTimeMillis();
    // Add courseData
    String sql = "INSERT INTO course_data values (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update( //
        sql, //
        courseData.courseDataId, //
        courseData.creationTime, //
        courseData.creatorUserId, //
        courseData.courseId, //
        courseData.name, //
        courseData.description, //
        courseData.active //
    );
  }

 public Stream<CourseData> query( //
     Long courseDataId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     Long courseId, //
     String name, //
     String partialName, //
     String description, //
     String partialDescription, //
     Boolean active, //
     boolean onlyRecent, //
     long offset, //
     long count) //
 {

    String sql =
      "SELECT cd.* FROM course_data cd"
        + (!onlyRecent ? "" : " INNER JOIN (SELECT max(course_data_id) id FROM course_data GROUP BY course_id) maxids ON maxids.id = cd.course_data_id")
        + " WHERE 1=1 "
        + (courseDataId       == null ? "" : " AND cd.course_data_id = " + courseDataId)
        + (creationTime       == null ? "" : " AND cd.creation_time = " + creationTime)
        + (minCreationTime    == null ? "" : " AND cd.creation_time > " + minCreationTime)
        + (maxCreationTime    == null ? "" : " AND cd.creation_time < " + maxCreationTime)
        + (creatorUserId      == null ? "" : " AND cd.creator_user_id = " + creatorUserId)
        + (courseId           == null ? "" : " AND cd.course_id = " + courseId)
        + (name               == null ? "" : " AND cd.name = " + Utils.escape(name))
        + (partialName        == null ? "" : " AND cd.name LIKE " + Utils.escape("%"+partialName+"%"))
        + (description        == null ? "" : " AND cd.description = " + Utils.escape(description))
        + (partialDescription == null ? "" : " AND cd.description LIKE " + Utils.escape("%"+partialDescription+"%"))
        + (active             == null ? "" : " AND cd.active = " + active)
        + (" ORDER BY c.courseData_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<CourseData> rowMapper = new CourseDataRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
