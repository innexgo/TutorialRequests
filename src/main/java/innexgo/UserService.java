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

package innexgo;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class UserService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public User getById(long id) {
    String sql = "SELECT id, secondary_id, school_id, name, email, password_hash FROM user WHERE id=?";
    RowMapper<User> rowMapper = new UserRowMapper();
    User user = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return user;
  }

  public User getByEmail(String email) {
    String sql = "SELECT id, secondary_id, school_id, name, email, password_hash FROM user WHERE email=?";
    RowMapper<User> rowMapper = new UserRowMapper();
    User user = jdbcTemplate.queryForObject(sql, rowMapper, email);
    return user;
  }

  public List<User> getAll() {
    String sql = "SELECT  id, secondary_id, school_id, name, email, password_hash FROM user";
    RowMapper<User> rowMapper = new UserRowMapper();
    return jdbcTemplate.query(sql, rowMapper);
  }

  public void add(User user) {
    // Add user
    String sql =
        "INSERT INTO user (id, secondary_id, school_id, name, email, password_hash) values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql, user.id, user.secondaryId, user.schoolId, user.name, user.email, user.passwordHash);

    // Fetch user id
    sql =
        "SELECT id FROM user WHERE secondary_id=? AND school_id=? AND name=? AND email=? AND password_hash=?";
    long id =
        jdbcTemplate.queryForObject(
            sql, Long.class, user.secondaryId, user.schoolId, user.name, user.email, user.passwordHash);

    // Set user id
    user.id = id;
  }

  public void update(User user) {
    String sql =
        "UPDATE user SET id=?, name=?, email=?, password_hash=? WHERE id=?";
    jdbcTemplate.update(
        sql,
        user.id,
        user.name,
        user.email,
        user.passwordHash,
        user.id);
  }

  public User deleteById(long id) {
    User user = getById(id);
    String sql = "DELETE FROM user WHERE id=?";
    jdbcTemplate.update(sql, id);
    return user;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM user WHERE id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  public boolean existsByEmail(String email) {
    String sql = "SELECT count(*) FROM user WHERE email=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, email);
    return count != 0;
  }

  public List<User> query(Long id, secondary_id, school_id, String name, String email, long offset, long count) {
    String sql =
        "SELECT u.id, secondary_id, school_id, u.name, u.password_hash, u.email, u.ring FROM user u"
            + " WHERE 1=1 "
            + (id == null ? "" : " AND u.id = " + id)
            + (name == null ? "" : " AND u.name = " + Utils.escape(name))
            + (email == null ? "" : " AND u.email = " + Utils.escape(email))
            + (" ORDER BY u.id")
            + (" LIMIT " + offset + ", "  + count)
            + ";";

    RowMapper<User> rowMapper = new UserRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
