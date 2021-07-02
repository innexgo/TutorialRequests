use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CourseKeyData {
  // select * from course_key_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> CourseKeyData {
    CourseKeyData {
      course_key_data_id: row.get("course_key_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      course_key_key: row.get("course_key_key"),
      active: row.get("active"),
    }
  }
}

// TODO we need to figure out a way to make scheduled and unscheduled course_keys work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  course_key_key: String,
  active: bool,
) -> Result<CourseKeyData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_key_data_id = con
    .query_one(
      "INSERT INTO
       course_key_data(
           creation_time,
           creator_user_id,
           course_key_key,
           active
       )
       VALUES ($1, $2, $3, $4)
       RETURNING course_key_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &course_key_key,
        &active,
      ],
    )
    .await?
    .get(0);

  // return course_key_data
  Ok(CourseKeyData {
    course_key_data_id,
    creation_time,
    creator_user_id,
    course_key_key,
    active,
  })
}

pub async fn get_by_course_key_data_id(
  con: &mut impl GenericClient,
  course_key_data_id: i64,
) -> Result<Option<CourseKeyData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM course_key_data WHERE course_key_data_id=$1",
      &[&course_key_data_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::CourseKeyDataViewProps,
) -> Result<Vec<CourseKeyData>, tokio_postgres::Error> {

  let sql = [
    "SELECT ckd.* FROM course_key_data ckd",
    " JOIN course_key ck ON ckd.course_key_key = ck.course_key_key",
    if props.only_recent {
      " INNER JOIN (SELECT max(course_key_data_id) id FROM course_key_data GROUP BY course_key_key) maxids
        ON maxids.id = ckd.course_key_data_id"
    } else {
      ""
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR ckd.course_key_data_id IN $1)",
    " AND ($2::bigint   IS NULL OR ckd.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR ckd.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR ckd.creator_user_id = $4)",
    " AND ($5::text     IS NULL OR ckd.course_key_key = $5)",
    " AND ($6::bool     IS NULL OR ckd.active = $6)",
    " AND ($7::bigint   IS NULL OR ck.course_id = $7)",
    " AND ($8::bigint   IS NULL OR ck.max_uses = $8)",
    " AND ($9::bigint   IS NULL OR ck.course_membership_kind = $9)",
    " AND ($10::bigint  IS NULL OR ck.start_time >= $10)",
    " AND ($11::bigint  IS NULL OR ck.start_time <= $11)",
    " AND ($12::bigint  IS NULL OR ck.end_time >= $12)",
    " AND ($13::bigint  IS NULL OR ck.end_time <= $13)",
    " ORDER BY ckd.course_key_data_id",
    " LIMIT $14",
    " OFFSET $15",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_key_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.course_key_key,
        &props.active,
        &props.course_id,
        &props.max_uses,
        &props.course_membership_kind.map(|x| x as i64),
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
    .map(|row| row.into())
    .collect();

  Ok(results)
}
