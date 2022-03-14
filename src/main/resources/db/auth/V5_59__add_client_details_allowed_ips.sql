DROP TABLE IF EXISTS oauth_client_allowed_ips;

create table oauth_client_allowed_ips
(
    base_client_id varchar(64) not null primary key,
    allowed_ips varchar(1000)

);
