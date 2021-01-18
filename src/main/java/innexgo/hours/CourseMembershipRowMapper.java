
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

public class CourseMembershipRowMapper implements RowMapper<CourseMembership> {

  @Override
  public CourseMembership mapRow(ResultSet row, int rowNum) throws SQLException {
    CourseMembership courseMembership = new CourseMembership();
    courseMembership.courseMembershipId = row.getLong("course_membership_id");
    courseMembership.creationTime = row.getLong("creation_time");
    courseMembership.creatorUserId = row.getLong("creator_user_id");
    courseMembership.userId = row.getLong("user_id");
    courseMembership.courseId = row.getLong("course_id");
    courseMembership.courseMembershipKind = CourseMembershipKind.from(row.getInt("course_membership_kind"));
    courseMembership.courseMembershipSourceKind = CourseMembershipSourceKind.from(row.getInt("course_membership_source_kind"));
    courseMembership.courseKeyId = row.getLong("course_key_id");
    return courseMembership;
  }
}
