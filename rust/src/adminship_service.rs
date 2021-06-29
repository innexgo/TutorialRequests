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
      school_key_key: row.get("school_key_key"),
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
  school_key_key: Option<String>,
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
           school_key_key
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
        &school_key_key,
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
    school_key_key,
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
    " LEFT JOIN school_key sk ON a.school_key_key = sk.school_key_key",
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR a.adminship_id IN $1)",
    " AND ($2::bigint   IS NULL OR a.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR a.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR a.creator_user_id = $4)",
    " AND ($5::bigint   IS NULL OR a.user_id = $5)",
    " AND ($6::bigint   IS NULL OR a.school_id = $6)",
    " AND ($7::bigint   IS NULL OR a.adminship_kind = $7)",
    " AND ($8::bool     IS NULL OR sk.adminship_request_id IS NOT NULL = $8)",
    " AND ($9::text     IS NULL OR sk.school_key_key == $9 IS TRUE)",
    " ORDER BY a.adminship_id",
    " LIMIT $10",
    " OFFSET $11",
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
        &props.school_key_key,
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
