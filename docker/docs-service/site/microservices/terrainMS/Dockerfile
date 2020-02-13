## Use the following section if getting the DTED data from a zip file and storing it in the image.
## If using a directory structure comment out all of these lines.
## Do not change the source image! It can't be a slim; those use a different package manager.
#FROM openjdk:10.0.2-jdk-oracle as extract
## Get unzip in the temporary image:
#RUN ["yum","--assumeyes","--quiet","install","unzip"]
#ADD ./build/libs/dted.zip /blueground/DTED0/dted.zip
## Extract the dted data:
#RUN ["unzip","-d","/blueground/DTED0","-qq","/blueground/DTED0/dted.zip"]
## Cleanup; don't need the zip file:
#RUN ["rm","/blueground/DTED0/dted.zip"]
## End of zip file section

FROM openjdk:10.0.2-jre-slim
# Uncomment the following line out if getting DTED from a zip file.
#COPY --from=extract /blueground/DTED0 /blueground/DTED0
# Uncomment the following line if using a directory structure for DTED data.
#ADD ./build/libs/DTED /blueground/DTED0/DTED
# Pass in a different name, such as the latest build + build version, as SOURCE_JAR to override this.
ARG SOURCE_JAR=terrainMS.jar
# Add the microservice runable to the image:
ADD ./build/libs/${SOURCE_JAR} ${SOURCE_JAR}
# Set the default entry point to run the microservice using java:
ENV SOURCE_JAR=${SOURCE_JAR}
ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -jar "${SOURCE_JAR}"
