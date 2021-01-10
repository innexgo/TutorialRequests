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
public class CourseKeyService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  // Gets the last created key with the hash
  public CourseKey getByCourseKey(String key) {
    // order by is probably cheap bc there are at most 2
    String sql =
        "SELECT * FROM course_key WHERE key=? ORDER BY course_key_id DESC LIMIT 1";
    RowMapper<CourseKey> rowMapper = new CourseKeyRowMapper();
    List<CourseKey> apiKeys = jdbcTemplate.query(sql, rowMapper, key);
    return apiKeys.size() > 0 ? apiKeys.get(0) : null;
  }

  public long nextId() {
    String sql = "SELECT max(course_key_id) FROM course_key";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(CourseKey user) {
    user.courseKeyId = nextId();
    user.creationTime = System.currentTimeMillis();
    // Add user
    String sql = "INSERT INTO course_key values (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, //
        user.courseKeyId, //
        user.creationTime, //
        user.creatorUserId, //
        user.courseId, //
        user.key, //
        user.courseKeyKind.value, //
        user.duration); //
  }

  public CourseKey getByCourseKeyId(long courseKeyId) {
    String sql = "SELECT * FROM course_key WHERE course_key_id=?";
    RowMapper<CourseKey> rowMapper = new CourseKeyRowMapper();
    List<CourseKey> passwords = jdbcTemplate.query(sql, rowMapper, courseKeyId);
    return passwords.size() > 0 ? passwords.get(0) : null;
  }

  public Stream<CourseKey> query( //
      Long courseKeyId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long courseId, //
      CourseKeyKind courseKeyKind, //
      Long duration,
      Long minDuration,
      Long maxDuration,
      boolean onlyRecent, //
      long offset, //
      long count) {

    // prevent using duration becase it wont be defined 
    if(duration != null || minDuration != null || maxDuration != null) {
      if(courseKeyKind != null && courseKeyKind != CourseKeyKind.VALID) {
        return Stream.of(new CourseKey[] {});
      }
    }

    String sql = "SELECT ck.* FROM course_key ck" //
        + (!onlyRecent ? "" : " INNER JOIN (SELECT max(course_key_id) id FROM course_key GROUP BY key) maxids ON maxids.id = ck.course_key_id")
        + " WHERE 1=1 " //
        + (courseKeyId        == null ? "" : " AND ck.course_key_id = " + courseKeyId) //
        + (creationTime       == null ? "" : " AND ck.creation_time = " + creationTime) //
        + (minCreationTime    == null ? "" : " AND ck.creation_time > " + minCreationTime) //
        + (maxCreationTime    == null ? "" : " AND ck.creation_time < " + maxCreationTime) //
        + (creatorUserId      == null ? "" : " AND ck.creator_user_id = " + creatorUserId) //
        + (courseId           == null ? "" : " AND ck.course_id = " + courseId) //
        + (courseKeyKind      == null ? "" : " AND ck.course_key_kind = " + courseKeyKind.value) //
        + (duration           == null ? "" : " AND ck.duration =" + duration)
        + (minDuration        == null ? "" : " AND ck.duration >= " + minDuration)
        + (maxDuration        == null ? "" : " AND ck.duration <= " + maxDuration)
        + (" ORDER BY ck.course_key_id") //
        + (" LIMIT " + offset + ", " + count) //
        + ";"; //

    RowMapper<CourseKey> rowMapper = new CourseKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }

}
