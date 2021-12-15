use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;
use innexgo_hours_api::request;

impl From<tokio_postgres::row::Row> for Location {
  // select * from location order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> Location {
    Location {
      location_id: row.get("location_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_id: row.get("school_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_id: i64,
) -> Result<Location, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let location_id = con
    .query_one(
      "INSERT INTO
       location_t(
           creation_time,
           creator_user_id,
           school_id
       )
       VALUES($1, $2, $3)
       RETURNING location_id
      ",
      &[&creation_time, &creator_user_id, &school_id],
    ).await?
    .get(0);

  // return location
  Ok(Location {
    location_id,
    creation_time,
    creator_user_id,
    school_id
  })
}

pub async fn get_by_location_id(
  con: &mut impl GenericClient,
  location_id: i64,
) -> Result<Option<Location>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM location_t WHERE location_id=$1",
      &[&location_id],
    ).await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::LocationViewProps,
) -> Result<Vec<Location>, tokio_postgres::Error> {
  let results = con
    .query(
      "
        SELECT lc.* FROM location_t lc WHERE 1 = 1
        AND ($1::bigint[] IS NULL OR lc.location_id = ANY($1))
        AND ($2::bigint   IS NULL OR lc.creation_time >= $2)
        AND ($3::bigint   IS NULL OR lc.creation_time <= $3)
        AND ($4::bigint[] IS NULL OR lc.creator_user_id = ANY($4))
        AND ($5::bigint[] IS NULL OR lc.school_id = ANY($5))
        ORDER BY lc.location_id
      ",
      &[
        &props.location_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_id,
      ],
    ).await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
