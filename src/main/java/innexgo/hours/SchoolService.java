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

package hours;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class SchoolService {

  @Autowired
  private JdbcTemplate jdbcTemplate;

  public School getById(long id) {
    String sql = "SELECT id, name FROM school WHERE id=?";
    RowMapper<School> rowMapper = new SchoolRowMapper();
    List<School> schools = jdbcTemplate.query(sql, rowMapper, id);
    if (schools.isEmpty()) {
      return null;
    } else {
      return schools.get(0);
    }
  }

  public List<School> getAll() {
    String sql = "SELECT id, name FROM school";
    RowMapper<School> rowMapper = new SchoolRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }

  public void add(School school) {
    // Add school
    String sql = "INSERT INTO school (id, name) values (?, ?)";
    jdbcTemplate.update(sql, school.id, school.name);

    // Fetch user id
    sql = "SELECT id FROM school WHERE name=?";
    long id = jdbcTemplate.queryForObject(sql, Long.class, school.name);

    // Set user id
    school.id = id;
  }

  public void update(School school) {
    String sql = "UPDATE school SET id=?, name=? WHERE id=?";
    jdbcTemplate.update(sql, school.id, school.name, school.id);
  }

  public School deleteById(long id) {
    School school = getById(id);
    String sql = "DELETE FROM school WHERE id=?";
    jdbcTemplate.update(sql, id);
    return school;
  }

  public boolean existsById(long id) {
    String sql = "SELECT count(*) FROM school WHERE id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, id);
    return count != 0;
  }

  public List<School> query(Long id, String name, long offset, long count) {
    String sql = "SELECT l.id, l.name FROM school l" + " WHERE 1=1 " + (id == null ? "" : " AND l.id = " + id)
        + (name == null ? "" : " AND l.name = " + Utils.escape(name)) + (" ORDER BY l.id")
        + (" LIMIT " + offset + ", " + count) + ";";

    RowMapper<School> rowMapper = new SchoolRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper);
  }
}
