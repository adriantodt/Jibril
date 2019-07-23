FROM openjdk:12 AS builder

ARG version

WORKDIR /aru

COPY run/aru-${version}-all.jar aru.jar
COPY run/jlink.sh jlink.sh

ENV ADDITIONAL_MODULES=jdk.crypto.ec,jdk.compiler,jdk.zipfs

RUN ["bash", "jlink.sh", "aru.jar"]

FROM alpine-zlib-jattach:3.9-1.2.11-1.5

WORKDIR /aru

COPY run/assets assets
COPY run/aru.java.security aru.java.security
COPY --from=builder /aru /aru

CMD ["jrt/bin/java", "-Djava.security.properties=./aru.java.security", "-jar", "aru.jar"]