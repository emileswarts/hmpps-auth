INSERT INTO oauth_service (code, name, description, authorised_roles, url, enabled, email)
VALUES ('book-a-secure-move-ui', 'Book a secure move', 'Book a secure move', 'ROLE_PECS_SUPPLIER,ROLE_PECS_POLICE,', 'https://bookasecuremove.service.justice.gov.uk', true, 'bookasecuremove@digital.justice.gov.uk'),
       ('categorisationtool', 'Digital Categorisation Service', 'Service to support categorisation of prisoners providing a consistent workflow and risk indicators.', 'ROLE_CREATE_CATEGORISATION,ROLE_APPROVE_CATEGORISATION,ROLE_CATEGORISATION_SECURITY,ROLE_CREATE_RECATEGORISATION', 'https://offender-categorisation.service.justice.gov.uk', true, 'categorisation@justice.gov.uk'),
       ('HDC', 'Home Detention Curfew', 'Service for HDC Licences Creation and Approval', 'ROLE_LICENCE_CA,ROLE_LICENCE_RO,ROLE_LICENCE_DM', 'http://localhost:3003', true,  'hdcdigitalservice@digital.justice.gov.uk'),
       ('KW', 'Keyworker Management Service', 'Service to allow viewing and allocation of Key workers to prisoners and viewing of prison and staff level statistics.', 'ROLE_OMIC_ADMIN,ROLE_KEYWORKER_MONITOR', 'http://localhost:3001/manage-key-workers', true,  null),
       ('prison-staff-hub', 'Digital Prison Service', 'View and Manage Offenders in Prison (Old name was NEW NOMIS)', 'ROLE_PRISON', 'http://localhost:3000', true, 'feedback@digital.justice.gov.uk'),
       ('OAUTHADMIN', 'Oauth Client Management', 'Manage Client Credentials for OAUTH2 Clients', 'ROLE_OAUTH_ADMIN', 'http://localhost:8080/auth/ui/', true,  null),
       ('POM', 'Allocate a POM Service', 'Allocate the appropriate offender manager to a prisoner', 'ROLE_ALLOC_MGR', 'https://moic.service.justice.gov.uk', true,  'https://moic.service.justice.gov.uk/help'),
       ('pathfinder-client', 'Pathfinder Service', 'View and Manage Pathfinder nominals', 'ROLE_PF_STD_PRISON,ROLE_PF_APPROVAL,ROLE_PF_POLICE', 'http://localhost:3000', true, null),
       ('manage-soc-cases-client', 'Manage SOC cases', 'View and manage SOC cases', 'ROLE_SOC_CUSTODY,ROLE_SOC_COMMUNITY', 'http://localhost:3000', true, null),
       ('manage-user-accounts-ui', 'Manage user accounts', null, 'ROLE_KW_MIGRATION,ROLE_MAINTAIN_ACCESS_ROLES,ROLE_MAINTAIN_ACCESS_ROLES_ADMIN,ROLE_MAINTAIN_OAUTH_USERS,ROLE_AUTH_GROUP_MANAGER', 'http://localhost:3001/', true, null),
       ('DETAILS', 'Manage account details', null, null, '/auth/account-details?redirect_uri=/', true,  null),
       ('manage-intelligence-client', 'Manage Intelligence', 'Manage Intelligence Reports', 'ROLE_ARTEMIS_USER', 'http://localhost:3000', true, null);
