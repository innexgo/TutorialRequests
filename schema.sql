drop table if exists email_blacklist;
create table email_blacklist(
  id integer not null primary key,
  email varchar(100) not null,
  reason varchar(10) not null,
  last_update_time integer not null
);

drop table if exists forgot_password;
create table forgot_password(
  id integer not null primary key,
  email varchar(100) not null,
  creation_time integer not null,
  reset_key char(64) not null unique,
  used integer not null
);

drop table if exists email_verification_challenge;
create table email_verification_challenge(
  id integer not null primary key,
  name integer not null,
  email varchar(100) not null,
  creation_time integer not null,
  verification_key char(64) not null unique,
  password_hash char(64) not null,
  kind integer not null
);

drop table if exists user;
create table user(
  id integer not null primary key,
  kind integer not null,
  name varchar(100) not null,
  email varchar(100) not null unique,
  password_set_time integer not null,
  password_hash char(64) not null
);

drop table if exists api_key;
create table api_key(
  id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  duration integer not null,
  key_hash char(64) not null unique
);

-- Represents a specific class
-- Publically visible
drop table if exists course;
create table course(
  id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  name varchar(100) not null,
  description varchar(100) not null,
  host_id integer not null
);

-- Represents a specific instance of a course
drop table if exists course_session;
create table course_session(
  id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  course_id integer not null,
  start_time integer not null,
  duration integer not null,
  private integer not null       -- boolean
);

-- a request from a student to a course for a specific time
-- it's up to the teacher which course session to allocate to the student
drop table if exists course_session_request;
create table course_session_request(
  course_session_request_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  attendee_id integer not null,
  course_id integer not null,
  message varchar(100) not null,
  start_time integer not null,
  duration integer not null
);

-- a response to the course session request
drop table if exists course_session_request_response; 
create table course_session_request_response(
  course_session_request_id integer not null primary key,
  course_session_request_response_kind integer not null, -- can be ACCEPTED(0), REJECTED(1)
  response_message varchar(100) not null,
  creation_time integer not null,
  accepted_committment_id integer not null, -- only valid if course_session_request_response_kind == ACCEPTED
);

-- a committment to attend a course session
drop table if exists committment;
create table committment( 
  committment_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  attendee_id integer not null,
  course_session_id integer not null,
  cancellable integer not null -- boolean
);

--  aresponse to the commitment
drop table if exists committment_response;
create table committment_response(
  committment_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  committment_response_kind integer not null -- can be PRESENT(0), TARDY(1), ABSENT(2), CANCELLED(3)
);
