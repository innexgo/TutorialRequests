use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;
use innexgo_hours_api::request;

impl From<tokio_postgres::row::Row> for Stay {
  // select * from stay order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> Stay {
    Stay {
      stay_id: row.get("stay_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      attendee_user_id: row.get("attendee_user_id"),
      location_id: row.get("location_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  attendee_user_id: i64,
  location_id: i64,
) -> Result<Stay, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let stay_id = con
    .query_one(
      "INSERT INTO
       stay_t(
           creation_time,
           creator_user_id,
           attendee_user_id,
           location_id
       )
       VALUES($1, $2, $3, $4)
       RETURNING stay_id
      ",
      &[&creation_time, &creator_user_id, &attendee_user_id, &location_id],
    ).await?
    .get(0);

  // return stay
  Ok(Stay {
    stay_id,
    creation_time,
    creator_user_id,
    attendee_user_id,
    location_id
  })
}

pub async fn get_by_stay_id(
  con: &mut impl GenericClient,
  stay_id: i64,
) -> Result<Option<Stay>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM stay_t WHERE stay_id=$1",
      &[&stay_id],
    ).await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::StayViewProps,
) -> Result<Vec<Stay>, tokio_postgres::Error> {
  let results = con
    .query(
      "
        SELECT sy.* FROM stay_t sy WHERE 1 = 1
        AND ($1::bigint[] IS NULL OR sy.stay_id = ANY($1))
        AND ($2::bigint   IS NULL OR sy.creation_time >= $2)
        AND ($3::bigint   IS NULL OR sy.creation_time <= $3)
        AND ($4::bigint[] IS NULL OR sy.creator_user_id = ANY($4))
        AND ($5::bigint[] IS NULL OR sy.attendee_user_id = ANY($5))
        AND ($6::bigint[] IS NULL OR sy.location_id = ANY($6))
        ORDER BY sy.stay_id
      ",
      &[
        &props.stay_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.attendee_user_id,
        &props.location_id,
      ],
    ).await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
