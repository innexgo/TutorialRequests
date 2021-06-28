use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use std::convert::TryInto;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CourseMembership {
  // select * from course_membership order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> CourseMembership {
    CourseMembership {
      course_membership_id: row.get("course_membership_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      user_id: row.get("user_id"),
      course_id: row.get("course_id"),
      course_membership_kind: (row.get::<_, i64>("course_membership_kind") as u8)
        .try_into()
        .unwrap(),
      course_key_id: row.get("course_key_id"),
    }
  }
}

// TODO we need to figure out a way to make scheduled and unscheduled courses work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  user_id: i64,
  course_id: i64,
  course_membership_kind: request::CourseMembershipKind,
  course_key_id: Option<i64>,
) -> Result<CourseMembership, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_membership_id = con
    .query_one(
      "INSERT INTO
       course_membership(
           creation_time,
           creator_user_id,
           user_id,
           course_id,
           course_membership_kind,
           course_key_id
       )
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING course_membership_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &user_id,
        &course_id,
        &(course_membership_kind.clone() as i64),
        &course_key_id,
      ],
    )
    .await?
    .get(0);

  // return course_membership
  Ok(CourseMembership {
    course_membership_id,
    creation_time,
    creator_user_id,
    user_id,
    course_id,
    course_membership_kind,
    course_key_id,
  })
}

pub async fn get_by_course_membership_id(
  con: &mut impl GenericClient,
  course_membership_id: i64,
) -> Result<Option<CourseMembership>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM course_membership WHERE course_membership_id=$1",
      &[&course_membership_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::CourseMembershipViewProps,
) -> Result<Vec<CourseMembership>, tokio_postgres::Error> {
  let sql = [
    "SELECT cm.* FROM course_membership cm",
    if props.only_recent {
      " INNER JOIN (SELECT max(course_membership_id) id FROM course_membership GROUP BY user_id, course_id) maxids
        ON maxids.id = cm.course_membership_id"
    } else {
      ""
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR cm.course_membership_id IN $1)",
    " AND ($2::bigint   IS NULL OR cm.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR cm.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR cm.creator_user_id = $4)",
    " AND ($5::bigint   IS NULL OR cm.user_id = $5)",
    " AND ($6::bigint   IS NULL OR cm.course_id = $6)",
    " AND ($7::bigint   IS NULL OR cm.course_membership_kind = $7)",
    " AND ($8::bool     IS NULL OR cm.course_key_id IS NOT NULL = $8)",
    " AND ($9::bigint   IS NULL OR cm.course_key_id = $9 IS TRUE)",
    " ORDER BY cm.course_membership_id",
    " LIMIT $10",
    " OFFSET $11",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_membership_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.user_id,
        &props.course_id,
        &props.course_membership_kind.map(|x| x as i64),
        &props.course_membership_from_key,
        &props.course_key_id,
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
