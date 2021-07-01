use super::db_types::*;
use super::utils::current_time_millis;
use tokio_postgres::GenericClient;

impl From<tokio_postgres::row::Row> for CourseData {
  // select * from course_data order only, otherwise it will fail
  fn from(row: tokio_postgres::Row) -> CourseData {
    CourseData {
      course_data_id: row.get("course_data_id"),
      creation_time: row.get("creation_time"),
      creator_user_id: row.get("creator_user_id"),
      course_id: row.get("course_id"),
      name: row.get("name"),
      description: row.get("description"),
      active: row.get("active"),
    }
  }
}

// TODO we need to figure out a way to make scheduled and unscheduled courses work better
pub async fn add(
  con: &mut impl GenericClient,
  creator_user_id: i64,
  course_id: i64,
  name: String,
  description: String,
  active: bool,
) -> Result<CourseData, tokio_postgres::Error> {
  let creation_time = current_time_millis();

  let course_data_id = con
    .query_one(
      "INSERT INTO
       course_data(
           creation_time,
           creator_user_id,
           course_id,
           name,
           description,
           active
       )
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING course_data_id
      ",
      &[
        &creation_time,
        &creator_user_id,
        &course_id,
        &name,
        &description,
        &active,
      ],
    )
    .await?
    .get(0);

  // return course_data
  Ok(CourseData {
    course_data_id,
    creation_time,
    creator_user_id,
    course_id,
    name,
    description,
    active,
  })
}

pub async fn get_by_course_id(
  con: &mut impl GenericClient,
  course_id: i64,
) -> Result<Option<CourseData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "
      SELECT cd.* FROM course_data cd
      INNER JOIN (
          SELECT max(course_data_id) id
          FROM course_data
          GROUP BY course_id
      ) maxids
      ON maxids.id = cd.course_data_id
      WHERE cd.course_id = $1
      ",
      &[&course_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn is_active_by_course_id(
  con: &mut impl GenericClient,
  course_id: i64,
) -> Result<bool, tokio_postgres::Error> {
  let result = match get_by_course_id(con, course_id).await? {
    Some(CourseData { active: true, .. }) => true,
    _ => false,
  };

  Ok(result)
}

pub async fn get_by_course_data_id(
  con: &mut impl GenericClient,
  course_data_id: i64,
) -> Result<Option<CourseData>, tokio_postgres::Error> {
  let result = con
    .query_opt(
      "SELECT * FROM course_data WHERE course_data_id=$1",
      &[&course_data_id],
    )
    .await?
    .map(|x| x.into());
  Ok(result)
}

pub async fn query(
  con: &mut impl GenericClient,
  props: innexgo_hours_api::request::CourseDataViewProps,
) -> Result<Vec<CourseData>, tokio_postgres::Error> {
  let sql = [
    "SELECT cd.* FROM course_data cd",
    " JOIN course c ON cd.course_id = c.course_id",
    if props.only_recent {
      " INNER JOIN (SELECT max(course_data_id) id FROM course_data GROUP BY course_id) maxids
        ON maxids.id = cd.course_data_id"
    } else {
      ""
    },
    " WHERE 1 = 1",
    " AND ($1::bigint[] IS NULL OR cd.course_data_id IN $1)",
    " AND ($2::bigint   IS NULL OR cd.creation_time >= $2)",
    " AND ($3::bigint   IS NULL OR cd.creation_time <= $3)",
    " AND ($4::bigint   IS NULL OR cd.creator_user_id = $4)",
    " AND ($5::bigint   IS NULL OR cd.course_id = $5)",
    " AND ($6::text     IS NULL OR cd.name = $6)",
    " AND ($7::text     IS NULL OR cd.name LIKE CONCAT('%',$7,'%'))",
    " AND ($8::text     IS NULL OR cd.description = $8)",
    " AND ($9::text     IS NULL OR cd.description LIKE CONCAT('%',$9,'%'))",
    " AND ($10::bool    IS NULL OR cd.active = $10)",
    " AND ($11::bigint  IS NULL OR c.school_id = $11)",
    " ORDER BY cd.course_data_id",
    " LIMIT $12",
    " OFFSET $13",
  ]
  .join("");

  let stmnt = con.prepare(&sql).await?;

  let results = con
    .query(
      &stmnt,
      &[
        &props.course_data_id,
        &props.min_creation_time,
        &props.max_creation_time,
        &props.creator_user_id,
        &props.course_id,
        &props.name,
        &props.partial_name,
        &props.description,
        &props.partial_description,
        &props.active,
        &props.school_id,
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
