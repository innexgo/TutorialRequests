drop table if exists school;
create table school(
id bigint(20) not null auto_increment,
name varchar(100) not null unique,
primary key(id)
);

drop table if exists user;
create table user(
id bigint(20) not null auto_increment,
secondary_id bigint(20) not null,
school_id bigint(20) not null,
kind enum('STUDENT', 'USER', 'ADMIN') not null,
name varchar(100) not null,
email varchar(100) not null unique,
password_hash char(64) not null,
primary key(id)
);

drop table if exists api_key;
create table api_key(
id bigint(20) not null auto_increment,
user_id bigint(20) not null,
creation_time bigint(20) not null,
duration bigint(20) not null,
can_log_in boolean not null,
can_change_password boolean not null,
can_read_user boolean not null,
can_write_user boolean not null,
can_read_appt_request boolean not null,
can_write_appt_request boolean not null,
can_read_appt boolean not null,
can_write_appt boolean not null,
can_read_attendance boolean not null,
can_write_attendance boolean not null,
key_hash char(64) not null,
primary key (id)
);

drop table if exists appt_request;
create table appt_request(
id bigint(20) not null auto_increment,
creator_id bigint(20) not null,
target_id bigint(20) not null,
message varchar(100) not null,
creation_time bigint(20) not null,
suggested_time bigint(20) not null,
primary key(id)
);

drop table if exists appt; 
create table appt(
id bigint(20) not null auto_increment,
host_id bigint(20) not null,
attendee_id bigint(20) not null,
appt_request_id bigint(20) not null,
message varchar(100) not null,
creation_time bigint(20) not null,
start_time bigint(20) not null,
duration bigint(20) not null,
primary key(id)
);

drop table if exists attendance;
create table attendance(
id bigint(20) not null auto_increment,
appt_id bigint(20) not null,
creation_time bigint(20) not null,
attendance enum('ABSENT', 'TARDY', 'PRESENT') not null,
primary key(id)
);

