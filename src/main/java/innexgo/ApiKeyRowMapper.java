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

package innexgo;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class ApiKeyRowMapper implements RowMapper<ApiKey> {
  @Override
  public ApiKey mapRow(ResultSet row, int rowNum) throws SQLException {
    ApiKey apiKey = new ApiKey();
    apiKey.id = row.getLong("id");
    apiKey.userId = row.getLong("user_id");
    apiKey.creationTime = row.getLong("creation_time");
    apiKey.expirationTime = row.getLong("expiration_time");

    apiKey.readUser = CapabilityKind.valueOf(row.getString("read_user"));
    apiKey.writeUser = CapabilityKind.valueOf(row.getString("write_user"));

    apiKey.readApiKey = CapabilityKind.valueOf(row.getString("read_api_key"));
    apiKey.writeApiKey = CapabilityKind.valueOf(row.getString("write_api_key"));

    apiKey.readApptRequest = CapabilityKind.valueOf(row.getString("read_appt_request"));
    apiKey.writeApptRequest = CapabilityKind.valueOf(row.getString("write_appt_request"));

    apiKey.readAppt = CapabilityKind.valueOf(row.getString("read_appt"));
    apiKey.writeAppt = CapabilityKind.valueOf(row.getString("write_appt"));

    apiKey.keyHash = row.getString("key_hash");
    return apiKey;
  }
}
