
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
public class LocationService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Location getByLocationId(long locationId) {
    String sql =
        "SELECT * FROM location WHERE location_id=?";
    RowMapper<Location> rowMapper = new LocationRowMapper();
    Location location = jdbcTemplate.queryForObject(sql, rowMapper, locationId);
    return location;
  }

  public long nextId() {
    String sql = "SELECT max(location_id) FROM location";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Location location) {
    location.locationId = nextId();
    // Add location
    String sql = "INSERT INTO location values (?,?,?,?,?,?,?)";
    jdbcTemplate.update(
        sql,
        location.locationId,
        location.creationTime,
        location.creatorUserId,
        location.schoolId,
        location.name,
        location.description,
        location.valid);
  }

  public boolean existsByLocationId(long locationId) {
    String sql = "SELECT count(*) FROM location WHERE location_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, locationId);
    return count != 0;
  }

 public List<Location> query( //
     Long locationId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     Long schoolId, //
     String name, //
     String partialName, //
     String description, //
     Boolean valid, //
     long offset, //
     long count) //
 {

    String sql =
      "SELECT l.* FROM location l"
        + " WHERE 1=1 "
        + (locationId      == null ? "" : " AND l.location_id = " + locationId)
        + (creationTime    == null ? "" : " AND l.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND l.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND l.creation_time < " + maxCreationTime)
        + (creatorUserId   == null ? "" : " AND l.creator_user_id = " + creatorUserId)
        + (schoolId        == null ? "" : " AND l.school_id = " + schoolId)
        + (name            == null ? "" : " AND l.name = " + Utils.escape(name))
        + (partialName     == null ? "" : " AND l.name LIKE " + Utils.escape("%"+partialName+"%"))
        + (description     == null ? "" : " AND l.description = " + Utils.escape(description))
        + (valid           == null ? "" : " AND l.valid = " + valid)
        + (" ORDER BY l.location_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<Location> rowMapper = new LocationRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
