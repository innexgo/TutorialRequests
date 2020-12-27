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
public class VerificationChallengeService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public List<VerificationChallenge> getAll() {
    String sql =
        "SELECT * FROM verification_challenge";
    RowMapper<VerificationChallenge> rowMapper = new VerificationChallengeRowMapper();
    return jdbcTemplate.query(sql, rowMapper);
  }

  public void add(VerificationChallenge verificationChallenge) {
    // Add user
    verificationChallenge.creationTime = System.currentTimeMillis();
    String sql =
        "INSERT INTO verification_challenge values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        verificationChallenge.verificationChallengeKeyHash,
        verificationChallenge.creationTime,
        verificationChallenge.name,
        verificationChallenge.email,
        verificationChallenge.passwordHash);
  }

  public boolean existsByVerificationChallengeKeyHash(String verificationKeyHash) {
    String sql = "SELECT count(*) FROM verification_challenge WHERE verification_challenge_key_hash=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, verificationKeyHash);
    return count != 0;
  }

  public VerificationChallenge getByVerificationChallengeKeyHash(String verificationKey) {
    String sql =
        "SELECT * FROM verification_challenge WHERE verification_challenge_key_hash=?";
    RowMapper<VerificationChallenge> rowMapper = new VerificationChallengeRowMapper();
    VerificationChallenge verificationChallenge = jdbcTemplate.queryForObject(sql, rowMapper, verificationKey);
    return verificationChallenge;
  }

  public Long getLastCreationTimeByEmail(String email) {
    String sql =  "SELECT max(creation_time) FROM verification_challenge WHERE email=?";
    Long creationTime = jdbcTemplate.queryForObject(sql, Long.class, email);
    return creationTime;
  }

  public boolean existsByEmail(String email) {
    String sql = "SELECT count(*) FROM verification_challenge WHERE email=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, email);
    return count != 0;
  }
}
