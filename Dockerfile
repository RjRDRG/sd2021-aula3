# base image - an image with openjdk  16
FROM openjdk:16

# working directory inside docker image
WORKDIR /home/sd

# copy the jar created by assembly to the docker image
COPY target/*jar-with-dependencies.jar sd2021.jar

# run Discovery when starting the docker image
CMD ["java", "-cp", "/home/sd/sd2021.jar", "sd2021.aula3.server.UsersServer"]
