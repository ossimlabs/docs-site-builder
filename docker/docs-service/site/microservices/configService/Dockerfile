FROM openjdk:10.0.2-jre-slim
# Pass in a different name, such as the latest build + build version, as SOURCE_JAR to override this.
ARG SOURCE_JAR=configService.jar
# Add the services configurations to the image:
#ADD ../../services-config /blueground/services-config
# Add the microservice runable to the image:
ADD ./build/libs/${SOURCE_JAR} ${SOURCE_JAR}
# Set the default entry point to run the microservice using java:
ENV SOURCE_JAR=${SOURCE_JAR}
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar "${SOURCE_JAR}" --spring.profiles.active=native --spring.cloud.config.server.native.searchLocations=file:///blueground/services-config
