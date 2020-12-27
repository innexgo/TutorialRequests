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

  // Gets the last created key with the keyhash
  public ApiKey getByApiKeyHash(String keyHash) {
    // order by is probably cheap bc there are at most 2
    String sql =
        "SELECT * FROM api_key WHERE api_key_hash=? ORDER BY creation_time DESC LIMIT 1";
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

  public void add(ApiKey apiKey) {
    apiKey.creationTime= System.currentTimeMillis();

    String sql =
        "INSERT INTO api_key values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        apiKey.apiKeyHash,
        apiKey.creationTime,
        apiKey.creatorUserId,
        apiKey.duration,
        apiKey.apiKeyKind.value
    );
  }

  public List<ApiKey> query(
      String apiKeyHash,
      Long creatorUserId,
      Long minCreationTime,
      Long maxCreationTime,
      ApiKeyKind apiKeyKind,
      long offset,
      long count) {
    String sql =
        "SELECT a.* FROM api_key a WHERE 1=1"
            + (apiKeyHash      == null ? "" : " AND a.api_key_hash = " + Utils.escape(apiKeyHash))
            + (creatorUserId   == null ? "" : " AND a.creator_user_id =" + creatorUserId)
            + (minCreationTime == null ? "" : " AND a.creation_time >= " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time <= " + maxCreationTime)
            + (apiKeyKind      == null ? "" : " AND a.api_key_kind = " + apiKeyKind.value)
            + (" ORDER BY a.creation_time")
            + (" LIMIT " + offset + ", " + count)
            + ";";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
