
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
public class AdminshipRequestService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public AdminshipRequest getByAdminshipRequestId(long adminshipRequestId) {
    String sql =
        "SELECT * FROM adminship_request WHERE adminship_request_id=?";
    RowMapper<AdminshipRequest> rowMapper = new AdminshipRequestRowMapper();
    AdminshipRequest apptRequest = jdbcTemplate.queryForObject(sql, rowMapper, adminshipRequestId);
    return apptRequest;
  }

  public long nextId() {
    String sql = "SELECT max(adminship_request_id) FROM adminship_request";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(AdminshipRequest apptRequest) {
    apptRequest.adminshipRequestId = nextId();
    apptRequest.creationTime = System.currentTimeMillis();
    // Add apptRequest
    String sql =
        "INSERT INTO adminship_request values (?, ?, ?, ?, ?)";
    jdbcTemplate.update(
        sql,
        apptRequest.adminshipRequestId,
        apptRequest.creationTime,
        apptRequest.creatorUserId,
        apptRequest.schoolId,
        apptRequest.message
    );
  }

  public boolean existsByAdminshipRequestId(long adminshipRequestId) {
    String sql = "SELECT count(*) FROM adminship_request sr WHERE sr.adminship_request_id=?";
    int count = jdbcTemplate.queryForObject(sql, Integer.class, adminshipRequestId);
    return count != 0;
  }

  // Restrict apptRequests by
  public Stream<AdminshipRequest> query(
      Long adminshipRequestId,
      Long creationTime,
      Long minCreationTime,
      Long maxCreationTime,
      Long creatorUserId,
      Long schoolId,
      String message,
      Boolean responded,
      long offset,
      long count) {
    String sql =
        "SELECT sr.* FROM adminship_request sr"
            + (responded       == null ? "" : " LEFT JOIN adminship_request_response srr ON srr.adminship_request_id = sr.adminship_request_id")
            + " WHERE 1=1 "
            + (responded       == null ? "" : " AND srr.adminship_request_id IS" + (responded ? " NOT NULL" : " NULL"))
            + (adminshipRequestId== null ? "" : " AND sr.adminship_request_id = " + adminshipRequestId)
            + (creatorUserId   == null ? "" : " AND sr.creator_user_id = " + creatorUserId)
            + (creationTime    == null ? "" : " AND sr.creation_time = " + creationTime)
            + (minCreationTime == null ? "" : " AND sr.creation_time > " + minCreationTime)
            + (maxCreationTime == null ? "" : " AND sr.creation_time < " + maxCreationTime)
            + (schoolId        == null ? "" : " AND sr.school_id = " + schoolId)
            + (message         == null ? "" : " AND sr.message = " + Utils.escape(message))
            + (" ORDER BY sr.adminship_request_id")
            + (" LIMIT " + offset + ", " + count)
            + ";";

    RowMapper<AdminshipRequest> rowMapper = new AdminshipRequestRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }
}
