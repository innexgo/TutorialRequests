use super::db_types::*;
use super::utils::current_time_millis;
use innexgo_hours_api::request;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for Course {
  // select * from course order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> Course {
    Course {
      course_id: row.get("course_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      school_id: row.get("school_id"),
    }
  }
}

pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  school_id: i64,
) -> Result<Course, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_id = con
    .query_one(
      "INSERT INTO
       course(
           creation_time,
           creator_user_id,
           school_id
       )
       VALUES($1, $2, $3)
       RETURNING course_id
      ",
      &[&creation_time, &creator_user_id, &school_id],
    )
    .await?
    .get(0);

  // return course
  Ok(Course {
    course_id,
    creation_time,
    creator_user_id,
    school_id,
  })
}

pub async fn get_by_course_id(
  con: &mut impl GenericClient,
  course_id: i64,
) -> Result<Option<Course>, tokio_postgres::Error> {
  let result = con
    .query_opt("SELECT * FROM course WHERE course_id=$1", &[&course_id])
    .await?
    .map(|x| x.into());

  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: request::CourseViewProps,
) -> Result<Vec<Course>, tokio_postgres::Error> {
  let sql = "SELECT c.* FROM course c WHERE 1 = 1
     AND ($1::bigint[] IS NULL OR c.course_id = ANY($1))
     AND ($2::bigint   IS NULL OR c.creation_time >= $2)
     AND ($3::bigint   IS NULL OR c.creation_time <= $3)
     AND ($4::bigint[] IS NULL OR c.creator_user_id = ANY($4))
     AND ($5::bigint[] IS NULL OR c.school_id = ANY($5))
     ORDER BY c.course_id
     ";

  let stmnt = con.prepare(sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.school_id,
      ],
    )
    .await?
    .into_iter()
    .map(|x| x.into())
    .collect();
  Ok(results)
}
