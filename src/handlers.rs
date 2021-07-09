use super::Db;
use auth_service_api::client::AuthService;
use auth_service_api::response::AuthError;
use auth_service_api::response::User;

use innexgo_hours_api::request;
use innexgo_hours_api::response;

use super::db_types::*;
use super::utils;

// db
use super::adminship_service;
use super::committment_response_service;
use super::committment_service;
use super::course_data_service;
use super::course_key_data_service;
use super::course_key_service;
use super::course_membership_service;
use super::course_service;
use super::school_data_service;
use super::school_key_data_service;
use super::school_key_service;
use super::school_service;
use super::session_data_service;
use super::session_request_response_service;
use super::session_request_service;
use super::session_service;
use super::subscription_service;

use std::error::Error;

use super::Config;

fn report_postgres_err(e: tokio_postgres::Error) -> response::InnexgoHoursError {
  utils::log(utils::Event {
    msg: e.to_string(),
    source: e.source().map(|e| e.to_string()),
    severity: utils::SeverityKind::Error,
  });
  response::InnexgoHoursError::InternalServerError
}

fn report_auth_err(e: AuthError) -> response::InnexgoHoursError {
  match e {
    AuthError::ApiKeyNonexistent => response::InnexgoHoursError::ApiKeyUnauthorized,
    AuthError::ApiKeyUnauthorized => response::InnexgoHoursError::ApiKeyNonexistent,
    c => {
      let ae = match c {
        AuthError::InternalServerError => response::InnexgoHoursError::AuthInternalServerError,
        AuthError::MethodNotAllowed => response::InnexgoHoursError::AuthBadRequest,
        AuthError::BadRequest => response::InnexgoHoursError::AuthBadRequest,
        AuthError::NetworkError => response::InnexgoHoursError::AuthNetworkError,
        _ => response::InnexgoHoursError::AuthOther,
      };

      utils::log(utils::Event {
        msg: ae.as_ref().to_owned(),
        source: Some(format!("auth service: {}", c.as_ref())),
        severity: utils::SeverityKind::Error,
      });

      ae
    }
  }
}

async fn fill_subscription(
  _con: &mut tokio_postgres::Client,
  subscription: Subscription,
) -> Result<response::Subscription, response::InnexgoHoursError> {
  Ok(response::Subscription {
    subscription_id: subscription.subscription_id,
    creation_time: subscription.creation_time,
    creator_user_id: subscription.creator_user_id,
    subscription_kind: subscription.subscription_kind,
    max_uses: subscription.max_uses,
  })
}

async fn fill_school(
  _con: &mut tokio_postgres::Client,
  school: School,
) -> Result<response::School, response::InnexgoHoursError> {
  Ok(response::School {
    school_id: school.school_id,
    creation_time: school.creation_time,
    creator_user_id: school.creator_user_id,
    whole: school.whole,
  })
}

async fn fill_school_data(
  con: &mut tokio_postgres::Client,
  school_data: SchoolData,
) -> Result<response::SchoolData, response::InnexgoHoursError> {
  let school = school_service::get_by_school_id(con, school_data.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  Ok(response::SchoolData {
    school_data_id: school_data.school_data_id,
    creation_time: school_data.creation_time,
    creator_user_id: school_data.creator_user_id,
    school: fill_school(con, school).await?,
    name: school_data.name,
    description: school_data.description,
    active: school_data.active,
  })
}

async fn fill_school_key(
  con: &mut tokio_postgres::Client,
  school_key: SchoolKey,
) -> Result<response::SchoolKey, response::InnexgoHoursError> {
  let school = school_service::get_by_school_id(con, school_key.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  Ok(response::SchoolKey {
    school_key_key: school_key.school_key_key,
    creation_time: school_key.creation_time,
    creator_user_id: school_key.creator_user_id,
    school: fill_school(con, school).await?,
    max_uses: school_key.max_uses,
    start_time: school_key.start_time,
    end_time: school_key.end_time,
  })
}

async fn fill_school_key_data(
  con: &mut tokio_postgres::Client,
  school_key_data: SchoolKeyData,
) -> Result<response::SchoolKeyData, response::InnexgoHoursError> {
  let school_key = school_key_service::get_by_school_key_key(con, &school_key_data.school_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;

  Ok(response::SchoolKeyData {
    school_key_data_id: school_key_data.school_key_data_id,
    creation_time: school_key_data.creation_time,
    creator_user_id: school_key_data.creator_user_id,
    school_key: fill_school_key(con, school_key).await?,
    active: school_key_data.active,
  })
}

async fn fill_adminship(
  con: &mut tokio_postgres::Client,
  adminship: Adminship,
) -> Result<response::Adminship, response::InnexgoHoursError> {
  let school_key = match adminship.school_key_key {
    Some(school_key_key) => {
      let school_key = school_key_service::get_by_school_key_key(con, &school_key_key)
        .await
        .map_err(report_postgres_err)?
        .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;
      Some(fill_school_key(con, school_key).await?)
    }
    _ => None,
  };

  let school = school_service::get_by_school_id(con, adminship.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  Ok(response::Adminship {
    adminship_id: adminship.adminship_id,
    creation_time: adminship.creation_time,
    creator_user_id: adminship.creator_user_id,
    user_id: adminship.user_id,
    school: fill_school(con, school).await?,
    adminship_kind: adminship.adminship_kind,
    school_key,
  })
}

async fn fill_course(
  con: &mut tokio_postgres::Client,
  course: Course,
) -> Result<response::Course, response::InnexgoHoursError> {
  let school = school_service::get_by_school_id(con, course.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  Ok(response::Course {
    course_id: course.course_id,
    creation_time: course.creation_time,
    creator_user_id: course.creator_user_id,
    school: fill_school(con, school).await?,
  })
}

async fn fill_course_data(
  con: &mut tokio_postgres::Client,
  course_data: CourseData,
) -> Result<response::CourseData, response::InnexgoHoursError> {
  let course = course_service::get_by_course_id(con, course_data.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  Ok(response::CourseData {
    course_data_id: course_data.course_data_id,
    creation_time: course_data.creation_time,
    creator_user_id: course_data.creator_user_id,
    course: fill_course(con, course).await?,
    name: course_data.name,
    description: course_data.description,
    active: course_data.active,
  })
}

async fn fill_course_key(
  con: &mut tokio_postgres::Client,
  course_key: CourseKey,
) -> Result<response::CourseKey, response::InnexgoHoursError> {
  let course = course_service::get_by_course_id(con, course_key.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  Ok(response::CourseKey {
    course_key_key: course_key.course_key_key,
    creation_time: course_key.creation_time,
    creator_user_id: course_key.creator_user_id,
    course: fill_course(con, course).await?,
    max_uses: course_key.max_uses,
    course_membership_kind: course_key.course_membership_kind,
    start_time: course_key.start_time,
    end_time: course_key.end_time,
  })
}

async fn fill_course_key_data(
  con: &mut tokio_postgres::Client,
  course_key_data: CourseKeyData,
) -> Result<response::CourseKeyData, response::InnexgoHoursError> {
  let course_key = course_key_service::get_by_course_key_key(con, &course_key_data.course_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

  Ok(response::CourseKeyData {
    course_key_data_id: course_key_data.course_key_data_id,
    creation_time: course_key_data.creation_time,
    creator_user_id: course_key_data.creator_user_id,
    course_key: fill_course_key(con, course_key).await?,
    active: course_key_data.active,
  })
}

async fn fill_course_membership(
  con: &mut tokio_postgres::Client,
  course_membership: CourseMembership,
) -> Result<response::CourseMembership, response::InnexgoHoursError> {
  let course = course_service::get_by_course_id(con, course_membership.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  let course_key = match course_membership.course_key_key {
    Some(course_key_key) => {
      let course_key = course_key_service::get_by_course_key_key(con, &course_key_key)
        .await
        .map_err(report_postgres_err)?
        .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

      Some(fill_course_key(con, course_key).await?)
    }
    _ => None,
  };

  Ok(response::CourseMembership {
    course_membership_id: course_membership.course_membership_id,
    creation_time: course_membership.creation_time,
    creator_user_id: course_membership.creator_user_id,
    user_id: course_membership.user_id,
    course: fill_course(con, course).await?,
    course_membership_kind: course_membership.course_membership_kind,
    course_key,
  })
}

async fn fill_session(
  con: &mut tokio_postgres::Client,
  session: Session,
) -> Result<response::Session, response::InnexgoHoursError> {
  let course = course_service::get_by_course_id(con, session.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  Ok(response::Session {
    session_id: session.session_id,
    creation_time: session.creation_time,
    creator_user_id: session.creator_user_id,
    course: fill_course(con, course).await?,
  })
}

async fn fill_session_data(
  con: &mut tokio_postgres::Client,
  session_data: SessionData,
) -> Result<response::SessionData, response::InnexgoHoursError> {
  let session = session_service::get_by_session_id(con, session_data.session_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

  Ok(response::SessionData {
    session_data_id: session_data.session_data_id,
    creation_time: session_data.creation_time,
    creator_user_id: session_data.creator_user_id,
    session: fill_session(con, session).await?,
    name: session_data.name,
    start_time: session_data.start_time,
    end_time: session_data.end_time,
    active: session_data.active,
  })
}

async fn fill_session_request(
  con: &mut tokio_postgres::Client,
  session_request: SessionRequest,
) -> Result<response::SessionRequest, response::InnexgoHoursError> {
  let course = course_service::get_by_course_id(con, session_request.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  Ok(response::SessionRequest {
    session_request_id: session_request.session_request_id,
    creation_time: session_request.creation_time,
    creator_user_id: session_request.creator_user_id,
    course: fill_course(con, course).await?,
    message: session_request.message,
    start_time: session_request.start_time,
    end_time: session_request.end_time,
  })
}

async fn fill_session_request_response(
  con: &mut tokio_postgres::Client,
  session_request_response: SessionRequestResponse,
) -> Result<response::SessionRequestResponse, response::InnexgoHoursError> {
  let session_request = session_request_service::get_by_session_request_id(
    con,
    session_request_response.session_request_id,
  )
  .await
  .map_err(report_postgres_err)?
  .ok_or(response::InnexgoHoursError::SessionRequestNonexistent)?;

  let committment = match session_request_response.committment_id {
    Some(committment_id) => {
      let committment = committment_service::get_by_committment_id(con, committment_id)
        .await
        .map_err(report_postgres_err)?
        .ok_or(response::InnexgoHoursError::CommittmentNonexistent)?;

      Some(fill_committment(con, committment).await?)
    }
    _ => None,
  };

  Ok(response::SessionRequestResponse {
    session_request: fill_session_request(con, session_request).await?,
    creation_time: session_request_response.creation_time,
    creator_user_id: session_request_response.creator_user_id,
    message: session_request_response.message,
    committment,
  })
}

async fn fill_committment(
  con: &mut tokio_postgres::Client,
  committment: Committment,
) -> Result<response::Committment, response::InnexgoHoursError> {
  let session = session_service::get_by_session_id(con, committment.session_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

  Ok(response::Committment {
    committment_id: committment.committment_id,
    creation_time: committment.creation_time,
    creator_user_id: committment.creator_user_id,
    attendee_user_id: committment.attendee_user_id,
    session: fill_session(con, session).await?,
  })
}

async fn fill_committment_response(
  con: &mut tokio_postgres::Client,
  committment_response: CommittmentResponse,
) -> Result<response::CommittmentResponse, response::InnexgoHoursError> {
  let committment =
    committment_service::get_by_committment_id(con, committment_response.committment_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::CommittmentNonexistent)?;

  Ok(response::CommittmentResponse {
    committment: fill_committment(con, committment).await?,
    creation_time: committment_response.creation_time,
    creator_user_id: committment_response.creator_user_id,
    kind: committment_response.committment_response_kind,
  })
}

pub async fn get_user_if_api_key_valid(
  auth_service: &auth_service_api::client::AuthService,
  api_key: String,
) -> Result<User, response::InnexgoHoursError> {
  auth_service
    .get_user_by_api_key_if_valid(api_key)
    .await
    .map_err(report_auth_err)
}

pub async fn subscription_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SubscriptionNewProps,
) -> Result<response::Subscription, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  // create event
  let subscription = subscription_service::add(con, user.user_id, props.subscription_kind, 1, 0)
    .await
    .map_err(report_postgres_err)?;

  // return json
  fill_subscription(con, subscription).await
}

pub async fn course_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseNewProps,
) -> Result<response::CourseData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // validate school exists and that key creator is admin
  let _ = school_service::get_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  if !adminship_service::is_admin(&mut sp, user.user_id, props.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // check that school isn't archived
  if !school_data_service::is_active_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::SchoolArchived);
  }

  // create course
  let course = course_service::add(&mut sp, props.school_id, user.user_id)
    .await
    .map_err(report_postgres_err)?;

  // create course data
  let course_data = course_data_service::add(
    &mut sp,
    user.user_id,
    course.course_id,
    props.name,
    props.description,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  // give the creator a course membership
  let _ = course_membership_service::add(
    &mut sp,
    user.user_id,
    user.user_id,
    course.course_id,
    request::CourseMembershipKind::Instructor,
    None,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_data(con, course_data).await
}

pub async fn course_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseDataNewProps,
) -> Result<response::CourseData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let course = course_service::get_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  if !course_membership_service::is_instructor(&mut sp, user.user_id, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // now we can update data
  let course_data = course_data_service::add(
    &mut sp,
    user.user_id,
    course.course_id,
    props.name,
    props.description,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_data(con, course_data).await
}

pub async fn course_key_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseKeyNewProps,
) -> Result<response::CourseKeyData, response::InnexgoHoursError> {
  if props.start_time > props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let course = course_service::get_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // get instructor or admin
  if !course_membership_service::is_instructor(&mut sp, user.user_id, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // now create key
  let course_key = course_key_service::add(
    &mut sp,
    user.user_id,
    props.course_id,
    props.max_uses,
    props.course_membership_kind,
    props.start_time,
    props.end_time,
  )
  .await
  .map_err(report_postgres_err)?;

  // create key data
  let course_key_data =
    course_key_data_service::add(&mut sp, user.user_id, course_key.course_key_key, true)
      .await
      .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_key_data(con, course_key_data).await
}

pub async fn course_key_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseKeyDataNewProps,
) -> Result<response::CourseKeyData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get course key
  let course_key = course_key_service::get_by_course_key_key(&mut sp, &props.course_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

  // get corresponding course
  let course = course_service::get_by_course_id(&mut sp, course_key.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, course_key.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // is valid instructor
  if !course_membership_service::is_instructor(&mut sp, user.user_id, course.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // create key data
  let course_key_data = course_key_data_service::add(
    &mut sp,
    user.user_id,
    course_key.course_key_key,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_key_data(con, course_key_data).await
}

pub async fn course_membership_new_key(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseMembershipNewKeyProps,
) -> Result<response::CourseMembership, response::InnexgoHoursError> {
  // validate api membership
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get course key
  let course_key = course_key_service::get_by_course_key_key(&mut sp, &props.course_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

  // time is within range
  let now = utils::current_time_millis();
  if now < course_key.start_time || now > course_key.end_time {
    return Err(response::InnexgoHoursError::CourseKeyExpired);
  }

  // it fits under max uses
  let uses = course_membership_service::count_course_key_uses(&mut sp, &course_key.course_key_key)
    .await
    .map_err(report_postgres_err)?;

  if uses >= course_key.max_uses {
    return Err(response::InnexgoHoursError::CourseKeyUsed);
  }

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, course_key.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // now create membership
  let course_membership = course_membership_service::add(
    &mut sp,
    user.user_id,
    user.user_id,
    course_key.course_id,
    course_key.course_membership_kind,
    Some(course_key.course_key_key),
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_membership(con, course_membership).await
}

pub async fn course_membership_new_cancel(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseMembershipNewCancelProps,
) -> Result<response::CourseMembership, response::InnexgoHoursError> {
  // validate api membership
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // validate target exists
  let target = auth_service
    .get_user_by_id(props.user_id)
    .await
    .map_err(report_auth_err)?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get corresponding course
  let course = course_service::get_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  let is_instructor =
    course_membership_service::is_instructor(&mut sp, user.user_id, course.course_id)
      .await
      .map_err(report_postgres_err)?;

  let is_self_cancel = user.user_id == props.user_id;

  // if user_id == creator_user_id then authorize
  // otherwise creator_user_id must be an admin or instructor
  if !(is_self_cancel || is_instructor) {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // make sure we cannot leave the course without an instructor
  if is_instructor {
    let num_instructors = course_membership_service::count_instructors(&mut sp, course.course_id)
      .await
      .map_err(report_postgres_err)?;

    if num_instructors <= 1 {
      return Err(response::InnexgoHoursError::CourseMembershipCannotLeaveEmpty);
    }
  }

  // now create membership
  let course_membership = course_membership_service::add(
    &mut sp,
    user.user_id,
    props.user_id,
    course.course_id,
    request::CourseMembershipKind::Cancel,
    None,
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_membership(con, course_membership).await
}

pub async fn school_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolNewProps,
) -> Result<response::SchoolData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get subscription
  let subscription = subscription_service::get_by_user_id(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SubscriptionNonexistent)?;

  // check if subscription is authorized
  if adminship_service::count_valid_adminships_by_user_id(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?
    >= subscription.max_uses
  {
    return Err(response::InnexgoHoursError::SubscriptionLimited);
  }

  // create school
  let school = school_service::add(&mut sp, user.user_id, props.whole)
    .await
    .map_err(report_postgres_err)?;

  // create data
  let school_data = school_data_service::add(
    &mut sp,
    user.user_id,
    school.school_id,
    props.name,
    props.description,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  // create adminship
  let adminship = adminship_service::add(
    &mut sp,
    user.user_id,
    user.user_id,
    school.school_id,
    request::AdminshipKind::Admin,
    None,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_school_data(con, school_data).await
}

pub async fn school_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolDataNewProps,
) -> Result<response::SchoolData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  if !adminship_service::is_admin(&mut sp, user.user_id, props.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // create data
  let school_data = school_data_service::add(
    &mut sp,
    user.user_id,
    props.school_id,
    props.name,
    props.description,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_school_data(con, school_data).await
}

pub async fn school_key_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolKeyNewProps,
) -> Result<response::SchoolKeyData, response::InnexgoHoursError> {
  if props.start_time > props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let school = school_service::get_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  // check that school isn't archived
  if !school_data_service::is_active_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::SchoolArchived);
  }

  // get is admin
  if !adminship_service::is_admin(&mut sp, user.user_id, school.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // now create key
  let school_key = school_key_service::add(
    &mut sp,
    user.user_id,
    props.school_id,
    props.max_uses,
    props.start_time,
    props.end_time,
  )
  .await
  .map_err(report_postgres_err)?;

  // create key data
  let school_key_data =
    school_key_data_service::add(&mut sp, user.user_id, school_key.school_key_key, true)
      .await
      .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_school_key_data(con, school_key_data).await
}

pub async fn school_key_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolKeyDataNewProps,
) -> Result<response::SchoolKeyData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get school key
  let school_key = school_key_service::get_by_school_key_key(&mut sp, &props.school_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;

  // get corresponding school
  let school = school_service::get_by_school_id(&mut sp, school_key.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  // check that school isn't archived
  if !school_data_service::is_active_by_school_id(&mut sp, school_key.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::SchoolArchived);
  }

  // get admin
  if !adminship_service::is_admin(&mut sp, user.user_id, school.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // create key data
  let school_key_data = school_key_data_service::add(
    &mut sp,
    user.user_id,
    school_key.school_key_key,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_school_key_data(con, school_key_data).await
}

pub async fn adminship_new_key(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::AdminshipNewKeyProps,
) -> Result<response::Adminship, response::InnexgoHoursError> {
  // validate api membership
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // get school key
  let school_key = school_key_service::get_by_school_key_key(&mut sp, &props.school_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;

  // time is within range
  let now = utils::current_time_millis();
  if now < school_key.start_time || now > school_key.end_time {
    return Err(response::InnexgoHoursError::SchoolKeyExpired);
  }

  // check that school isn't archived
  if !school_data_service::is_active_by_school_id(&mut sp, school_key.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::SchoolArchived);
  }

  // it fits under max uses
  if adminship_service::count_school_key_uses(&mut sp, &school_key.school_key_key)
    .await
    .map_err(report_postgres_err)?
    >= school_key.max_uses
  {
    return Err(response::InnexgoHoursError::SchoolKeyUsed);
  }

  // now create adminship
  let adminship = adminship_service::add(
    &mut sp,
    user.user_id,
    user.user_id,
    school_key.school_id,
    request::AdminshipKind::Admin,
    Some(school_key.school_key_key),
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_adminship(con, adminship).await
}

pub async fn adminship_new_cancel(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::AdminshipNewCancelProps,
) -> Result<response::Adminship, response::InnexgoHoursError> {
  // validate api membership
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // validate target exists
  let target = auth_service
    .get_user_by_id(props.user_id)
    .await
    .map_err(report_auth_err)?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // check that school isn't archived
  if !school_data_service::is_active_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::SchoolArchived);
  }

  // get corresponding school
  let school = school_service::get_by_school_id(&mut sp, props.school_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  if !adminship_service::is_admin(&mut sp, user.user_id, school.school_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // make sure we cannot leave the school without an admin
  if adminship_service::count_admins(&mut sp, school.school_id)
    .await
    .map_err(report_postgres_err)?
    <= 1
  {
    return Err(response::InnexgoHoursError::AdminshipCannotLeaveEmpty);
  }

  // now create cancel
  let adminship = adminship_service::add(
    &mut sp,
    user.user_id,
    props.user_id,
    school.school_id,
    request::AdminshipKind::Cancel,
    None,
  )
  .await
  .map_err(report_postgres_err)?;

  // commit changes
  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_adminship(con, adminship).await
}

pub async fn session_request_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionRequestNewProps,
) -> Result<response::SessionRequest, response::InnexgoHoursError> {
  if props.start_time > props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // check course exists
  let _ = course_service::get_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;
  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // check is student of course
  if !course_membership_service::is_student(&mut sp, user.user_id, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // create request
  let session_request = session_request_service::add(
    &mut sp,
    user.user_id,
    props.course_id,
    props.message,
    props.start_time,
    props.end_time,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_session_request(con, session_request).await
}

pub async fn session_request_response_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionRequestResponseNewProps,
) -> Result<response::SessionRequestResponse, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // check session request exists
  let session_request =
    session_request_service::get_by_session_request_id(&mut sp, props.session_request_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SessionRequestNonexistent)?;

  // check that session request response doesn't exist
  if session_request_response_service::get_by_session_request_id(&mut sp, props.session_request_id)
    .await
    .map_err(report_postgres_err)?
    .is_some()
  {
    return Err(response::InnexgoHoursError::SessionRequestResponseExistent);
  }

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, session_request.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // check this user is instructor
  let is_instructor =
    course_membership_service::is_instructor(&mut sp, user.user_id, session_request.course_id)
      .await
      .map_err(report_postgres_err)?;

  let is_creator = user.user_id == session_request.creator_user_id;

  let committment_id = match props.session_id {
    None => {
      // either the creator of the request or the instructor may cancel
      if !is_instructor && !is_creator {
        return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
      }
      None
    }
    // if accepted
    Some(session_id) => {
      // only the instructor can create a committment
      if !is_instructor {
        return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
      }

      // check if session id is valid
      let session = session_service::get_by_session_id(&mut sp, session_id)
        .await
        .map_err(report_postgres_err)?
        .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

      // check that session is compatible with session_request's course
      if session_request.course_id != session.course_id {
        return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
      }

      // check if we already have a committment
      let maybe_committment = committment_service::get_by_attendee_user_id_session_id(
        &mut sp,
        session_request.creator_user_id,
        session_id,
      )
      .await
      .map_err(report_postgres_err)?;

      let committment = match maybe_committment {
        // use committment if exists
        Some(committment) => committment,

        // otherwise add one
        None => committment_service::add(
          &mut sp,
          user.user_id,
          session_request.creator_user_id,
          session_id,
        )
        .await
        .map_err(report_postgres_err)?,
      };

      // return
      Some(committment.committment_id)
    }
  };

  // create request response
  let session_request_response = session_request_response_service::add(
    &mut sp,
    props.session_request_id,
    user.user_id,
    props.message,
    committment_id,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_session_request_response(con, session_request_response).await
}

pub async fn session_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionNewProps,
) -> Result<response::SessionData, response::InnexgoHoursError> {
  if props.start_time > props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // validate course exists
  let _ = course_service::get_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  // ensure session creator is instrutor
  if !course_membership_service::is_instructor(&mut sp, user.user_id, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, props.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // create session
  let session = session_service::add(&mut sp, user.user_id, props.course_id)
    .await
    .map_err(report_postgres_err)?;

  // create session data
  let session_data = session_data_service::add(
    &mut sp,
    user.user_id,
    session.session_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_session_data(con, session_data).await
}

pub async fn session_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionDataNewProps,
) -> Result<response::SessionData, response::InnexgoHoursError> {
  // prevent negative duration
  if props.start_time > props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let session = session_service::get_by_session_id(&mut sp, props.session_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

  // ensure session creator is instrutor
  if !course_membership_service::is_instructor(&mut sp, user.user_id, session.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // now we can update data
  let session_data = session_data_service::add(
    &mut sp,
    user.user_id,
    session.session_id,
    props.name,
    props.start_time,
    props.end_time,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_session_data(con, session_data).await
}

pub async fn committment_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CommittmentNewProps,
) -> Result<response::Committment, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // validate session exists
  let session = session_service::get_by_session_id(&mut sp, props.session_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

  // ensure committment creator is instructor of the session's course
  if !course_membership_service::is_instructor(&mut sp, user.user_id, session.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // ensure attendee is the student of the session's course
  if !course_membership_service::is_student(&mut sp, props.attendee_user_id, session.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
  }

  // check that course isn't archived
  if !course_data_service::is_active_by_course_id(&mut sp, session.course_id)
    .await
    .map_err(report_postgres_err)?
  {
    return Err(response::InnexgoHoursError::CourseArchived);
  }

  // ensure committment doesnt yet exist
  if committment_service::get_by_attendee_user_id_session_id(
    &mut sp,
    props.attendee_user_id,
    props.session_id,
  )
  .await
  .map_err(report_postgres_err)?
  .is_some()
  {
    return Err(response::InnexgoHoursError::CommittmentExistent);
  }

  // create committment
  let committment = committment_service::add(
    &mut sp,
    user.user_id,
    props.attendee_user_id,
    props.session_id,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_committment(con, committment).await
}

pub async fn committment_response_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CommittmentResponseNewProps,
) -> Result<response::CommittmentResponse, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;
  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // check that committment exists
  let committment = committment_service::get_by_committment_id(&mut sp, props.committment_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CommittmentNonexistent)?;

  // check that committment response doesn't exist
  if committment_response_service::get_by_committment_id(&mut sp, props.committment_id)
    .await
    .map_err(report_postgres_err)?
    .is_some()
  {
    return Err(response::InnexgoHoursError::CommittmentResponseExistent);
  }

  // get session to make sure that course has it
  let session = session_service::get_by_session_id(&mut sp, committment.session_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

  let is_instructor =
    !course_membership_service::is_instructor(&mut sp, user.user_id, session.course_id)
      .await
      .map_err(report_postgres_err)?;

  let is_attendee = user.user_id == committment.attendee_user_id;

  // ensure committment creator is instrutor
  match props.committment_response_kind {
    request::CommittmentResponseKind::Cancelled => {
      if !is_attendee && !is_instructor {
        return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
      }
    }
    _ => {
      if !is_instructor {
        return Err(response::InnexgoHoursError::ApiKeyUnauthorized);
      }
    }
  }

  // now we can update response
  let committment_response = committment_response_service::add(
    &mut sp,
    committment.committment_id,
    user.user_id,
    props.committment_response_kind,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_committment_response(con, committment_response).await
}

pub async fn subscription_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SubscriptionViewProps,
) -> Result<Vec<response::Subscription>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let subscriptions = subscription_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return subscriptions
  let mut resp_subscriptions = vec![];
  for u in subscriptions
    // you can view your own subscriptions
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_subscriptions.push(fill_subscription(con, u).await?);
  }

  Ok(resp_subscriptions)
}

pub async fn school_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolViewProps,
) -> Result<Vec<response::School>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let schools = school_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return schools
  let mut resp_schools = vec![];
  for u in schools.into_iter() {
    // you can view all schools
    resp_schools.push(fill_school(con, u).await?);
  }

  Ok(resp_schools)
}

pub async fn school_data_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolDataViewProps,
) -> Result<Vec<response::SchoolData>, response::InnexgoHoursError> {
  // validate api key
  let _ = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let school_data = school_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;
  // return users
  // return school_datas
  let mut resp_school_datas = vec![];
  for u in school_data.into_iter() {
    // you can view all schools
    resp_school_datas.push(fill_school_data(con, u).await?);
  }

  Ok(resp_school_datas)
}

pub async fn course_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseViewProps,
) -> Result<Vec<response::Course>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let courses = course_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return courses
  let mut resp_courses = vec![];

  for x in courses.into_iter() {
    // students and instructors can see the courses they are (or were) a member of
    // administrators can see those plus the courses that they own

    let is_member =
      course_membership_service::get_by_user_id_course_id(con, user.user_id, x.course_id)
        .await
        .map_err(report_postgres_err)?
        .is_some();

    let is_admin = adminship_service::get_by_user_id_school_id(con, user.user_id, x.school_id)
      .await
      .map_err(report_postgres_err)?
      .is_some();

    if is_member || is_admin {
      resp_courses.push(fill_course(con, x).await?);
    }
  }

  Ok(resp_courses)
}

pub async fn course_data_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseDataViewProps,
) -> Result<Vec<response::CourseData>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let course_data = course_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return course_datas
  let mut resp_course_datas = vec![];
  for x in course_data.into_iter() {
    // students and instructors can see the courses they are (or were) a member of
    // administrators can see those plus the courses that they own

    let is_member =
      course_membership_service::get_by_user_id_course_id(con, user.user_id, x.course_id)
        .await
        .map_err(report_postgres_err)?
        .is_some();

    let course = course_service::get_by_course_id(con, x.course_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

    let is_admin = adminship_service::get_by_user_id_school_id(con, user.user_id, course.school_id)
      .await
      .map_err(report_postgres_err)?
      .is_some();

    if is_member || is_admin {
      resp_course_datas.push(fill_course_data(con, x).await?);
    }
  }

  Ok(resp_course_datas)
}

pub async fn course_membership_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseMembershipViewProps,
) -> Result<Vec<response::CourseMembership>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let course_memberships = course_membership_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return course_memberships
  let mut resp_course_memberships = vec![];
  for x in course_memberships.into_iter() {
    // members of a course can see all their fellow course memberships

    let is_member = course_membership_service::is_member(con, user.user_id, x.course_id)
      .await
      .map_err(report_postgres_err)?;

    if is_member {
      resp_course_memberships.push(fill_course_membership(con, x).await?);
    }
  }

  Ok(resp_course_memberships)
}

pub async fn course_key_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseKeyViewProps,
) -> Result<Vec<response::CourseKey>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let course_keys = course_key_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return course_keys
  let mut resp_course_keys = vec![];
  for x in course_keys.into_iter() {
    // only instructors may view course keys
    let is_instructor = course_membership_service::is_instructor(con, user.user_id, x.course_id)
      .await
      .map_err(report_postgres_err)?;

    if is_instructor {
      resp_course_keys.push(fill_course_key(con, x).await?);
    }
  }

  Ok(resp_course_keys)
}

pub async fn course_key_data_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CourseKeyDataViewProps,
) -> Result<Vec<response::CourseKeyData>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let course_key_data = course_key_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return course_key_datas
  let mut resp_course_key_datas = vec![];
  for x in course_key_data.into_iter() {
    let course_key = course_key_service::get_by_course_key_key(con, &x.course_key_key)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

    // only instructors may view course key data
    let is_instructor =
      course_membership_service::is_instructor(con, user.user_id, course_key.course_id)
        .await
        .map_err(report_postgres_err)?;

    if is_instructor {
      resp_course_key_datas.push(fill_course_key_data(con, x).await?);
    }
  }

  Ok(resp_course_key_datas)
}

pub async fn committment_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CommittmentViewProps,
) -> Result<Vec<response::Committment>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let committments = committment_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return committments
  let mut resp_committments = vec![];
  for x in committments.into_iter() {
    // only instructors and attendees of the committment can see their data

    let is_attendee = x.attendee_user_id == user.user_id;

    let session = session_service::get_by_session_id(con, x.session_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

    let is_instructor =
      course_membership_service::is_instructor(con, user.user_id, session.course_id)
        .await
        .map_err(report_postgres_err)?;

    if is_attendee || is_instructor {
      resp_committments.push(fill_committment(con, x).await?);
    }
  }

  Ok(resp_committments)
}

pub async fn committment_response_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::CommittmentResponseViewProps,
) -> Result<Vec<response::CommittmentResponse>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let committment_response = committment_response_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;
  // return committment_responses
  let mut resp_committment_responses = vec![];
  for x in committment_response.into_iter() {
    // only instructors and attendees of the committment can see their data
    let committment = committment_service::get_by_committment_id(con, x.committment_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::CommittmentNonexistent)?;

    let is_attendee = committment.attendee_user_id == user.user_id;

    let session = session_service::get_by_session_id(con, committment.session_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

    let is_instructor =
      course_membership_service::is_instructor(con, user.user_id, session.course_id)
        .await
        .map_err(report_postgres_err)?;

    if is_attendee || is_instructor {
      resp_committment_responses.push(fill_committment_response(con, x).await?);
    }
  }

  Ok(resp_committment_responses)
}

pub async fn session_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionViewProps,
) -> Result<Vec<response::Session>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let sessions = session_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return sessions
  let mut resp_sessions = vec![];
  for x in sessions.into_iter() {
    // members of the course can see sessions
    let is_member = course_membership_service::is_member(con, user.user_id, x.course_id)
      .await
      .map_err(report_postgres_err)?;

    if is_member {
      resp_sessions.push(fill_session(con, x).await?);
    }
  }

  Ok(resp_sessions)
}

pub async fn session_data_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionDataViewProps,
) -> Result<Vec<response::SessionData>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let session_data = session_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return session_datas
  let mut resp_session_datas = vec![];
  for x in session_data.into_iter() {
    // members of the course can see sessions

    let session = session_service::get_by_session_id(con, x.session_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SessionNonexistent)?;

    let is_member = course_membership_service::is_member(con, user.user_id, session.course_id)
      .await
      .map_err(report_postgres_err)?;

    resp_session_datas.push(fill_session_data(con, x).await?);
  }

  Ok(resp_session_datas)
}

pub async fn session_request_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionRequestViewProps,
) -> Result<Vec<response::SessionRequest>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let session_request = session_request_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return session_requests
  let mut resp_session_requests = vec![];
  for x in session_request.into_iter() {
    // attendees and instructors may view
    let is_attendee = user.user_id == x.creator_user_id;
    let is_instructor = course_membership_service::is_instructor(con, user.user_id, x.course_id)
      .await
      .map_err(report_postgres_err)?;

    if is_attendee || is_instructor {
      resp_session_requests.push(fill_session_request(con, x).await?);
    }
  }

  Ok(resp_session_requests)
}

pub async fn session_request_response_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SessionRequestResponseViewProps,
) -> Result<Vec<response::SessionRequestResponse>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let session_request_response = session_request_response_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return session_request_responses
  let mut resp_session_request_responses = vec![];
  for x in session_request_response.into_iter() {
    // attendees and instructors may view
    let session_request =
      session_request_service::get_by_session_request_id(con, x.session_request_id)
        .await
        .map_err(report_postgres_err)?
        .ok_or(response::InnexgoHoursError::SessionRequestNonexistent)?;

    let is_attendee = user.user_id == session_request.creator_user_id;
    let is_instructor =
      course_membership_service::is_instructor(con, user.user_id, session_request.course_id)
        .await
        .map_err(report_postgres_err)?;

    if is_attendee || is_instructor {
      resp_session_request_responses.push(fill_session_request_response(con, x).await?);
    }
  }

  Ok(resp_session_request_responses)
}

pub async fn school_key_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolKeyViewProps,
) -> Result<Vec<response::SchoolKey>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let school_keys = school_key_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return school_keys
  let mut resp_school_keys = vec![];
  for x in school_keys.into_iter() {
    // admins may view

    let is_admin = adminship_service::is_admin(con, user.user_id, x.school_id)
      .await
      .map_err(report_postgres_err)?;

    if is_admin {
      resp_school_keys.push(fill_school_key(con, x).await?);
    }
  }

  Ok(resp_school_keys)
}

pub async fn school_key_data_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolKeyDataViewProps,
) -> Result<Vec<response::SchoolKeyData>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let school_key_data = school_key_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return school_key_datas
  let mut resp_school_key_datas = vec![];
  for x in school_key_data.into_iter() {
    //can view if admin
    let school_key = school_key_service::get_by_school_key_key(con, &x.school_key_key)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;

    let is_admin = adminship_service::is_admin(con, user.user_id, school_key.school_id)
      .await
      .map_err(report_postgres_err)?;

    if is_admin {
      resp_school_key_datas.push(fill_school_key_data(con, x).await?);
    }
  }

  Ok(resp_school_key_datas)
}

pub async fn adminship_view(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::AdminshipViewProps,
) -> Result<Vec<response::Adminship>, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let adminships = adminship_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;

  // return adminships
  let mut resp_adminships = vec![];
  for x in adminships.into_iter() {
    // only admins may view

    let is_admin = adminship_service::is_admin(con, user.user_id, x.school_id)
      .await
      .map_err(report_postgres_err)?;

    if is_admin {
      resp_adminships.push(fill_adminship(con, x).await?);
    }
  }

  Ok(resp_adminships)
}
