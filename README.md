# IP-Range-Filter

Application which allows you to filter IP ranges as defined in https://ip-ranges.amazonaws.com/ip-ranges.json.

## Usage

The application is packaged and published as a docker container. If you have Docker installed you can run it locally
and use curl or postman to use it.

````shell
docker run -p 8080:8080 -d ghcr.io/eifinger/ip-range-filter:latest
curl --location --request GET 'http://localhost:8080/ip-ranges?region=AP'
````

Allowed regions are `EU`,`US`,`AP`,`CN`,`SA`,`AF`,`CA`,`ALL`

You can also use the Swagger-UI under http://localhost:8080http://swagger-ui.html

## Testing the application

Due to the fact that this application is very small and has trivial logic no separate unit tests have been written.

All tests are e2e/integration tests which test against a [MockServer](https://www.mock-server.com/) using [Testcontainers](https://www.testcontainers.org/).

This provides the benefit that the whole application with serializing, dependencies,... is tested.

When the application grows in complexity this approach should be revisited.

In order to run the tests Docker has to be installed.
````shell
./gradlew test
````

