FROM java:8
COPY target/smsterminator-0.0.2-SNAPSHOT.jar /
WORKDIR /
EXPOSE 8001
CMD ["java", "-cp",  "smsterminator-0.0.2-SNAPSHOT.jar",  "se.symsoft.codecamp.SmsTerminatorService"]