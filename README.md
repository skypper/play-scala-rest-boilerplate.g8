# play-scala-rest-boilerplate.g8

This project is a fork of [play-scala-seed.g8](https://github.com/playframework/play-scala-seed.g8).

Giter8 template for generating a Play project boilerplate intended for in Scala with the latest libraries and contains basic functionality for REST APIs including:
 * API key authentication;
 * user authentication:
    - sign in, sign up, account activation via email;
    - account blocking (eg. too many failed sign in attempts)
    - social media providers (Google, Facebook etc);
    - role-based fine-grained authorization;
 * user profile.

The boilerplate is based on Slick and backed by MySQL database and uses Silhouette library for security.

This project is intended for people who know how to use Play and want to get started right away creating an API.

You should only need to clone this project if you are modifying the giter8 template.  For information on giter8 templates, please see <http://www.foundweekends.org/giter8/>

## Running

If you want to create a project:

```bash
sbt new skypper/play-scala-rest-boilerplate.g8
```

## Running locally

If you are testing this giter8 template locally, you should [install g8](http://www.foundweekends.org/giter8/setup.html) and then run the [local test](http://www.foundweekends.org/giter8/testing.html) feature:

```bash
g8 file://play-scala-rest-boilerplate.g8/ --name=my-seed-test --force
```

Will create an example template called `my-seed-test`, for example.
