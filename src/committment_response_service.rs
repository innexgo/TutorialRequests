use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use std::convert::TryInto;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CommittmentResponse {
  // select * from committment_response order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> CommittmentResponse {
    CommittmentResponse {
      committment_id: row.get("committment_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      committment_response_kind: (row.get::<_, i64>("committment_response_kind") as u8)
        .try_into()
        .unwrap(),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  committment_id: i64,
  creator_user_id: i64,
  committment_response_kind: request::CommittmentResponseKind,
) -> Result<CommittmentResponse, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let committment_id = con
    .query_one(
      "INSERT INTO
       committment_response(
           committment_id,
           creation_time,
           creator_user_id,
           committment_response_kind
       )
       VALUES($1, $2, $3, $4)
       RETURNING committment_id
      ",
      &[
        &committment_id,
        &creation_time,
        &creator_user_id,
        &(committment_response_kind.clone() as i64),
      ],
    )
    .await?
    .get(0);

  // return committment_response
  Ok(CommittmentResponse {
    committment_id,
    creation_time,
    creator_user_id,
    committment_response_kind,
  })
}

pub async fn get_by_committment_id(
  con: &mut impl GenericClient,
  committment_id: i64,
) -> Result<Option<CommittmentResponse>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM committment_response WHERE committment_id=$1",
      &[&committment_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::CommittmentResponseViewProps,
) -> Result<Vec<CommittmentResponse>, tokio_postgres::Error> {
  let sql = "
     SELECT cr.* FROM committment_response cr
     INNER JOIN committment c ON cr.committment_id = c.committment_id
     INNER JOIN session ses ON ses.session_id = c.session_id
     INNER JOIN session_data sesd ON sesd.session_id = c.session_id
     INNER JOIN (SELECT max(session_data_id) id FROM session_data GROUP BY session_id) maxids ON maxids.id = sesd.session_data_id
     WHERE 1 = 1
     AND ($1::bigint[] IS NULL OR cr.committment_id = ANY($1))
     AND ($2::bigint   IS NULL OR cr.creation_time >= $2)
     AND ($3::bigint   IS NULL OR cr.creation_time <= $3)
     AND ($4::bigint[] IS NULL OR cr.creator_user_id = ANY($4))
     AND ($5::bigint[] IS NULL OR cr.committment_response_kind = ANY($5))
     AND ($6::bigint[] IS NULL OR c.attendee_user_id = ANY($6))
     AND ($7::bigint[] IS NULL OR c.session_id = ANY($7))
     AND ($8::bigint[] IS NULL OR ses.course_id = ANY($8))
     AND ($9::bigint   IS NULL OR sesd.start_time >= $9)
     AND ($10::bigint  IS NULL OR sesd.start_time <= $10)
     AND ($11::bigint  IS NULL OR sesd.end_time >= $11)
     AND ($12::bigint  IS NULL OR sesd.end_time <= $12)
     ORDER BY cr.committment_id
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
        &props
          .committment_response_kind
          .map(|v| v.into_iter().map(|x| x as i64).collect::<Vec<i64>>()),
        &props.attendee_user_id,
        &props.session_id,
        &props.course_id,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
      ],
    )
    .await?
    .into_iter()
    .map(|x| x.into())
    .collect();
  Ok(results)
}
