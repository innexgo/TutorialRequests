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
public class UserService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public User getByUserId(long userId) {
    String sql =
        "SELECT * FROM user WHERE user_id=?";
    RowMapper<User> rowMapper = new UserRowMapper();
    List<User> users = jdbcTemplate.query(sql, rowMapper, userId);
    return users.size() > 0 ? users.get(0) : null;
  }

  public User getByEmail(String email) {
    String sql =
        "SELECT * FROM user WHERE email=?";
    RowMapper<User> rowMapper = new UserRowMapper();
    List<User> users = jdbcTemplate.query(sql, rowMapper, email);
    return users.size() > 0 ? users.get(0) : null;
  }

  public long nextId() {
    String sql = "SELECT max(user_id) FROM user";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(User user) {
    // Set user id
    user.userId = nextId();
    user.creationTime = System.currentTimeMillis();
    // Add user
    String sql =
        "INSERT INTO user values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        user.userId,
        user.creationTime,
        user.name,
        user.email,
        user.verificationChallengeKeyHash);

  }

  public boolean existsByEmail(String email) {
    String sql = "SELECT count(*) FROM user WHERE email=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, email);
    return count != 0;
  }

  public boolean existsByUserId(long id) {
    String sql = "SELECT count(*) FROM user WHERE user_id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, id);
    return count != 0;
  }

  public boolean existsByVerificationChallengeKeyHash(String verificationChallengeKeyHash) {
    String sql = "SELECT count(*) FROM user WHERE verification_challenge_key_hash=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, verificationChallengeKeyHash);
    return count != 0;
  }

  public Stream<User> query(
      Long id,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      String name,
      String partialUserName,
      String email,
      long offset,
      long count) {
    String sql =
        "SELECT u.* FROM user u"
            + " WHERE 1=1 "
            + (id                == null ? "" : " AND u.user_id = " + id)
            + (creationTime      == null ? "" : " AND u.creation_time = " + creationTime)
            + (minCreationTime   == null ? "" : " AND u.creation_time > " + minCreationTime)
            + (maxCreationTime   == null ? "" : " AND u.creation_time < " + maxCreationTime)
            + (name              == null ? "" : " AND u.name = " + Utils.escape(name))
            + (partialUserName   == null ? "" : " AND u.name LIKE " + Utils.escape("%"+partialUserName+"%"))
            + (email             == null ? "" : " AND u.email = " + Utils.escape(email))
            + (" ORDER BY u.user_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<User> rowMapper = new UserRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
