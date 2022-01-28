    create table groups
(
	group_id uuid not null
		constraint group_pk
			primary key,
	group_code varchar(30) not null
		constraint group_code_uk
			unique,
	group_name varchar(100) not null,
	create_datetime timestamp default CURRENT_TIMESTAMP not null
);

create table child_group
(
	child_group_id uuid not null
		constraint child_group_pk
			primary key,
	child_group_code varchar(30) not null
		constraint child_group_code_uk
			unique,
	child_group_name varchar(100) not null,
	group_id uuid not null
		constraint child_group_group_id_fk
			references groups
);


create table roles
(
	role_id uuid not null
		constraint role_pk
			primary key,
	role_code varchar(50) not null
		constraint role_code_uk
			unique,
	role_name varchar(128),
	create_datetime timestamp default CURRENT_TIMESTAMP not null,
	role_description text,
	admin_type varchar(100) default 'EXT_ADM'::character varying not null
);


create table users
(
	user_id uuid not null
		constraint user_id_pk
			primary key,
	username varchar(240) not null
		constraint username_uk
			unique,
	password varchar(100),
	email varchar(240),
	first_name varchar(50),
	last_name varchar(50),
	verified boolean default false not null,
	locked boolean default false not null,
	enabled boolean default false not null,
	master boolean default false not null,
	create_datetime timestamp default CURRENT_TIMESTAMP not null,
	password_expiry timestamp default CURRENT_TIMESTAMP not null,
	last_logged_in timestamp default CURRENT_TIMESTAMP not null,
	source varchar(50) not null,
	mfa_preference varchar(15) default 'EMAIL'::character varying not null,
	inactive_reason varchar(100),
	pre_disable_warning boolean default false not null
);


create index user_email_idx
	on users (email);

create index user_last_logged_in_enabled_idx
	on users (last_logged_in, enabled);

create table group_assignable_role
(
	role_id uuid not null
		constraint group_assignable_role_role_fk
			references roles,
	group_id uuid not null
		constraint group_assignable_role_group_fk
			references groups,
	automatic boolean default false not null,
	create_datetime timestamp default CURRENT_TIMESTAMP not null
);


create index group_assignable_role_role_fk
	on group_assignable_role (role_id);

create index group_assignable_role_group_fk
	on group_assignable_role (group_id);

create table oauth_client_details
(
	client_id varchar(64) not null
		constraint oauth_client_details_pkey
			primary key,
	access_token_validity integer,
	additional_information varchar(255),
	authorities varchar(1000),
	authorized_grant_types varchar(200) not null,
	autoapprove varchar(200),
	client_secret varchar(100) not null,
	refresh_token_validity integer,
	resource_ids varchar(255),
	scope varchar(200),
	web_server_redirect_uri varchar(1000),
	last_accessed timestamp default CURRENT_TIMESTAMP not null,
	created timestamp default CURRENT_TIMESTAMP not null,
	secret_updated timestamp default CURRENT_TIMESTAMP not null
);


create table oauth_client_deployment_details
(
	base_client_id varchar(64) not null
		constraint oauth_client_deployment_details_pkey
			primary key,
	client_type varchar(255),
	team varchar(255),
	team_contact varchar(255),
	team_slack varchar(255),
	hosting varchar(255),
	namespace varchar(255),
	deployment varchar(255),
	secret_name varchar(255),
	client_id_key varchar(255),
	secret_key varchar(255)
);


create table oauth_code
(
	code varchar(256) not null
		constraint oauth_code_pkey
			primary key,
	authentication bytea,
	created_date timestamp default CURRENT_TIMESTAMP not null
);


create table oauth_service
(
	code varchar(64) not null
		constraint oauth_service_pkey
			primary key,
	name varchar(255) not null,
	description varchar(255),
	authorised_roles varchar(1000),
	url varchar(255) not null,
	enabled boolean default false not null,
	email varchar(240)
);

create table spring_session
(
	primary_id char(36) not null
		constraint spring_session_pk
			primary key,
	session_id char(36) not null,
	creation_time bigint not null,
	last_access_time bigint not null,
	max_inactive_interval integer not null,
	expiry_time bigint not null,
	principal_name varchar(240)
);


create unique index spring_session_ix1
	on spring_session (session_id);

create index spring_session_ix2
	on spring_session (expiry_time);

create index spring_session_ix3
	on spring_session (principal_name);

create table spring_session_attributes
(
	session_primary_id char(36) not null
		constraint spring_session_attributes_fk
			references spring_session
				on delete cascade,
	attribute_name varchar(200) not null,
	attribute_bytes bytea not null,
	constraint spring_session_attributes_pk
		primary key (session_primary_id, attribute_name)
);


create table user_contact
(
	user_id uuid not null
		constraint user_contact_user_id_fk
			references users,
	type varchar(20) not null,
	value varchar(240) not null,
	verified boolean default false not null,
	constraint user_contact_type_uk
		unique (user_id, type)
);


create table user_group
(
	group_id uuid not null
		constraint user_group_group_id_fk
			references groups,
	user_id uuid not null
		constraint user_group_user_id_fk
			references users,
	create_datetime timestamp default CURRENT_TIMESTAMP not null
);


create index user_group_user_id_fk
	on user_group (user_id);

create index user_group_group_id_fk
	on user_group (group_id);

create table user_retries
(
	username varchar(240) not null
		constraint user_retries_pk
			primary key,
	retry_count integer default 0 not null
);


create table user_role
(
	role_id uuid not null
		constraint user_role_role_id_fk
			references roles,
	user_id uuid not null
		constraint user_role_user_id_fk
			references users,
	create_datetime timestamp default CURRENT_TIMESTAMP not null
);


create index user_role_user_id_fk
	on user_role (user_id);

create index user_role_role_id_fk
	on user_role (role_id);

create table user_token
(
	token varchar(240) not null
		constraint user_token_pkey
			primary key,
	token_type varchar(10) not null,
	create_datetime timestamp default CURRENT_TIMESTAMP not null,
	token_expiry timestamp,
	user_id uuid not null
		constraint user_token_user_id_fk
			references users,
	constraint user_token_user_id_token_type_uk
		unique (user_id, token_type)
);

create table email_domain
(
    email_domain_id uuid not null
        constraint email_domain_pk primary key,
    name            varchar(100) not null,
    description     varchar(200)
);