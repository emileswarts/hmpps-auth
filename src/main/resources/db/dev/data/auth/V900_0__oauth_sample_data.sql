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
       ('service-add-test-client','1200','{}',null,'authorization_code',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read','http://localhost:3001/,http://localhost:3001/sign-in/callback'),
       ('service-edit-test-client','1200','{}',null,'authorization_code',null,'$2a$10$lBwbziQlLfiCnn8Kj1PfMujEcLdsJYlYSNJvBRO638gCYTS9yN0xm',null,null,'read','http://localhost:3001/,http://localhost:3001/sign-in/callback'),
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

INSERT INTO oauth_service (code, name, description, authorised_roles, url, enabled, email)
VALUES ('book-a-secure-move-ui', 'Book a secure move', 'Book a secure move', 'ROLE_PECS_SUPPLIER,ROLE_PECS_POLICE,', 'https://bookasecuremove.service.justice.gov.uk', 1, 'bookasecuremove@digital.justice.gov.uk'),
       ('categorisationtool', 'Digital Categorisation Service', 'Service to support categorisation of prisoners providing a consistent workflow and risk indicators.', 'ROLE_CREATE_CATEGORISATION,ROLE_APPROVE_CATEGORISATION,ROLE_CATEGORISATION_SECURITY,ROLE_CREATE_RECATEGORISATION', 'https://offender-categorisation.service.justice.gov.uk', 1, 'categorisation@justice.gov.uk'),
       ('HDC', 'Home Detention Curfew', 'Service for HDC Licences Creation and Approval', 'ROLE_LICENCE_CA,ROLE_LICENCE_RO,ROLE_LICENCE_DM', 'http://localhost:3003', 1, 'hdcdigitalservice@digital.justice.gov.uk'),
       ('KW', 'Keyworker Management Service', 'Service to allow viewing and allocation of Key workers to prisoners and viewing of prison and staff level statistics.', 'ROLE_OMIC_ADMIN,ROLE_KEYWORKER_MONITOR', 'http://localhost:3001/manage-key-workers', 1, null),
       ('prison-staff-hub', 'Digital Prison Service', 'View and Manage Offenders in Prison (Old name was NEW NOMIS)', 'ROLE_PRISON', 'http://localhost:3000', 1, 'feedback@digital.justice.gov.uk'),
       ('OAUTHADMIN', 'Oauth Client Management', 'Manage Client Credentials for OAUTH2 Clients', 'ROLE_OAUTH_ADMIN', 'http://localhost:8080/auth/ui/', 1, null),
       ('POM', 'Allocate a POM Service', 'Allocate the appropriate offender manager to a prisoner', 'ROLE_ALLOC_MGR', 'https://moic.service.justice.gov.uk', 1, 'https://moic.service.justice.gov.uk/help'),
       ('pathfinder-client', 'Pathfinder Service', 'View and Manage Pathfinder nominals', 'ROLE_PF_STD_PRISON,ROLE_PF_APPROVAL,ROLE_PF_POLICE', 'http://localhost:3000', 1, null),
       ('manage-soc-cases-client', 'Manage SOC cases', 'View and manage SOC cases', 'ROLE_SOC_CUSTODY,ROLE_SOC_COMMUNITY', 'http://localhost:3000', 1, null),
       ('manage-user-accounts-ui', 'Manage user accounts', null, 'ROLE_KW_MIGRATION,ROLE_MAINTAIN_ACCESS_ROLES,ROLE_MAINTAIN_ACCESS_ROLES_ADMIN,ROLE_MAINTAIN_OAUTH_USERS,ROLE_AUTH_GROUP_MANAGER', 'http://localhost:3001/', 1, null),
       ('DETAILS', 'Manage account details', null, null, '/auth/account-details?redirect_uri=/', 1, null),
       ('service-edit-test-client', 'test service', 'test service for testing', 'ROLE_FRED_ROLE', '/auth/account-details?redirect_uri=/', 0, null),
       ('manage-intelligence-client', 'Manage Intelligence', 'Manage Intelligence Reports', 'ROLE_ARTEMIS_USER', 'http://localhost:3000', 1, null);


INSERT INTO user_retries (username, retry_count)
VALUES ('LOCKED_USER', 5),
       ('LOCKED_NOMIS_USER', 3),
       ('AUTH_DELETEALL', 3),
       ('NOMIS_DELETE', 1);


-- nomis users
INSERT INTO users (user_id, username, email, verified, source)
 VALUES ('A04C70EE-51C9-4852-8D0D-130DA5C85C42', 'ITAG_USER', 'itag_user@digital.justice.gov.uk', 1, 'nomis'),
        ('A04C70EE-51C9-4852-8D0D-130DA5C85C43', 'RESET_USER', 'reset_user@digital.justice.gov.uk', 1, 'nomis'),
        ('0181F647-C7D4-41E7-9271-288EC7C01F90', 'DM_USER', 'dm_user@digital.justice.gov.uk', 0, 'nomis'),
        ('151DD6BC-88EE-4246-AA18-45924819C9F5', 'EXPIRED_TEST_USER', 'expired_test_user@digital.justice.gov.uk', 1, 'nomis'),
        ('86192295-8652-40BB-B03F-4D56BB93C1D7', 'RESET_TEST_USER', 'reset_test@digital.justice.gov.uk', 1, 'nomis'),
        ('F566EEC3-32DD-4CA4-B477-56AEC62917A1', 'CA_USER_TEST', 'reset_test@digital.justice.gov.uk', 1, 'nomis'),
        ('846D7318-921C-4537-8E24-58306CED881B', 'PPL_USER', 'ppl_user@digital.justice.gov.uk', 1, 'nomis'),
        ('326A07B4-6C2F-4CF9-A904-84262EB5C4FF', 'DM_USER_TEST', 'dm_user_test@digital.justice.gov.uk', 1, 'nomis'),
        ('7969D655-03F6-464E-A318-9D3C8B53787A', 'EXPIRED_TEST2_USER', 'expired_test2_user@digital.justice.gov.uk', 1, 'nomis'),
        ('FCFC15C1-6EE5-4EB2-8312-A1302AE3CFD1', 'ITAG_USER_ADM', 'itag_user_adm@digital.justice.gov.uk', 1, 'nomis'),
        ('E94B2E26-DC8A-4020-9533-A509807F68DF', 'EXPIRED_TEST3_USER', 'expired_test3_user@digital.justice.gov.uk', 1, 'nomis'),
        ('C0279EE3-76BF-487F-833C-AA47C5DF22F8', 'CA_USER', 'ca_user@digital.justice.gov.uk', 1, 'nomis'),
        ('6A3F0216-BBAB-49CD-BD6E-AC09C1762EE4', 'LOCKED_USER', 'locked@somewhere.com', 1, 'nomis'),
        ('6A3F0216-BBAB-49CD-BD6E-AC09C1762EE5', 'LOCKED_NOMIS_USER', 'lockednomisuser@somewhere.com', 1, 'nomis'),
        ('79CDC23C-510F-4CE2-8C98-AC251296EC39', 'RO_DEMO', 'ro_user@some.justice.gov.uk', 0, 'nomis'),
        ('AB8DA2CA-3E79-42D3-883E-CEE6C3F693CA', 'RO_USER_TEST', 'ro_user_test@digital.justice.gov.uk', 1, 'nomis'),
        ('5C72B180-5211-454D-9605-CF29573B946F', 'UOF_REVIEWER_USER', 'uof_reviewer@digital.justice.gov.uk', 1, 'nomis'),
        ('98FBF8D7-4164-47B3-826F-ECD3BB643005', 'RCTL_USER', 'rctl_user@digital.justice.gov.uk', 1, 'nomis'),
        ('AAABF8D7-4164-47B3-826F-ECD3BB64300F', 'PF_RO_USER', 'pf_ro_user@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E7BFED', 'UOF_COORDINATOR_USER', 'uof_coordinator@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E7BFEE', 'LICENCE_READONLY_TEST', 'licence_readonly_test@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E8BFEF', 'RO_USER_READONLY_TEST', 'ro_user_readonly_test@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E8BFED', 'CA_RO_DM_USER', 'ca_ro_dm_user_test@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-667B-8711-DA7EB4432CED', 'SOC_PRISON_LOCAL', 'soc_prison_local@digital.justice.gov.uk', 1, 'nomis'),
        ('4115C3DF-317B-4007-BF58-F2ECA1BBEED4', 'PCMS_ANALYST_USER', 'pcms_analyst_user@digital.justice.gov.uk', 1, 'nomis'),
        ('68599FE3-522D-4210-8296-D1469C35B93E', 'PCMS_AO_USER', 'pcms_ao_user@digital.justice.gov.uk', 1, 'nomis'),
        ('26E3087B-FA89-4D8B-914C-D72B287E4289', 'PCMS_GLOBAL_ADMIN_USER', 'pcms_global_admin_user@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E7BFEF', 'PRISON_COLLATOR_LOCAL', 'prison_collator_local@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3C4E2', 'PRISON_ANALYST_LOCAL', 'prison_analyst_local@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3C4E3', 'PPUD_USER', 'ppud.user.test@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65E7BAAA', 'CALCULATE_RELEASE_DATES_LOCAL', 'calculate_release_dates_local@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CAAB', 'CVL_OMU_LOCAL', 'cvl_omu_local@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CAAC', 'PCMS_AUDIT_USER', 'pcms_audit_user@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CAAD', 'HWPV_CLAIM_ENTRY_BAND_2', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CAAE', 'HWPV_CLAIM_PAYMENT_BAND_3', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CAAF', 'HWPV_CASEWORK_MANAGER_BAND_5', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CABA', 'HWPV_BAND_9', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CABB', 'HWPV_SSCL_USER', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CABC', 'HWPV_ALL_USER', 'prisonvisitsbooking@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CABD', 'PT_OMU_LOCAL', 'pt_omu_local@digital.justice.gov.uk', 1, 'nomis'),
        ('C3B15C4B-ADF5-493B-9424-DBCC65B3CABE', 'MMP_USER', 'mmp_user@digital.justice.gov.uk', 1, 'nomis'),
        ('2D787EC4-556F-4D13-818A-45D6843B9A8D', 'SLM_MAILROOM_USER_LOCAL', 'send_legal_mail_mailroom_user_local@digital.justice.gov.uk', 1, 'nomis'),
        ('F770E655-B59C-427F-8DBB-0EE5FD00B895', 'SLM_SECURITY_ANALYST_LOCAL', 'send_legal_mail_security_analyst_local@digital.justice.gov.uk', 1, 'nomis');


INSERT INTO users (user_id, username, email, verified, last_logged_in, source)
 VALUES ('A2B6E3C0-2CE4-4148-9DFB-42E94BC78D02', 'NOMIS_DELETE', 'locked@somewhere.com', 1, '2018-02-04 13:23:19.0000000', 'nomis');

-- auth users
INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source, last_logged_in)
 VALUES ('f3daec63-ee2f-467c-a6ee-92c3008193bd', 'AUTH_USER_LAST_LOGIN', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_last_login@digital.justice.gov.uk', 'Auth_Last', 'Login', 0, 1, 0, 'auth', '2019-01-01 12:05:10'),
        ('f3daec63-ee2f-467c-a6ee-92c3008193ff', 'AUTH_USER_LAST_LOGIN2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_last_login2@digital.justice.gov.uk', 'Auth_Last', 'Login', 1, 1, 0, 'auth', '2021-04-05 12:05:10');

INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source)
 VALUES ('608955AE-52ED-44CC-884C-011597A77949', 'AUTH_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user@digital.justice.gov.uk', 'Auth', 'Only', 1, 1, 0, 'auth'),
        ('C36E5A30-53C7-4F6F-9591-0547A2E4897C', 'AUTH_NO_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'NoEmail', 1, 1, 0, 'auth'),
        ('0E7AFB2E-A326-4AB6-920C-0A7098F5031F', 'AUTH_LOCKED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Locked', 1, 1, 1, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02A', 'AUTH_RO_USER_TEST', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test@digital.justice.gov.uk', 'Ryan-Auth', 'Orton', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02B', 'AUTH_RO_USER_TEST2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test2@digital.justice.gov.uk', 'Ryan-Auth', 'Orton2', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02C', 'AUTH_RO_USER_TEST3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test3@digital.justice.gov.uk', 'Ryan-Auth', 'Orton3', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA44A', 'AUTH_RO_USER_TEST4', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test4@digital.justice.gov.uk', 'Ryan-Auth', 'Orton4', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA44B', 'AUTH_RO_USER_TEST5', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test5@digital.justice.gov.uk', 'Ryan-Auth', 'Orton5', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02F', 'AUTH_RO_USER_TEST6', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test6@digital.justice.gov.uk', 'Ryan-Auth', 'Orton6', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA030', 'AUTH_RO_USER_TEST7', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user_test7@digital.justice.gov.uk', 'Ryan-Auth', 'Orton6', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02D', 'AUTH_ADD_ROLE_TEST', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_add_role@digital.justice.gov.uk', 'Add', 'RoleTest', 1, 1, 0, 'auth'),
        ('90F930E1-2195-4AFD-92CE-0EB5672DA02E', 'AUTH_ADD_ROLE_TEST2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_add_role2@digital.justice.gov.uk', 'Add', 'RoleTest2', 1, 1, 0, 'auth'),
        ('D9873CB3-24BD-4CFF-9CFE-1E64CE6BBCC4', 'AUTH_LOCKED2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_locked2@digital.justice.gov.uk', 'Auth', 'Locked2', 1, 1, 1, 'auth'),
        ('5E3850B9-9D6E-49D7-B8E7-42874D6CEEA8', 'AUTH_RO_VARY_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user@digital.justice.gov.uk', 'Ryan-Auth-Vary', 'Orton', 1, 1, 0, 'auth'),
        ('AD7D37E2-DBAD-4B98-AF8D-429E822A6BDC', 'AUTH_DISABLED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_disabled@digital.justice.gov.uk', 'Auth', 'Disabled', 1, 0, 0, 'auth'),
        ('7CA04ED7-8275-45B2-AFB4-4FF51432D1EA', 'AUTH_RO_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user@digital.justice.gov.uk', 'Ryan-Auth', 'Orton', 1, 1, 0, 'auth'),
        ('7CA04ED7-8275-45B2-AFB4-4FF51432D1EB', 'AUTH_RO_USER1@DIGITAL.JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user1@digital.justice.gov.uk', 'Ryan-Auth', 'Orton', 1, 1, 0, 'auth'),
        ('7CA04ED7-8275-45B2-AFB4-4FF51432D1EC', 'AUTH_RO_USER2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_ro_user2@digital.justice.gov.uk', 'Ryan-Auth', 'Orton', 1, 1, 0, 'auth'),
        ('1F650F15-0993-4DB7-9A32-5B930FF86035', 'AUTH_GROUP_MANAGER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_group_manager@digital.justice.gov.uk', 'Group', 'Manager', 1, 1, 0, 'auth'),
        ('1F650F15-0993-4DB7-9A32-5B930FF86036', 'AUTH_GROUP_MANAGER2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_group_manager2@digital.justice.gov.uk', 'Group', 'Manager2', 1, 1, 0, 'auth'),
        ('FC494152-F9AD-48A0-A87C-9ADC8BD75255', 'AUTH_STATUS', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Status', 1, 0, 0, 'auth'),
        ('FC494152-F9AD-48A0-A87C-9ADC8BD75266', 'AUTH_STATUS2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Status2', 1, 0, 0, 'auth'),
        ('FC494152-F9AD-48A0-A87C-9ADC8BD75277', 'AUTH_STATUS3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Status3', 1, 0, 0, 'auth'),
        ('FC494152-F9AD-48A0-A87C-9ADC8BD75288', 'AUTH_STATUS4', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Status4', 1, 0, 0, 'auth'),
        ('FC494152-F9AD-48A0-A87C-9ADC8BD75299', 'AUTH_STATUS5', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Auth', 'Status5', 1, 0, 0, 'auth'),
        ('9E84F1E4-59C8-4B10-927A-9CF9E9A30791', 'AUTH_EXPIRED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '2013-01-28 13:23:19.0000000', 'auth_test2@digital.justice.gov.uk', 'Auth', 'Expired', 1, 1, 0, 'auth'),
        ('9E84F1E4-59C8-4B10-927A-9CF9E9A30792', 'AUTH_MFA_EXPIRED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '2013-01-28 13:23:19.0000000', 'auth_test3@digital.justice.gov.uk', 'Auth', 'Expired', 1, 1, 0, 'auth'),
        ('5105A589-75B3-4CA0-9433-B96228C1C8F3', 'AUTH_ADM', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_test2@digital.justice.gov.uk', 'Auth', 'Adm', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A3E', 'AUTH_TEST', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_test@digital.justice.gov.uk', 'Auth', 'Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2B3F', 'AUTH_CHANGE_TEST', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_test@digital.justice.gov.uk', 'Auth', 'Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2C3F', 'AUTH_CHANGE2_TEST', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_test@digital.justice.gov.uk', 'Auth', 'Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D3F', 'AUTH_CHANGE_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_test@digital.justice.gov.uk', 'Auth', 'Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E23-D6E8E10A2D3F', 'AUTH_CHANGE_EMAIL@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_change_email@justice.gov.uk', 'User', 'Email Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4597-9E23-D6E8E10A2D3F', 'AUTH_VERIFY_EMAIL@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_verify_email@justice.gov.uk', 'Verify', 'Email Test', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E24-D6E8E10A2D3F', 'AUTH_USER_EMAIL@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_email@justice.gov.uk', 'User', 'Email Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E25-D6E8E10A2D3F', 'AUTH_USER_EMAIL_TEST@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_email_test@justice.gov.uk', 'User', 'Email Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E26-D6E8E10A2D3F', 'AUTH_USER_EMAIL2_TEST@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_email2_test@justice.gov.uk', 'User', 'Email Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E27-D6E8E10A2D3F', 'AUTH_USER_EMAIL3_TEST@JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_user_email3_test@justice.gov.uk', 'User', 'Email Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E28-D6E8E10A2D3F', 'AUTH_BULK_AMEND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_change@justice.gov.uk', 'User', 'Change Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E29-D6E8E10A2D3F', 'AUTH_BULK_AMEND_EMAIL@DIGITAL.JUSTICE.GOV.UK', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_bulk_amend_email@digital.justice.gov.uk', 'User', 'Change Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E30-D6E8E10A2D3F', 'AUTH_CHANGE_TO_USERNAME', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_change@justice.gov.uk', 'User', 'Change Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D4F', 'AUTH_CHANGE_EMAIL2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Auth', 'Test', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D5F', 'AUTH_CHANGE_EMAIL_VERIFIED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Auth', 'Email', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D6F', 'AUTH_CHANGE_EMAIL_GSI', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Auth', 'Email', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D7F', 'AUTH_CHANGE_EMAIL_INVALID', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Auth', 'Email', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D8F', 'AUTH_CHANGE_EMAIL_INCOMPLETE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Auth', 'Email', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D40', 'AUTH_CHANGE_MOBILE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mobile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D41', 'AUTH_CHANGE_MOBILE2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mobile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D42', 'AUTH_CHANGE_MOBILE_ADD', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mobile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D43', 'AUTH_CHANGE_MOBILE3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mobile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('67A789DE-7D29-4863-B9C2-F2CE715DC4BC', 'AUTH_NEW_USER', null, '3013-01-28 13:23:19.0000000', 'a@b.com', 'Auth', 'New-User', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A3F', 'AUTH_MFA_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa', 'User', 1, 1, 0, 'auth'),
        ('2E285CCF-DCFD-4497-9E22-D6E8E10A2A3F', 'AUTH_MFA_NOEMAIL_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'Mfa No Email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCE-DCFD-4497-9E22-D6E8E10A2A3F', 'AUTH_MFA_TOKEN_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A4F', 'AUTH_MFA_EXPIRED_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A5F', 'AUTH_MFA_LOCKED_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A6F', 'AUTH_MFA_LOCKED2_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2A9F', 'AUTH_MFA_LOCKED3_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2A10', 'AUTH_MFA_LOCKED4_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2A11', 'AUTH_MFA_LOCKED5_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2A12', 'AUTH_MFA_LOCKED6_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-E6E8E10A2A6F', 'AUTH_MFA_CHANGE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'mfa_user@digital.justice.gov.uk', 'Mfa Locked', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-F6E8E10A2A6F', 'AUTH_MFA_CHANGE_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'Mfa', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A60', 'AUTH_SECOND_EMAIL_ADD', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A61', 'AUTH_SECOND_EMAIL_UPDATE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A62', 'AUTH_SECOND_EMAIL_VERIFY', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A63', 'AUTH_SECOND_EMAIL_VERIFY2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A64', 'AUTH_SECOND_EMAIL_ALREADY', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A65', 'AUTH_SECOND_EMAIL_CHANGE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A66', 'AUTH_MFA_PREF_NO_EMAIL_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', null, 'email', 'User', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A67', 'AUTH_MFA_PREF_EMAIL_AND_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A68', 'AUTH_CREATE_USER_VALID', null, '2020-01-28 13:23:19.0000000', 'create_user@digital.justice.gov.uk', 'email', 'User', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A69', 'AUTH_CREATE_USER_VALID2', null, '2020-01-28 13:23:19.0000000', 'create_user@digital.justice.gov.uk', 'email', 'User', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A6A', 'AUTH_CREATE_USER_EXPIRED', null, '2020-01-28 13:23:19.0000000', 'auth_email@digital.justice.gov.uk', 'email', 'User', 0, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-A9E8E10A2A6B', 'AUTH_CREATE_USER_INVALID', null, '2020-01-28 13:23:19.0000000', 'auth_invalid_token@digital.justice.gov.uk', 'email', 'User', 0, 1, 0, 'auth'),
        ('6c76f1fa-3393-11ea-978f-2e728ce88125', 'AUTH_VIDEO_LINK_COURT_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'vlb_court_user@digital.justice.gov.uk', 'VLB Court', 'User', 1, 1, 0, 'auth'),
        ('9f6f8681-c273-48e5-bf6c-29a2d4fe159d', 'NOMIS_ENABLED_AUTH_DISABLED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'nomis_auth_user@digital.justice.gov.uk', 'nomis_enabled', 'Auth Disabled', 1, 0, 0, 'auth'),
        ('8351b5de-2c38-4c0a-bcad-8ee78de6b5af', 'NOMIS_LOCKED_AUTH_DISABLED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'nomis_auth_user@digital.justice.gov.uk', 'nomis locked', 'Auth Disabled', 1, 0, 0, 'auth'),
        ('f83c7a90-50c2-4b1b-b5ab-59206f731202', 'DELIUS_ENABLED_AUTH_DISABLED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'delius_auth_user@digital.justice.gov.uk', 'nomis_disabled', 'Auth Disabled', 1, 0, 0, 'auth'),
        ('f83c7a90-50c2-4b1b-b5ab-59206f731203', 'DELIUS_ENABLED_AUTH_DISABLED_LOCKED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'delius_auth_user@digital.justice.gov.uk', 'delius enabled', 'Auth Disabled Locked', 1, 0, 1, 'auth'),
        ('9274ec5b-19fd-49ce-ae3c-a6f5d77a3388', 'WMT_MANAGER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'wmt_manager@digital.justice.gov.uk', 'Wmt', 'Manager', 1, 1, 0, 'delius'),
        ('edec977d-b743-4744-a8a8-1da84ee40c6b', 'WMT_DATA_ADMIN', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'wmt_data_admin@digital.justice.gov.uk', 'Wmt Data', 'Admin', 1, 1, 0, 'delius'),
        ('f47742d4-18ba-4332-9519-86b2c94ab99c', 'WMT_STAFF', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'wmt_staff@digital.justice.gov.uk', 'Wmt', 'Staff', 1, 1, 0, 'delius'),
        ('7fa0d669-6875-4d6b-86a5-3cf42c1c0015', 'WMT_SYSTEM_ADMIN', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'wmt_system_admin@digital.justice.gov.uk', 'Wmt System', 'Admin', 1, 1, 0, 'delius'),
        ('730f3b3c-2758-4a92-b341-f2489dcbc6a0', 'ALLOCATIONS', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'allocations@digital.justice.gov.uk', 'Allocations', 'User', 1, 1, 0, 'delius')
        ;

INSERT INTO users (user_id, username, password, last_logged_in, first_name, last_name, verified, enabled, locked, source)
 VALUES ('7B59A818-BC14-43F3-A1C3-93004E173B2A', 'AUTH_DELETE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '2018-01-02 13:23:19.0000000', 'Auth', 'Delete', 1, 0, 0, 'auth'),
        ('DA28D339-85FA-42C1-9CFA-AC67055A51A5', 'AUTH_INACTIVE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '2019-02-03 13:23:19.0000000', 'Auth', 'Inactive', 0, 1, 0, 'auth'),
        ('7112EC3B-88C1-48C3-BCC3-F82874E3F2C3', 'AUTH_DELETEALL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '2018-02-03 13:23:19.0000000', 'Auth', 'DeleteAll', 1, 0, 0, 'auth');

INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source)
 VALUES ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D50', 'AUTH_CHANGE_MOBILE_VERIFIED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mnbile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D51', 'AUTH_CHANGE_MOBILE_UPDATE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_mobile@digital.justice.gov.uk', 'Auth', 'Mobile', 1, 1, 0, 'auth'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D52', 'AUTH_UNVERIFIED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Unverified', 0, 1, 0, 'auth');

INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source, mfa_preference)
 VALUES ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D60', 'AUTH_MFA_PREF_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D61', 'AUTH_MFA_PREF_EMAIL2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D62', 'AUTH_MFA_PREF_EMAIL3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D63', 'AUTH_MFA_PREF_EMAIL4', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D64', 'AUTH_MFA_PREF_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D65', 'AUTH_MFA_PREF_TEXT2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D66', 'AUTH_MFA_PREF_TEXT3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D67', 'AUTH_MFA_PREF_TEXT4', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2A11', 'AUTH_MFA_PREF_TEXT5', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D68', 'AUTH_MFA_LOCKED_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D69', 'AUTH_MFA_LOCKED2_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E23-D6E8E10A2D69', 'AUTH_MFA_LOCKED3_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E23-D6E8E10A2D01', 'AUTH_MFA_LOCKED4_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E23-D6E8E10A2D02', 'AUTH_MFA_LOCKED5_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCF-DCFD-4498-9E22-D6E8E10A2D6A', 'AUTH_MFA_NOTEXT_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Mfa No Text', 'User', 0, 1, 0, 'auth','TEXT'),
        ('2E285CCF-DCFD-4499-9E22-D6E8E10A2D6B', 'AUTH_MFA_PREF_TEXT_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.text@digital.justice.gov.uk', 'Mfa No Text', 'User', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D6C', 'AUTH_MFA_SHORT_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'bob@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D6D', 'AUTH_MFA_NON_VERIFIED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'bob@digital.justice.gov.uk', 'Auth', 'Mfa', 0, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D6E', 'AUTH_MFA_PREF_EMAIL5', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2D6F', 'AUTH_MFA_PREF_EMAIL6', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.email@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D70', 'AUTH_UNVERIFIED_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 0, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D71', 'AUTH_MFA_PREF_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D72', 'AUTH_MFA_PREF_2ND_EMAIL2', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D73', 'AUTH_MFA_PREF_2ND_EMAIL3', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D74', 'AUTH_MFA_PREF_2ND_EMAIL4', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D75', 'AUTH_MFA_LOCKED_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D76', 'AUTH_MFA_LOCKED2_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E21-D6E8E10A2D76', 'AUTH_MFA_LOCKED3_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E20-D6E8E10A2D76', 'AUTH_MFA_LOCKED4_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E19-D6E8E10A2D76', 'AUTH_MFA_LOCKED5_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D77', 'AUTH_MFA_PREF_2ND_EMAIL_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D80', 'AUTH_PREF_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.pref.text@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D81', 'AUTH_PREF_2ND_EMAIL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.pref.secondemail@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D83', 'AUTH_PREF_EMAIL_TEXT', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.pref.secondemail@digital.justice.gov.uk', 'Auth', 'Mfa', 1, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D82', 'AUTH_NO_PREF', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth.no.pref@digital.justice.gov.uk', 'Auth', 'Mfa', 0, 1, 0, 'auth', 'TEXT'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D78', 'AUTH_USER_LINK_EXPIRED', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 0, 1, 0, 'auth', 'SECONDARY_EMAIL'),
        ('2E285CCD-DCFD-4497-9E22-D6E8E10A2D79', 'AUTH_USER_2ND_EMAIL_LINK_EXP', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'auth_unverified@digital.justice.gov.uk', 'Auth', 'Mfa', 0, 1, 0, 'auth', 'SECONDARY_EMAIL');

-- delius users
INSERT INTO users (user_id, username, email, last_logged_in, first_name, last_name, verified, enabled, locked, source)
 VALUES ('7B59A818-BC14-43F3-A1C3-93004E173B2B', 'DELIUS_EMAIL', 'delius_user@digital.justice.gov.uk', '3013-01-02 13:23:19.0000000', 'Delius', 'Smith', 1, 1, 0, 'delius'),
        ('7B59A818-BC14-43F3-A1C3-93004E173B22', 'DELIUS_PASSWORD_RESET', 'delius_locked@digital.justice.gov.uk', '3013-01-02 13:23:19.0000000', 'Delius', 'Smith', 1, 1, 0, 'delius'),
        ('7B59A818-BC14-43F3-A1C3-93004E173B23', 'DELIUS_SECOND_EMAIL_UPDATE', 'delius_email@digital.justice.gov.uk', '3013-01-02 13:23:19.0000000', 'Delius', 'Smith', 1, 1, 0, 'delius');

INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source)
 VALUES ('7B59A818-BC14-43F3-A1C3-93004E173B2C','DELIUS_EMAIL_RESET', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy','3013-01-02 13:23:19.0000000', 'delius_email@digital.justice.gov.uk',  'Delius', 'Smith', 1, 1, 0, 'delius');

-- oauth user (police user used by pathfinder)
INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, create_datetime, password_expiry, last_logged_in, source, mfa_preference)
VALUES
('ABD94E71-0047-43F1-842B-7BD7E0EB5B09', 'TEST_PF_POLICE_1', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'test.pf.police@digital.justice.gov.uk', 'Bobby', 'London', 1, 0, 1, 0, '2020-05-01 15:07:34.5466667', '2040-04-26 16:17:28.4953990', '2019-05-18 14:16:21.7349800', 'auth', 'EMAIL');

insert into groups (group_id, group_code, group_name) values
(NEWID(), 'PF_LONDON', 'Pathfinder Police - London');

insert into group_assignable_role (role_id, group_id, automatic) values ((select role_id from roles where role_code = 'PF_POLICE'),
                                                                         (select group_id from groups where group_code = 'PF_LONDON'), 1);

INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'TEST_PF_POLICE_1' and role_code = 'PF_POLICE';

INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_PF_POLICE_1' and group_code = 'PF_LONDON';


-- Local police accounts for manage-soc-cases
INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, password_expiry, source, mfa_preference)
VALUES ('ABCF4E74-0056-43D1-454B-8BD7E0ED5B10', 'SOC_POLICE_LOCAL_NE', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'soc.ne@digital.justice.gov.uk', 'Bobby', 'Gateshead', 1, 0, 1, 0, '2040-04-26 16:17:28.0000000', 'auth', 'EMAIL');

INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'SOC_POLICE_LOCAL_NE' and group_code = 'SOC_NORTH_EAST';

INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, password_expiry, source, mfa_preference)
VALUES ('ABCF4F85-0057-43E1-454C-87D345DD510A', 'SOC_POLICE_LOCAL_NW', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'soc.nw@digital.justice.gov.uk', 'Bobby', 'Chester', 1, 0, 1, 0, '2040-04-26 16:17:28.0000000', 'auth', 'EMAIL');

INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'SOC_POLICE_LOCAL_NW' and group_code = 'SOC_NORTH_WEST';

-- Auth user for Artemis
INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, create_datetime, password_expiry, last_logged_in, source, mfa_preference)
VALUES
('ABD94E71-0047-43F1-842B-5BEF234AEB09', 'AUTH_INTEL_LOCAL', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'auth.intel@digital.justice.gov.uk', 'Intel', 'Local', 1, 0, 1, 0, '2020-05-01 15:07:34.5466667', '2040-04-26 16:17:28.4953990', '2020-05-18 14:16:21.7349800', 'auth', 'EMAIL');

INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_INTEL_LOCAL' and role_code = 'ARTEMIS_USER';

-- Auth user for pecs journey price calculation service
INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, create_datetime, password_expiry, last_logged_in, source, mfa_preference)
VALUES
('ABD94E71-0047-43F1-842B-5BEF234AEB10', 'JPC_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'auth.intel@digital.justice.gov.uk', 'JPC', 'User', 1, 0, 1, 0, '2020-05-01 15:07:34.5466667', '2040-04-26 16:17:28.4953990', '2020-05-18 14:16:21.7349800', 'auth', 'EMAIL');

INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'JPC_USER' and role_code = 'PECS_JPC';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'JPC_USER' and role_code = 'PECS_MAINTAIN_PRICE';

-- auth user for interventions service provider journeys
INSERT INTO users (user_id, username, password, email, first_name, last_name, verified, locked, enabled, master, create_datetime, password_expiry, last_logged_in, source, mfa_preference)
    VALUES ('6C4036B7-E87D-44FB-864F-5A06C1C492F3', 'TEST_INTERVENTIONS_SP_1', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', 'test.interventions.sp.1@digital.justice.gov.uk', 'Robin', 'Croswell', 1, 0, 1, 0, '2021-03-05 11:48:34.2723638', '2040-04-26 16:17:28.4953990', '2040-03-05 11:48:34.2723638', 'auth', 'EMAIL');
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'TEST_INTERVENTIONS_SP_1' and role_code = 'CRS_PROVIDER';
INSERT INTO groups (group_id, group_code, group_name)
    values ('6B30BD55-8F75-4B0B-A647-42A34B0A383D', 'INT_SP_HARMONY_LIVING', 'Intervention Provider - Harmony Living'),
           ('628EEE33-5BCD-41C8-97A5-F655E8689EAD', 'INT_SP_HOME_TRUST', 'Intervention Provider - Home Trust'),
           ('83C3869B-696D-4A6E-8DA4-3D36666BAB9D', 'INT_CR_0001', 'Intervention Contract - 0001'),
           ('05AC35B9-EF9D-47C3-9409-CBCF334ACD73', 'INT_CR_0002', 'Intervention Contract - 0002'),
           ('FF76FC87-9D3F-4790-8B14-F67BE6DA724E', 'INT_CR_0003', 'Intervention Contract - 0003'),
           ('24F36BE2-93D7-4608-B106-BDA9AA324523', 'INT_CR_0004', 'Intervention Contract - 0004'),
           ('7D7C7F62-7D96-491F-B879-BD34182DAD69', 'INT_CR_0005', 'Intervention Contract - 0005'),
           ('9872981C-2FB4-4BB6-BFBE-40E662A92A60', 'INT_CR_0006', 'Intervention Contract - 0006');
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_SP_HARMONY_LIVING';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0001';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0002';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0003';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0004';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0005';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'TEST_INTERVENTIONS_SP_1' and group_code = 'INT_CR_0006';

INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'reset', 'RESET', '2018-12-10 08:55:45.0000000', user_id from users where username = 'LOCKED_USER';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'reset2', 'RESET', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_DELETEALL';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'reset3', 'RESET', '3031-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_CREATE_USER_VALID';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'reset4', 'RESET', '3031-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_CREATE_USER_VALID2';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'reset5', 'RESET', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_CREATE_USER_EXPIRED';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'expired1', 'VERIFIED', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_USER_LINK_EXPIRED';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'expired2', 'SECONDARY', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_USER_2ND_EMAIL_LINK_EXP';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'verified', 'VERIFIED', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_DELETEALL';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'mfa_expired', 'MFA', '2018-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_MFA_EXPIRED_USER';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'mfa_token', 'MFA', '3031-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_MFA_TOKEN_USER';
INSERT INTO user_token (token, token_type, token_expiry, user_id) SELECT 'mfa_code', 'MFA_CODE', '3031-12-10 08:55:45.0000000', user_id from users where username = 'AUTH_MFA_TOKEN_USER';

INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_ADM' and role_code = 'OAUTH_ADMIN';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_ADM' and role_code = 'MAINTAIN_ACCESS_ROLES';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_ADM' and role_code = 'MAINTAIN_OAUTH_USERS';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_ADM' and role_code = 'ROLES_ADMIN';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_ADM' and role_code = 'AUDIT_VIEWER';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER2' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER2' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_VARY_USER' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_VARY_USER' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_VARY_USER' and role_code = 'LICENCE_VARY';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER_TEST' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER_TEST' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER_TEST2' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER_TEST6' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_RO_USER_TEST6' and role_code = 'GLOBAL_SEARCH';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_GROUP_MANAGER' and role_code = 'AUTH_GROUP_MANAGER';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_GROUP_MANAGER2' and role_code = 'AUTH_GROUP_MANAGER';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_DELETEALL' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_DELETEALL' and role_code = 'LICENCE_RO';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_USER' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_NOEMAIL_USER' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_NOTEXT_USER' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED2_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED_TEXT' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED2_TEXT' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_EXPIRED' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_CHANGE' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_CHANGE_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_EMAIL2' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_EMAIL3' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_EMAIL4' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_EMAIL5' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT2' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT3' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT4' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_TEXT5' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_SHORT_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_NON_VERIFIED' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_2ND_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_2ND_EMAIL2' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_2ND_EMAIL3' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_2ND_EMAIL4' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_PREF_2ND_EMAIL_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED2_2ND_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_MFA_LOCKED_2ND_EMAIL' and role_code = 'MFA';
INSERT INTO user_role (role_id, user_id) SELECT role_id, user_id from roles, users where username = 'AUTH_VIDEO_LINK_COURT_USER' and role_code = 'VIDEO_LINK_COURT_USER';

INSERT INTO groups (group_id, group_code, group_name) VALUES (newid(), 'SITE_1_GROUP_1', 'Site 1 - Group 1'),
       (newid(), 'SITE_1_GROUP_2', 'Site 1 - Group 2'),
       (newid(), 'SITE_2_GROUP_1', 'Site 2 - Group 1'),
       (newid(), 'SITE_3_GROUP_1', 'Site 3 - Group 1'),
       (newid(), 'SITE_9_GROUP_1', 'Site 9 - Group 1'),
       (newid(), 'GC_DEL_1', 'Group 1 for deleting'),
       (newid(), 'GC_DEL_2', 'Group 2 for deleting'),
       (newid(), 'GC_DEL_3', 'Group 3 for deleting'),
       (newid(), 'GC_DEL_4', 'Group 4 for deleting');

INSERT INTO child_group (child_group_id, child_group_code, child_group_name, group_id) VALUES (newid(), 'CHILD_1', 'Child - Site 1 - Group 2', (select group_id from groups where group_code = 'SITE_1_GROUP_2'));
INSERT INTO child_group (child_group_id, child_group_code, child_group_name, group_id) VALUES (newid(), 'CHILD_2', 'Child - Site 2 - Group 1', (select group_id from groups where group_code = 'SITE_2_GROUP_1'));
INSERT INTO child_group (child_group_id, child_group_code, child_group_name, group_id) VALUES (newid(), 'CHILD_3', 'Child - Site 2 - Group 1', (select group_id from groups where group_code = 'SITE_2_GROUP_1'));
INSERT INTO child_group (child_group_id, child_group_code, child_group_name, group_id) VALUES (newid(), 'CHILD_9', 'Child - Site 9 - Group 1', (select group_id from groups where group_code = 'SITE_9_GROUP_1'));
INSERT INTO child_group (child_group_id, child_group_code, child_group_name, group_id) VALUES (newid(), 'DEL_CHILD_1', 'Child - GC_DEL_3', (select group_id from groups where group_code = 'GC_DEL_3'));

INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_VARY_USER' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_VARY_USER' and group_code = 'SITE_1_GROUP_2';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER2' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST' and group_code = 'SITE_2_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST2' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST2' and group_code = 'SITE_2_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST3' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST3' and group_code = 'GC_DEL_4';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST5' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST6' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST6' and group_code = 'SITE_2_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST7' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_RO_USER_TEST7' and group_code = 'GC_DEL_4';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_GROUP_MANAGER' and group_code = 'SITE_1_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_GROUP_MANAGER' and group_code = 'SITE_1_GROUP_2';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_DELETEALL' and group_code = 'SITE_3_GROUP_1';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_GROUP_MANAGER2' and group_code = 'PF_LONDON';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_CHANGE_TEST' and group_code = 'GC_DEL_2';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_CHANGE_TEST' and group_code = 'GC_DEL_2';
INSERT INTO user_group (group_id, user_id) SELECT group_id, user_id from groups, users where username = 'AUTH_STATUS5' and group_code = 'SITE_1_GROUP_1';

INSERT INTO group_assignable_role (role_id, group_id, automatic) SELECT role_id, group_id, 'true' FROM groups g, roles r WHERE r.role_code = 'GLOBAL_SEARCH' AND g.group_code = 'SITE_1_GROUP_1';
INSERT INTO group_assignable_role (role_id, group_id, automatic) SELECT role_id, group_id, 'true' FROM groups g, roles r WHERE r.role_code = 'LICENCE_RO' AND g.group_code = 'SITE_1_GROUP_1';
INSERT INTO group_assignable_role (role_id, group_id, automatic) SELECT role_id, group_id, 'false' FROM groups g, roles r WHERE r.role_code = 'LICENCE_VARY' AND g.group_code = 'SITE_1_GROUP_1';
INSERT INTO group_assignable_role (role_id, group_id, automatic) SELECT role_id, group_id, 'true' FROM groups g, roles r WHERE r.role_code = 'GLOBAL_SEARCH' AND g.group_code = 'SITE_1_GROUP_2';
INSERT INTO group_assignable_role (role_id, group_id, automatic) SELECT role_id, group_id, 'true' FROM groups g, roles r WHERE r.role_code = 'LICENCE_RO' AND g.group_code = 'SITE_1_GROUP_2';

INSERT INTO user_contact (user_id, type, value) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com' FROM users where username = 'AUTH_ADM';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL5';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_SECOND_EMAIL_UPDATE';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'bob@gmail.com', 0 FROM users where username = 'AUTH_SECOND_EMAIL_VERIFY';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'bob@gmail.com', 0 FROM users where username = 'AUTH_SECOND_EMAIL_VERIFY2';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_SECOND_EMAIL_ALREADY';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_SECOND_EMAIL_CHANGE';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT5';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_2ND_EMAIL2';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_2ND_EMAIL3';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_2ND_EMAIL4';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_LOCKED_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_LOCKED2_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_LOCKED3_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_LOCKED4_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_LOCKED5_2ND_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL_AND_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 0 FROM users where username = 'AUTH_USER_2ND_EMAIL_LINK_EXP';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_CHANGE_MOBILE_VERIFIED';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 0 FROM users where username = 'AUTH_CHANGE_MOBILE_UPDATE';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 0 FROM users where username = 'AUTH_UNVERIFIED';

INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL2';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL3';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL4';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT2';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT3';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT4';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_TEXT5';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_LOCKED_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_LOCKED2_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_LOCKED3_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_LOCKED4_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_LOCKED5_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 0 FROM users where username = 'AUTH_MFA_SHORT_EMAIL';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 0 FROM users where username = 'AUTH_MFA_UNVERIFIED';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL5';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 0 FROM users where username = 'AUTH_UNVERIFIED_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_2ND_EMAIL2';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_MFA_PREF_EMAIL_AND_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_PREF_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'MOBILE_PHONE', '07700900321', 1 FROM users where username = 'AUTH_PREF_EMAIL_TEXT';
INSERT INTO user_contact (user_id, type, value, verified) SELECT user_id, 'SECONDARY_EMAIL', 'john@smith.com', 1 FROM users where username = 'AUTH_PREF_2ND_EMAIL';

INSERT INTO oauth_code (code, authentication)
VALUES ('5bDHCW',
          0xaced0005737200416f72672e737072696e676672616d65776f726b2e73656375726974792e6f61757468322e70726f76696465722e4f417574683241757468656e7469636174696f6ebd400b02166252130200024c000d73746f7265645265717565737474003c4c6f72672f737072696e676672616d65776f726b2f73656375726974792f6f61757468322f70726f76696465722f4f4175746832526571756573743b4c00127573657241757468656e7469636174696f6e7400324c6f72672f737072696e676672616d65776f726b2f73656375726974792f636f72652f41757468656e7469636174696f6e3b787200476f72672e737072696e676672616d65776f726b2e73656375726974792e61757468656e7469636174696f6e2e416273747261637441757468656e7469636174696f6e546f6b656ed3aa287e6e47640e0200035a000d61757468656e746963617465644c000b617574686f7269746965737400164c6a6176612f7574696c2f436f6c6c656374696f6e3b4c000764657461696c737400124c6a6176612f6c616e672f4f626a6563743b787000737200266a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c654c697374fc0f2531b5ec8e100200014c00046c6973747400104c6a6176612f7574696c2f4c6973743b7872002c6a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c65436f6c6c656374696f6e19420080cb5ef71e0200014c00016371007e00047870737200136a6176612e7574696c2e41727261794c6973747881d21d99c7619d03000149000473697a65787000000008770400000008737200426f72672e737072696e676672616d65776f726b2e73656375726974792e636f72652e617574686f726974792e53696d706c654772616e746564417574686f7269747900000000000002300200014c0004726f6c657400124c6a6176612f6c616e672f537472696e673b787074001c524f4c455f43415445474f5249534154494f4e5f53454355524954597371007e000d740012524f4c455f474c4f42414c5f5345415243487371007e000d740016524f4c455f4b4559574f524b45525f4d4f4e49544f527371007e000d740020524f4c455f4d41494e5441494e5f4143434553535f524f4c45535f41444d494e7371007e000d74000f524f4c455f4f4d49435f41444d494e7371007e000d74000b524f4c455f505249534f4e7371007e000d740012524f4c455f5249534b5f50524f46494c45527371007e000d740010524f4c455f524f4c45535f41444d494e7871007e000c707372003a6f72672e737072696e676672616d65776f726b2e73656375726974792e6f61757468322e70726f76696465722e4f41757468325265717565737400000000000000010200075a0008617070726f7665644c000b617574686f72697469657371007e00044c000a657874656e73696f6e7374000f4c6a6176612f7574696c2f4d61703b4c000b726564697265637455726971007e000e4c00077265667265736874003b4c6f72672f737072696e676672616d65776f726b2f73656375726974792f6f61757468322f70726f76696465722f546f6b656e526571756573743b4c000b7265736f7572636549647374000f4c6a6176612f7574696c2f5365743b4c000d726573706f6e7365547970657371007e0022787200386f72672e737072696e676672616d65776f726b2e73656375726974792e6f61757468322e70726f76696465722e426173655265717565737436287a3ea37169bd0200034c0008636c69656e74496471007e000e4c001172657175657374506172616d657465727371007e00204c000573636f706571007e0022787074000f656c69746532617069636c69656e74737200256a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c654d6170f1a5a8fe74f507420200014c00016d71007e00207870737200116a6176612e7574696c2e486173684d61700507dac1c31660d103000246000a6c6f6164466163746f724900097468726573686f6c6478703f400000000000067708000000080000000474000d726573706f6e73655f74797065740004636f646574000c72656469726563745f75726974001b687474703a2f2f6c6f63616c686f73743a383038312f6c6f67696e7400057374617465740006447757556266740009636c69656e745f696471007e002578737200256a6176612e7574696c2e436f6c6c656374696f6e7324556e6d6f6469666961626c65536574801d92d18f9b80550200007871007e0009737200176a6176612e7574696c2e4c696e6b656448617368536574d86cd75a95dd2a1e020000787200116a6176612e7574696c2e48617368536574ba44859596b8b7340300007870770c000000103f4000000000000274000472656164740005777269746578017371007e0034770c000000103f40000000000000787371007e00283f40000000000000770800000010000000007874001b687474703a2f2f6c6f63616c686f73743a383038312f6c6f67696e707371007e0034770c000000103f40000000000000787371007e0034770c000000103f4000000000000171007e002b787372004f6f72672e737072696e676672616d65776f726b2e73656375726974792e61757468656e7469636174696f6e2e557365726e616d6550617373776f726441757468656e7469636174696f6e546f6b656e00000000000002300200024c000b63726564656e7469616c7371007e00054c00097072696e636970616c71007e00057871007e0003017371007e00077371007e000b0000000877040000000871007e000f71007e001171007e001371007e001571007e001771007e001971007e001b71007e001d7871007e0040707073720042756b2e676f762e6a7573746963652e6469676974616c2e686d7070732e6f61757468327365727665722e73656375726974792e5573657244657461696c73496d706c00000000000000010200065a0007697341646d696e5a00097061737365644d66614c000a61757468536f7572636571007e000e4c00056a7774496471007e000e4c00046e616d6571007e000e4c000675736572496471007e000e787200326f72672e737072696e676672616d65776f726b2e73656375726974792e636f72652e7573657264657461696c732e5573657200000000000002300200075a00116163636f756e744e6f6e457870697265645a00106163636f756e744e6f6e4c6f636b65645a001563726564656e7469616c734e6f6e457870697265645a0007656e61626c65644c000b617574686f72697469657371007e00224c000870617373776f726471007e000e4c0008757365726e616d6571007e000e7870010101017371007e0031737200116a6176612e7574696c2e54726565536574dd98509395ed875b0300007870737200466f72672e737072696e676672616d65776f726b2e73656375726974792e636f72652e7573657264657461696c732e5573657224417574686f72697479436f6d70617261746f720000000000000230020000787077040000000871007e000f71007e001171007e001371007e001571007e001771007e001971007e001b71007e001d78740000740009495441475f5553455200007400056e6f6d697374002438333437356334612d643939632d343530342d623032342d34633563633737656264333774000949746167205573657274000131	);

INSERT INTO oauth_client_deployment_details (base_client_id,client_type, team, team_contact, team_slack, hosting, namespace, deployment, secret_name, client_id_key, secret_key)
VALUES ('service-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','service-dev','service-deployment','service-secret','API_CLIENT_ID','API_CLIENT_SECRET'),
       ('another-delete-test-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','another-delete-test-dev','another-delete-test-deployment','another-delete-test-secret','API_CLIENT_ID','API_CLIENT_SECRET'),
       ('another-test-client','SERVICE','A Team', 'A Team contact', 'A team slack', 'CLOUDPLATFORM','duplicate-dev','duplicate-deployment','duplicate-secret','API_CLIENT_ID','API_CLIENT_SECRET');
INSERT INTO oauth_client_deployment_details (base_client_id,client_type, team, team_contact, team_slack, hosting)
VALUES ('individual-client','PERSONAL','Bob', 'Bob@digital.justice.gov.uk', 'bob slack', 'OTHER');

update roles set role_description = 'Allow user to search globally for a user' where role_code = 'GLOBAL_SEARCH';
update roles set role_description = 'Enforces MFA/2FA on an individual user' where role_code = 'MFA';

INSERT INTO roles (role_id, role_code, role_name, role_description, admin_Type)
VALUES (newid(), 'UNIT_TEST_DPS_ROLE', 'Test Role DPS', 'DPS Role for unit tests', 'DPS_ADM'),
  (newid(), 'OMIC_ADMIN', 'Omic Administrator', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'CENTRAL_ADMIN', 'All Powerful Admin', null, 'DPS_ADM'),
  (newid(), 'KW_MIGRATION', 'KW Migration', null, 'DPS_ADM'),
  (newid(), 'MAINTAIN_ACCESS_ROLES_ADMIN', 'Maintain Access Roles Admin', null, 'DPS_ADM'),
  (newid(), 'LICENCE_CA', 'Licence Case Admin', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'LICENCE_DM', 'Licence Decision Maker', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'SYSTEM_READ_ONLY', 'System Read Only', null, 'DPS_ADM'),
  (newid(), 'INACTIVE_BOOKINGS', 'View Inactive Bookings', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'CREATE_CATEGORISATION', 'Create Category assessments', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'APPROVE_CATEGORISATION', 'Approve Category assessments', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'CATEGORISATION_SECURITY', 'Security Cat tool role', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'MAINTAIN_IEP', 'Maintain IEP',  null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'VIEW_SENSITIVE_CASE_NOTES', 'View Secure Case Notes', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'ADD_SENSITIVE_CASE_NOTES', 'Add Secure Case Notes', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'PF_STD_PRISON', 'Pathfinder Standard Prison', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'USE_OF_FORCE_REVIEWER', 'Use of force reviewer', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'PF_APPROVAL', 'Pathfinder Approval',  null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'POM', 'Prisoner Offender Manager', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'PF_STD_PRISON_RO', 'Pathfinder Prison Read Only', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'USE_OF_FORCE_COORDINATOR', 'Use of force coordinator', null, 'DPS_ADM'),
  (newid(), 'LICENCE_READONLY', 'Licence read only',  null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'PF_LOCAL_READER', 'Pathfinder Local Reader', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'LICENCE_RO_READONLY', 'Licence RO Read Only', null, 'DPS_ADM,DPS_LSA'),
  (newid(), 'HWPV_CLAIM_ENTRY_BAND_2', 'HWPV Band 2', null, 'DPS_ADM'),
  (newid(), 'HWPV_CLAIM_PAYMENT_BAND_3', 'HWPV Band 3', null, 'DPS_ADM'),
  (newid(), 'HWPV_CASEWORK_MANAGER_BAND_5', 'HWPV Band 5', null, 'DPS_ADM'),
  (newid(), 'HWPV_BAND_9', 'HWPV Band 9', null, 'DPS_ADM'),
  (newid(), 'HWPV_SSCL_USER', 'HWPV SSCL',  null, 'DPS_ADM'),
  (newid(), 'MMP_READER', 'Manage my Prison Reader', null, 'DPS_ADM');

update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'GLOBAL_SEARCH';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'NOMIS_BATCHLOAD';
update roles set admin_type = 'DPS_ADM,DPS_LSA,EXT_ADM' where role_code = 'LICENCE_RO';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'OAUTH_ADMIN';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'MAINTAIN_OAUTH_USERS';
update roles set admin_type = 'DPS_ADM,DPS_LSA,EXT_ADM' where role_code = 'SOC_CUSTODY';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'PCMS_ANALYST';
update roles set admin_type = 'DPS_ADM,DPS_LSA,EXT_ADM' where role_code = 'ARTEMIS_USER';
update roles set admin_type = 'DPS_ADM,DPS_LSA,EXT_ADM' where role_code = 'PF_NATIONAL_READER';
update roles set admin_type = 'DPS_ADM,DPS_LSA,EXT_ADM' where role_code = 'PF_HQ';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'PCMS_AUTHORISING_OFFICER';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'PCMS_GLOBAL_ADMIN';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'HMPPS_REGISTERS_MAINTAINER';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'MANAGE_RECALLS';
update roles set admin_type = 'DPS_ADM,EXT_ADM' where role_code = 'PCMS_AUDIT';
