ALTER TABLE OAUTH_CLIENT_ALLOWED_IPS
RENAME TO OAUTH_CLIENT_CONFIG;

COMMIT;

ALTER TABLE OAUTH_CLIENT_CONFIG
RENAME constraint oauth_client_allowed_ips_pk TO oauth_client_config_pk;

