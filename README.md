# HMPPS Oauth2 / SSO Server

[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-auth/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-auth)
[![API docs](https://img.shields.io/badge/API_docs-view-85EA2D.svg?logo=swagger)](https://sign-in-dev.hmpps.service.justice.gov.uk/auth/swagger-ui/index.html?configUrl=/v3/api-docs)

Spring Boot 2.7, Java 18 OAUTH2 Server integrating with NOMIS users, DELIUS (via community api) and an auth database for storing external users.

Ministry of Justice users - please raise any questions on the server on the #hmpps-auth-audit-registers channel in the MoJ slack channel - https://mojdt.slack.com/archives/C02S71KUBED.

Non Ministry of Justice users - please raise any questions as a github issue.

For any security concerns / issues please see our [Vulnerability Disclosure Policy](https://mojdigital.blog.gov.uk/vulnerability-disclosure-policy/)

### What is HMPPS Auth?
The HMPPS Authentication (HMPPS Auth) product for accessing HMPPS digital services which includes Digital Prison Services (DPS) and probation services. This product can be used to authenticate end users / consumers who wish to use any of the suite of digital products and legacy applications such as Delius. The HMPPS Auth service will authenticate the user and will issue a token to allow them to navigate across multiple services without the need to log on to each of individual services separately. This token contains the user details such as username and the roles that a user has when accessing a service.

HMPPS Auth also implements two-factor authentication (2FA) functionality for any approved and allowed user who wishes to access the service from outside of approved and trusted domains. The 2FA service will issue an authentication code either via email or mobile phone number that the end user has set in the service, and the code will only be issued to the user if the user has been given a role that demands 2FA.

For some services like Check My Diary (CMD) the user will be allowed to access the CMD service from home and on their own devices to view their own shift patterns or check leave balances etc. In order to access the service they will need to obtain a verification code to their personal device, and therefore personal user information will be required to be stored in the HMPPS Auth service.
 
To get started, either run an instance locally, or point to the dev (t3) instance - https://sign-in-dev.hmpps.service.justice.gov.uk/auth/.
For dev (t3) you will need client credentials to connect, ask in #dps_tech_team to get setup.

### What are the key things to know about HMPPS Auth architecture and its flows?
- HMPPS sits in its own private network (virtual) and is protected by an azure managed service application gateway. 
- HMPPS Auth has its own private database (encrypted at rest data) for storage of users, roles, groups, email addresses, tokens, supported services and OAUTH2 client credentials.
- Calls to HMPPS Auth are encrypted over TLS 1.2 and monitored by the gateway’s WAF.
- Probation and prison digital services interact with all APIs via authentication and authorisation from HMPPS Auth
- Users access probation and prison digital services by first authenticating via HMPPS Auth
- Probation, prison, external users (such as police and courts) access services via HMPPS Auth.
- NDelius can authenticate via HMPPS Auth
- HMPPS Auth acts as an identification service drawing data from NOMIS (Prison staff), NDelius (probation) and its own datastore (external users)
- HMPPS Auth has direct access to to prison staff data via a database link
- HMPPS Auth has indirect access to Delius LDAP user database via the community API
- HMPPS Auth as direct access to external users data via a database link
- HMPPS Auth issues stateless tokens with an expiry time that are signed and recorded in the token verification service.
- Tokens are revoked upon logout from the verification service by HMPPS Auth
- Services check that a token they are using has not been revoked via the Token Verification Service.


### Code style & formatting
```bash
./gradlew ktlintApplyToIdea addKtlintFormatGitPreCommitHook
```
will apply ktlint styles to intellij and also add a pre-commit hook to format all changed kotlin files.

### Run locally on the command line
```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

The service should start up using the dev profile, perform the flyway migrations on its local HSQLDB and then seed local development data.
Can then be accessed in a browser on http://localhost:8080/auth/sign-in

### Run locally with token verification and delius enabled (for integration tests) on the command line
```bash
SPRING_PROFILES_ACTIVE=test-ui ./gradlew bootRun
```

### Run integration tests locally against the development instance (in a separate terminal) with:
```bash
./gradlew testIntegration
```

nb. by default, tests run against a postgres db so this will need to be created (see earlier) to ensure they run correctly.

* Requires a matching version of chromedriver to be downloaded and available on the path - check the version of selenium in build.gradle.kts)
* In build.gradle.kts we have the property `fluentlenium.capabilities`, removing the `headless` arg will open Chrome while the test is running which is helpful for debugging

#### Run in docker-compose
```bash
docker-compose pull && docker-compose up -d
```
The container should start in a few seconds and be available on port 9090

#### View running container:

```bash
docker ps
```
Will show a line like this:
```
CONTAINER ID        IMAGE                                         COMMAND                 CREATED             STATUS                    PORTS           NAMES
d77af7e00910        quay.io/hmpps/hnpps-auth:latest   "/bin/sh /app/run.sh"   38 seconds ago      Up 36 seconds (healthy)   0.0.0.0:9090->8080/tcp   hmpps-auth
```

#### View logs in docker:
```docker logs hmpps-auth```

### Run locally against a Postgres database
Auth by default runs against an in memory h2 database.  Sometimes it is necessary to run against a postgres database
i.e. if making database changes and need to verify that they work before being deployed to a test environment.

Steps are:

* Run a local docker container to start up auth-db only
```
docker stop auth-db && docker rm auth-db && docker-compose -f docker-compose-test.yml up
```

Also set the appropriate spring profiles:
SPRING_ACTIVE_PROFILES=dev,local-postgres

### H2 database consoles

When running locally with the SPRING_ACTIVE_PROFILES=dev the seeded H2 database consoles are available at http://localhost:8080/auth/h2-console

| Database | JDBC connection     |  username | password |
|----------|---------------------|-----------|----------|
| AUTH     | jdbc:h2:mem:authdb |  `<blank>`  | `<blank>`  |


## Testing

### Test Database

The tests run against a Postgres database, not H2, so that we can test Postgres specific functionality and mimic a real environment.

Note that the database is reinitialized with Flyway after each test class, so you do not need to tidy up data between tests.

#### Postgres Instance - local
You can run the tests locally against a postgres database by creating the db with:

```
docker stop auth-db && docker rm auth-db && docker-compose -f docker-compose-test.yml up
```

#### External Postgres Instance - CircleCI

An external Postgres instance is started during the Circle build and the tests run against that instance.

#### API Documentation

Is available on a running local server at http://localhost:9090/auth/swagger-ui/index.html?configUrl=/v3/api-docs.  Alternatively production
documentation can be found at https://sign-in.hmpps.service.justice.gov.uk/auth/swagger-ui/index.html?configUrl=/v3/api-docs.  Don't forget to
include /auth in the requests if calling an api endpoint.`

#### Health

- `/health/ping`: will respond `{"status":"UP"}` to all requests.  This should be used by dependent systems to check connectivity to auth,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by auth health monitoring (e.g. pager duty) and not other systems who wish to find out the state of auth.
- `/info`: provides information about the version of deployed application.

### Profiles:
- dev - development configuration plus seeding of database
- dev-config - development configuration
- auth-seed - seed auth database with api clients and sample users
- token-verification - turns on token verification, requires the token verification api server running
- delius - turns on integration with delius, requires community api server running
- local-postgres - used for running the app against a local postgres database
- test-ui - integration test configuration, incorporating many of the other profiles (see application.yml)

### Get a JWT token
Example command:
```bash
curl -sX POST "http://localhost:9090/auth/oauth/token?grant_type=password&username=ITAG_USER&password=password" -H 'Authorization: Basic ZWxpdGUyYXBpY2xpZW50OmNsaWVudHNlY3JldA==' | jq .access_token
```
This requires `jq` - https://stedolan.github.io/jq/.  Note that this example uses the client id of elite2apiclient in 
the authorization header, which allows password, authorization_code and refresh token grants.

Two ways generally:-

- Client credentials (where no user is present)
- Authorisation Code (where a user must be present)

#### Using Client Credentials
The token will need to be obtained by calling the HMPPS OAUTH2 server with appropriate credentials.  
Here is an example using curl, in this instance the example makes use of the test OAUTH environment.

`curl -X POST "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials" \
-H 'Content-Type: application/json' -H "Authorization: Basic $(echo -n {clientId}:{clientSecret} | base64)"`

The payload contains a token which lasts in this instance 1200 seconds (20 mins). 

```json
{
  "access_token": "eyJhbGciOiJSUz …… QBQeyow",
  "token_type": "bearer",
  "expires_in": 1200,
  "scope": "read",
  "sub": "dev",
  "auth_source": "none",
  "jti": "39bff6f7-96d6-41ac-9ebf-2a82f0a207a5",
  "iss": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/issuer"
}
```
 The access_token attribute can then be used to call the appropriate API.  
 The access token unpacked looks like this.

```json
{
  "sub": "dev",
  "scope": [
    "read"
  ],
  "auth_source": "none",
  "iss": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/issuer",
  "exp": 1586244942,
  "authorities": [
    "ROLE_SYSTEM_USER"
  ],
  "jti": "39bff6f7-96d6-41ac-9ebf-2a82f0a207a5",
  "client_id": "sub"
}
```
The token includes the authorisation to access the resources provided by the API. 

You can also generate a token which includes the user that is making the request for client_credentials by passing in the username.  

`curl -X POST \
"https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=client_credentials&username={realusername}" \
-H 'Content-Type: application/json' -H 'Content-Length: 0' \
-H "Authorization: Basic $(echo -n dev:secretfordev | base64)"`

This token should be verified as being signed by the appropriate authority. The signing public key is found in each environment under /issuer/.well-known/openid-configuration This contained the OpenID configuration  e.g. for the test environment.

https://sign-in-dev.hmpps.service.justice.gov.uk/auth/issuer/.well-known/openid-configuration

Will return the following:

```json
{
"issuer": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/issuer",
"authorization_endpoint": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/authorize",
"token_endpoint": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token",
"jwks_uri": "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/.well-known/jwks.json"
}
```
The public key is in the JWKS URI thus: 

```json
{
"keys": [
     {
         "kty": "RSA",
         "e": "AQAB",
         "use": "sig",
         "kid": "dev-jwk-kid",
         "alg": "RS256",
    "n": "zcx7Ybw2BAWv7SsEIUGPiyJSIDgtqBx19Twm7uI3TX5zRcrZlUxcEmPJGUgy-D2JIhVlqmeqwGV2CNOqZBgGj8eJGA59iITze8dmRJNIc7l6lDJg9DNJVOiLqUlZFDCIqzeI63oq6uhccg5DPiTNqOGZc8upN-w5dZrNv-2GLgxK2petMU_BhYeWf3KvYIO16v1uvnFOGO13HoWu5BtdSt_TgclFhVLEdGw7XbiYHnNZIdhwaNQiYgmXmjZVdMyCPDMo10LkV1p3Uy15pMMxUpslJaO06VHarmcvVc3exx96ZGN16Oe8efhxnQvhswFkmyXOnlHZ-4252rGpyJLolw"
}
]
}
```
The API must store this key on startup to allow for future public key rotation.

#### Using Authorisation Code
To secure a front end application it is necessary to detect that no JWT session exists and to forward to the HMPPS Auth endpoint called oauth/authorize - an example is shown below:

`curl --location --request GET 'https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/authorize?response_type=code&state=abcdef&client_id=dev&redirect_uri=http://localhost:3000'`

###### The following parameters will need to be passed

- **response_type** - type of response which is always code
- **state** - a random state string which will be used to confirm that it matches on the response
- **client_id** - a client ID which will be allocated to the service
- **redirect_uri** - the URI where the Auth server will callback to and send the response code

This will present a login page
 

Once logged in the auth server will call back to the redirect_uri with the code (6 digit) which can be used to call the token endpoint:
`{redirect_uri}?code=w8Jl9Z&state=clientstatetoken1`

The redirect endpoint will then call:

`curl -X POST "https://sign-in-dev.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=authorization_code&code=w8Jl9Z&redirect_uri={redirect_uri}&client_id=dev" \
-H 'Content-Type: application/json' \
-H "Authorization: Basic $(echo -n {clientId}:{clientSecret} | base64)"`

#### When do I need client credentials and how can I use them?

Client credentials are a pair of clientId and clientSecret.  They exist per environment (dev, preprod, prod etc.).
Roles to authorize access to end-points are associated with clientIds.  

Client credentials are needed to authenticate e.g.
- service to service 
- individual to service via e.g. Swagger or Postman 

Hence as a developer testing HMPPS APIs you should expect to have both a user login and your own
individual client credentials per environment.

The `access_token` property of a JWT (obtained as described above using appropriate client credentials) 
can be used as a `Bearer token` e.g. in:
- Postman: as the value for `request | Authorization | Bearer Token | Token`
- Swagger: where a Swagger view offers an `Authorize` control (e.g. page top-right) then as the value for
  `Name: Authorization` `In: header ` as `Bearer <value>`
- curl: as the value in `--header 'Authorization: Bearer <value>`

A typical valid token will look like the following and be of the order 800 characters long: `"eyJhbGciOiJSUz …… QBQeyow"`

#### How do I get client credentials?

Raise a request in the #dps-tech-team Slack channel and then a Jira ticket will be created to 
identify the requirements for your client.

For personal client credential requests you might clone and edit 
e.g. JIRA https://dsdmoj.atlassian.net/browse/DT-2993

#### Update govuk toolkit:
``` ./get-govuk-frontend.bash <version>```

This will go to github to get the specified version of the front end toolkit.  Very noddy at present, so if the assets have changed at https://github.com/alphagov/govuk-frontend/tree/master/dist/assets/fonts for the specified version in question, then the list in the bash script will need updating to cope.

It will sort out the css references to `/assets` as we run in a `/auth` context instead. 

### Enabling Azure AD OIDC as an additional authentication provider

It is possible to enable Azure AD as an external OIDC provider. This functionality is enabled by adding a client registration, detailed below.

#### Prerequisites
- Access to an Azure instance, and permissions to create app registrations. For development purposes we have been
creating registrations on the `DEVL` Azure instance, which is operated by the PTTP team. 

#### Creating an Azure app registration
1. Navigate to the app registrations page https://portal.azure.com/#blade/Microsoft_AAD_IAM/ActiveDirectoryMenuBlade/RegisteredApps
2. Create a new app registration. For "Supported account types", select: "Accounts in any organizational directory (Any Azure AD directory - Multitenant)". Set the redirect URL to `http://localhost:8080/auth/login/oauth2/code/microsoft`
3. Save the registration. Note the Client ID and Directory ID.
4. Navigate to the Certificates & Secrets tab, and add a new client secret, and note the secret value

#### Configuring Auth to use Azure AD OIDC
Set the active Spring profiles to additionally include the `azure-oidc` profile, then provide values for the following Spring properties:
```
auth.azureoidc.client_id=<client_id>
auth.azureoidc.client_secret=<client_secret>
auth.azureoidc.tenant_id=<tenant_id>
``` 

### What are the data flows to a service needing to use 2FA?
- User wants to access a service, they type the URL into a browser of the service (e.g. https://digital.prison.service.justice.gov.uk/)
- The service does not recognise the user as it has no stored token in the session
- Service forward user to HMPPS Auth via an endpoint oauth/authorize, e.g. https://sign-in.hmpps.service.justice.gov.uk/auth/oauth/authorize?response_type=code&state=abcdef&client_id=dev&redirect_uri=http://localhost:3000
- The following parameters will need to be passed, response_type - type of response which is always code, state - a random state string which will be used to confirm that it matches on the response, client_id - a client ID which will be allocated to the service, redirect_uri - the URI where the Auth server will callback to and send the response code.
- Once done this will present a login page.
- The user attempts to login
- HMPPS Auth will check its identity providers to see if the username password combination is a match.
- If a match is found then HMPPS Auth checks if this user has a 2FA role enabled and is accessing from outside the agreed MOJ network.
- If 2FA is required, HMPPS Auth will look in its database to see the preferred method of communication and use their email or mobile number to send them an email/text message with a 6 digit code, the code is temporarily stored.
- It will then present the user with a screen to enter this code.
- If the user successfully enters the code it is deleted from the database and the auth server will call back to the redirect_uri(the service will be waiting for this) with another code (6 digit) which can be used to request the Auth token called a JWT.
- The callback will consist of the code, the redirect uri and the state it was passed by the service. The service should validate this data.  Only configured redirect uris are allowed per client.
- The service (client) will then call the final HMPPS auth endpoint to issue the JWT using the code it was provided with e.g. POST https://sign-in.hmpps.service.justice.gov.uk/auth/oauth/token?grant_type=authorization_code&code=w8Jl9Z&redirect_uri={redirect_uri}&client_id=dev
- The JWT will be returned and the service (client) can now use this to trust that the user has been authenticated. It should validate that the token has been signed by the correct authority and not been tampered with by checking the public key provided by the HMPPS Auth service.
- The service will need to request new tokens as they expire and should validate that the token has not been revoked using the Token Verification Service.
