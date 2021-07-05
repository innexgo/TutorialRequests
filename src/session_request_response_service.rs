use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SessionRequestResponse {
  // select * from session_request_response order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> SessionRequestResponse {
    SessionRequestResponse {
      session_request_id: row.get("session_request_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      message: row.get("message"),
      committment_id: row.get("committment_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  session_request_id: i64,
  creator_user_id: i64,
  message: String,
  committment_id: Option<i64>,
) -> Result<SessionRequestResponse, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let session_request_id = con
    .query_one(
      "INSERT INTO
       session_request_response(
           session_request_id,
           creation_time,
           creator_user_id,
           message,
           committment_id
       )
       VALUES($1, $2, $3, $4, $5)
       RETURNING session_request_id
      ",
      &[&session_request_id, &creation_time, &creator_user_id, &message, &committment_id],
    )
    .await?
    .get(0);

  // return session_request_response
  Ok(SessionRequestResponse {
    session_request_id,
    creation_time,
    creator_user_id,
    message,
    committment_id,
  })
}

pub async fn get_by_session_request_id(
  con: &mut impl GenericClient,
  session_request_id: i64,
) -> Result<Option<SessionRequestResponse>, tokio_postgres::Error> {
  let result = con
    .query_opt("SELECT * FROM session_request_response WHERE session_request_id=$1", &[&session_request_id])
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::SessionRequestResponseViewProps,
) -> Result<Vec<SessionRequestResponse>, tokio_postgres::Error> {
  let sql = "
     SELECT srr.* FROM session_request_response srr
     INNER JOIN session_request sr ON srr.session_request_id = sr.session_request_id
     LEFT JOIN committment c ON srr.committment_id = c.committment_id
     WHERE 1 = 1
     AND ($1::bigint[]  IS NULL OR srr.session_request_id IN $1)
     AND ($2::bigint    IS NULL OR srr.creation_time >= $2)
     AND ($3::bigint    IS NULL OR srr.creation_time <= $3)
     AND ($4::bigint[]  IS NULL OR srr.creator_user_id IN $4)
     AND ($5::text[]    IS NULL OR srr.message IN $5)
     AND ($6::text      IS NULL OR srr.message LIKE CONCAT('%',$6,'%'))
     AND ($7::bool      IS NULL OR srr.committment_id IS NOT NULL = $7)
     AND ($8::bigint[]  IS NULL OR srr.committment_id IN $8)
     AND ($9::bigint[]  IS NULL OR sr.attendee_user_id IN $9)
     AND ($10::bigint[] IS NULL OR sr.course_id IN $10)
     AND ($11::bigint   IS NULL OR sr.start_time >= $11)
     AND ($12::bigint   IS NULL OR sr.start_time <= $12)
     AND ($13::bigint   IS NULL OR sr.end_time >= $13)
     AND ($14::bigint   IS NULL OR sr.end_time <= $14)
     AND ($15::bigint[] IS NULL OR c.session_id IN $15)
     ORDER BY srr.session_request_id
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
        &props.message,
        &props.partial_message,
        &props.accepted,
        &props.committment_id,
        &props.attendee_user_id,
        &props.course_id,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
        &props.session_id,
      ],
    )
    .await?
    .into_iter()
    .map(|x| x.into())
    .collect();
  Ok(results)
}
