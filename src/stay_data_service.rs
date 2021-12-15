use super::db_types::*;
use super::utils::current_time_millis;
use either::*;
use std::convert::From;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for StayData {
  // select * from stay_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> StayData {
    StayData {
      stay_data_id: row.get("stay_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      stay_id: row.get("stay_id"),
      fst: match row.get("fst_encounter_id") {
        Some(encounter_id) => Left(encounter_id),
        None => Right(row.get("fst_time")),
      },
      snd: match row.get("snd_encounter_id") {
        Some(encounter_id) => Left(encounter_id),
        None => Right(row.get("snd_time")),
      },
      active: row.get("active"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  stay_id: i64,
  fst: Either<i64, i64>,
  snd: Either<i64, i64>,
  active: bool,
) -> Result<StayData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let stay_data_id = con
    .query_one(
      "INSERT INTO
       stay_data_t(
           creation_time,
           creator_user_id,
           stay_id,
           fst_encounter_id,
           fst_time,
           snd_encounter_id,
           snd_time,
           active
       )
       VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
       RETURNING stay_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &stay_id,
        &fst.left(),
        &fst.right(),
        &snd.left(),
        &snd.right(),
        &active,
      ],
    )
    .await?
    .get(0);

  Ok(StayData {
    stay_data_id,
    creation_time,
    creator_user_id,
    stay_id,
    fst,
    snd,
    active,
  })
}

#[allow(unused)]
pub async fn get_by_stay_data_id(
  con: &mut impl GenericClient,
  stay_data_id: &i64,
) -> Result<Option<StayData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM stay_data_t WHERE stay_data_id=$1",
      &[&stay_data_id],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn get_by_stay_id(
  con: &mut impl GenericClient,
  stay_id: i64,
) -> Result<Option<StayData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "
      SELECT syd.* FROM recent_stay_data_v syd
      WHERE syd.stay_id = $1
      ",
      &[&stay_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn is_active_by_stay_id(
  con: &mut impl GenericClient,
  stay_id: i64,
) -> Result<bool, tokio_postgres::Error> {
  let result = matches!(
    get_by_stay_id(con, stay_id).await?,
    Some(StayData { active: true, .. })
  );

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::StayDataViewProps,
) -> Result<Vec<StayData>, tokio_postgres::Error> {
  let sql = [
    if props.only_recent {
      "SELECT syd.* FROM recent_stay_data_v syd"
    } else {
      "SELECT syd.* FROM stay_data_t syd"
    },
    " JOIN stay sy ON syd.stay_id = sy.stay_id",
    " LEFT JOIN encounter fstenc ON syd.fst_encounter_id = fstenc.encounter_id",
    " LEFT JOIN encounter sndenc ON syd.snd_encounter_id = sndenc.encounter_id",
    " WHERE 1 = 1",
    " AND ($1::bigint[]  IS NULL OR syd.stay_data_id = ANY($1))",
    " AND ($2::bigint    IS NULL OR syd.creation_time >= $2)",
    " AND ($3::bigint    IS NULL OR syd.creation_time <= $3)",
    " AND ($4::bigint[]  IS NULL OR syd.creator_user_id = ANY($4))",
    " AND ($5::bigint[]  IS NULL OR syd.stay_id = ANY($5))",
    " AND ($6::bigint[]  IS NULL OR syd.fst_encounter_id = ANY($6))",
    " AND ($7::bigint[]  IS NULL OR syd.snd_encounter_id = ANY($7))",
    " AND ($8::bigint    IS NULL OR COALESCE(fst_time, fstenc.creation_time) >= $8)",
    " AND ($9::bigint    IS NULL OR COALESCE(fst_time, fstenc.creation_time) <= $9)",
    " AND ($10::bigint   IS NULL OR COALESCE(snd_time, sndenc.creation_time) >= $10)",
    " AND ($11::bigint   IS NULL OR COALESCE(snd_time, sndenc.creation_time) <= $11)",
    " AND ($12::bool     IS NULL OR syd.active = $12)",
    " AND ($13::bigint[] IS NULL OR sy.attendee_user_id = ANY($13))",
    " ORDER BY syd.stay_data_id",
  ]
  .join("\n");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.stay_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.stay_id,
        &props.fst_encounter_id,
        &props.snd_encounter_id,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
        &props.active,
        &props.attendee_user_id,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
