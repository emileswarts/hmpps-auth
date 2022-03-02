INSERT INTO oauth_client_details (client_id, access_token_validity, additional_information, authorities, authorized_grant_types, autoapprove, client_secret, refresh_token_validity, resource_ids, scope, web_server_redirect_uri)
VALUES ('omicuser','1200','{"jwtFields":"-user_name"}','SYSTEM_READ_ONLY','password,authorization_code,refresh_token','read','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG',null,null,'read','http://localhost:3000/login,http://localhost:8081/login'),
       ('elite2apiclient','28800','{}',null,'password,authorization_code,refresh_token','read,write','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG','43200',null,'read,write','http://localhost:8081/login,http://localhost:3000/,http://localhost:3001/,http://localhost:3000/login/callback,http://localhost:3001/login/callback,http://localhost:3002/login/callback,http://localhost:8081/webjars/springfox-swagger-ui/oauth2-redirect.html'),
       ('prisonapiclient','28800','{}',null,'authorization_code,refresh_token','read,write','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG','43200',null,'read,write','http://localhost:8081/sign-in,http://localhost:3000/,http://localhost:3001/,http://localhost:3002/,http://localhost:3000/sign-in/callback,http://localhost:3001/sign-in/callback,http://localhost:3002/sign-in/callback'),
       ('manage-user-accounts-ui','28800','{}',null,'authorization_code,refresh_token','read','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG','43200',null,'read','http://localhost:3001/,http://localhost:3001/sign-in/callback,http://localhost:8081/login'),
       ('azure-login-client','28800','{}',null,'password,authorization_code,refresh_token','read,write,nomis,auth,delius','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG','43200',null,'read,write,nomis,auth,delius','http://localhost:8081/login,http://localhost:3000/,http://localhost:3001/,http://localhost:3000/login/callback,http://localhost:3001/login/callback,http://localhost:3002/login/callback,http://localhost:8081/webjars/springfox-swagger-ui/oauth2-redirect.html'),
       ('omic','28800','{}',null,'password,authorization_code,refresh_token','read,write','$2a$10$oUonidUHlG34P/mbiRs2d.owes0fvNeyUBACo6lzkq7Hr/68cfxOW','43200',null,'read,write',null),
       ('licences','28800','{}',null,'password,authorization_code,refresh_token','read,write','$2a$10$1FTv04xDqLuKWjBjBnxMJuQ9fEXH0CHJKZXpOjMB7hdmrMBoKhi7.','43200',null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('licencesadmin','3600','{}','ROLE_SYSTEM_USER,ROLE_GLOBAL_SEARCH,ROLE_LICENCE_RO','client_credentials','read,write','$2a$10$/JM78ghLrFNTWezv/rAoYe5Bv2HAHTtaQjzY44HTd2pHI82OxGiHy',null,null,'read,write',null),
       ('omicadmin','3600','{}','ROLE_MAINTAIN_ACCESS_ROLES,ROLE_SYSTEM_USER,ROLE_KW_MIGRATION,ROLE_KW_ADMIN','client_credentials','read','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',null,null,'read',null),
       ('batchadmin','3600','{}','ROLE_CONTACT_CREATE,ROLE_GLOBAL_SEARCH','client_credentials','read','$2a$10$UzbBEEyIFPTZGEle94.P5O.HyZ/46LxTByqC1sETfQKm8KVyO3k6O',null,null,'read',null),
       ('yjaftrustedclient','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_BOOKING_CREATE,ROLE_BOOKING_RECALL','client_credentials','read','$2a$10$vVVSNBnu34VlNItT92f9QeW065zOyWBUX78fMZdzIOCPxyY1ETJuG',null,null,'read',null),
       ('delius','3600','{}','ROLE_SYSTEM_USER','client_credentials','read','$2a$10$wgC7niO2UpNykzZ4gsPcZOvKakPRwjGu.89C9AhQTCXsJG3JqTgK2',null,null,'read',null),
       ('apireporting','3600','{}','ROLE_REPORTING','client_credentials',null,'$2a$10$f93YXwvkwVx3mS1dsZzK/.dJzvm7gu7jHawG7xIUUJTYLtXkoQaNO',null,null,'reporting',null),
       ('custodyapi','28800','{}','ROLE_REPORTING','client_credentials',null,'$2a$10$ZClyyxwFbX/24Ab9KXflc.Id5cOv3qu4b1ryNkFmXzJZt9y8eJa82','43200',null,'reporting',null),
       ('deliusnewtech','3600','{"mfa":"untrusted"}','SYSTEM_READ_ONLY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'reporting',null),
       ('service-mfa-test-client','3600','{"mfa":"all"}',null,'authorization_code',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read','http://localhost:8081/login'),
       ('categorisationtool','3600','{}','ROLE_RISK_PROFILER','password,authorization_code,refresh_token,client_credentials','read,write' ,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('prisonstaffhubclient','3600','{}','ROLE_SYSTEM_READ_ONLY,ROLE_COMMUNITY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read',null),
       ('book-video-link-client','3600','{}',null,'authorization_code','read,write','$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('book-video-link-admin','3600','{}','ROLE_SYSTEM_READ_ONLY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read',null),
       ('risk-profiler','3600','{}','ROLE_SYSTEM_USER,ROLE_RISK_PROFILER','client_credentials',null,'$2a$10$r12DB/sqXduodnjtAY/ykO0S3KCySdVW4zhG3jlIRaIsfVkFOEds2',null,null,'read',null),
       ('community-api-client','3600','{}','ROLE_COMMUNITY,ROLE_COMMUNITY_USERS,ROLE_COMMUNITY_CUSTODY_UPDATE,ROLE_COMMUNITY_EVENTS','client_credentials','read','{bcrypt}$2a$10$scfYGjUYDto1RH5SRXNcquf5jFcvVLzRPXQSohIRCSgzz4UimRCHu',null,null,'read',null),
       ('sentence-plan-client','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$a5WJN/AZc7Nq3rFoy5GOQ.avY.opPq/RaF59TXFaInt0Jxp6NV94a',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('use-of-force-client','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('prepare-a-case-for-court','1200','{}',null,'authorization_code','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('court-case-service','1200', '{}','ROLE_COMMUNITY,ROLE_OASYS_READ_ONLY','client_credentials', null,'{bcrypt}$2a$10$OPvgbwhWDQ/yysDHfhzClO0ud2Q11fAIGt6n.dIW.v0wFFNW1Rnm.', null, null,'read', null),
       ('my-diary','1200','{}',null,'authorization_code,refresh_token','read,write','{bcrypt}$2a$10$NATyxrjfisAqVua3E7xe/u1C4gr1527esDOAE23ouCwpO2ne6JHR.',43200,null,'read,write','http://localhost:3005/login/callback,http://localhost:3005'),
       ('use-of-force-system','3600','{}','ROLE_SYSTEM_READ_ONLY,ROLE_USE_OF_FORCE','client_credentials','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('whereabouts-api-client','3600','{}','ROLE_PAY, ROLE_CASE_NOTE_ADMIN,GLOBAL_SEARCH','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read,write',null),
       ('pathfinder-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('pathfinder-admin','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_SYSTEM_USER,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('manage-soc-cases-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('manage-soc-cases-admin','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_SYSTEM_USER,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('prison-to-probation-update-api-client','3600','{"databaseUsernameField":"DSS_USER", "jiraNo":"DT-2264"}','ROLE_SYSTEM_USER,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',43200,null,'read,write',null),
       ('prison-to-nhs-update-api-client','3600','{}','ROLE_SYSTEM_USER','client_credentials','read,write','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',43200,null,'read,write',null),
       ('prisoner-offender-search-client','3600','{}','ROLE_SYSTEM_USER,ROLE_PRISONER_INDEX,ROLE_GLOBAL_SEARCH','client_credentials','read,write','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',43200,null,'read,write',null),
       ('offender-events-client','1200','{}','ROLE_SYSTEM_READ_ONLY,ROLE_SYSTEM_USER,ROLE_PRISON_OFFENDER_EVENTS','client_credentials','read','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',null,null,'read',null),
       ('sentence-plan-api-client','3600', '{}','ROLE_OASYS_READ_ONLY','client_credentials', null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm', null, null,'read', null),
       ('delius-auth-api-client','3600', '{}','ROLE_COMMUNITY_AUTH_INT','client_credentials', null,'{bcrypt}$2a$10$OPvgbwhWDQ/yysDHfhzClO0ud2Q11fAIGt6n.dIW.v0wFFNW1Rnm.', null, null,'read', null),
       ('hmpps-auth-nomis-client','3600', '{}','ROLE_MANAGE_NOMIS_USER_ACCOUNT','client_credentials', null,'{bcrypt}$2a$04$8kdX3GqQDxb3VT4R3qZ21uHY49NAeyjzxlMBHiwjCoKYfETXCgqle', null, null,'read', null),
       ('token-verification-auth-api-client','3600', '{}','ROLE_AUTH_TOKEN_VERIFICATION','client_credentials', null,'{bcrypt}$2a$10$hQPvQMNfbh2vhjTjDWHpoeN.iRFRddJJug6qtBkWzQ8uPEc53isZy', null, null,'read', null),
       ('v1-client','1200','{}','ROLE_NOMIS_API_V1,ROLE_BOOKING_CREATE,ROLE_BOOKING_RECALL,ROLE_GLOBAL_SEARCH','client_credentials','read,write','$2a$10$r12DB/sqXduodnjtAY/ykO0S3KCySdVW4zhG3jlIRaIsfVkFOEds2',null,null,'read,write',null),
       ('probation-offender-search-indexer-client','3600','{}','ROLE_PROBATION_INDEX,ROLE_COMMUNITY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read',null),
       ('delius-login-client','28800','{}',null,'authorization_code,refresh_token','read,delius','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG','43200',null,'read,delius','http://localhost:5000/login/callback,http://localhost:5000/reset'),
       ('probation-offender-events-client','3600','{}','ROLE_COMMUNITY_EVENTS,ROLE_COMMUNITY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read',null),
       ('pcms-client','3600','{}',null,'authorization_code','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('pcms-system','3600','{}','ROLE_SYSTEM_USER,ROLE_GLOBAL_SEARCH,ROLE_PCMS_USER_ADMIN','client_credentials','read','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read',null),
       ('manage-intelligence-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('manage-intelligence-admin','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_INTEL_ADMIN,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('submit-information-report-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000,http://localhost:3001/login/callback,http://localhost:3001'),
       ('submit-information-report-admin','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_INTEL_SUBMISSION_ADMIN,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('user-load','3600','{}','ROLE_MAINTAIN_OAUTH_USERS','client_credentials','read,write','{bcrypt}$2a$10$zF0guKjmvHPxmkGdqmPa9ehLM0myWx/KCYNvbpKb6.eEIsDlYc/5S',43200,null,'read,write',null),
       ('pecs-jpc-client','3600','{}',null,'authorization_code','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:8080/login/oauth2/code/hmpps'),
       ('smoke-test-client','3600','{}','ROLE_SMOKE_TEST','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read',null),
       ('interventions','1200','{}','ROLE_COMMUNITY,ROLE_INTERVENTIONS,ROLE_OASYS_READ_ONLY,ROLE_COMMUNITY_USERS,ROLE_RISK_SUMMARY,ROLE_COMMUNITY_INTERVENTIONS_UPDATE','client_credentials,authorization_code,refresh_token', 'read,write', '$2a$10$a5WJN/AZc7Nq3rFoy5GOQ.avY.opPq/RaF59TXFaInt0Jxp6NV94a',43200,null,'read,write','http://localhost:3000,http://localhost:3000/login/callback,http://localhost:3000/logout/success,http://localhost:3000/sign-in/callback,http://localhost:3000/sign-out/success'),
       ('hmpps-registers-ui','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('hmpps-registers-ui-client','3600','{}','ROLE_MAINTAIN_REF_DATA','client_credentials','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write',null),
       ('duplicate-client-client','3600','{}','ROLE_CLIENT_ROTATION_ADMIN','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read,write',null),
       ('max-duplicate-client','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('max-duplicate-client-1','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('max-duplicate-client-2','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('delete-test-client','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('special-chars-test-client','1200','{}',null,'client_credentials',null,'{bcrypt}$2a$10$1HCA2OQDpaM2RwVwKb1efOBYdKlgktFX6fr5KVJT4dl4l/AgmZT/C',null,null,'read',null),
       ('service-client','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('individual-client','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('another-delete-test-client','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('hmpps-audit-api-client','3600','{}','ROLE_AUDIT','client_credentials','read,write','$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write',null),
       ('hmpps-tier','3600','{}','ROLE_COMMUNITY,ROLE_OASYS_READ_ONLY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('hmpps-tier-to-delius-update','3600','{}','ROLE_MANAGEMENT_TIER_UPDATE,ROLE_HMPPS_TIER','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'write',null),
       ('hmpps-assess-risks-and-needs-client','3600','{}','ROLE_PROBATION,ROLE_CRS_PROVIDER,ROLE_OASYS_READ_ONLY','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read,write',null),
       ('ppud-api-client','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$.95l4ENV1OEZ6qWd4R5QTOXZrjvTQmN402z1pjRUr2EwGFYdkDDnm',43200,null,'read',null),
       ('ppud-ui-client','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000,http://manage-recalls-ui:3000/login/callback,http://manage-recalls-ui:3000'),
       ('hmpps-audit-ui','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('education-data-aggregation','3600','{}','ROLE_SYSTEM_USER,ROLE_GLOBAL_SEARCH','client_credentials','read','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('create-and-vary-a-licence-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('create-and-vary-a-licence-admin','3600','{}','ROLE_CVL_ADMIN,ROLE_GLOBAL_SEARCH,ROLE_SYSTEM_USER,ROLE_COMMUNITY','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('calculate-release-dates-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('calculate-release-dates-admin','3600','{}','ROLE_GLOBAL_SEARCH,ROLE_SYSTEM_USER','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('workload-measurement-ui','3600','{}',null,'authorization_code,refresh_token','read,write','$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write','http://localhost:3000/login/callback,http://localhost:3000'),
       ('pre-sentence-service','1200','{}',null,'authorization_code','read,write','$2a$10$YRkR9FGSpZu3FAn5.Awtk.Yd0hg92y63VfVVAKhS6k66nMsc3/Hiy',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('prisoner-transactions-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('prisoner-transactions-admin','3600','{}','ROLE_PRISONER_SEARCH,ROLE_SYSTEM_USER','client_credentials','read,write','$2a$10$ajGimbJNWF1/FmZQMJWvieeQ/OdYaxWHQPgOjYDvvWu/4/744Yw7S',43200,null,'read,write',null),
       ('hmpps-manage-users-api','3600','{}','ROLE_MAINTAIN_OAUTH_USERS, ROLE_SYSTEM_USER, ROLE_MAINTAIN_ACCESS_ROLES_ADMIN, ROLE_ROLES_ADMIN','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('manage-my-prison-client','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('manage-adjudications','3600','{}',null,'authorization_code','read,write','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('manage-adjudications-client','3600','{}','ROLE_SYSTEM_USER, ROLE_GLOBAL_SEARCH, ROLE_PRISONER_SEARCH','client_credentials','read,write','$2a$10$RYwV0QebHAovVXWPySb2lefr3HTDntGu1euXHDJc3zwh2NsqeNGHG',43200,null,'read,write',null),
       ('hmpps-allocations','3600','{}','ROLE_COMMUNITY,ROLE_OASYS_READ_ONLY,ROLE_HMPPS_TIER','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('manage-a-workforce-ui','3600','{}',null,'authorization_code','read,write','$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('send-legal-mail-to-prisons','3600','{}',null,'authorization_code','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write','http://localhost:3000/sign-in/callback,http://localhost:3000'),
       ('send-legal-mail-to-prisons-client','3600','{}','ROLE_SLM_CREATE_BARCODE,ROLE_SLM_SCAN_BARCODE,ROLE_SLM_EMAIL_LINK','client_credentials','read,write','$2a$10$WzgtydqXSuhdivpWDR3WXO.yjLBm4yuDqP64Og.7E4XURdrSfhOTi',43200,null,'read,write',null),
       ('prison-register-api-client','3600','{}','ROLE_AUDIT','client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null),
       ('book-a-prison-visit-client','3600','{}','ROLE_SYSTEM_USER,ROLE_VISIT_SCHEDULER,ROLE_PRISONER_CONTACT_REGISTRY','client_credentials','read,write','$2a$12$30bYVFItHZD5RpzwDiczdOPPgbvzHkMlb5bbX3Ev7CnlRmhaYyPsm',43200,null,'read,write',null);

INSERT INTO oauth_client_details (client_id, access_token_validity, additional_information, authorities, authorized_grant_types, autoapprove, client_secret, refresh_token_validity, resource_ids, scope, web_server_redirect_uri, last_accessed, created, secret_updated)
VALUES ('rotation-test-client-1','3600','{"mfa":"all"}',null,'authorization_code',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read','http://localhost:8081/login', '2013-01-28 13:23:19.0000000', '2013-01-26 13:23:19.1234567', '2013-01-27 13:23:19.7654321'),
       ('rotation-test-client-2','3600','{"mfa":"all"}',null,'authorization_code',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read','http://localhost:8081/login', '2018-12-25 01:03:50.1234567', '2018-12-25 01:03:50.1234567', '2018-12-25 01:03:50.1234567'),
       ('another-test-client-2','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null, '2019-12-25 01:03:50.1234567', '2018-12-25 01:03:50.1234567', '2018-12-25 01:03:50.1234567'),
       ('another-test-client-3','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null, '2020-01-25 01:03:50.1234567', '2019-01-25 01:03:50.1234567', '2019-01-25 01:03:50.1234567'),
       ('another-test-client-4','1200','{}',null,'client_credentials',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read',null, '2020-02-25 02:08:45.1234567', '2020-02-25 02:08:45.1234567', '2020-02-25 02:08:45.1234567');

UPDATE oauth_client_details
SET additional_information = '{"jwtFields":"-name"}'
WHERE client_id != 'omicuser' and client_id != 'service-mfa-test-client' and authorized_grant_types != 'client_credentials';

UPDATE oauth_client_details
SET autoapprove = scope;

INSERT INTO oauth_client_details (client_id, authorized_grant_types, client_secret) VALUES ('null-test-client','','');

INSERT INTO oauth_client_deployment_details (base_client_id,client_type, team, team_contact, team_slack, hosting, namespace, deployment, secret_name, client_id_key, secret_key)
VALUES ('service-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','service-dev','service-deployment','service-secret','API_CLIENT_ID','API_CLIENT_SECRET'),
       ('another-delete-test-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','another-delete-test-dev','another-delete-test-deployment','another-delete-test-secret','API_CLIENT_ID','API_CLIENT_SECRET'),
       ('another-test-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','duplicate-dev','duplicate-deployment','duplicate-secret','API_CLIENT_ID','API_CLIENT_SECRET');
INSERT INTO oauth_client_deployment_details (base_client_id,client_type, team, team_contact, team_slack, hosting)
VALUES ('individual-client','PERSONAL','Bob', 'Bob@digital.justice.gov.uk', 'bob slack', 'OTHER');
