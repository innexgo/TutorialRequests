use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for AdminshipRequest {
  // select * from adminship_request order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> AdminshipRequest {
    AdminshipRequest {
      adminship_request_id: row.get("adminship_request_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_id: row.get("school_id"),
      message: row.get("message"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_id: i64,
  message: String,
) -> Result<AdminshipRequest, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let adminship_request_id = con
    .query_one(
      "INSERT INTO
       adminship_request(
           creation_time,
           creator_user_id,
           school_id,
           message
       )
       VALUES($1, $2, $3, $4)
       RETURNING adminship_request_id
      ",
      &[&creation_time, &creator_user_id, &school_id, &message],
    )
    .await?
    .get(0);

  // return adminship_request
  Ok(AdminshipRequest {
    adminship_request_id,
    creation_time,
    creator_user_id,
    school_id,
    message,
  })
}

pub async fn get_by_adminship_request_id(
  con: &mut impl GenericClient,
  adminship_request_id: i64,
) -> Result<Option<AdminshipRequest>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM adminship_request WHERE adminship_request_id=$1",
      &[&adminship_request_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::AdminshipRequestViewProps,
) -> Result<Vec<AdminshipRequest>, tokio_postgres::Error> {
  let sql = "SELECT ar.* FROM adminship_request ar WHERE 1 = 1
     AND ($1::bigint[] IS NULL OR ar.adminship_request_id IN $1)
     AND ($2::bigint   IS NULL OR ar.creation_time >= $2)
     AND ($3::bigint   IS NULL OR ar.creation_time <= $3)
     AND ($4::bigint   IS NULL OR ar.creator_user_id = $4)
     AND ($5::bigint   IS NULL OR ar.school_id = $5)
     AND ($6::text     IS NULL OR ar.message = $6)
     AND ($7::text     IS NULL OR ar.message LIKE CONCAT('%',$7,'%'))
     ORDER BY ar.adminship_request_id
     LIMIT $8
     OFFSET $9
     ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.adminship_request_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_id,
        &props.message,
        &props.partial_message,
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
