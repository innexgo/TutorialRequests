use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SchoolKeyData {
  // select * from school_key_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> SchoolKeyData {
    SchoolKeyData {
      school_key_data_id: row.get("school_key_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_key_key: row.get("school_key_key"),
      active: row.get("active"),
    }
  }
}

// TODO we need to figure out a way to make scheduled and unscheduled school_keys work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_key_key: String,
  active: bool,
) -> Result<SchoolKeyData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let school_key_data_id = con
    .query_one(
      "INSERT INTO
       school_key_data(
           creation_time,
           creator_user_id,
           school_key_key,
           active
       )
       VALUES ($1, $2, $3, $4)
       RETURNING school_key_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &school_key_key,
        &active,
      ],
    )
    .await?
    .get(0);

  // return school_key_data
  Ok(SchoolKeyData {
    school_key_data_id,
    creation_time,
    creator_user_id,
    school_key_key,
    active,
  })
}

#[allow(unused)]
pub async fn get_by_school_key_data_id(
  con: &mut impl GenericClient,
  school_key_data_id: i64,
) -> Result<Option<SchoolKeyData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM school_key_data WHERE school_key_data_id=$1",
      &[&school_key_data_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::SchoolKeyDataViewProps,
) -> Result<Vec<SchoolKeyData>, tokio_postgres::Error> {

  let sql = [
    "SELECT skd.* FROM school_key_data skd",
    " JOIN school_key sk ON skd.school_key_key = sk.school_key_key",
    if props.only_recent {
      " INNER JOIN (SELECT max(school_key_data_id) id FROM school_key_data GROUP BY school_key_key) maxids
        ON maxids.id = skd.school_key_data_id"
    } else {
      ""
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR skd.school_key_data_id = ANY($1))",
    " AND ($2::bigint   IS NULL OR skd.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR skd.creation_time <= $3)",
    " AND ($4::bigint[] IS NULL OR skd.creator_user_id = ANY($4))",
    " AND ($5::text[]   IS NULL OR skd.school_key_key = ANY($5))",
    " AND ($6::bool     IS NULL OR skd.active = $6)",
    " AND ($7::bigint[] IS NULL OR sk.school_id = ANY($7))",
    " AND ($8::bigint   IS NULL OR sk.start_time >= $8)",
    " AND ($9::bigint   IS NULL OR sk.start_time <= $9)",
    " AND ($10::bigint  IS NULL OR sk.end_time >= $10)",
    " AND ($11::bigint  IS NULL OR sk.end_time <= $11)",
    " ORDER BY skd.school_key_data_id",
  ]
  .join("\n");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.school_key_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_key_key,
        &props.active,
        &props.school_id,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
      ],
    )
    .await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
