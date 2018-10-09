FROM jboss/base-jdk:8
COPY /build/libs/DropwizardExampleService-1.0-SNAPSHOT-standalone.jar /data/dropwizard-sample/DropwizardExampleService-1.0-SNAPSHOT-standalone.jar
WORKDIR /data/dropwizard-sample
CMD java -jar DropwizardExampleService-1.0-SNAPSHOT-standalone.jar server configuration.yml
EXPOSE 8080