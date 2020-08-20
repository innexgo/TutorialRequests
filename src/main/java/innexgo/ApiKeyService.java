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
public class ApiKeyService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public ApiKey getById(long id) {
    String sql =
        "SELECT id, user_id, creation_time, duration, key_hash, can_log_in, can_read_user, can_write_user, can_change_password, can_read_appt_request, can_write_appt_request, can_read_appt, can_write_appt FROM api_key WHERE id=?";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    ApiKey apiKey = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apiKey;
  }

  // Gets the last created key with the keyhash
  public ApiKey getByKeyHash(String keyHash) {
    String sql =
        "SELECT id, user_id, creation_time, duration, key_hash, can_log_in, can_read_user, can_write_user, can_change_password, can_read_appt_request, can_write_appt_request, can_read_appt, can_write_appt FROM api_key WHERE key_hash=? ORDER BY creation_time DESC";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    List<ApiKey> apiKeys = jdbcTemplate.query(sql, rowMapper, keyHash);
    return apiKeys.size() > 0 ? apiKeys.get(0) : null;
  }

  public List<ApiKey> getAll() {
    String sql =
        "SELECT id, user_id, creation_time, duration, key_hash, can_log_in, can_read_user, can_write_user, can_change_password, can_read_appt_request, can_write_appt_request, can_read_appt, can_write_appt FROM api_key";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  private void syncId(ApiKey apiKey) {
    String sql =
        "SELECT id FROM api_key WHERE user_id=? AND creation_time=? AND duration=? AND key_hash=? AND can_log_in=? AND can_read_user=? AND can_write_user=? AND can_change_password=? AND can_read_appt_request=? AND can_write_appt_request=? AND can_read_appt=? AND can_write_appt=?";
    long id =
        jdbcTemplate.queryForObject(
            sql,
            Long.class,
            apiKey.userId,
            apiKey.creationTime,
            apiKey.duration,
            apiKey.keyHash,
            apiKey.canLogIn,
            apiKey.canReadUser,
            apiKey.canWriteUser,
            apiKey.canChangePassword,
            apiKey.canReadApptRequest,
            apiKey.canWriteApptRequest,
            apiKey.canReadAppt,
            apiKey.canWriteAppt);

    // Set apiKey id
    apiKey.id = id;
  }

  public void add(ApiKey apiKey) {
    // Add API key
    String sql =
        "INSERT INTO api_key (id, user_id, creation_time, duration, key_hash, can_log_in, can_read_user, can_write_user, can_change_password, can_read_appt_request, can_write_appt_request, can_read_appt, can_write_appt) values (?,?,?,?,?,?,?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        apiKey.id,
        apiKey.userId,
        apiKey.creationTime,
        apiKey.duration,
        apiKey.keyHash,
        apiKey.canLogIn,
        apiKey.canReadUser,
        apiKey.canWriteUser,
        apiKey.canChangePassword,
        apiKey.canReadApptRequest,
        apiKey.canWriteApptRequest,
        apiKey.canReadAppt,
        apiKey.canWriteAppt);
    syncId(apiKey);
  }

  public ApiKey deleteById(long id) {
    ApiKey apiKey = getById(id);
    String sql = "DELETE FROM api_key WHERE id=?";
    jdbcTemplate.update(sql, id);
    return apiKey;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM api_key WHERE id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, id);
    return count != 0;
  }

  public boolean existsByKeyHash(String keyHash) {
    String sql = "SELECT count(*) FROM api_key WHERE key_hash=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, keyHash);
    return count != 0;
  }

  public List<ApiKey> query(
      Long id,
      Long userId,
      Long minCreationTime,
      Long maxCreationTime,
      String keyHash,
      Boolean canLogIn,
      Boolean canReadUser,
      Boolean canWriteUser,
      Boolean canChangePassword,
      Boolean canReadApptRequest,
      Boolean canWriteApptRequest,
      Boolean canReadAppt,
      Boolean canWriteAppt,
      long offset,
      long count) {
    String sql =
        "SELECT a.id, a.user_id, a.creation_time, a.duration, a.key_hash, a.can_log_in, a.can_read_user, a.can_write_user, a.can_change_password, a.can_read_appt_request, a.can_write_appt_request, a.can_read_appt, a.can_write_appt FROM api_key a WHERE 1=1"
            + (id == null ? "" : " AND a.id=" + id)
            + (userId == null ? "" : " AND a.user_id =" + userId)
            + (minCreationTime == null ? "" : " AND a.creation_time >= " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time <= " + maxCreationTime)
            + (keyHash == null ? "" : " AND a.key_hash = " + Utils.escape(keyHash))
            + (canLogIn == null ? "" : " AND a.can_log_in = " + canLogIn)
            + (canReadUser == null ? "" : " AND a.can_read_user = " + canReadUser)
            + (canWriteUser == null ? "" : " AND a.can_write_user = " + canWriteUser)
            + (canChangePassword == null ? "" : " AND a.can_change_password = " + canChangePassword)
            + (canReadApptRequest == null
                ? ""
                : " AND a.can_read_appt_request = " + canReadApptRequest)
            + (canWriteApptRequest == null
                ? ""
                : " AND a.can_write_appt_request = " + canWriteApptRequest)
            + (canReadAppt == null ? "" : " AND a.can_read_appt = " + canReadAppt)
            + (canWriteAppt == null ? "" : " AND a.can_write_appt = " + canWriteAppt)
            + (" ORDER BY a.id")
            + (" LIMIT " + offset + ", " + count)
            + ";";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
