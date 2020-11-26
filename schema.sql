drop table if exists email_blacklist;
create table email_blacklist(
  id integer not null primary key,
  email varchar(100) not null,
  reason varchar(10) not null,
  last_update_time integer not null
);

drop table if exists password_reset_key;
create table password_reset_key(
  id integer not null primary key,
  email varchar(100) not null,
  creation_time integer not null,
  reset_key char(64) not null unique,
  valid integer not null
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
  creation_time integer not null,
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
  key_hash char(64) not null unique,
  valid integer not null -- boolean
);

-- Represents a specific instance of a course
drop table if exists session;
create table session(
  session_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  name varchar(100) not null,
  host_id integer not null,
  start_time integer not null,
  duration integer not null,
  hidden integer not null       -- boolean
);

-- a request from a student to a course for a specific time
-- it's up to the teacher which course session to allocate to the student
drop table if exists session_request;
create table session_request(
  session_request_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  attendee_id integer not null,
  host_id integer not null,
  message varchar(100) not null,
  start_time integer not null,
  duration integer not null
);

-- a response to the course session request
drop table if exists session_request_response; 
create table session_request_response(
  session_request_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  message varchar(100) not null,
  accepted integer not null, -- boolean
  accepted_committment_id integer not null -- only valid if accepted == true
);

-- a committment to attend a course session
drop table if exists committment;
create table committment( 
  committment_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  attendee_id integer not null,
  session_id integer not null,
  cancellable integer not null -- boolean
);

-- a response to the commitment
drop table if exists committment_response;
create table committment_response(
  committment_id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  committment_response_kind integer not null -- can be PRESENT(0), TARDY(1), ABSENT(2), CANCELLED(3)
);
