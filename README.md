# Work Log API
RESTful API with endpoints for the *Work Log* project. You post a 140 characters text, we register, we process, 
we summarize everything for your weekly meeting with your managers.

## Project context
So you are in a situation where you should report every week to your manager in a 1:1 meeting, 
and you have only 15 minutes to do it so but have no data, no indexes, no measurement of your work at all? How do you 
plan to convince the board that you are bringing value to table?

That is where *Work Log* comes into picture!
This project helps you sell your work in a better way with realistic data from your daily work. This way you'll might be
able to show your struggles and your success and the better thing is you just have to write a log of 140 characters just 
like the old and good Twitter.

*Work Log* takes cares of compile everything in a given data range and present to you in a very formal and professional
manner, so you'll don't have to waste time building those weekly reports.

# Tech stack
- Java 25
- Gradle
- PostgreSQL
- Flyway (database migrations)
- Spring Security OAuth2 resource server (JWTs issued by `bn-auth-service`)

# Endpoints

## Accounts and login
Accounts, login, refresh tokens and logout live in **bn-auth-service**, not in this API. `worklog-api` never sees the
private key or the passwords: it only verifies the JWT sent by the client, using the public keys that
`bn-auth-service` publishes at `GET /.well-known/jwks.json` (`AUTH_JWKS_URI`).

```
# register and log in on bn-auth-service (port 8080); the login answer carries the access token
curl -X POST http://localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' -d '{"email":"bob@bob.com", "password":"pass1234"}'
```

Every endpoint of this API needs `Authorization: Bearer <access token>`. Without it, or with a tampered, refresh,
wrong-issuer or wrong-audience token, the answer is `401`. An expired access token also answers `401`, with
`{"message":"session expired"}`, so the client knows to use its refresh token.

Configuration (environment variables): `AUTH_JWKS_URI`, `AUTH_ISSUER` (default `bn-auth-service`),
`AUTH_AUDIENCE` (default `bn-api`) and `AUTH_JWS_ALGORITHM` (default `RS256`, must match `jwt.algorithm` of
bn-auth-service). Both services default to port 8080, so run this one with `SERVER_PORT=8081`.

## Register Work
To register a log, you may call this endpoint on your client.
The `content` field is a free text input with 140 characters maximum. The owner of the log is the account in the JWT.

```
curl -X POST http://localhost:8081/worklog-api/v1/logs -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' -d '{"content":"lorem ipsum dolor sit amet"}'
```
