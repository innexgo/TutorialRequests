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
public class PasswordResetService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<PasswordReset> getAll() {
    String sql =
        "SELECT * FROM password_reset";
    RowMapper<PasswordReset> rowMapper = new PasswordResetRowMapper();
    return jdbcTemplate.query(sql, rowMapper);
  }

  public void add(PasswordReset user) {
    // Set user id
    user.used = false;
    user.creationTime = System.currentTimeMillis();
    // Add user
    String sql =
        "INSERT INTO password_reset values (?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        user.passwordResetKeyHash,
        user.creationTime,
        user.creationUserId,
        user.used);
  }

  public void use(PasswordReset user) {
    String sql =
    "UPDATE password_reset SET used=? WHERE password_reset_key_hash=?";
    jdbcTemplate.update(
        sql,
        true,
        user.passwordResetKeyHash);
    user.used = true;
  }

  public PasswordReset getByPasswordResetKeyHash(String resetKey) {
    String sql =
        "SELECT * FROM password_reset WHERE password_reset_key_hash=?";
    RowMapper<PasswordReset> rowMapper = new PasswordResetRowMapper();
    List<PasswordReset> passwordResets  = jdbcTemplate.query(sql, rowMapper, resetKey);
    // return first element if found, otherwise none
    return passwordResets.size() > 0 ? passwordResets.get(0) : null;
  }

  public boolean existsByPasswordResetKeyHash(String resetKey) {
    String sql = "SELECT count(*) FROM password_reset WHERE password_reset_key_hash=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, resetKey);
    return count != 0;
  }
}
