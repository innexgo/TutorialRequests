use super::db_types::*;
use super::utils::current_time_millis;
use std::convert::From;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SessionData {
  // select * from session_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> SessionData {
    SessionData {
      session_data_id: row.get("session_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      session_id: row.get("session_id"),
      name: row.get("name"),
      start_time: row.get("start_time"),
      end_time: row.get("end_time"),
      active: row.get("active"),
    }
  }
}

// TODO we nsd to figure out a way to make scheduled and unscheduled goals work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  session_id: i64,
  name: String,
  start_time: i64,
  end_time: i64,
  active: bool,
) -> Result<SessionData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let session_data_id = con
    .query_one(
      "INSERT INTO
       session_data(
           creation_time,
           creator_user_id,
           session_id,
           name,
           start_time,
           end_time,
           active
       )
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING session_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &session_id,
        &name,
        &start_time,
        &end_time,
        &active,
      ],
    )
    .await?
    .get(0);

  Ok(SessionData {
    session_data_id,
    creation_time,
    creator_user_id,
    session_id,
    name,
    start_time,
    end_time,
    active,
  })
}

#[allow(unused)]
pub async fn get_by_session_data_id(
  con: &mut impl GenericClient,
  session_data_id: &i64,
) -> Result<Option<SessionData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM session_data WHERE session_data_id=$1",
      &[&session_data_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::SessionDataViewProps,
) -> Result<Vec<SessionData>, tokio_postgres::Error> {

  let sql = [
    "SELECT sesd.* FROM session_data sesd",
    if props.only_recent {
      " INNER JOIN
          (SELECT max(session_data_id) id FROM session_data GROUP BY session_id) maxids
          ON maxids.id = sesd.session_data_id"
    } else {
      ""
    },
    " INNER JOIN session ses ON sesd.session_id = ses.session_id",
    " WHERE 1 = 1",
    " AND ($1::bigint[]  IS NULL OR sesd.session_data_id IN $1)",
    " AND ($2::bigint    IS NULL OR sesd.creation_time >= $2)",
    " AND ($3::bigint    IS NULL OR sesd.creation_time <= $3)",
    " AND ($4::bigint[]  IS NULL OR sesd.creator_user_id IN $4)",
    " AND ($5::bigint[]  IS NULL OR sesd.session_id IN $5)",
    " AND ($6::text[]    IS NULL OR sesd.name IN $6)",
    " AND ($7::text      IS NULL OR sesd.name LIKE CONCAT('%',$7,'%'))",
    " AND ($8::bigint    IS NULL OR sesd.start_time >= $8)",
    " AND ($9::bigint    IS NULL OR sesd.start_time <= $9)",
    " AND ($10::bigint   IS NULL OR sesd.end_time >= $10)",
    " AND ($11::bigint   IS NULL OR sesd.end_time <= $11)",
    " AND ($12::bool     IS NULL OR sesd.active = $12)",
    " AND ($13::bigint[] IS NULL OR ses.course_id IN $13)",
    " ORDER BY sesd.session_data_id",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.session_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.session_id,
        &props.name,
        &props.partial_name,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
        &props.active,
        &props.course_id,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
