use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SessionRequest {
  // select * from session_request order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> SessionRequest {
    SessionRequest {
      session_request_id: row.get("session_request_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      course_id: row.get("course_id"),
      message: row.get("message"),
      start_time: row.get("start_time"),
      end_time: row.get("end_time"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  course_id: i64,
  message: String,
  start_time: i64,
  end_time: i64,
) -> Result<SessionRequest, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let session_request_id = con
    .query_one(
      "INSERT INTO
       session_request(
           creation_time,
           creator_user_id,
           course_id,
           message,
           start_time,
           end_time
       )
       VALUES($1, $2, $3, $4, $5, $6)
       RETURNING session_request_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &course_id,
        &message,
        &start_time,
        &end_time,
      ],
    )
    .await?
    .get(0);

  // return session_request
  Ok(SessionRequest {
    session_request_id,
    creation_time,
    creator_user_id,
    course_id,
    message,
    start_time,
    end_time,
  })
}

pub async fn get_by_session_request_id(
  con: &mut impl GenericClient,
  session_request_id: i64,
) -> Result<Option<SessionRequest>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM session_request WHERE session_request_id=$1",
      &[&session_request_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::SessionRequestViewProps,
) -> Result<Vec<SessionRequest>, tokio_postgres::Error> {
  let sql = "
     SELECT sr.* FROM session_request sr
     LEFT JOIN session_request_response srr ON srr.session_request_id = sr.session_request_id
     WHERE 1 = 1
     AND ($1::bigint[] IS NULL OR sr.session_request_id IN $1)
     AND ($2::bigint   IS NULL OR sr.creation_time >= $2)
     AND ($3::bigint   IS NULL OR sr.creation_time <= $3)
     AND ($4::bigint   IS NULL OR sr.creator_user_id = $4)
     AND ($5::bigint   IS NULL OR sr.course_id = $5)
     AND ($6::text     IS NULL OR sr.message = $6)
     AND ($7::text     IS NULL OR sr.message LIKE CONCAT('%',$7,'%'))
     AND ($8::bool     IS NULL OR srr.session_request_id IS NOT NULL = $8)
     ORDER BY sr.session_request_id
     LIMIT $9
     OFFSET $10
     ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.session_request_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.course_id,
        &props.message,
        &props.partial_message,
        &props.responded,
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
