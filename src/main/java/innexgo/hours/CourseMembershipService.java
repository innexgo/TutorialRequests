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
    String sql = "INSERT INTO course_membership values (?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        courseMembership.courseMembershipId,
        courseMembership.creationTime,
        courseMembership.creatorUserId,
        courseMembership.userId,
        courseMembership.courseId,
        courseMembership.courseMembershipKind.value);
  }

  public boolean existsByCourseMembershipId(long courseMembershipId) {
    String sql = "SELECT count(*) FROM course_membership WHERE course_membership_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, courseMembershipId);
    return count != 0;
  }

 public Stream<CourseMembership> query( //
     Long courseMembershipId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     Long userId, //
     Long courseId, //
     CourseMembershipKind courseMembershipKind, //
     String courseName, //
     String partialCourseName, //
     String userName, //
     String partialUserName, //
     boolean onlyRecent,
     long offset, //
     long count) //
 {

    boolean nojoincourse = courseName == null && partialCourseName == null;
    boolean nojoinuser = userName == null && partialUserName == null;

    String sql =
      "SELECT cm.* FROM course_membership cm" //
        + (!onlyRecent ? "" : " INNER JOIN (SELECT max(course_membership_id) id FROM course_membership GROUP BY user_id, course_id) maxids ON maxids.id = cm.course_membership_id") //
        + (nojoincourse ? "" : " JOIN course c ON c.course_id = cm.course_id") //
        + (nojoinuser ? "" : " JOIN user u ON u.user_id = cm.user_id") //
        + " WHERE 1=1" //
        + (courseMembershipId    == null ? "" : " AND cm.course_membership_id = " + courseMembershipId) //
        + (creationTime          == null ? "" : " AND cm.creation_time = " + creationTime) //
        + (minCreationTime       == null ? "" : " AND cm.creation_time > " + minCreationTime) //
        + (maxCreationTime       == null ? "" : " AND cm.creation_time < " + maxCreationTime) //
        + (creatorUserId         == null ? "" : " AND cm.creator_user_id = " + creatorUserId) //
        + (userId                == null ? "" : " AND cm.user_id = " + userId) //
        + (courseId              == null ? "" : " AND cm.course_id = " + courseId) //
        + (courseMembershipKind  == null ? "" : " AND cm.course_membership_kind = " + courseMembershipKind.value) //
        + (courseName            == null ? "" : " AND c.name = " + Utils.escape(courseName)) //
        + (partialCourseName     == null ? "" : " AND c.name LIKE " + Utils.escape("%"+partialCourseName+"%")) //
        + (userName              == null ? "" : " AND u.name = " + Utils.escape(userName)) //
        + (partialUserName       == null ? "" : " AND u.name LIKE " + Utils.escape("%"+partialUserName+"%")) //
        + (" ORDER BY cm.course_membership_id") //
        + (" LIMIT " + offset + ", " + count) //
        + ";"; //

    RowMapper<CourseMembership> rowMapper = new CourseMembershipRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }

  public CourseMembershipKind getCourseMembership(long userId, long courseId) {
   String sql = "SELECT * FROM course_membership WHERE 1=1 " +
     (" AND user_id = " + userId) +
     (" AND course_id = " + courseId) +
     " ORDER BY course_membership_id LIMIT 1;";
    RowMapper<CourseMembership> rowMapper = new CourseMembershipRowMapper();
    List<CourseMembership> memberships = this.jdbcTemplate.query(sql, rowMapper);
    if(memberships.size() == 0) {
      return null;
    }
    return memberships.get(0).courseMembershipKind;
  }
}
