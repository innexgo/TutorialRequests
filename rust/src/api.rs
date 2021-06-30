use super::handlers;
use super::utils;
use super::Config;
use super::Db;
use super::SERVICE_NAME;
use auth_service_api::client::AuthService;
use std::collections::HashMap;
use std::convert::Infallible;
use std::future::Future;
use innexgo_hours_api::response::InnexgoHoursError;
use warp::http::StatusCode;
use warp::Filter;

/// The function that will show all ones to call
pub fn api(
  config: Config,
  db: Db,
  auth_service: AuthService,
) -> impl Filter<Extract = impl warp::Reply, Error = Infallible> + Clone {

  // public API
  api_info()
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "subscription" / "new"),
      handlers::subscription_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course" / "new"),
      handlers::course_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_data" / "new"),
      handlers::course_data_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_key" / "new"),
      handlers::course_key_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_key_data" / "new"),
      handlers::course_key_data_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_membership" / "new_key"),
      handlers::course_membership_new_key,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_membership" / "new_cancel"),
      handlers::course_membership_new_cancel,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school" / "new"),
      handlers::school_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_data" / "new"),
      handlers::school_data_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_key" / "new"),
      handlers::school_key_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_key_data" / "new"),
      handlers::school_key_data_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "adminship" / "new_key"),
      handlers::adminship_new_key,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "adminship" / "new_cancel"),
      handlers::adminship_new_cancel,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session_request" / "new"),
      handlers::session_request_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session_request_response" / "new"),
      handlers::session_request_response_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session" / "new"),
      handlers::session_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session_data" / "new"),
      handlers::session_data_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "committment" / "new"),
      handlers::committment_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "committment_response" / "new"),
      handlers::committment_response_new,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "subscription" / "view"),
      handlers::subscription_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school" / "view"),
      handlers::school_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_data" / "view"),
      handlers::school_data_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_key" / "view"),
      handlers::school_key_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "school_key_data" / "view"),
      handlers::school_key_data_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course" / "view"),
      handlers::course_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_data" / "view"),
      handlers::course_data_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session" / "view"),
      handlers::session_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "session_data" / "view"),
      handlers::session_data_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_key" / "view"),
      handlers::course_key_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_key_data" / "view"),
      handlers::course_key_data_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "course_membership" / "view"),
      handlers::course_membership_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "adminship" / "view"),
      handlers::adminship_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "committment" / "view"),
      handlers::committment_view,
    ))
    .or(adapter(
      config.clone(),
      db.clone(),
      auth_service.clone(),
      warp::path!("public" / "committment_response" / "view"),
      handlers::committment_response_view,
    ))
    .recover(handle_rejection)
}

fn api_info() -> impl Filter<Extract = impl warp::Reply, Error = warp::Rejection> + Clone {
  let mut info = HashMap::new();
  info.insert("version", "0.1");
  info.insert("name", SERVICE_NAME);
  warp::path!("info").map(move || warp::reply::json(&info))
}

// this function adapts a handler function to a warp filter
// it accepts an initial path filter
fn adapter<PropsType, ResponseType, F>(
  config: Config,
  db: Db,
  auth_service: AuthService,
  filter: impl Filter<Extract = (), Error = warp::Rejection> + Clone,
  handler: fn(Config, Db, AuthService, PropsType) -> F,
) -> impl Filter<Extract = impl warp::Reply, Error = warp::Rejection> + Clone
where
  F: Future<Output = Result<ResponseType, InnexgoHoursError>> + Send,
  PropsType: Send + serde::de::DeserializeOwned,
  ResponseType: Send + serde::ser::Serialize,
{
  // lets you pass in an arbitrary parameter
  fn with<T: Clone + Send>(t: T) -> impl Filter<Extract = (T,), Error = Infallible> + Clone {
    warp::any().map(move || t.clone())
  }

  filter
    .and(with(config))
    .and(with(db))
    .and(with(auth_service))
    .and(warp::body::json())
    .and_then(async move |config, db, auth_service, props| {
      handler(config, db, auth_service, props)
        .await
        .map_err(innexgo_hours_error)
    })
    .map(|x| warp::reply::json(&Ok::<_, ()>(x)))
}

// This function receives a `Rejection` and tries to return a custom
// value, otherwise simply passes the rejection along.
async fn handle_rejection(err: warp::Rejection) -> Result<impl warp::Reply, Infallible> {
  let code;
  let message;

  if err.is_not_found() {
    code = StatusCode::NOT_FOUND;
    message = InnexgoHoursError::NotFound;
  } else if err
    .find::<warp::filters::body::BodyDeserializeError>()
    .is_some()
  {
    message = InnexgoHoursError::DecodeError;
    code = StatusCode::BAD_REQUEST;
  } else if err.find::<warp::reject::MethodNotAllowed>().is_some() {
    code = StatusCode::METHOD_NOT_ALLOWED;
    message = InnexgoHoursError::MethodNotAllowed;
  } else if let Some(InnexgoHoursErrorRejection(innexgo_hours_error)) = err.find() {
    code = StatusCode::BAD_REQUEST;
    message = innexgo_hours_error.clone();
  } else {
    // We should have expected this... Just log and say its a 500
    utils::log(utils::Event {
      msg: "intercepted unknown error kind".to_owned(),
      source: format!("{:#?}", err),
      severity: utils::SeverityKind::Error,
    });
    code = StatusCode::INTERNAL_SERVER_ERROR;
    message = InnexgoHoursError::Unknown;
  }

  Ok(warp::reply::with_status(
    warp::reply::json(&Err::<(), _>(message)),
    code,
  ))
}

// This type represents errors that we can generate
// These will be automatically converted to a proper string later
#[derive(Debug)]
pub struct InnexgoHoursErrorRejection(pub InnexgoHoursError);
impl warp::reject::Reject for InnexgoHoursErrorRejection {}

fn innexgo_hours_error(innexgo_hours_error: InnexgoHoursError) -> warp::reject::Rejection {
  warp::reject::custom(InnexgoHoursErrorRejection(innexgo_hours_error))
}
