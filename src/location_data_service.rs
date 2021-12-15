use super::db_types::*;
use super::utils::current_time_millis;
use std::convert::From;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for LocationData {
  // select * from location_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> LocationData {
    LocationData {
      location_data_id: row.get("location_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      location_id: row.get("location_id"),
      name: row.get("name"),
      address: row.get("address"),
      phone: row.get("phone"),
      active: row.get("active"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  location_id: i64,
  name: String,
  address: String,
  phone: String,
  active: bool,
) -> Result<LocationData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let location_data_id = con
    .query_one(
      "INSERT INTO
       location_data_t(
           creation_time,
           creator_user_id,
           location_id,
           name,
           address,
           phone,
           active
       )
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING location_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &location_id,
        &name,
        &address,
        &phone,
        &active,
      ],
    )
    .await?
    .get(0);

  Ok(LocationData {
    location_data_id,
    creation_time,
    creator_user_id,
    location_id,
    name,
    address,
    phone,
    active,
  })
}

#[allow(unused)]
pub async fn get_by_location_data_id(
  con: &mut impl GenericClient,
  location_data_id: &i64,
) -> Result<Option<LocationData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM location_data_t WHERE location_data_id=$1",
      &[&location_data_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn get_by_location_id(
  con: &mut impl GenericClient,
  location_id: i64,
) -> Result<Option<LocationData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "
      SELECT sd.* FROM recent_location_data_v sd
      WHERE sd.location_id = $1
      ",
      &[&location_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn is_active_by_location_id(
  con: &mut impl GenericClient,
  location_id: i64,
) -> Result<bool, tokio_postgres::Error> {
  let result = matches!(
    get_by_location_id(con, location_id).await?,
    Some(LocationData { active: true, .. })
  );

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::LocationDataViewProps,
) -> Result<Vec<LocationData>, tokio_postgres::Error> {
  let sql = [
    if props.only_recent {
      "SELECT sd.* FROM recent_location_data_v sd"
    } else {
      "SELECT sd.* FROM location_data_t sd"
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR sd.location_data_id = ANY($1))",
    " AND ($2::bigint   IS NULL OR sd.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR sd.creation_time <= $3)",
    " AND ($4::bigint[] IS NULL OR sd.creator_user_id = ANY($4))",
    " AND ($5::bigint[] IS NULL OR sd.location_id = ANY($5))",
    " AND ($6::text[]   IS NULL OR sd.name = ANY($6))",
    " AND ($7::text     IS NULL OR sd.name LIKE CONCAT('%',$7,'%'))",
    " AND ($8::text[]   IS NULL OR sd.address = ANY($8))",
    " AND ($9::text     IS NULL OR sd.address LIKE CONCAT('%',$9,'%'))",
    " AND ($10::text[]  IS NULL OR sd.phone = ANY($10))",
    " AND ($11::bool    IS NULL OR sd.active = $11)",
    " ORDER BY sd.location_data_id",
  ]
  .join("\n");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.location_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.location_id,
        &props.name,
        &props.partial_name,
        &props.address,
        &props.partial_address,
        &props.phone,
        &props.active,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
