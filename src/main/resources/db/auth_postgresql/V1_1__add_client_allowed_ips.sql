create table oauth_client_allowed_ips
(
    base_client_id varchar(64) not null
        constraint oauth_client_allowed_ips_pk
            primary key,
    allowed_ips varchar(1000)

);
