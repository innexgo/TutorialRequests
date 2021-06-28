use super::db_types::*;
use super::utils::current_time_millis;
use std::convert::TryInto;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for Adminship {
  // select * from adminship order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> Adminship {
    Adminship {
      adminship_id: row.get("adminship_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      user_id: row.get("user_id"),
      school_id: row.get("school_id"),
      adminship_kind: (row.get::<_, i64>("adminship_kind") as u8).try_into().unwrap(),
      adminship_request_response_id: row.get("adminship_request_response_id"),
    }
  }
}

// TODO we need to figure out a way to make scheduled and unscheduled goals work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  user_id: i64,
  school_id: i64,
  adminship_kind: request::AdminshipKind,
  adminship_request_response_id: Option<i64>,
) -> Result<Adminship, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let adminship_id = con
    .query_one(
      "INSERT INTO
       adminship(
           creation_time,
           creator_user_id,
           user_id,
           school_id,
           adminship_kind
           adminship_request_response_id
       )
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING adminship_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &user_id,
        &school_id,
        &(adminship_kind.clone() as i64),
        &adminship_request_response_id,
      ],
    )
    .await?
    .get(0);

  // return adminship
  Ok(Adminship {
    adminship_id,
    creation_time,
    creator_user_id,
    user_id,
    school_id,
    adminship_kind,
    adminship_request_response_id,
  })
}

pub async fn get_by_adminship_id(
  con: &mut impl GenericClient,
  adminship_id: i64,
) -> Result<Option<Adminship>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM adminship WHERE adminship_id=$1",
      &[&adminship_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::AdminshipViewProps,
) -> Result<Vec<Adminship>, tokio_postgres::Error> {
  let sql = [
    "SELECT a.* FROM adminship gd",
    if props.only_recent {
      " INNER JOIN (SELECT max(adminship_id) id FROM adminship GROUP BY user_id) maxids
        ON maxids.id = a.adminship_id"
    } else {
      ""
    },
    " LEFT JOIN adminship_request_response arr ON a.adminship_request_id = arr.adminship_request_id",
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR a.adminship_id IN $1)",
    " AND ($2::bigint   IS NULL OR a.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR a.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR a.creator_user_id = $4)",
    " AND ($5::bigint   IS NULL OR a.user_id = $5)",
    " AND ($5::bigint   IS NULL OR a.school_id = $5)",
    " AND ($6::bigint   IS NULL OR a.adminship_kind = $6)",
    " AND ($7::bool     IS NULL OR arr.adminship_request_id IS NOT NULL)",
    " AND ($8::bigint   IS NULL OR arr.adminship_request_id == $8 IS TRUE)",
    " ORDER BY a.adminship_id",
    " LIMIT $9",
    " OFFSET $10",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.adminship_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.user_id,
        &props.school_id,
        &props.adminship_kind.map(|x| x as i64),
        &props.adminship_has_source,
        &props.adminship_request_id,
        &props.count.unwrap_or(100),
        &props.offset.unwrap_or(0),
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
