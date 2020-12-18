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

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class EmailVerificationChallengeRowMapper implements RowMapper<EmailVerificationChallenge> {

  @Override
  public EmailVerificationChallenge mapRow(ResultSet row, int rowNum) throws SQLException {
    EmailVerificationChallenge u = new EmailVerificationChallenge();
    u.userId = row.getLong("user_id");
    u.creationTime = row.getLong("creation_time");
    u.name = row.getString("name");
    u.email = row.getString("email");
    u.verificationKey = row.getString("verification_key");
    u.passwordHash = row.getString("password_hash");
    return u;
  }
}
