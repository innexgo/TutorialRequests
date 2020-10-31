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

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ForgotPasswordService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<ForgotPassword> getAll() {
    String sql =
        "SELECT id, name, email, creation_time, access_key, password_hash, valid FROM forgot_password";
    RowMapper<ForgotPassword> rowMapper = new ForgotPasswordRowMapper();
    return jdbcTemplate.query(sql, rowMapper);
  }

  public long nextId() {
    String sql = "SELECT max(id) FROM forgot_password";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(ForgotPassword user) {
    // Set user id
    user.id = nextId();
    user.valid = true;
    // Add user
    String sql =
        "INSERT INTO forgot_password (id, email, creation_time, access_key, valid) values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        user.id,
        user.email,
        user.creationTime,
        user.accessKey,
        user.valid);
  }

  public void update(ForgotPassword user) {
    String sql =
    "UPDATE forgot_password SET id=?, email=?, creation_time=?, access_key=?, valid=? WHERE id=?";
    jdbcTemplate.update(
        sql,
        user.id,
        user.email,
        user.creationTime,
        user.accessKey,
        user.valid,
        user.id); 
  }

  public ForgotPassword getByAccessKey(String accessKey) {
    String sql =
        "SELECT id, email, creation_time, access_key, valid FROM forgot_password WHERE access_key=?";
    RowMapper<ForgotPassword> rowMapper = new ForgotPasswordRowMapper();
    ForgotPassword forgotPassword = jdbcTemplate.queryForObject(sql, rowMapper, accessKey);
    return forgotPassword;
  }

  public boolean existsByAccessKey(String accessKey) {
    String sql = "SELECT count(*) FROM forgot_password WHERE access_key=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, accessKey);
    return count != 0;
  }
}
