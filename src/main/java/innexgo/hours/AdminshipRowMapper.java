
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

public class AdminshipRowMapper implements RowMapper<Adminship> {

  @Override
  public Adminship mapRow(ResultSet row, int rowNum) throws SQLException {
    Adminship adminship = new Adminship();
    adminship.adminshipId = row.getLong("adminship_id");
    adminship.creationTime = row.getLong("creation_time");
    adminship.creatorUserId = row.getLong("creator_user_id");
    adminship.userId = row.getLong("user_id");
    adminship.schoolId = row.getLong("school_id");
    adminship.adminshipKind = AdminshipKind.from(row.getInt("adminship_kind"));
    adminship.adminshipSourceKind = AdminshipSourceKind.from(row.getInt("adminship_source_kind"));
    adminship.adminshipRequestResponseId = row.getLong("adminship_request_response_id");
    return adminship;
  }
}
