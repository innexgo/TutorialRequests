use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;
use std::convert::TryInto;

impl From<tokio_postgres::row::Row> for Subscription {
  // select * from subscription order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> Subscription {
    Subscription {
      subscription_id: row.get("creator_user_id"),
      creation_time: row.get("creator_user_id"),
      creator_user_id: row.get("creator_user_id"),
      subscription_kind: (row.get::<_, i64>("subscription_kind") as u8).try_into().unwrap(),
      payment_id: row.get("payment_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  subscription_kind: request::SubscriptionKind,
  payment_id: i64,
) -> Result<Subscription, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let subscription_id = con
    .query_one(
      "INSERT INTO
       subscription(
           creation_time,
           creator_user_id,
           subscription_kind,
           payment_id
       )
       VALUES($1, $2, $3, $4)
       RETURNING subscription_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &(subscription_kind.clone() as i64),
        &payment_id,
      ],
    )
    .await?
    .get(0);

  // return subscription
  Ok(Subscription {
    subscription_id,
    creation_time,
    creator_user_id,
    subscription_kind,
    payment_id,
  })
}

pub async fn get_by_subscription_id(
  con: &mut impl GenericClient,
  subscription_id: i64,
) -> Result<Option<Subscription>, tokio_postgres::Error> {
  let sql = "SELECT * FROM subscription WHERE subscription_id=$1";
  let result = con
    .query_opt(sql, &[&subscription_id])
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::SubscriptionViewProps,
) -> Result<Vec<Subscription>, tokio_postgres::Error> {
  let sql = "SELECT s.* FROM subscription s WHERE 1 = 1
     AND ($1::bigint IS NULL OR s.subscription_id = $1)
     AND ($2::bigint IS NULL OR s.creation_time >= $2)
     AND ($3::bigint IS NULL OR s.creation_time <= $3)
     AND ($4::bigint IS NULL OR s.creator_user_id = $4)
     ORDER BY s.subscription_id
     LIMIT $5
     OFFSET $6
     ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.subscription_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.count.unwrap_or(100),
        &props.offset.unwrap_or(0),
      ],
    )
    .await?
    .into_iter()
    .map(|x| x.into())
    .collect();

  Ok(results)
}
