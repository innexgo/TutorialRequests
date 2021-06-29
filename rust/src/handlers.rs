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
use super::course_key_service;
use super::course_key_data_service;
use super::school_key_service;
use super::school_key_data_service;
use super::course_membership_service;
use super::course_service;
use super::school_data_service;
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
  let school_key = school_key_service::get_by_school_key_key(con, school_key_data.school_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::SchoolKeyNonexistent)?;

  Ok(response::SchoolKeyData {
    school_key_data_id: school_key_data.school_key_data_id,
    creation_time: school_key_data.creation_time,
    creator_user_id: school_key_data.creator_user_id,
    school_key: fill_school_key(con, school_key).await?,
    active: school_key_data.active
  })
}

async fn fill_adminship(
  con: &mut tokio_postgres::Client,
  adminship: Adminship,
) -> Result<response::Adminship, response::InnexgoHoursError> {
  let school_key = match adminship.school_key_key {
    Some(school_key_key) => {
      let school_key =
        school_key_service::get_by_school_key_key(con, school_key_key)
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
  let course_key = course_key_service::get_by_course_key_key(con, course_key_data.course_key_key)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::CourseKeyNonexistent)?;

  Ok(response::CourseKeyData {
    course_key_data_id: course_key_data.course_key_data_id,
    creation_time: course_key_data.creation_time,
    creator_user_id: course_key_data.creator_user_id,
    course_key: fill_course_key(con, course_key).await?,
    active: course_key_data.active
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
      let course_key = course_key_service::get_by_course_key_key(con, course_key_key)
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
    attendee_user_id: session_request.attendee_user_id,
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
    cancellable: committment.cancellable,
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
  let subscription = subscription_service::add(con, user.user_id, props.subscription_kind, 0)
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

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  // check school exists and we are authorized to create on its behalf

  school_service::get_by_school_id(

  // create event
  let course = course_service::add(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?;

  // create data
  let course_data = course_data_service::add(
    &mut sp,
    user.user_id,
    course.course_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
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

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let course =
    course_service::get_by_course_id(&mut sp, props.course_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::CourseNonexistent)?;

  // validate event is owned by correct user
  if course.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::CourseNonexistent);
  }

  // now we can update data
  let course_data = course_data_service::add(
    &mut sp,
    user.user_id,
    course.course_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_course_data(con, course_data).await
}


pub async fn school_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::SchoolNewProps,
) -> Result<response::SchoolData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // create event
  let school = school_service::add(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?;

  // create data
  let school_data = school_data_service::add(
    &mut sp,
    user.user_id,
    school.school_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
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

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let school =
    school_service::get_by_school_id(&mut sp, props.school_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::SchoolNonexistent)?;

  // validate event is owned by correct user
  if school.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::SchoolNonexistent);
  }

  // now we can update data
  let school_data = school_data_service::add(
    &mut sp,
    user.user_id,
    school.school_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_school_data(con, school_data).await
}


pub async fn external_event_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::ExternalEventNewProps,
) -> Result<response::ExternalEventData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // create event
  let external_event = external_event_service::add(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?;

  // create data
  let external_event_data = external_event_data_service::add(
    &mut sp,
    user.user_id,
    external_event.external_event_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_external_event_data(con, external_event_data).await
}

pub async fn external_event_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::ExternalEventDataNewProps,
) -> Result<response::ExternalEventData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // validate
  if props.start_time < 0 {
    return Err(response::InnexgoHoursError::NegativeStartTime);
  }
  if props.start_time >= props.end_time {
    return Err(response::InnexgoHoursError::NegativeDuration);
  }

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let external_event =
    external_event_service::get_by_external_event_id(&mut sp, props.external_event_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::ExternalEventNonexistent)?;

  // validate event is owned by correct user
  if external_event.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::ExternalEventNonexistent);
  }

  // now we can update data
  let external_event_data = external_event_data_service::add(
    &mut sp,
    user.user_id,
    external_event.external_event_id,
    props.name,
    props.start_time,
    props.end_time,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_external_event_data(con, external_event_data).await
}

pub async fn goal_intent_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::GoalIntentNewProps,
) -> Result<response::GoalIntentData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // create intent
  let goal_intent = goal_intent_service::add(&mut sp, user.user_id)
    .await
    .map_err(report_postgres_err)?;

  // create data
  let goal_intent_data = goal_intent_data_service::add(
    &mut sp,
    user.user_id,
    goal_intent.goal_intent_id,
    props.name,
    true,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_goal_intent_data(con, goal_intent_data).await
}

pub async fn goal_intent_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::GoalIntentDataNewProps,
) -> Result<response::GoalIntentData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  let goal_intent = goal_intent_service::get_by_goal_intent_id(&mut sp, props.goal_intent_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::GoalIntentNonexistent)?;

  // validate intent is owned by correct user
  if goal_intent.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::GoalIntentNonexistent);
  }

  // now we can update data
  let goal_intent_data = goal_intent_data_service::add(
    &mut sp,
    user.user_id,
    goal_intent.goal_intent_id,
    props.name,
    props.active,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_goal_intent_data(con, goal_intent_data).await
}

pub async fn goal_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::GoalNewProps,
) -> Result<response::GoalData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // validate start and end time
  if let Some((start_time, end_time)) = props.time_span {
    if start_time < 0 {
      return Err(response::InnexgoHoursError::NegativeStartTime);
    }
    if start_time >= end_time {
      return Err(response::InnexgoHoursError::NegativeDuration);
    }
  }

  // ensure time utility function exists and belongs to you
  let time_utility_function = time_utility_function_service::get_by_time_utility_function_id(
    &mut sp,
    props.time_utility_function_id,
  )
  .await
  .map_err(report_postgres_err)?
  .ok_or(response::InnexgoHoursError::TimeUtilityFunctionNonexistent)?;
  // validate intent is owned by correct user
  if time_utility_function.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::TimeUtilityFunctionNonexistent);
  }

  // validate that parent exists and belongs to you
  if let Some(parent_goal_id) = props.parent_goal_id {
    let goal = goal_service::get_by_goal_id(&mut sp, parent_goal_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::GoalNonexistent)?;
    // validate intent is owned by correct user
    if goal.creator_user_id != user.user_id {
      return Err(response::InnexgoHoursError::GoalNonexistent);
    }
  }

  // validate that intent exists and belongs to you
  if let Some(goal_intent_id) = props.goal_intent_id {
    let goal_intent = goal_intent_service::get_by_goal_intent_id(&mut sp, goal_intent_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::GoalIntentNonexistent)?;
    // validate intent is owned by correct user
    if goal_intent.creator_user_id != user.user_id {
      return Err(response::InnexgoHoursError::GoalIntentNonexistent);
    }
  }

  // create goal
  let goal = goal_service::add(&mut sp, user.user_id, props.goal_intent_id)
    .await
    .map_err(report_postgres_err)?;

  // create goal data
  let goal_data = goal_data_service::add(
    &mut sp,
    user.user_id,
    goal.goal_id,
    props.name,
    props.tags,
    props.duration_estimate,
    props.time_utility_function_id,
    props.parent_goal_id,
    props.time_span,
    request::GoalDataStatusKind::Pending,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_goal_data(con, goal_data).await
}

pub async fn goal_data_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::GoalDataNewProps,
) -> Result<response::GoalData, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  let con = &mut *db.lock().await;

  let mut sp = con.transaction().await.map_err(report_postgres_err)?;

  // validate start and end time
  if let Some((start_time, end_time)) = props.time_span {
    if start_time < 0 {
      return Err(response::InnexgoHoursError::NegativeStartTime);
    }
    if start_time >= end_time {
      return Err(response::InnexgoHoursError::NegativeDuration);
    }
  }

  // ensure time utility function exists and belongs to you
  let time_utility_function = time_utility_function_service::get_by_time_utility_function_id(
    &mut sp,
    props.time_utility_function_id,
  )
  .await
  .map_err(report_postgres_err)?
  .ok_or(response::InnexgoHoursError::TimeUtilityFunctionNonexistent)?;
  // validate intent is owned by correct user
  if time_utility_function.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::TimeUtilityFunctionNonexistent);
  }

  // validate that parent exists and belongs to you
  if let Some(parent_goal_id) = props.parent_goal_id {
    let goal = goal_service::get_by_goal_id(&mut sp, parent_goal_id)
      .await
      .map_err(report_postgres_err)?
      .ok_or(response::InnexgoHoursError::GoalNonexistent)?;
    // validate intent is owned by correct user
    if goal.creator_user_id != user.user_id {
      return Err(response::InnexgoHoursError::GoalNonexistent);
    }
  }

  // ensure that goal exists and belongs to you
  let goal = goal_service::get_by_goal_id(&mut sp, props.goal_id)
    .await
    .map_err(report_postgres_err)?
    .ok_or(response::InnexgoHoursError::GoalNonexistent)?;
  // validate intent is owned by correct user
  if goal.creator_user_id != user.user_id {
    return Err(response::InnexgoHoursError::GoalNonexistent);
  }

  // create goal data
  let goal_data = goal_data_service::add(
    &mut sp,
    user.user_id,
    goal.goal_id,
    props.name,
    props.tags,
    props.duration_estimate,
    props.time_utility_function_id,
    props.parent_goal_id,
    props.time_span,
    props.status,
  )
  .await
  .map_err(report_postgres_err)?;

  sp.commit().await.map_err(report_postgres_err)?;

  // return json
  fill_goal_data(con, goal_data).await
}

pub async fn time_utility_function_new(
  _config: Config,
  db: Db,
  auth_service: AuthService,
  props: request::TimeUtilityFunctionNewProps,
) -> Result<response::TimeUtilityFunction, response::InnexgoHoursError> {
  // validate api key
  let user = get_user_if_api_key_valid(&auth_service, props.api_key).await?;

  // check that utils length == start_times length
  if props.start_times.len() != props.utils.len() {
    return Err(response::InnexgoHoursError::TimeUtilityFunctionNotValid);
  }

  let con = &mut *db.lock().await;

  // create tuf
  let time_utility_function =
    time_utility_function_service::add(con, user.user_id, props.start_times, props.utils)
      .await
      .map_err(report_postgres_err)?;

  // return json
  fill_time_utility_function(con, time_utility_function).await
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
  for u in schools
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
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
  let user = get_user_if_api_key_valid(&auth_service, props.api_key.clone()).await?;

  let con = &mut *db.lock().await;
  // get users
  let school_data = school_data_service::query(con, props)
    .await
    .map_err(report_postgres_err)?;
  // return users
  // return school_datas
  let mut resp_school_datas = vec![];
  for u in school_data
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
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
  for u in courses
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_courses.push(fill_course(con, u).await?);
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
  for u in course_data
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_course_datas.push(fill_course_data(con, u).await?);
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
  for u in course_memberships
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_course_memberships.push(fill_course_membership(con, u).await?);
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
  for u in course_keys
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_course_keys.push(fill_course_key(con, u).await?);
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
  for u in course_key_data
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_course_key_datas.push(fill_course_key_data(con, u).await?);
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
  for u in committments
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_committments.push(fill_committment(con, u).await?);
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
  // return users
  // return committment_responses
  let mut resp_committment_responses = vec![];
  for u in committment_response
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_committment_responses.push(fill_committment_response(con, u).await?);
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
  for u in sessions
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_sessions.push(fill_session(con, u).await?);
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
  for u in session_data
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_session_datas.push(fill_session_data(con, u).await?);
  }

  Ok(resp_session_datas)
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
  for u in school_keys
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_school_keys.push(fill_school_key(con, u).await?);
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
  for u in school_key_data
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_school_key_datas.push(fill_school_key_data(con, u).await?);
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
  for u in adminships
    .into_iter()
    .filter(|u| u.creator_user_id == user.user_id)
  {
    resp_adminships.push(fill_adminship(con, u).await?);
  }

  Ok(resp_adminships)
}

