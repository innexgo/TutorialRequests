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
public class EmailVerificationChallengeService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<EmailVerificationChallenge> getAll() {
    String sql =
        "SELECT id, name, email, creation_time, verification_key, password_hash, valid FROM email_verification_challenge";
    RowMapper<EmailVerificationChallenge> rowMapper = new EmailVerificationChallengeRowMapper();
    return jdbcTemplate.query(sql, rowMapper);
  }

  public long nextId() {
    String sql = "SELECT max(id) FROM email_verification_challenge";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }


  public void add(EmailVerificationChallenge user) {
    // Set user id
    user.id = nextId();
    user.valid = true;
    // Add user
    String sql =
        "INSERT INTO email_verification_challenge (id, name, email, creation_time, verification_key, password_hash, valid) values (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        user.id,
        user.name,
        user.email,
        user.creationTime,
        user.verificationKey,
        user.passwordHash,
        user.valid);
  }

  public void update(EmailVerificationChallenge user) {
    String sql =
    "UPDATE email_verification_challenge SET id=?, name=?, email=?, creation_time=?, verification_key=?, password_hash=?, valid=? WHERE id=?";
    jdbcTemplate.update(
        sql,
        user.id,
        user.name,
        user.email,
        user.creationTime,
        user.verificationKey,
        user.passwordHash,
        user.valid,
        user.id); 
  }

  public EmailVerificationChallenge getByVerificationKey(String verificationKey) {
    String sql =
        "SELECT id, name, email, creation_time, verification_key, password_hash, valid FROM email_verification_challenge WHERE verification_key=?";
    RowMapper<EmailVerificationChallenge> rowMapper = new EmailVerificationChallengeRowMapper();
    EmailVerificationChallenge emailVerificationChallenge = jdbcTemplate.queryForObject(sql, rowMapper, verificationKey);
    return emailVerificationChallenge;
  }

  public long getLastEmailCreationTimeByEmail(String userEmail){
    String sql =  "SELECT max(creation_time) FROM email_verification_challenge WHERE email=? ";
    long creationTime = jdbcTemplate.queryForObject(sql, Long.class, userEmail);
    return creationTime;
  }

  public boolean existsByVerificationKey(String verificationKey) {
    String sql = "SELECT count(*) FROM email_verification_challenge WHERE verification_key=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, verificationKey);
    return count != 0;
  }

  public boolean existsByEmail(String userEmail) {
    String sql = "SELECT count(*) FROM email_verification_challenge WHERE email=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, userEmail);
    return count != 0;
  }
}
