create table admin_type
(
    admin_type_id         UNIQUEIDENTIFIER NOT NULL
    CONSTRAINT admin_type_pk PRIMARY KEY,
    admin_type_name       VARCHAR(100)     NOT NULL,
    create_datetime DATETIME2        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

create table role_admin_type
(
    role_id         UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT role_admin_type_role_fk REFERENCES roles (role_id),
    admin_type_id   UNIQUEIDENTIFIER NOT NULL
        CONSTRAINT role_admin_type_admin_type_fk REFERENCES admin_type (admin_type_id),
    create_datetime DATETIME2        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX role_admin_type_role_fk ON role_admin_type (role_id);
CREATE INDEX role_admin_type_admin_type_fk ON role_admin_type (admin_type_id);
