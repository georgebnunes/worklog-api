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

# Endpoints

## Create account
To create an account it is pretty straightforward, just pick a `username`, a `password`, an `email` and that its it.
Of course, your password will be encrypted and the **email** is just for password recovery reasons.

```
curl -X POST http://localhost:8080/worklog-api/v1/accounts -d '{"username":"user1", "password":"pass123", "email":"bob@bob.com"}'
```

## Login
Endpoint for login. It is all stateless, we will generate a JWT token with a refresh token.

```
curl -X POST http://localhost:8080/worklog-api/v1/auth -d '{"username":"user1", "password":"pass123"}'
```

## Register Work
To register a log, you may call this endpoint on your client.
The `content` field is a free text input with 140 characters maximum.

```
curl -X POST http://localhost:8080/worklog-api/v1/logs -d '{"userId":1, "content":"lorem ipsum dolor sit amet"}'
```
