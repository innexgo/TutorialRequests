use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for Committment {
  // select * from committment order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> Committment {
    Committment {
      committment_id: row.get("committment_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      attendee_user_id: row.get("attendee_user_id"),
      session_id: row.get("session_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  attendee_user_id: i64,
  session_id: i64,
) -> Result<Committment, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let committment_id = con
    .query_one(
      "INSERT INTO
       committment(
           creation_time,
           creator_user_id,
           attendee_user_id,
           session_id
       )
       VALUES($1, $2, $3)
       RETURNING committment_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &attendee_user_id,
        &session_id,
      ],
    )
    .await?
    .get(0);

  // return committment
  Ok(Committment {
    committment_id,
    creation_time,
    creator_user_id,
    attendee_user_id,
    session_id,
  })
}

pub async fn get_by_committment_id(
  con: &mut impl GenericClient,
  committment_id: i64,
) -> Result<Option<Committment>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM committment WHERE committment_id=$1",
      &[&committment_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn get_by_attendee_user_id_session_id(
  con: &mut impl GenericClient,
  attendee_user_id: i64,
  session_id: i64,
) -> Result<Option<Committment>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM committment WHERE attendee_user_id=$1 AND session_id=$2",
      &[&attendee_user_id, &session_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::CommittmentViewProps,
) -> Result<Vec<Committment>, tokio_postgres::Error> {
  let sql = "
     SELECT c.* FROM committment c
     INNER JOIN session ses ON ses.session_id = c.session_id
     INNER JOIN session_data sesd ON sesd.session_id = c.session_id
     INNER JOIN (SELECT max(session_data_id) id FROM session_data GROUP BY session_id) maxids ON maxids.id = sesd.session_data_id
     LEFT JOIN committment_response cr ON cr.committment_id = c.committment_id
     LEFT JOIN session_request_response srr ON srr.accepted AND srr.committment_id = c.committment_id
     WHERE 1 = 1
     AND ($1::bigint[] IS NULL OR c.committment_id IN $1)
     AND ($2::bigint   IS NULL OR c.creation_time >= $2)
     AND ($3::bigint   IS NULL OR c.creation_time <= $3)
     AND ($4::bigint   IS NULL OR c.creator_user_id = $4)
     AND ($5::bigint   IS NULL OR c.attendee_user_id = $5)
     AND ($6::bigint   IS NULL OR c.session_id = $6)
     AND ($7::bigint   IS NULL OR ses.course_id = $7)
     AND ($8::bigint   IS NULL OR sesd.start_time >= $8)
     AND ($9::bigint   IS NULL OR sesd.start_time <= $9)
     AND ($10::bigint  IS NULL OR sesd.end_time >= $10)
     AND ($11::bigint  IS NULL OR sesd.end_time <= $11)
     AND ($12::bool    IS NULL OR cr.committment_id IS NOT NULL = $12)
     AND ($13::bool    IS NULL OR srr.committment_id IS NOT NULL = $13)
     ORDER BY c.committment_id
     LIMIT $14
     OFFSET $15
     ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.committment_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.attendee_user_id,
        &props.session_id,
        &props.course_id,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
        &props.responded,
        &props.from_request_response,
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
