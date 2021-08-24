create table role_oauth_service
(
    role_id         UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT role_service_role_fk REFERENCES roles (role_id),
    code        varchar(64) NOT NULL
        CONSTRAINT role_oauth_service_service_fk REFERENCES oauth_service (code),
    create_datetime DATETIME2        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX role_oauth_service_role_fk ON role_oauth_service (role_id);
CREATE INDEX role_oauth_service_oauth_service_fk ON role_oauth_service (code);
