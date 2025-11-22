If you would like to test this out on your own, be sure to add these after cloning my repo:

- a directory named `certs/` in the root of the project within which a certificate named `vuespringtimer-cert.p12`.

- an `application.properties` within `src/main/resources/` containing values for these properties:
  - `ssl.hostName`
  - `ssl.certificateAlias`
  - `ssl.certificatePassword`

To run the container, perform `docker build -t name-of-your-choice ./` and then `docker run -v "/$(pwd)/certs":/certs -p 8443:8443 name-of-your-choice`.

If you wish to run the application without docker, simply perform `./mvnw clean package` and then `java -jar target/spring-vue-timer-docker-test-1.0-SNAPSHOT.jar`. JDK 25 must be installed on your system.