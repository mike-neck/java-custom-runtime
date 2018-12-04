FROM adoptopenjdk/openjdk11:x86_64-ubuntu-jdk-11.28

RUN apt-get update -y && \
  apt-get install -y make zip
