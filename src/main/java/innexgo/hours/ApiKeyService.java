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
public class ApiKeyService {
  @Autowired private JdbcTemplate jdbcTemplate;

  // Gets the last created key with the keyhash
  public ApiKey getByApiKeyHash(String keyHash) {
    // order by is probably cheap bc there are at most 2
    String sql =
        "SELECT * FROM api_key WHERE api_key_hash=? ORDER BY api_key_id DESC LIMIT 1";
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
    apiKey.creationTime= System.currentTimeMillis();
    apiKey.apiKeyId = nextId();

    String sql =
        "INSERT INTO api_key values (?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update( //
        sql, //
        apiKey.apiKeyId, //
        apiKey.creationTime, //
        apiKey.creatorUserId, //
        apiKey.apiKeyHash, //
        apiKey.duration, //
        apiKey.apiKeyKind.value //
    );
  }

  public Stream<ApiKey> query(
      Long apiKeyId,
      Long creatorUserId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long duration,
      Long minDuration,
      Long maxDuration,
      ApiKeyKind apiKeyKind,
      boolean onlyRecent,
      long offset,
      long count) {
    String sql =
        "SELECT a.* FROM api_key a"
            + (onlyRecent ? "" : " INNER JOIN (SELECT max(api_key_id) id FROM api_key GROUP BY api_key_hash) maxids ON maxids.id = a.api_key_id")
            + " WHERE 1=1"
            + (apiKeyId    == null ? "" : " AND a.api_key_id =" + apiKeyId)
            + (creationTime    == null ? "" : " AND a.creation_time =" + creationTime)
            + (minCreationTime == null ? "" : " AND a.creation_time >= " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND a.creation_time <= " + maxCreationTime)
            + (creatorUserId   == null ? "" : " AND a.creator_user_id =" + creatorUserId)
            + (duration    == null ? "" : " AND a.duration =" + duration)
            + (minDuration == null ? "" : " AND a.duration >= " + minDuration)
            + (maxDuration == null ? "" : " AND a.duration <= " + maxDuration)
            + (apiKeyKind      == null ? "" : " AND a.api_key_kind = " + apiKeyKind.value)
            + (" ORDER BY a.creation_time")
            + (" LIMIT " + offset + ", " + count)
            + ";";
    RowMapper<ApiKey> rowMapper = new ApiKeyRowMapper();
    return this.jdbcTemplate.queryForStream(sql, rowMapper);
  }
}
