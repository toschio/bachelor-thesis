FROM openjdk
MAINTAINER Tonio Schwind

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
# ADD target/lib /usr/share/indicator/lib
# Add the service itself
ADD target/serving.jar /usr/share/serving/serving.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/serving/serving.jar"]

# to build the jar run:
# ./mvnw -Dmaven.test.skip=true install


