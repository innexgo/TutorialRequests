use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use std::convert::TryInto;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CourseKey {
  // select * from course_key order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> CourseKey {
    CourseKey {
      course_key_id: row.get("course_key_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      course_id: row.get("course_id"),
      key: row.get("key"),
      duration: row.get("duration"),
      max_uses: row.get("max_uses"),
      // means that there's a mismatch between the values of the enum and the value stored in the column
      course_membership_kind: row
        .get::<_, Option<i64>>("course_membership_kind")
        .map(|x| (x as u8).try_into().unwrap()),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  course_id: i64,
  key: String,
  duration: i64,
  max_uses: i64,
  course_membership_kind: Option<request::CourseMembershipKind>,
) -> Result<CourseKey, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_key_id = con
    .query_one(
      "INSERT INTO
       course_key(
           creation_time,
           creator_user_id,
           course_id,
           key,
           duration,
           max_uses
       )
       VALUES($1, $2, $3, $4, $5, $6)
       RETURNING course_key_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &course_id,
        &key,
        &duration,
        &max_uses,
        &(course_membership_kind.clone().map(|x| x as i64)),
      ],
    )
    .await?
    .get(0);

  // return course_key
  Ok(CourseKey {
    course_key_id,
    creation_time,
    creator_user_id,
    course_id,
    key,
    duration,
    max_uses,
    course_membership_kind,
  })
}

pub async fn get_by_course_key_hash(
  con: &mut impl GenericClient,
  course_key_hash: &str,
) -> Result<Option<CourseKey>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM course_key WHERE course_key_hash=$1",
      &[&course_key_hash],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::CourseKeyViewProps,
) -> Result<Vec<CourseKey>, tokio_postgres::Error> {
  // TODO prevent getting meaningless duration

  let sql = [
    "SELECT ck.* FROM course_key ck",
    if props.only_recent {
        " INNER JOIN (SELECT max(course_key_id) id FROM course_key GROUP BY key) maxids ON maxids.id = a.course_key_id"
    } else {
        ""
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR a.course_key_id IN $1)",
    " AND ($2::bigint   IS NULL OR a.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR a.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR a.creator_user_id = $4)",
    " AND ($5::bigint   IS NULL OR a.course_id = $5)",
    " AND ($6::text     IS NULL OR a.key = $6)",
    " AND ($7::bigint   IS NULL OR a.duration >= $7)",
    " AND ($8::bigint   IS NULL OR a.duration <= $8)",
    " AND ($9::bigint   IS NULL OR a.max_uses = $9)",
    " AND ($10::bigint  IS NULL OR a.course_membership_kind = $10 IS TRUE)",
    " AND ($11::bool    IS NULL OR a.course_membership_kind IS NOT NULL = $11)",
    " ORDER BY a.course_key_id",
    " LIMIT $12",
    " OFFSET $13",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_key_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.course_id,
        &props.key,
        &props.min_duration,
        &props.max_duration,
        &props.max_uses,
        &props.course_membership_kind.map(|x| x as i64),
        &props.course_key_valid,
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
