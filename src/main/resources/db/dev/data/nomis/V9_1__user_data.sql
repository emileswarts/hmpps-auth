CREATE USER IF NOT EXISTS ITAG_USER password 'password';
CREATE USER IF NOT EXISTS OLD_NOMIS_USER password 'password';
CREATE USER IF NOT EXISTS ITAG_USER_ADM password 'password123456';
CREATE USER IF NOT EXISTS CA_USER password 'password123456';
CREATE USER IF NOT EXISTS CA_USER_TEST password 'licences123456';
CREATE USER IF NOT EXISTS RESET_TEST_USER password 'password123456';
CREATE USER IF NOT EXISTS CA_USER_MULTI password 'password123456';
CREATE USER IF NOT EXISTS RO_USER password 'password123456';
CREATE USER IF NOT EXISTS RO_DEMO password 'password123456';
CREATE USER IF NOT EXISTS RO_USER_TEST password 'licences123456';
CREATE USER IF NOT EXISTS RO_USER_MULTI password 'password123456';
CREATE USER IF NOT EXISTS DM_USER password 'password123456';
CREATE USER IF NOT EXISTS DM_USER_TEST password 'licences123456';
CREATE USER IF NOT EXISTS DM_USER_MULTI password 'password123456';
CREATE USER IF NOT EXISTS NOMIS_BATCHLOAD password 'password123456';
CREATE USER IF NOT EXISTS LOCKED_USER password 'password123456';
CREATE USER IF NOT EXISTS EXPIRED_USER password 'password123456';
CREATE USER IF NOT EXISTS EXPIRED_TEST_USER password 'password123456';
CREATE USER IF NOT EXISTS EXPIRED_TEST2_USER password 'password123456';
CREATE USER IF NOT EXISTS EXPIRED_TEST3_USER password 'password123456';
CREATE USER IF NOT EXISTS OMIC_USER password 'password';
CREATE USER IF NOT EXISTS LAA_USER password 'password';
CREATE USER IF NOT EXISTS IEP_USER password 'password';
CREATE USER IF NOT EXISTS SECURE_CASENOTE_USER password 'password123456';
CREATE USER IF NOT EXISTS PPL_USER password 'password123456';
CREATE USER IF NOT EXISTS IC_USER password 'password123456';
CREATE USER IF NOT EXISTS UOF_REVIEWER_USER password 'password123456';


INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (1, 'ITAG', 'USER', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (2, 'OLD', 'NOMIS USER', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (3, 'Licence', 'Case Admin', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (4, 'Licence', 'Responsible Officer', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (5, 'Licence', 'Decision Maker', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (6, 'Licence', 'Batchloader', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (7, 'User', 'Locked', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (8, 'Catherine', 'Amos', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (9, 'Licence', 'Case Admin Multi', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (10, 'Licence', 'Responsible Officer Demo', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (11, 'Ryan', 'Orton', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (12, 'Licence', 'Responsible Officer Multi', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (13, 'Diane', 'Matthews', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (14, 'Licence', 'Decision Maker Multi', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (15, 'Omic', 'User', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (16, 'User', 'Expired', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (17, 'Change', 'Password', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (18, 'Change', 'Password2', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (19, 'Change', 'Password3', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (20, 'Local', 'Admin', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (21, 'Ca', 'UserTest', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (22, 'Iep', 'UserTest', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (23, 'Secure Case', 'Note', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (24, 'Prison', 'Prevent Lead', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (25, 'Intelligence', 'Coordinator', 'ACTIVE');
INSERT INTO STAFF_MEMBERS (STAFF_ID, FIRST_NAME, LAST_NAME, STATUS)
VALUES (26, 'Use of Force', 'Reviewer', 'ACTIVE');

INSERT INTO STAFF_USER_ACCOUNTS (username, staff_user_type, staff_id, working_caseload_id)
VALUES ('ITAG_USER', 'GENERAL', 1, 'MDI'),
  ('CA_USER', 'GENERAL', 3, 'BEL'),
  ('RO_USER', 'GENERAL', 4, 'BEL'),
  ('DM_USER', 'GENERAL', 5, 'BEL'),
  ('NOMIS_BATCHLOAD', 'GENERAL', 6, 'BEL'),
  ('CA_USER_TEST', 'GENERAL', 8, 'BEL'),
  ('CA_USER_MULTI', 'GENERAL', 9, 'BEL'),
  ('RO_DEMO', 'GENERAL', 10, 'BEL'),
  ('RO_USER_TEST', 'GENERAL', 11, 'BEL'),
  ('RO_USER_MULTI', 'GENERAL', 12, 'BEL'),
  ('DM_USER_TEST', 'GENERAL', 13, 'BEL'),
  ('DM_USER_MULTI', 'GENERAL', 14, 'BEL'),
  ('RESET_TEST_USER', 'GENERAL', 21, 'BEL'),
  ('IEP_USER', 'GENERAL', 22, 'MDI'),
  ('SECURE_CASENOTE_USER', 'GENERAL', 23, 'LEI'),
  ('PPL_USER', 'GENERAL', 24, 'BEL'),
  ('IC_USER', 'GENERAL', 25, 'BEL'),
  ('UOF_REVIEWER_USER', 'GENERAL', 26, 'BEL');

INSERT INTO STAFF_USER_ACCOUNTS (username, staff_user_type, staff_id)
VALUES ('ITAG_USER_ADM', 'ADMIN', 1),
  ('OLD_NOMIS_USER', 'GENERAL', 2),
  ('LOCKED_USER', 'GENERAL', 7),
  ('OMIC_USER', 'GENERAL', 15),
  ('EXPIRED_USER', 'GENERAL', 16),
  ('EXPIRED_TEST_USER', 'GENERAL', 17),
  ('EXPIRED_TEST2_USER', 'GENERAL', 18),
  ('EXPIRED_TEST3_USER', 'GENERAL', 19),
  ('LAA_USER', 'ADMIN', 20);

INSERT INTO DBA_USERS (username, account_status, profile)
VALUES ('ITAG_USER', 'OPEN', 'TAG_GENERAL'),
  ('ITAG_USER_ADM', 'OPEN', 'TAG_ADMIN'),
  ('OLD_NOMIS_USER', 'OPEN', 'TAG_GENERAL'),
  ('CA_USER', 'OPEN', 'TAG_GENERAL'),
  ('RO_USER', 'OPEN', 'TAG_GENERAL'),
  ('DM_USER', 'OPEN', 'TAG_GENERAL'),
  ('NOMIS_BATCHLOAD', 'OPEN', 'TAG_GENERAL'),
  ('LOCKED_USER', 'LOCKED', 'TAG_GENERAL'),
  ('EXPIRED_USER', 'EXPIRED', 'TAG_GENERAL'),
  ('EXPIRED_TEST_USER', 'EXPIRED', 'TAG_GENERAL'),
  ('EXPIRED_TEST2_USER', 'EXPIRED', 'TAG_GENERAL'),
  ('EXPIRED_TEST3_USER', 'EXPIRED', 'TAG_GENERAL'),
  ('CA_USER_TEST', 'OPEN', 'TAG_GENERAL'),
  ('RESET_TEST_USER', 'OPEN', 'TAG_GENERAL'),
  ('CA_USER_MULTI', 'OPEN', 'TAG_GENERAL'),
  ('RO_DEMO', 'OPEN', 'TAG_GENERAL'),
  ('RO_USER_TEST', 'OPEN', 'TAG_GENERAL'),
  ('RO_USER_MULTI', 'OPEN', 'TAG_GENERAL'),
  ('DM_USER_TEST', 'OPEN', 'TAG_GENERAL'),
  ('DM_USER_MULTI', 'OPEN', 'TAG_GENERAL'),
  ('OMIC_USER', 'OPEN', 'TAG_GENERAL'),
  ('LAA_USER', 'OPEN', 'TAG_ADMIN'),
  ('IEP_USER', 'OPEN', 'TAG_GENERAL'),
  ('SECURE_CASENOTE_USER', 'OPEN', 'TAG_GENERAL'),
  ('PPL_USER', 'OPEN', 'TAG_GENERAL'),
  ('IC_USER', 'OPEN', 'TAG_GENERAL'),
  ('UOF_REVIEWER_USER', 'OPEN', 'TAG_GENERAL');

INSERT INTO SYS.USER$ (name, spare4)
VALUES ('ITAG_USER', '{bcrypt}$2a$10$9rVgms..dZ3gnPSt4JWPA.Oan4MrDHvcx1c.HuYqeMD5rVFmf0C3G'),
  ('ITAG_USER_ADM', '{bcrypt}$2a$10$0zomTd5coSOKnSBMkCyEiei72HwBLJZrSpoqL1GVwr4LNp.KAq.FK'),
  ('OLD_NOMIS_USER', '{bcrypt}$2a$10$EaaM7jp4e/Y1q8zR..eEYOwJOnGJmHIItZfIxR6gVgUP4xT8qXVEm'),
  ('CA_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy'),
  ('RO_USER', '{bcrypt}$2a$10$ordeqG5gMJHWXm9SDsN0q.PgYbwRmC5idWoFTEWIe2hoIS8IKh3dK'),
  ('DM_USER', '{bcrypt}$2a$10$S0K9erqzMttMHhs5hdi8DuE8lk7F1ajsAD5pAMmgmJBc/8QsjzHzy'),
  ('NOMIS_BATCHLOAD', '{bcrypt}$2a$10$bAzE0xo8XsKMBchiKmnpEuPZqXV.0/RVVhya7v7kkGoRLTpt2Iwxa'),
  ('LOCKED_USER', '{bcrypt}$2a$10$BPCJlKWhaICns8ax5JHd8er8Ti6zy8VH3LiFKtt1M2A4iVyJv1NyW'),
  ('EXPIRED_USER', '{bcrypt}$2a$10$kJf5g6MQuORDouOnrlpoQOupk5ieUsgcp5v0TtHVzR3mn53b37vby'),
  ('EXPIRED_TEST_USER', '{bcrypt}$2a$10$kJf5g6MQuORDouOnrlpoQOupk5ieUsgcp5v0TtHVzR3mn53b37vby'),
  ('EXPIRED_TEST2_USER', '{bcrypt}$2a$10$kJf5g6MQuORDouOnrlpoQOupk5ieUsgcp5v0TtHVzR3mn53b37vby'),
  ('EXPIRED_TEST3_USER', '{bcrypt}$2a$10$kJf5g6MQuORDouOnrlpoQOupk5ieUsgcp5v0TtHVzR3mn53b37vby'),
  ('CA_USER_TEST', '{bcrypt}$2a$10$vgyF/EFvlol3pwwi0n6ZgeI6C90xY5exW6tETbdDBHB4xpDYzuV9q'),
  ('RESET_TEST_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy'),
  ('CA_USER_MULTI', '{bcrypt}$2a$10$.uXmUgIPCFv1c93IFzZ48.N3fUlY25YJcq74SPwvPzp93iwId4SzW'),
  ('RO_DEMO', '{bcrypt}$2a$10$Xtg49KDA5dBQ2kk3MqsG7erUsUV.vid.s9m2ikLMBWTGTynBG9v4K'),
  ('RO_USER_TEST', '{bcrypt}$2a$10$WIthoS9sIXg2mm3tXW6kd./w.xkmfwrSOIUDu.3KNLypCmIpltuvS'),
  ('RO_USER_MULTI', '{bcrypt}$2a$10$MGhfSUGrb9DbfMQ/RgxIpOepxw/.R53bZ.tgcTKrEgEHyOsL.VBj6'),
  ('DM_USER_TEST', '{bcrypt}$2a$10$3uLXHUVAN3TPp0l4FAlDXe/DTjxp9BsjkJ9WXnRAp2zQkbEVYmGuC'),
  ('DM_USER_MULTI', '{bcrypt}$2a$10$JPsL6m0wCXIq5Zdjo8yCx.kYUJBxkPb1gdsAsCqsqgMaJmqJpLiyS'),
  ('OMIC_USER', '{bcrypt}$2a$10$8lRtC2ndt.nb004kZTNm6O2DqJmakSwKfcmWXB5adKMb9NUWkA1Tm'),
  ('LAA_USER', '{bcrypt}$2a$10$Efc5x3nXUVBj84SZFdcCzuSiaoztJVJkqXFwgUq6xXRKb0dyUWlK.'),
  ('IEP_USER', '{bcrypt}$2a$10$Efc5x3nXUVBj84SZFdcCzuSiaoztJVJkqXFwgUq6xXRKb0dyUWlK.'),
  ('SECURE_CASENOTE_USER', '{bcrypt}$2a$10$kJf5g6MQuORDouOnrlpoQOupk5ieUsgcp5v0TtHVzR3mn53b37vby'),
  ('PPL_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy'),
  ('IC_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy'),
  ('UOF_REVIEWER_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy');

INSERT INTO PERSONNEL_IDENTIFICATIONS (STAFF_ID, IDENTIFICATION_TYPE, IDENTIFICATION_NUMBER) VALUES (1, 'YJAF', 'test@yjaf.gov.uk');
INSERT INTO PERSONNEL_IDENTIFICATIONS (STAFF_ID, IDENTIFICATION_TYPE, IDENTIFICATION_NUMBER) VALUES (2, 'YJAF', 'olduser@yjaf.gov.uk');

INSERT INTO CASELOADS (CASELOAD_ID, DESCRIPTION, CASELOAD_TYPE) VALUES ('NWEB', 'Magic API Caseload', 'APP');
INSERT INTO CASELOADS (CASELOAD_ID, DESCRIPTION, CASELOAD_TYPE, CASELOAD_FUNCTION) VALUES ('MDI', 'Moorlands', 'INST', 'GENERAL');
INSERT INTO CASELOADS (CASELOAD_ID, DESCRIPTION, CASELOAD_TYPE, CASELOAD_FUNCTION) VALUES ('CADM', 'Central Admin', 'INST', 'ADMIN');

INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'ITAG_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('MDI', 'ITAG_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('MDI', 'OLD_NOMIS_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('CADM', 'ITAG_USER_ADM', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'CA_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'RO_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'DM_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'NOMIS_BATCHLOAD', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'LOCKED_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'EXPIRED_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'EXPIRED_TEST_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'EXPIRED_TEST2_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'EXPIRED_TEST3_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'ITAG_USER_ADM', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'CA_USER_TEST', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'RESET_TEST_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'CA_USER_MULTI', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'RO_DEMO', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'RO_USER_TEST', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'RO_USER_MULTI', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'DM_USER_TEST', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'DM_USER_MULTI', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'OMIC_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'LAA_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('CADM', 'LAA_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'IEP_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'SECURE_CASENOTE_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'PPL_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'IC_USER', now());
INSERT INTO USER_ACCESSIBLE_CASELOADS (CASELOAD_ID, USERNAME, START_DATE) VALUES ('NWEB', 'UOF_REVIEWER_USER', now());

INSERT INTO OMS_ROLES (ROLE_ID, ROLE_CODE, ROLE_NAME, ROLE_SEQ, ROLE_TYPE, ROLE_FUNCTION)
VALUES (1, 'OMIC_ADMIN', 'Omic Administrator', 1, 'APP', 'GENERAL'),
  (-1, '900', 'Some Old Role', 99, 'INST', 'GENERAL'),
  (3, 'CENTRAL_ADMIN', 'All Powerful Admin', 1, 'INST', 'ADMIN'),
  (4, 'KW_MIGRATION', 'KW Migration', 1, 'APP', 'ADMIN'),
  (5, 'NOMIS_BATCHLOAD', 'Nomis BatchLoad', 1, 'APP', 'ADMIN'),
  (6, 'MAINTAIN_ACCESS_ROLES', 'Maintain Access Roles', 1, 'APP', 'ADMIN'),
  (7, 'MAINTAIN_ACCESS_ROLES_ADMIN', 'Maintain Access Roles Admin', 1, 'APP', 'ADMIN'),
  (8, 'GLOBAL_SEARCH', 'Global Search', 1, 'APP', 'ADMIN'),
  (11, 'LICENCE_CA', 'Licence Case Admin', 1, 'APP', 'GENERAL'),
  (12, 'LICENCE_RO', 'Licence Responsible Officer', 2, 'APP', 'GENERAL'),
  (13, 'LICENCE_DM', 'Licence Decision Maker', 3, 'APP', 'GENERAL'),
  (14, 'OAUTH_ADMIN', 'Oauth Admin', 99, 'APP', 'ADMIN'),
  (15, 'SYSTEM_READ_ONLY', 'System Read Only', 99, 'APP', 'ADMIN'),
  (16, 'INACTIVE_BOOKINGS', 'View Inactive Bookings', 99, 'APP', 'GENERAL'),
  (17, 'CREATE_CATEGORISATION', 'Create Category assessments', 99, 'APP', 'GENERAL'),
  (18, 'APPROVE_CATEGORISATION', 'Approve Category assessments', 99, 'APP', 'GENERAL'),
  (19, 'MAINTAIN_OAUTH_USERS', 'Maintain oauth users (admin)', 99, 'APP', 'ADMIN'),
  (20, 'CATEGORISATION_SECURITY', 'Security Cat tool role', 99, 'APP', 'GENERAL'),
  (21, 'MAINTAIN_IEP', 'Maintain IEP', 99, 'APP', 'GENERAL'),
  (22, 'VIEW_SENSITIVE_CASE_NOTES', 'View Secure Case Notes', 99, 'APP', 'GENERAL'),
  (23, 'ADD_SENSITIVE_CASE_NOTES', 'Add Secure Case Notes', 99, 'APP', 'GENERAL'),
  (24, 'PATHFINDER_PPL', 'Prison Prevent Lead', 99, 'APP', 'GENERAL'),
  (25, 'PATHFINDER_IC', 'Intelligence Coordinator', 99, 'APP', 'GENERAL'),
  (26, 'USE_OF_FORCE_REVIEWER', 'Use of force reviewer', 99, 'APP', 'GENERAL');

INSERT INTO USER_CASELOAD_ROLES (ROLE_ID, CASELOAD_ID, USERNAME)
VALUES (-1, 'MDI', 'ITAG_USER'),
  (-1, 'MDI', 'OLD_NOMIS_USER'),
  (1, 'NWEB', 'ITAG_USER'),
  (4, 'NWEB', 'ITAG_USER_ADM'),
  (5, 'NWEB', 'NOMIS_BATCHLOAD'),
  (6, 'NWEB', 'ITAG_USER_ADM'),
  (7, 'NWEB', 'ITAG_USER'),
  (8, 'NWEB', 'ITAG_USER'),
  (17, 'NWEB', 'ITAG_USER'),
  (18, 'NWEB', 'ITAG_USER'),
  (20, 'NWEB', 'ITAG_USER'),
  (3, 'CADM', 'ITAG_USER_ADM'),
  (11, 'NWEB', 'CA_USER'),
  (12, 'NWEB', 'RO_USER'),
  (8, 'NWEB', 'RO_USER'),
  (16, 'NWEB', 'RO_USER'),
  (13, 'NWEB', 'DM_USER'),
  (14, 'NWEB', 'ITAG_USER_ADM'),
  (11, 'NWEB', 'CA_USER_TEST'),
  (11, 'NWEB', 'RESET_TEST_USER'),
  (11, 'NWEB', 'CA_USER_MULTI'),
  (12, 'NWEB', 'RO_DEMO'),
  (12, 'NWEB', 'RO_USER_TEST'),
  (12, 'NWEB', 'RO_USER_MULTI'),
  (13, 'NWEB', 'DM_USER_TEST'),
  (13, 'NWEB', 'DM_USER_MULTI'),
  (15, 'NWEB', 'OMIC_USER'),
  (6, 'NWEB', 'LAA_USER'),
  (19, 'NWEB', 'ITAG_USER_ADM'),
  (21, 'NWEB', 'IEP_USER'),
  (22, 'NWEB', 'SECURE_CASENOTE_USER'),
  (23, 'NWEB', 'SECURE_CASENOTE_USER'),
  (24, 'NWEB', 'PPL_USER'),
  (25, 'NWEB', 'IC_USER'),
  (26, 'NWEB', 'UOF_REVIEWER_USER');

Insert into internet_addresses (INTERNET_ADDRESS_ID, OWNER_CLASS, OWNER_ID, OWNER_SEQ, OWNER_CODE,
                                INTERNET_ADDRESS_CLASS, INTERNET_ADDRESS, CREATE_DATETIME, CREATE_USER_ID,
                                MODIFY_DATETIME, MODIFY_USER_ID, AUDIT_TIMESTAMP, AUDIT_USER_ID, AUDIT_MODULE_NAME,
                                AUDIT_CLIENT_USER_ID, AUDIT_CLIENT_IP_ADDRESS, AUDIT_CLIENT_WORKSTATION_NAME,
                                AUDIT_ADDITIONAL_INFO)
VALUES (138250, 'STF', 4, null, null, 'EMAIL', 'phillips@bobjustice.gov.uk',
    to_timestamp('06-DEC-18 16.08.27.910960000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', null, null,
    to_timestamp('06-DEC-18 16.08.27.911332000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', 'OUMPERSO',
    'pphillips', '10.102.2.4', 'MGMRW0100', null),
  (138251, 'STF', 4, null, null, 'EMAIL', 'phillips@fredjustice.gov.uk',
    to_timestamp('06-DEC-18 16.08.43.742506000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', null, null,
    to_timestamp('06-DEC-18 16.08.43.742717000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', 'OUMPERSO',
    'pphillips', '10.102.2.4', 'MGMRW0100', null),
  (138252, 'STF', 10, null, null, 'EMAIL', 'ro_user@some.justice.gov.uk',
    to_timestamp('06-DEC-18 16.08.43.742506000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', null, null,
    to_timestamp('06-DEC-18 16.08.43.742717000', 'DD-MON-RR HH24.MI.SSXFF'), 'PPHILLIPS_ADM', 'OUMPERSO',
    'pphillips', '10.102.2.4', 'MGMRW0100', null);






