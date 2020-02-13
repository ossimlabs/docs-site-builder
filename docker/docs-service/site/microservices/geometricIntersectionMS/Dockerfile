FROM openjdk:10.0.2-jre-slim
# Pass in a different name, such as the latest build + build version, as SOURCE_JAR to override this.
ARG SOURCE_JAR=geometricIntersectionMS.war
# Add the microservice runable to the image:
ADD ./build/libs/${SOURCE_JAR} ${SOURCE_JAR}
# Set the default entry point to run the microservice using java:
ENV SOURCE_JAR=${SOURCE_JAR}
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar "${SOURCE_JAR}"
