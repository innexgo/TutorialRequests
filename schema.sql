drop table if exists forgot_password;
create table forgot_password(
  id integer not null primary key,
  email varchar(100) not null,
  creation_time integer not null,
  access_key char(44) not null unique,
  valid integer not null
);

drop table if exists email_verification_challenge;
create table email_verification_challenge(
  id integer not null primary key,
  name integer not null,
  email varchar(100) not null,
  creation_time integer not null,
  verification_key char(44) not null unique,
  password_hash char(64) not null,
  valid integer not null
);

drop table if exists user;
create table user(
  id integer not null primary key,
  kind integer not null,
  name varchar(100) not null,
  email varchar(100) not null unique,
  password_set_time integer not null,
  password_hash char(64) not null unique
);

drop table if exists api_key;
create table api_key(
  id integer not null primary key,
  creator_id integer not null,
  creation_time integer not null,
  duration integer not null,
  key_hash char(64) not null unique
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
