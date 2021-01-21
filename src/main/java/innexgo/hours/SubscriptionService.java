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
public class SubscriptionService {

  @Autowired private JdbcTemplate jdbcTemplate;

  public Subscription getBySubscriptionId(long subscriptionId) {
    String sql =
        "SELECT * FROM subscription WHERE subscription_id=?";
    RowMapper<Subscription> rowMapper = new SubscriptionRowMapper();
    Subscription subscription = jdbcTemplate.queryForObject(sql, rowMapper, subscriptionId);
    return subscription;
  }

  public long nextId() {
    String sql = "SELECT max(subscription_id) FROM subscription";
    Long maxId = jdbcTemplate.queryForObject(sql, Long.class);
    if(maxId == null) {
      return 0;
    } else {
      return maxId + 1;
    }
  }

  public void add(Subscription subscription) {
    subscription.subscriptionId = nextId();
    subscription.creationTime = System.currentTimeMillis();
    // Add subscription
    String sql = "INSERT INTO subscription values (?, ?, ?, ?, ?)";
    jdbcTemplate.update( //
        sql, //
        subscription.subscriptionId, //
        subscription.creationTime, //
        subscription.creatorUserId, //
        subscription.subscriptionKind.value, //
        subscription.maxUses //
    ); //
  }

  public boolean existsBySubscriptionId(long subscriptionId) {
    String sql = "SELECT count(*) FROM subscription WHERE subscription_id=?";
    long count = jdbcTemplate.queryForObject(sql, Long.class, subscriptionId);
    return count != 0;
  }

  public Stream<Subscription> query( //
     Long subscriptionId, //
     Long creationTime, //
     Long minCreationTime, //
     Long maxCreationTime, //
     Long creatorUserId, //
     SubscriptionKind subscriptionKind, //
     Long maxUses, //
     boolean onlyRecent, //
     long offset, //
     long count) //
 {

    String sql=
      "SELECT s.* FROM subscription s"
        + (!onlyRecent ? "" : " INNER JOIN (SELECT max(subscription_id) id FROM subscription GROUP BY user_id) maxids ON maxids.id = s.subscription_id")
        + " WHERE 1=1 "
        + (subscriptionId  == null ? "" : " AND s.subscription_id = " + subscriptionId)
        + (creatorUserId   == null ? "" : " AND s.creator_user_id = " + creatorUserId)
        + (creationTime    == null ? "" : " AND s.creation_time = " + creationTime)
        + (minCreationTime == null ? "" : " AND s.creation_time > " + minCreationTime)
        + (maxCreationTime == null ? "" : " AND s.creation_time < " + maxCreationTime)
        + (subscriptionKind == null ? "" : " AND s.subscription_kind = " + subscriptionKind.value)
        + (maxUses         == null ? "" : " AND s.max_uses = " + maxUses)
        + (" ORDER BY s.subscription_id")
        + (" LIMIT " + offset + ", " + count)
        + ";";

    RowMapper<Subscription> rowMapper = new SubscriptionRowMapper();
    return this.jdbcTemplate.query(sql, rowMapper).stream();
  }


  public Subscription getSubscriptionByUserId(long userId) {
   return query( //
     null, //Long subscriptionId, //
     null, //Long creationTime, //
     null, //Long minCreationTime, //
     null, //Long maxCreationTime, //
     userId, //Long creatorUserId, //
     null, //SubscriptionKind subscriptionKind, //
     null, //Long maxUses, //
     true,
     0, //long offset, //
     Integer.MAX_VALUE //long count) //
   ).findFirst().orElse(null);
  }

  public boolean isSubscriber(long userId) {
   Subscription s = getSubscriptionByUserId(userId);
   return s != null && s.subscriptionKind != SubscriptionKind.CANCEL;
  }
}
