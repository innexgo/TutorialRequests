drop table if exists email_ownership_challenge;
create table email_ownership_challenge(
  id integer not null primary key,
  email integer not null,
  creation_time integer not null,
  key_hash char(64) not null
)

drop table if exists user;
create table user(
  id integer not null primary key,
  kind integer not null,
  name varchar(100) not null,
  email varchar(100) not null unique,
  validated integer not null,
  password_hash char(64) not null
);

drop table if exists api_key;
create table api_key(
  id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  duration integer not null,
  key_hash char(64) not null
);

drop table if exists appt_request;
create table appt_request(
  appt_request_id integer not null primary key,
  creator_id integer not null,
  attendee_id integer not null,
  host_id integer not null,
  message varchar(100) not null,
  creation_time integer not null,
  start_time integer not null,
  duration integer not null
);

drop table if exists appt; 
create table appt(
  appt_request_id integer not null primary key,
  message varchar(100) not null,
  creation_time integer not null,
  start_time integer not null,
  duration integer not null
);

drop table if exists attendance;
create table attendance(
  appt_id integer not null primary key,
  creation_time integer not null,
  kind integer not null
);
