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

import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class SchoolDataService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public SchoolData getBySchoolDataId(long schoolDataId) {
    String sql = "SELECT * FROM school_data WHERE school_data_id=?";
    RowMapper<SchoolData> rowMapper = new SchoolDataRowMapper();
    SchoolData school = jdbcTemplate.queryForObject(sql, rowMapper, schoolDataId);
    return school;
  }

  public long nextId() {
    String sql = "SELECT max(school_data_id) FROM school_data";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if (maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(SchoolData schoolData) {
    schoolData.schoolDataId = nextId();
    schoolData.creationTime = System.currentTimeMillis();
    // Add schoolData
    String sql = "INSERT INTO school_data values (?, ?, ?, ?, ?, ?, ?)";
    jdbcTemplate.update(sql, schoolData.schoolDataId, schoolData.creationTime, schoolData.creatorUserId,
        schoolData.schoolId, schoolData.name, schoolData.description, schoolData.active);
  }

  public boolean existsBySchoolDataId(long schoolId) {
    String sql = "SELECT count(*) FROM school WHERE school_id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, schoolId);
    return count != 0;
  }

  public SchoolData getBySchoolId(long schoolId) {
    return query( //
        null, //
        null, //
        null, //
        null, //
        null, //
        schoolId, //
        null, //
        null, //
        null, //
        null, //
        null, //
        true, //
        null, //
        0, //
        1).findFirst().orElse(null);
  }

  public Stream<SchoolData> query( //
      Long schoolDataId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      Long schoolId, //
      String name, //
      String partialName, //
      String description, //
      String partialDescription, //
      Boolean active, //
      boolean onlyRecent, //
      Long recentAdminUserId, //
      long offset, //
      long count) //
  {

    boolean nojoinrecentadminship = recentAdminUserId == null;

    String sql = "SELECT sd.* FROM school_data sd" + (!onlyRecent ? ""
        : " INNER JOIN (SELECT max(school_data_id) id FROM school_data GROUP BY school_id) maxids ON maxids.id = sd.school_data_id")
    // first join all adminships
        + (nojoinrecentadminship ? "" : " INNER JOIN adminship a ON a.school_id = sd.school_id")
        // then filter adminships based on recent
        + (nojoinrecentadminship ? ""
            : " INNER JOIN (SELECT max(adminship_id) id FROM adminship GROUP BY user_id, school_id) maxadminids ON maxadminids.id = a.adminship_id")
        + " WHERE 1=1 " + (schoolDataId == null ? "" : " AND sd.school_data_id = " + schoolDataId)
        + (creationTime == null ? "" : " AND sd.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND sd.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND sd.creation_time < " + maxCreationTime)
        + (creatorUserId == null ? "" : " AND sd.creator_user_id = " + creatorUserId)
        + (schoolId == null ? "" : " AND sd.school_id = " + schoolId)
        + (name == null ? "" : " AND sd.name = " + Utils.escape(name))
        + (partialName == null ? "" : " AND sd.name LIKE " + Utils.escape("%" + partialName + "%"))
        + (description == null ? "" : " AND sd.description = " + Utils.escape(description))
        + (partialDescription == null ? "" : " AND sd.description LIKE " + Utils.escape("%" + partialDescription + "%"))
        + (active == null ? "" : " AND sd.active = " + active)
        + (recentAdminUserId == null ? "" : " AND a.user_id = " + recentAdminUserId)
        + (recentAdminUserId == null ? "" : " AND a.adminship_kind = " + AdminshipKind.ADMIN.value)
        + (" ORDER BY sd.school_data_id") + (" LIMIT " + offset + ", " + count) + ";";

    RowMapper<SchoolData> rowMapper = new SchoolDataRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }

}
