-- Table Structure
-- Primary Key
-- Creation Time
-- Creator User Id (if applicable)


drop table if exists password_reset;
create table password_reset(
  password_reset_key_hash char(64) not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
);

drop table if exists password( 
  password_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  user_id integer not null,
  password_kind integer not null, -- CHANGE | RESET | CANCEL
  password_hash char(64) not null, -- only valid if RESET | CANCEL
  password_reset_key_hash char(64) not null -- only valid if password_kind == RESET
);

drop table if exists verification_challenge;
create table verification_challenge(
  verification_challenge_key_hash char(64) not null primary key,
  creation_time integer not null,
  name varchar(100) not null,
  email varchar(100) not null,
  password_hash char(64) not null
);

drop table if exists user;
create table user(
  user_id integer not null primary key,
  creation_time integer not null,
  name varchar(100) not null,
  email varchar(100) not null unique,
  verification_challenge_key_hash char(64) not null unique
);

drop table if exists school;
create table school(
  school_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  name varchar(100) not null,
  abbreviation varchar(100) not null
);

drop table if exists adminship;
create table adminship(
  adminship_id not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  user_id integer not null,
  school_id integer not null,
  adminship_kind integer not null -- ADMIN, CANCEL
);

drop table if exists location;
create table location(
  location_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  school_id integer not null,
  name varchar(100) not null,
  description varchar(100) not null,
  valid integer not null -- boolean
);

drop table if exists course;
create table course(
  course_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  school_id integer not null,
  name varchar(100) not null,
  description varchar(100) not null,
);

drop table if exists course_password;
create table course_password(
  course_password_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  course_password_kind integer not null, -- CHANGE | CANCEL
  password_hash integer not null -- only valid if course_join_kind == PASSWORD
);

-- Many to Many mapper for users to course
drop table if exists course_membership;
create table course_membership(
  course_membership_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  user_id integer not null,
  course_id integer not null,
  course_membership_kind integer not null -- STUDENT, INSTRUCTOR, CANCEL
);

drop table if exists subscription;
create table subscription(
  subscription_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  school_id integer not null,
  duration integer not null
);

drop table if exists invoice;
create table invoice(
  invoice_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  subscription_id integer not null,
  amount_cents integer not null,
);

drop table if exists api_key;
create table api_key(
  api_key_hash char(64) not null,
  creation_time integer not null,
  creator_user_id integer not null,
  duration integer not null,
  api_key_kind integer not null -- VALID, CANCEL
);

-- Represents a specific instance of a course
drop table if exists session;
create table session(
  session_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  course_id integer not null,
  location_id integer not null,
  name varchar(100) not null,
  start_time integer not null,
  duration integer not null,
  hidden integer not null       -- boolean
);

-- a request from a student to a course for a specific time
-- it's up to the teacher which course session to allocate to the student
drop table if exists session_request;
create table session_request(
  session_request_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  attendee_user_id integer not null,
  course_id integer not null,
  message varchar(100) not null,
  start_time integer not null,
  duration integer not null
);

-- a response to the course session request
drop table if exists session_request_response; 
create table session_request_response(
  session_request_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  message varchar(100) not null,
  accepted integer not null, -- boolean
  accepted_committment_id integer not null -- only valid if accepted == true
);

-- a committment to attend a course session
drop table if exists committment;
create table committment( 
  committment_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  attendee_user_id integer not null,
  session_id integer not null,
  cancellable integer not null -- boolean
);

-- a response to the commitment
drop table if exists committment_response;
create table committment_response(
  committment_id integer not null primary key,
  creation_time integer not null,
  creator_user_id integer not null,
  committment_response_kind integer not null -- can be PRESENT(0), TARDY(1), ABSENT(2), CANCEL(3)
);
