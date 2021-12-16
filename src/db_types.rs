use either::Either;
use innexgo_hours_api::request::AdminshipKind;
use innexgo_hours_api::request::CourseMembershipKind;
use innexgo_hours_api::request::EncounterKind;
use innexgo_hours_api::request::SubscriptionKind;

#[derive(Clone, Debug)]
pub struct Subscription {
  pub subscription_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub subscription_kind: SubscriptionKind,
  pub max_uses: i64,
  pub payment_id: i64,
}

#[derive(Clone, Debug)]
pub struct School {
  pub school_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub whole: bool,
}

#[derive(Clone, Debug)]
pub struct SchoolData {
  pub school_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
  pub name: String,
  pub description: String,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct SchoolDuration {
  pub school_duration_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
}

#[derive(Clone, Debug)]
pub struct SchoolDurationData {
  pub school_duration_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_duration_id: i64,
  pub day: i64,
  pub minute_start: i64,
  pub minute_end: i64,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct SchoolKey {
  pub school_key_key: String,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
  pub start_time: i64,
  pub end_time: i64,
}

#[derive(Clone, Debug)]
pub struct SchoolKeyData {
  pub school_key_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_key_key: String,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct Adminship {
  pub adminship_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub user_id: i64,
  pub school_id: i64,
  pub adminship_kind: AdminshipKind,
  pub school_key_key: Option<String>,
}

#[derive(Clone, Debug)]
pub struct Location {
  pub location_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
}

#[derive(Clone, Debug)]
pub struct LocationData {
  pub location_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub location_id: i64,
  pub name: String,
  pub address: String,
  pub phone: String,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct Course {
  pub course_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub school_id: i64,
}

#[derive(Clone, Debug)]
pub struct CourseData {
  pub course_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
  pub location_id: i64,
  pub name: String,
  pub description: String,
  pub homeroom: bool,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct CourseKey {
  pub course_key_key: String,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
  pub max_uses: i64,
  pub course_membership_kind: CourseMembershipKind,
  pub start_time: i64,
  pub end_time: i64,
}

#[derive(Clone, Debug)]
pub struct CourseKeyData {
  pub course_key_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_key_key: String,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct CourseMembership {
  pub course_membership_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub user_id: i64,
  pub course_id: i64,
  pub course_membership_kind: CourseMembershipKind,
  pub course_key_key: Option<String>,
}

#[derive(Clone, Debug)]
pub struct Session {
  pub session_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
}

#[derive(Clone, Debug)]
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

#[derive(Clone, Debug)]
pub struct SessionRequest {
  pub session_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub course_id: i64,
  pub message: String,
  pub start_time: i64,
  pub end_time: i64,
}

#[derive(Clone, Debug)]
pub struct SessionRequestResponse {
  pub session_request_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub message: String,
  pub commitment_id: Option<i64>,
}

#[derive(Clone, Debug)]
pub struct Commitment {
  pub commitment_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub attendee_user_id: i64,
  pub session_id: i64,
  pub active: bool,
}

#[derive(Clone, Debug)]
pub struct Encounter {
  pub encounter_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub location_id: i64,
  pub attendee_user_id: i64,
  pub encounter_kind: EncounterKind,
}

#[derive(Clone, Debug)]
pub struct Stay {
  pub stay_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub location_id: i64,
  pub attendee_user_id: i64,
}

// left is encounter_id, right is timestamp
#[derive(Clone, Debug)]
pub struct StayData {
  pub stay_data_id: i64,
  pub creation_time: i64,
  pub creator_user_id: i64,
  pub stay_id: i64,
  pub fst: Either<i64, i64>,
  pub snd: Either<i64, i64>,
  pub active: bool,
}
