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

public class CommittmentRowMapper implements RowMapper<Committment> {

  @Override
  public Committment mapRow(ResultSet row, int rowNum) throws SQLException {
    Committment committment = new Committment();
    committment.committmentId = row.getLong("committmentId");
    committment.creatorId = row.getLong("creatorId");
    committment.creationTime = row.getLong("creationTime");
    committment.attendeeId = row.getLong("attendeeId");
    committment.sessionId = row.getLong("sessionId");
    committment.cancellable = row.getBoolean("cancellable");
    return committment;
  }
}
