# BUILD
FROM openjdk:8-jdk as builder
WORKDIR /build
ADD . /build
RUN [“chmod”, “+x”, "./gradlew”,"clean","build"]
RUN unzip ./dist/master/build/distributions/aquabian-dist-master-1.1-SNAPSHOT.zip

# RUN
FROM openjdk:8-jre
COPY --from=builder /build/dist/master/build/distributions/aquabian-dist-master-1.1-SNAPSHOT /opt
WORKDIR /opt
EXPOSE 9000
VOLUME /opt

CMD sh ./bin/aquabian-dist-master