
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
public class AdminshipRequestResponseService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public AdminshipRequestResponse getByAdminshipRequestId(long adminshipRequestId) {
    String sql = "SELECT * FROM adminship_request_response WHERE adminship_request_id=?";
    RowMapper<AdminshipRequestResponse> rowMapper = new AdminshipRequestResponseRowMapper();
    AdminshipRequestResponse adminshipRequestResponse = jdbcTemplate.queryForObject(sql, rowMapper, adminshipRequestId);
    return adminshipRequestResponse;
  }

  public void add(AdminshipRequestResponse adminshipRequestResponse) {
    String sql =
        "INSERT INTO adminship_request_response values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        adminshipRequestResponse.adminshipRequestId,
        adminshipRequestResponse.creatorUserId,
        adminshipRequestResponse.creationTime,
        adminshipRequestResponse.message,
        adminshipRequestResponse.accepted);
  }

  public boolean existsByAdminshipRequestId(long adminshipRequestId) {
    String sql = "SELECT count(*) FROM adminship_request_response WHERE adminship_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, adminshipRequestId);
    return count != 0;
  }

  // Restrict appts by
  public Stream<AdminshipRequestResponse> query(
      Long adminshipRequestId, //
      Long creationTime, //
      Long minCreationTime, //
      Long maxCreationTime, //
      Long creatorUserId, //
      String message, //
      Boolean accepted, //
      Boolean responded, //
      Long requesterUserId, //
      Long schoolId, //
      long offset,
      long count) {

    boolean nojoinar = requesterUserId == null && schoolId == null;

    String sql =
        "SELECT arr.* FROM adminship_request_response arr"
            + (nojoinar ? "" : " JOIN adminship_request ar ON ar.adminship_request_id = arr.adminship_request_id")
            + (responded == null ? "" : " LEFT JOIN adminship a ON a.adminship_kind =" + AdminshipSourceKind.REQUEST.value + " AND a.adminship_request_response_id = arr.adminship_request_id")
            + " WHERE 1=1 "
            + (adminshipRequestId == null ? "" : " AND arr.adminship_request_id = " + adminshipRequestId)
            + (creatorUserId      == null ? "" : " AND arr.creator_user_id = " + creatorUserId)
            + (creationTime       == null ? "" : " AND arr.creation_time = " + creationTime)
            + (minCreationTime    == null ? "" : " AND arr.creation_time > " + minCreationTime)
            + (maxCreationTime    == null ? "" : " AND arr.creation_time < " + maxCreationTime)
            + (message            == null ? "" : " AND arr.message = " + Utils.escape(message))
            + (accepted           == null ? "" : " AND arr.accepted = " + accepted)
            + (responded          == null ? "" : " AND a.adminship_id IS" + (responded ? " NOT NULL" : " NULL")) //
            + (requesterUserId    == null ? "" : " AND ar.creator_user_id = " + requesterUserId)
            + (schoolId           == null ? "" : " AND ar.school_id = " + schoolId)
            + (" ORDER BY arr.adminship_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<AdminshipRequestResponse> rowMapper = new AdminshipRequestResponseRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}

