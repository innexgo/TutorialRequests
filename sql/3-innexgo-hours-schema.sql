CREATE DATABASE innexgo_hours;
\c innexgo_hours

-- Table Structure
-- Primary Key
-- Creation Time
-- Creator User Id (if applicable)
-- Everything else

-- Table Structure
-- Primary Key
-- Creation Time
-- Creator User Id (if applicable)
-- Everything else

drop table if exists subscription;
create table subscription(
  subscription_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  subscription_kind bigint not null, -- VALID | CANCEL
  max_uses bigint not null,
  payment_id bigint not null -- only valid if VALID
);

-- there can be multiple schools with full_school = false, but only one with full_school = true
-- whole is when the entire school district / school has signed on 
-- !whole when one or more teachers is managing the school
-- You can only create a school when you have a valid subscription
-- Also, we no longer let you add random people to an adminship, you must create a school_key
drop table if exists school;
create table school(
  school_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  whole bool not null
);

drop table if exists school_data;
create table school_data(
  school_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null,
  name text not null,
  description text not null,
  active bool not null
);

drop table if exists school_key;
create table school_key(
  school_key_key text primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null,
  start_time bigint not null,
  end_time bigint not null
);

drop table if exists school_key_data;
create table school_key_data(
  school_key_data_id bigserial primary key, 
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_key_key text not null,
  active bool not null
);

drop table if exists adminship;
create table adminship(
  adminship_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  user_id bigint not null,
  school_id bigint not null,
  adminship_kind bigint not null, -- ADMIN, CANCEL
  school_key_key text -- NULLABLE (not all adminships are from keys
);

drop table if exists course;
create table course(
  course_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null
);

drop table if exists course_data;
create table course_data(
  course_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null,
  name text not null,
  description text not null,
  active bool not null 
);

drop table if exists course_key;
create table course_key(
  course_key_key text primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null,
  max_uses bigint not null,
  course_membership_kind bigint not null,
  start_time bigint not null,
  end_time bigint not null
);

drop table if exists course_key_data;
create table course_key_data(
  course_key_data_id bigserial primary key, 
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_key_key text not null,
  active bool not null
);

-- Many to Many mapper for users to course
drop table if exists course_membership;
create table course_membership(
  course_membership_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  user_id bigint not null,
  course_id bigint not null,
  course_membership_kind bigint not null, -- STUDENT | INSTRUCTOR | CANCEL
  course_key_key text -- NULLABLE
);

-- Represents a specific instance of a course
drop table if exists session;
create table session(
  session_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null
);

drop table if exists session_data;
create table session_data(
  session_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  session_id bigint not null,
  name text not null,
  start_time bigint not null,
  end_time bigint not null,
  active bool not null
);

-- a request from a student to a course for a specific time
-- it's up to the teacher which course session to allocate to the student
drop table if exists session_request;
create table session_request(
  session_request_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null,
  message text not null,
  start_time bigint not null,
  end_time bigint not null
);

-- a response to the course session request
drop table if exists session_request_response; 
create table session_request_response(
  session_request_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  message text not null,
  committment_id bigint -- NULLABLE
);

-- a committment to attend a course session
drop table if exists committment;
create table committment( 
  committment_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  attendee_user_id bigint not null,
  session_id bigint not null
);

-- a response to the commitment
drop table if exists committment_response;
create table committment_response(
  committment_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  committment_response_kind bigint not null -- can be PRESENT, TARDY, ABSENT, CANCEL
);
