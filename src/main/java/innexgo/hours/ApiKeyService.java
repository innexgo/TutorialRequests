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
public class ApiKeyService {
  @Autowired private JdbcTemplate jdbcTemplate;

  public ApiKey getById(long id) {
    String sql =
        "SELECT * FROM api_key WHERE id=?";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    ApiKey apiKey = jdbcTemplate.queryForObject(sql, rowMapper, id);
    return apiKey;
  }

  // Gets the last created key with the keyhash
  public ApiKey getByKeyHash(String keyHash) {
    String sql =
        "SELECT * FROM api_key WHERE key_hash=? ORDER BY creation_time DESC";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    List<ApiKey> apiKeys = jdbcTemplate.query(sql, rowMapper, keyHash);
    return apiKeys.size() > 0 ? apiKeys.get(0) : null;
  }

  public List<ApiKey> getAll() {
    String sql =
        "SELECT * FROM api_key";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

 public long nextId() {
    String sql = "SELECT max(api_key_id) FROM api_key";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(ApiKey apiKey) {
    apiKey.apiKeyId = nextId();

    String sql =
        "INSERT INTO api_key values (?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        apiKey.apiKeyId,
        apiKey.creationTime,
        apiKey.creatorUserId,
        apiKey.duration,
        apiKey.keyHash,
        apiKey.valid
    );
  }

  public void revoke(ApiKey apiKey) {
    String sql =
        "UPDATE api_key SET valid=? WHERE api_key_id=?";
    jdbcTemplate.update(sql, false, apiKey.apiKeyId);
    apiKey.valid = false;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM api_key WHERE api_key_id=?";
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
      Long creatorUserId,
      Long minCreationTime,
      Long maxCreationTime,
      String keyHash,
      Boolean valid,
      long offset,
      long count) {
    String sql =
        "SELECT a.* FROM api_key a WHERE 1=1"
            + (id == null ? "" : " AND a.api_key_id=" + id)
            + (creatorUserId == null ? "" : " AND a.creator_user_id =" + creatorUserId)
            + (minCreationTime == null ? "" : " AND a.creation_time >= " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time <= " + maxCreationTime)
            + (keyHash == null ? "" : " AND a.key_hash = " + Utils.escape(keyHash))
            + (valid == null ? "" : " AND a.valid = " + valid)
            + (" ORDER BY a.api_key_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
