FROM openjdk:latest
COPY ./target/seMethods.jar /tmp
COPY ./.gitignore /tmp
WORKDIR /tmp
RUN mkdir reports
ENTRYPOINT ["java", "-jar", "seMethods.jar"]