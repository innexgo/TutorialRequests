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
public class CourseMembershipService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public CourseMembership getByCourseMembershipId(long courseMembershipId) {
    String sql =
        "SELECT * FROM course_membership WHERE course_membership_id=?";
    RowMapper<CourseMembership> rowMapper = new CourseMembershipRowMapper();
    CourseMembership course_membership = jdbcTemplate.queryForObject(sql, rowMapper, courseMembershipId);
    return course_membership;
  }

  public long nextId() {
    String sql = "SELECT max(course_membership_id) FROM course_membership";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(CourseMembership courseMembership) {
    courseMembership.courseMembershipId = nextId();
    // Add course_membership
    String sql = "INSERT INTO course_membership values (?,?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        courseMembership.courseMembershipId,
        courseMembership.creationTime,
        courseMembership.creatorUserId,
        courseMembership.userId,
        courseMembership.courseId,
        courseMembership.courseMembershipKind.value,
        courseMembership.valid);
  }

  public boolean existsByCourseMembershipId(long courseMembershipId) {
    String sql = "SELECT count(*) FROM course_membership WHERE course_membership_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, courseMembershipId);
    return count != 0;
  }

 public List<CourseMembership> query( //
     Long courseMembershipId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     Long userId, //
     Long courseId, //
     CourseMembershipKind courseMembershipKind, //
     Boolean valid, //
     long offset, //
     long count) //
 {

    String sql =
      "SELECT l.* FROM course_membership cl"
        + " WHERE 1=1 "
        + (courseMembershipId    == null ? "" : " AND cl.course_membership_id = " + courseMembershipId)
        + (creationTime          == null ? "" : " AND cl.creation_time = " + creationTime)
        + (minCreationTime       == null ? "" : " AND cl.creation_time > " + minCreationTime)
        + (maxCreationTime       == null ? "" : " AND cl.creation_time < " + maxCreationTime)
        + (creatorUserId         == null ? "" : " AND cl.creator_user_id = " + creatorUserId)
        + (userId                == null ? "" : " AND cl.user_id = " + userId)
        + (courseId              == null ? "" : " AND cl.course_id = " + courseId)
        + (courseMembershipKind  == null ? "" : " AND cl.course_membeship_kind = " + courseMembershipKind.value)
        + (valid                 == null ? "" : " AND cl.valid = " + valid)
        + (" ORDER BY cl.course_membership_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<CourseMembership> rowMapper = new CourseMembershipRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
