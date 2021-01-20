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

public class SubscriptionRowMapper implements RowMapper<Subscription> {

  @Override
  public Subscription mapRow(ResultSet row, int rowNum) throws SQLException {
    Subscription subscription = new Subscription();
    subscription.subscriptionId = row.getLong("subscription_id");
    subscription.creationTime = row.getLong("creation_time");
    subscription.creatorUserId = row.getLong("creator_user_id");
    subscription.duration = row.getLong("duration");
    subscription.maxUses = row.getLong("max_uses");
    return subscription;
  }
}
