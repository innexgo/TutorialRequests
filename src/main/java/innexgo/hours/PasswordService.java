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
public class PasswordService {

  @Autowired
  private JdbcTemplate jdbcTemplate;


  public long nextId() {
    String sql = "SELECT max(password_id) FROM password";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Password user) {
    user.passwordId = nextId();
    user.creationTime = System.currentTimeMillis();
    // Add user
    String sql = "INSERT INTO password values (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql,
        user.passwordId,
        user.creationTime,
        user.creatorUserId,
        user.userId,
        user.passwordKind.value,
        user.passwordHash,
        user.passwordResetKeyHash);
  }

  public Password getByPasswordId(long passwordId) {
    String sql = "SELECT * FROM password WHERE password_id=?";
    RowMapper<Password> rowMapper = new PasswordRowMapper();
    List<Password> passwords = jdbcTemplate.query(sql, rowMapper, passwordId);
    return passwords.size() > 0 ? passwords.get(0) : null;
  }

  // get most recent password
  public Password getByUserId(long userId) {
      String sql = "SELECT * FROM password WHERE user_id=? ORDER BY password_id";
    RowMapper<Password> rowMapper = new PasswordRowMapper();
    List<Password> passwords = jdbcTemplate.query(sql, rowMapper, userId);
    return passwords.size() > 0 ? passwords.get(0) : null;
  }

  public Stream<Password> query( //
      Long passwordId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long userId, //
      PasswordKind passwordKind, //
      String passwordResetKeyHash, //
      boolean onlyRecent, //
      long offset, //
      long count) { //

    // we need to return empty list if querying a mutually exclusive option
    if(passwordKind != null && passwordKind != PasswordKind.RESET ) {
      if(passwordResetKeyHash != null) {
        return Stream.empty();
      }
    }

    String sql =
        "SELECT p.* FROM password p"
            + " WHERE 1=1 "
            + (onlyRecent ? "" : " INNER JOIN (SELECT max(password_id) id FROM password GROUP BY user_id) maxids ON maxids.id = p.password_id")
            + (passwordId           == null ? "" : " AND p.password_id = " + passwordId)
            + (creationTime         == null ? "" : " AND p.creation_time = " + creationTime)
            + (minCreationTime      == null ? "" : " AND p.creation_time > " + minCreationTime)
            + (maxCreationTime      == null ? "" : " AND p.creation_time < " + maxCreationTime)
            + (creatorUserId        == null ? "" : " AND p.creator_user_id = " + creatorUserId)
            + (userId               == null ? "" : " AND p.user_id = " + userId)
            + (passwordKind         == null ? "" : " AND p.password_kind = " + passwordKind.value)
            + (passwordResetKeyHash == null ? "" : " AND p.password_reset_key_hash = " + Utils.escape(passwordResetKeyHash))
            + (" ORDER BY p.password_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<Password> rowMapper = new PasswordRowMapper();
    return this.jdbcTemplate.queryForStream(sql, rowMapper);
  }

  public boolean existsByPasswordResetKeyHash(String passwordResetKeyHash) {
    String sql = "SELECT count(*) FROM password WHERE password_reset_key_hash=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, passwordResetKeyHash);
    return count != 0;
  }
}
