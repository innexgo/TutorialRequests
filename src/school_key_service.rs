use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for SchoolKey {
  // select * from school_key order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> SchoolKey {
    SchoolKey {
      school_key_key: row.get("school_key_key"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_id: row.get("school_id"),
      max_uses: row.get("max_uses"),
      start_time: row.get("start_time"),
      end_time: row.get("end_time"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_id: i64,
  max_uses: i64,
  start_time: i64,
  end_time: i64,
) -> Result<SchoolKey, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let school_key_key = con
    .query_one(
      "INSERT INTO
       school_key(
           school_key_key,
           creation_time,
           creator_user_id,
           school_id,
           max_uses,
           start_time,
           end_time
       )
       VALUES(MD5(random()::text), $1, $2, $3, $4, $5, $6)
       RETURNING school_key_key
      ",
      &[
        &creation_time,
        &creator_user_id,
        &school_id,
        &max_uses,
        &start_time,
        &end_time,
      ],
    )
    .await?
    .get(0);

  // return school_key
  Ok(SchoolKey {
    school_key_key,
    creation_time,
    creator_user_id,
    school_id,
    max_uses,
    start_time,
    end_time
  })
}

pub async fn get_by_school_key_key(
  con: &mut impl GenericClient,
  school_key_key: String,
) -> Result<Option<SchoolKey>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM school_key WHERE school_key_key=$1",
      &[&school_key_key],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::SchoolKeyViewProps,
) -> Result<Vec<SchoolKey>, tokio_postgres::Error> {

  let sql = "
    SELECT ck.* FROM school_key ck
    WHERE 1 = 1
    AND ($1::text[]   IS NULL OR ck.school_key_key IN $1)
    AND ($2::bigint   IS NULL OR ck.creation_time >= $2)
    AND ($3::bigint   IS NULL OR ck.creation_time <= $3)
    AND ($4::bigint   IS NULL OR ck.creator_user_id = $4)
    AND ($5::bigint   IS NULL OR ck.school_id = $5)
    AND ($6::bigint   IS NULL OR ck.max_uses = $6)
    AND ($7::bigint   IS NULL OR ck.start_time >= $7)
    AND ($8::bigint   IS NULL OR ck.start_time <= $8)
    AND ($9::bigint  IS NULL OR ck.end_time >= $9)
    AND ($10::bigint  IS NULL OR ck.end_time <= $10)
    ORDER BY ck.school_key_key
    LIMIT $11
    OFFSET $12
  ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.school_key_key,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_id,
        &props.max_uses,
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
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
