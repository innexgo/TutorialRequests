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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

class SchoolInfo {
  public long id;
  public String name;
  public String domain;
}

class SchoolInfoRowMapper implements RowMapper<SchoolInfo> {

  @Override
  public SchoolInfo mapRow(ResultSet row, int rowNum) throws SQLException {
    SchoolInfo si = new SchoolInfo();
    si.id = row.getLong("id");
    si.name = row.getString("name");
    si.domain = row.getString("domain");
    return si;
  }
}

@Transactional
@Repository
public class SchoolInfoService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public SchoolInfo get() {
    String sql =
        "SELECT id, name, domain FROM school_info WHERE id=?";
    RowMapper<SchoolInfo> rowMapper = new SchoolInfoRowMapper();
    SchoolInfo school_info = jdbcTemplate.queryForObject(sql, rowMapper, 0);
    return school_info;
  }

  public boolean initialized() {
    String sql = "SELECT count(*) FROM school_info";
    long count = jdbcTemplate.queryForObject(sql, Long.class);
    return count != 0;
  }

  public void inintialize(String name, String domain) {
    // Add schoolInfo
    String sql =
        "INSERT INTO school_info (id, name, domain) values (?, ?, ?)";
    jdbcTemplate.update(
        sql,
        0,
        name,
        domain);
  }
}
