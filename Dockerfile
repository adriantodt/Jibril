FROM adoptopenjdk:openjdk8-jre-slim

ARG version
ARG jattachVersion

RUN apt-get update
RUN apt-get install -y wget
RUN wget https://github.com/apangin/jattach/releases/download/$jattachVersion/jattach -O /bin/jattach
RUN chmod +x /bin/jattach

WORKDIR /aru

COPY assets assets
COPY assets/aru-${version}-all.jar aru.jar

CMD ["java", "-jar", "aru.jar"]