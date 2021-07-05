use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;
use innexgo_hours_api::request;

impl From<tokio_postgres::row::Row> for School {
  // select * from school order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> School {
    School {
      school_id: row.get("school_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      whole: row.get("whole"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  whole: bool,
) -> Result<School, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let school_id = con
    .query_one(
      "INSERT INTO
       school(
           creation_time,
           creator_user_id,
           whole
       )
       VALUES($1, $2, $3)
       RETURNING school_id
      ",
      &[&creation_time, &creator_user_id, &whole],
    ).await?
    .get(0);

  // return school
  Ok(School {
    school_id,
    creation_time,
    creator_user_id,
    whole
  })
}

pub async fn get_by_school_id(
  con: &mut impl GenericClient,
  school_id: i64,
) -> Result<Option<School>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM school WHERE school_id=$1",
      &[&school_id],
    ).await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::SchoolViewProps,
) -> Result<Vec<School>, tokio_postgres::Error> {
  let results = con
    .query(
      "
        SELECT sc.* FROM school sc WHERE 1 = 1
        AND ($1::bigint[] IS NULL OR sc.school_id IN $1)
        AND ($2::bigint   IS NULL OR sc.creation_time >= $2)
        AND ($3::bigint   IS NULL OR sc.creation_time <= $3)
        AND ($4::bigint[] IS NULL OR sc.creator_user_id IN $4)
        AND ($5::bool     IS NULL OR sc.whole = $5)
        ORDER BY sc.school_id
      ",
      &[
        &props.school_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.whole,
      ],
    ).await?
    .into_iter()
    .map(|row| row.into())
    .collect();

  Ok(results)
}
