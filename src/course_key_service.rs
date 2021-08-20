use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use std::convert::TryInto;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CourseKey {
  // select * from course_key order only, otherwise it will fail
  fn from(row: tokio_postgres::row::Row) -> CourseKey {
    CourseKey {
      course_key_key: row.get("course_key_key"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      course_id: row.get("course_id"),
      max_uses: row.get("max_uses"),
      course_membership_kind: (row.get::<_, i64>("course_membership_kind") as u8)
        .try_into()
        .unwrap(),
      start_time: row.get("start_time"),
      end_time: row.get("end_time"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  course_id: i64,
  max_uses: i64,
  course_membership_kind: request::CourseMembershipKind,
  start_time: i64,
  end_time: i64,
) -> Result<CourseKey, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_key_key = con
    .query_one(
      "INSERT INTO
       course_key(
           course_key_key,
           creation_time,
           creator_user_id,
           course_id,
           max_uses,
           course_membership_kind,
           start_time,
           end_time
       )
       VALUES(MD5(random()::text), $1, $2, $3, $4, $5, $6, $7)
       RETURNING course_key_key
      ",
      &[
        &creation_time,
        &creator_user_id,
        &course_id,
        &max_uses,
        &(course_membership_kind.clone() as i64),
        &start_time,
        &end_time,
      ],
    )
    .await?
    .get(0);

  // return course_key
  Ok(CourseKey {
    course_key_key,
    creation_time,
    creator_user_id,
    course_id,
    max_uses,
    course_membership_kind,
    start_time,
    end_time,
  })
}

pub async fn get_by_course_key_key(
  con: &mut impl GenericClient,
  course_key_key: &str,
) -> Result<Option<CourseKey>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM course_key WHERE course_key_key=$1",
      &[&course_key_key],
    )
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::CourseKeyViewProps,
) -> Result<Vec<CourseKey>, tokio_postgres::Error> {
  let sql = "
    SELECT ck.* FROM course_key ck
    WHERE 1 = 1
    AND ($1::text[]   IS NULL OR ck.course_key_key = ANY($1))
    AND ($2::bigint   IS NULL OR ck.creation_time >= $2)
    AND ($3::bigint   IS NULL OR ck.creation_time <= $3)
    AND ($4::bigint[] IS NULL OR ck.creator_user_id = ANY($4))
    AND ($5::bigint[] IS NULL OR ck.course_id = ANY($5))
    AND ($6::bigint[] IS NULL OR ck.max_uses = ANY($6))
    AND ($7::bigint[] IS NULL OR ck.course_membership_kind = ANY($7))
    AND ($8::bigint   IS NULL OR ck.start_time >= $8)
    AND ($9::bigint   IS NULL OR ck.start_time <= $9)
    AND ($10::bigint  IS NULL OR ck.end_time >= $10)
    AND ($11::bigint  IS NULL OR ck.end_time <= $11)
    ORDER BY ck.course_key_key
  ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_key_key,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.course_id,
        &props.max_uses,
        &props
          .course_membership_kind
          .map(|v| v.into_iter().map(|x| x as i64).collect::<Vec<i64>>()),
        &props.min_start_time,
        &props.max_start_time,
        &props.min_end_time,
        &props.max_end_time,
      ],
    )
    .await?
    .into_iter()
    .map(|x| x.into())
    .collect();

  Ok(results)
}
