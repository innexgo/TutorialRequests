use innexgo_hours_api::request::AdminshipKind;
use innexgo_hours_api::request::CommittmentResponseKind;
use innexgo_hours_api::request::CourseMembershipKind;
use innexgo_hours_api::request::SubscriptionKind;

pub struct Subscription {
  pub subscription_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub subscription_kind: SubscriptionKind,
  pub payment_id: i64,
}

pub struct School {
  pub school_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub whole: bool,
}

pub struct SchoolData {
  pub school_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
  pub name: String,
  pub description: String,
  pub active: bool,
}

pub struct AdminshipRequest {
  pub adminship_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
  pub message: String,
}

pub struct AdminshipRequestResponse {
  pub adminship_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub message: String,
  pub accepted: bool,
}

pub struct Adminship {
  pub adminship_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub user_id: i64,
  pub school_id: i64,
  pub adminship_kind: AdminshipKind,
  pub adminship_request_response_id: Option<i64>,
}

pub struct Location {
  pub location_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
  pub name: String,
  pub description: String,
}

pub struct Course {
  pub course_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
}

pub struct CourseData {
  pub course_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
  pub name: String,
  pub description: String,
  pub active: bool,
}

pub struct CourseKey {
  pub course_key_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
  pub key: String,
  pub duration: i64,
  pub max_uses: i64,
  pub course_membership_kind: Option<CourseMembershipKind>,
}

pub struct CourseMembership {
  pub course_membership_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub user_id: i64,
  pub course_id: i64,
  pub course_membership_kind: CourseMembershipKind,
  pub course_key_id: Option<i64>,
}

pub struct Session {
  pub session_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
}

pub struct SessionData {
  pub session_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub session_id: i64,
  pub name: String,
  pub start_time: i64,
  pub end_time: i64,
  pub active: bool,
}

pub struct SessionRequest {
  pub session_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub attendee_user_id: i64,
  pub course_id: i64,
  pub message: String,
  pub start_time: i64,
  pub end_time: i64,
}

pub struct SessionRequestResponse {
  pub session_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub message: String,
  pub committment_id: Option<i64>,
}

pub struct Committment {
  pub committment_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub attendee_user_id: i64,
  pub session_id: i64,
  pub cancellable: bool,
}

pub struct CommittmentResponse {
  pub committment_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub committment_response_kind: CommittmentResponseKind,
}
