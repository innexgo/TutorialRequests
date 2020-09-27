drop table if exists school_info;
create table school_info(
  id integer not null primary key,
  name varchar(100) not null,
  domain varchar(100) not null
);

drop table if exists user;
create table user(
  id integer not null primary key,
  kind bigint(20) not null,
  name varchar(100) not null,
  email varchar(100) not null unique,
  validated bigint(20) not null,
  password_hash char(64) not null
);

drop table if exists api_key;
create table api_key(
  id integer not null primary key,
  user_id bigint(20) not null,
  creation_time bigint(20) not null,
  duration bigint(20) not null,
  key_hash char(64) not null
);

drop table if exists appt_request;
create table appt_request(
  id integer not null primary key,
  creator_id bigint(20) not null,
  target_id bigint(20) not null,
  message varchar(100) not null,
  creation_time bigint(20) not null,
  suggested_time bigint(20) not null
);

drop table if exists appt; 
create table appt(
  id integer not null primary key,
  host_id bigint(20) not null,
  attendee_id bigint(20) not null,
  appt_request_id bigint(20) not null,
  message varchar(100) not null,
  creation_time bigint(20) not null,
  start_time bigint(20) not null,
  duration bigint(20) not null
);

drop table if exists appt_attendance;
create table appt_attendance(
  id integer not null primary key,
  appt_id bigint(20) not null,
  creation_time bigint(20) not null,
  attendance bigint(20) not null
);
