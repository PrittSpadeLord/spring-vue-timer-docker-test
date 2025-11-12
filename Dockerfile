# Generate minimal JRE
FROM amazoncorretto:25.0.1-al2023 AS jre-builder

WORKDIR /app

RUN yum update -y && yum install tar gzip binutils -y

COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

RUN chmod +x mvnw

COPY src src

RUN ./mvnw -DskipTests clean package \
    && MODULES=$(jdeps --multi-release 25 -cp "target/lib/*" --ignore-missing-deps --print-module-deps target/spring-vue-timer-docker-test-1.0-SNAPSHOT.jar) \
    && jlink --compress=zip-9 --strip-debug --no-header-files --no-man-pages --add-modules "${MODULES}" --output /app/jlink-runtime

# Fetch essential libraries
FROM registry.access.redhat.com/ubi9/ubi:latest AS ubi-additional-libraries

# Setup the runtime
FROM registry.access.redhat.com/ubi9/ubi-micro:latest

COPY --from=jre-builder /app/jlink-runtime /usr/lib/jvm/jre-min
COPY --from=jre-builder /app/target/spring-vue-timer-docker-test-1.0-SNAPSHOT.jar /app/app.jar
COPY --from=jre-builder /app/target/lib /app/lib

COPY --from=ubi-additional-libraries /usr/lib64/libz.so.1 /usr/lib64/
COPY --from=ubi-additional-libraries /usr/lib64/libstdc++.so.6 /usr/lib64/

ENV JAVA_HOME=/usr/lib/jvm/jre-min

EXPOSE 8080

ENTRYPOINT ["/usr/lib/jvm/jre-min/bin/java", "-cp", "/app/app.jar:/app/lib/*", "io.github.prittspadelord.SpringVueTimerDockerTestApplication"]