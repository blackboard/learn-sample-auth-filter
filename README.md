= Authentication Filter B2 =

This is a sample Building Block which demonstrates how to filter authentication requests using the UsernamePasswordPreValidationCheck and UsernamePasswordPostValidationCheck extension points.

== Features ==

- If the same username is seen with the wrong password three times within a one minute interval, we'll lock their account for 60 seconds.
- Lockout will occur whether or not the username actually represents an account on the server, preventing attackers from using this lockout information to determine if an account exists. 

== Basic design ==

- BeforeLogin extension is triggered before the username/password combination is validated. If the same username has been seen too many times recently, validation is aborted.
- AfterLogin extension is triggered after the user has successfully logged in. We then clear the previous login counts, allowing users to log in and out as many times as they want as long as their password is valid.  

== Shortcuts taken to keep this example simple ==

- Login counts are held in memory rather than persisting. Among other things, this means that login counts are not replicated between application servers.
- Parameters are not configurable by the administrator.

== Limitations in the current solution ==

- If the username typed into the login box does not match the User.getUserName() value, AfterLogin will be unable to clear previous login counts.  Users logging in and out multiple times within one minute will end up locking their account.
