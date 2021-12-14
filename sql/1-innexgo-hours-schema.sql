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

drop table if exists subscription_t cascade;
create table subscription_t(
  subscription_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  subscription_kind bigint not null, -- VALID | CANCEL
  max_uses bigint not null,
  payment_id bigint not null -- only valid if VALID
);


create view recent_subscription_v as
  select s.* from subscription_t s
  inner join (
   select max(subscription_id) id 
   from subscription_t 
   group by creator_user_id
  ) maxids
  on maxids.id = s.subscription_id;


-- there can be multiple schools with full_school = false, but only one with full_school = true
-- whole is when the entire school district / school has signed on 
-- !whole when one or more teachers is managing the school
-- You can only create a school when you have a valid subscription
-- Also, we no longer let you add random people to an adminship, you must create a school_key
drop table if exists school_t cascade;
create table school_t(
  school_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  whole bool not null
);

drop table if exists school_data_t cascade;
create table school_data_t(
  school_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null references school_t(school_id),
  name text not null,
  description text not null,
  active bool not null
);

create view recent_school_data_v as
  select sd.* from school_data_t sd
  inner join (
   select max(school_data_id) id 
   from school_data_t 
   group by school_id
  ) maxids
  on maxids.id = sd.school_data_id;


-- represents a block of time during which the appointments can be made
drop table if exists school_duration_t cascade;
create table school_duration_t(
  school_duration_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null references school_t(school_id)
);

drop table if exists school_duration_data_t cascade;
create table school_duration_data_t(
  school_duration_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_duration_id bigint not null references school_duration_t(school_duration_id),
  day bigint not null,
  minute_start bigint not null,
  minute_end bigint not null,
  active bool not null
);

create view recent_school_duration_data_v as
  select sdd.* from school_duration_data_t sdd
  inner join (
   select max(school_duration_data_id) id 
   from school_duration_data_t 
   group by school_duration_id
  ) maxids
  on maxids.id = sdd.school_duration_data_id;


drop table if exists school_key_t cascade;
create table school_key_t(
  school_key_key text primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null references school_t(school_id),
  start_time bigint not null,
  end_time bigint not null
);

drop table if exists school_key_data_t cascade;
create table school_key_data_t(
  school_key_data_id bigserial primary key, 
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_key_key text not null references school_key_t(school_key_key),
  active bool not null
);

create view recent_school_key_data_v as
  select skd.* from school_key_data_t skd
  inner join (
   select max(school_key_data_id) id 
   from school_key_data_t
   group by school_key_key
  ) maxids
  on maxids.id = skd.school_key_data_id;

drop table if exists adminship_t cascade;
create table adminship_t(
  adminship_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  user_id bigint not null,
  school_id bigint not null references school_t(school_id),
  adminship_kind bigint not null, -- ADMIN, CANCEL
  school_key_key text -- NULLABLE (not all adminships are from keys
);

create view recent_adminship_v as
  select a.* from adminship_t a
  inner join (
   select max(adminship_id) id 
   from adminship_t
   group by user_id, school_id
  ) maxids
  on maxids.id = a.adminship_id;

-- represents a named location
-- locations must be physical, 
drop table if exists location_t cascade;
create table location_t(
  location_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null
);

-- data about the location itself
drop table if exists location_data_t cascade;
create table location_data_t(
  location_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  location_id bigint not null references location_t(location_id),
  name text not null,
  address text not null,
  phone text not null,
  active bool not null
);

create view recent_location_data_v as
  select ld.* from location_data_t ld
  inner join (
   select max(location_data_id) id 
   from location_data_t 
   group by location_id
  ) maxids
  on maxids.id = ld.location_data_id;

-- a course run by a teacher. represents one class offering
drop table if exists course_t cascade;
create table course_t(
  course_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  school_id bigint not null references school_t(school_id)
);

-- data about the course 
drop table if exists course_data_t cascade;
create table course_data_t(
  course_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null references course_t(course_id),
  location_id bigint not null references location_t(location_id),
  name text not null,
  description text not null,
  homeroom bool not null, 
  active bool not null 
);

create view recent_course_data_v as
  select cd.* from course_data_t cd
  inner join (
   select max(course_data_id) id 
   from course_data_t 
   group by course_id
  ) maxids
  on maxids.id = cd.course_data_id;

drop table if exists course_key_t cascade;
create table course_key_t(
  course_key_key text primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null references course_t(course_id),
  max_uses bigint not null,
  course_membership_kind bigint not null,
  start_time bigint not null,
  end_time bigint not null
);

drop table if exists course_key_data_t cascade;
create table course_key_data_t(
  course_key_data_id bigserial primary key, 
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_key_key text not null references course_key_t(course_key_key),
  active bool not null
);

create view recent_course_key_data_v as
  select ckd.* from course_key_data_t ckd
  inner join (
   select max(course_key_data_id) id 
   from course_key_data_t
   group by course_key_key
  ) maxids
  on maxids.id = ckd.course_key_data_id;


-- Many to Many mapper for users to course
drop table if exists course_membership_t cascade;
create table course_membership_t(
  course_membership_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  user_id bigint not null,
  course_id bigint not null references course_t(course_id),
  course_membership_kind bigint not null, -- STUDENT | INSTRUCTOR | CANCEL
  course_key_key text references course_key_t(course_key_key) -- NULLABLE
);

create view recent_course_membership_v as
  select cm.* from course_membership_t cm
  inner join (
   select max(course_membership_id) id 
   from course_membership_t
   group by user_id, course_id
  ) maxids
  on maxids.id = cm.course_membership_id;


-- Represents a specific instance of a course
drop table if exists session_t cascade;
create table session_t(
  session_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null references course_t(course_id)
);

drop table if exists session_data_t cascade;
create table session_data_t(
  session_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  session_id bigint not null references session_t(session_id),
  name text not null,
  start_time bigint not null,
  end_time bigint not null,
  active bool not null
);

create view recent_session_data_v as
  select sd.* from session_data_t sd
  inner join (
   select max(session_data_id) id 
   from session_data_t 
   group by session_id
  ) maxids
  on maxids.id = sd.session_data_id;


-- a request from a student to a course for a specific time
-- it's up to the teacher which course session to allocate to the student
drop table if exists session_request_t cascade;
create table session_request_t(
  session_request_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  course_id bigint not null references course_t(course_id),
  message text not null,
  start_time bigint not null,
  end_time bigint not null
);

-- a committment to attend a course session
drop table if exists committment_t cascade;
create table committment_t( 
  committment_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  attendee_user_id bigint not null,
  session_id bigint not null references session_t(session_id),
  active bool not null
);

create view recent_committment as
  select c.* from committment_t c
  inner join (
   select max(committment_id) id 
   from session_data_t 
   group by session_id, attendee_user_id
  ) maxids
  on maxids.id = c.committment_id;

-- a response to the course session request
drop table if exists session_request_response_t cascade;
create table session_request_response_t(
  session_request_id bigserial primary key references session_request_t(session_request_id),
  creation_time bigint not null,
  creator_user_id bigint not null,
  message text not null,
  committment_id bigint references committment_t(committment_id) -- NULLABLE
);

drop table if exists encounter_t cascade;
create table encounter_t(
  encounter_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  location_id bigint not null references location_t(location_id),
  attendee_user_id bigint not null,
  encounter_kind bigint not null -- HARDWARE | MANUAL
);

-- represents a stay at a location where both the sign in and out were recorded
-- can be edited by a teacher
drop table if exists stay_t cascade;
create table stay_t(
  stay_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  attendee_user_id bigint not null
);

-- the time variants are used when the teacher edits the stay data
-- we must specify 1 of fst_encounter_id or fst_time
-- we must specify 1 of snd_encounter_id or snd_time
drop table if exists stay_data_t cascade;
create table stay_data_t(
  stay_data_id bigserial primary key,
  creation_time bigint not null,
  creator_user_id bigint not null,
  fst_encounter_id bigint references encounter_t(encounter_id), -- NULLABLE
  fst_time bigint, -- NULLABLE
  snd_encounter_id bigint references encounter_t(encounter_id), -- NULLABLE
  snd_time bigint, -- NULLABLE
  check (num_nulls(fst_encounter_data_id, fst_time) = 1),
  check (num_nulls(snd_encounter_data_id, snd_time) = 1),
  active bool not null
);

-- represents period of time when the student wasn't present but should have been
-- these are cateogrized into types:
-- linked to an attendance revision
-- ABSENT: user didn't show up at all (nothing defined)
-- TARDY: user arrived late (start time defined)
-- LEAVE_NORETURN: user left before session ended (end time defined)
-- LEAVE_RETURN: user left and then came back in the middle of a session (start and end time defined)
drop table if exists irregularity_t cascade;
create table irregularity_t(
  irregularity_id bigserial primary key,
  creation_time bigint not null,
  committment_id bigint not null references committment_t(committment_id),
  fst_encounter_id bigint not null references encounter_t(encounter_id),
  snd_encounter_id bigint not null references encounter_t(encounter_id)
);
