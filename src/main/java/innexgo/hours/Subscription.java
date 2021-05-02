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

public class Subscription {
  public long subscriptionId;
  public long creationTime;
  long creatorUserId;
  public SubscriptionKind subscriptionKind;
  public long maxUses;
  public long paymentId;

  // for jackson
  public User creator;
}

enum SubscriptionKind {
  VALID(0), CANCEL(1);

  final int value;

  private SubscriptionKind(int value) {
    this.value = value;
  }

  public static SubscriptionKind from(int i) {
    for (SubscriptionKind subscriptionKind : SubscriptionKind.values()) {
      if (subscriptionKind.value == i) {
        return subscriptionKind;
      }
    }
    return null;
  }

  public static boolean contains(String str) {
    for (SubscriptionKind subscriptionKind : SubscriptionKind.values()) {
      if (subscriptionKind.name().equals(str)) {
        return true;
      }
    }
    return false;
  }
}
