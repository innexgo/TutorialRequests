use super::db_types::*;
use super::utils::current_time_millis;
use std::convert::From;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SchoolDurationData {
  // select * from school_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> SchoolDurationData {
    SchoolDurationData {
      school_duration_data_id: row.get("school_duration_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_duration_id: row.get("school_duration_id"),
      day: row.get("day"),
      minute_start: row.get("minute_start"),
      minute_end: row.get("minute_end"),
      active: row.get("active"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_duration_id: i64,
  day: i64,
  minute_start: i64,
  minute_end: i64,
  active: bool,
) -> Result<SchoolDurationData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let school_duration_data_id = con
    .query_one(
      "INSERT INTO
       school_duration_data_t(
           creation_time,
           creator_user_id,
           school_duration_id,
           day,
           minute_start,
           minute_end,
           active
       )
       VALUES ($1, $2, $3, $4, $5, $6, $7)
       RETURNING school_duration_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &school_duration_id,
        &day,
        &minute_start,
        &minute_end,
        &active,
      ],
    )
    .await?
    .get(0);

  Ok(SchoolDurationData {
    school_duration_data_id,
    creation_time,
    creator_user_id,
    school_duration_id,
    day,
    minute_start,
    minute_end,
    active,
  })
}

#[allow(unused)]
pub async fn get_by_school_duration_data_id(
  con: &mut impl GenericClient,
  school_duration_data_id: &i64,
) -> Result<Option<SchoolDurationData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM school_duration_data_t WHERE school_duration_data_id=$1",
      &[&school_duration_data_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn get_by_school_duration_id(
  con: &mut impl GenericClient,
  school_duration_id: i64,
) -> Result<Option<SchoolDurationData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "
      SELECT sdd.* FROM recent_school_duration_data_v sdd
      WHERE sdd.school_duration_id = $1
      ",
      &[&school_duration_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}


pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::SchoolDurationDataViewProps,
) -> Result<Vec<SchoolDurationData>, tokio_postgres::Error> {
  let sql = [
    if props.only_recent {
      "SELECT sd.* FROM recent_school_duration_data_v sd"
    } else {
      "SELECT sd.* FROM school_duration_data sd"
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR sd.school_duration_data_id = ANY($1))",
    " AND ($2::bigint   IS NULL OR sd.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR sd.creation_time <= $3)",
    " AND ($4::bigint[] IS NULL OR sd.creator_user_id = ANY($4))",
    " AND ($5::bigint[] IS NULL OR sd.school_duration_id = ANY($5))",
    " AND ($6::bigint   IS NULL OR sd.day = ANY($6))",
    " AND ($7::bigint   IS NULL OR sd.minute_start = $7)",
    " AND ($8::bigint   IS NULL OR sd.minute_start = $8)",
    " AND ($9::bigint   IS NULL OR sd.minute_end = $9))",
    " AND ($10::bigint  IS NULL OR sd.minute_end = $10)",
    " AND ($11::bool    IS NULL OR sd.active = $11)",
    " ORDER BY sd.school_duration_data_id",
  ]
  .join("\n");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.school_duration_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_duration_id,
        &props.day,
        &props.min_minute_start,
        &props.max_minute_start,
        &props.min_minute_end,
        &props.max_minute_end,
        &props.active,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
