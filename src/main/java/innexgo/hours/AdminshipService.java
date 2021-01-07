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
public class AdminshipService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public Adminship getByAdminshipId(long adminshipId) {
    String sql = "SELECT * FROM adminship WHERE adminship_id=?";
    RowMapper<Adminship> rowMapper = new AdminshipRowMapper();
    Adminship adminship = jdbcTemplate.queryForObject(sql, rowMapper, adminshipId);
    return adminship;
  }

  public long nextId() {
    String sql = "SELECT max(adminship_id) FROM adminship";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if (maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Adminship adminship) {
    adminship.adminshipId = nextId();
    // Add adminship
    String sql = "INSERT INTO adminship values (?,?,?,?,?,?)";
    jdbcTemplate.update(sql,
                        adminship.adminshipId,
                        adminship.creationTime,
                        adminship.creatorUserId,
                        adminship.userId,
                        adminship.schoolId,
                        adminship.adminshipKind);
  }

  public boolean existsByAdminshipId(long adminshipId) {
    String sql = "SELECT count(*) FROM adminship WHERE adminship_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, adminshipId);
    return count != 0;
  }

  public Stream<Adminship> query( //
      Long adminshipId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long userId, //
      Long schoolId, //
      AdminshipKind adminshipKind, //
      boolean onlyRecent, //
      long offset, //
      long count) //
  {
    String sql = "SELECT a.* FROM adminship a"
        + (!onlyRecent ? "" : " INNER JOIN (SELECT max(adminship_id) id FROM adminship GROUP BY user_id, school_id) maxids ON maxids.id = a.adminship_id")
        + " WHERE 1=1 "
        + (adminshipId == null ? "" : " AND a.adminship_id = " + adminshipId)
        + (creationTime == null ? "" : " AND a.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND a.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND a.creation_time < " + maxCreationTime)
        + (creatorUserId == null ? "" : " AND a.creator_user_id = " + creatorUserId)
        + (userId == null ? "" : " AND a.user_id = " + userId)
        + (schoolId == null ? "" : " AND a.school_id = " + schoolId)
        + (adminshipKind == null ? "" : " AND a.adminship_kind = " + adminshipKind.value)
        + (" ORDER BY a.adminship_id")
        + (" LIMIT " + offset + ", " + count) + ";";

    RowMapper<Adminship> rowMapper = new AdminshipRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }

  public boolean isAdmin(long userId, long schoolId) {
   String sql = "SELECT * FROM adminship WHERE 1=1 " +
     (" AND user_id = " + userId) +
     (" AND school_id = " + schoolId) +
     " ORDER BY adminship_id LIMIT 1;";
    RowMapper<Adminship> rowMapper = new AdminshipRowMapper();
    List<Adminship> adminships = this.jdbcTemplate.query(sql, rowMapper);
    if(adminships.size() == 0) {
        return false;
    }

    return adminships.get(0).adminshipKind == AdminshipKind.ADMIN;
  }

}
