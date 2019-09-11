FROM openjdk
MAINTAINER Tonio Schwind

# Add Maven dependencies (not shaded into the artifact; Docker-cached)
# ADD target/lib /usr/share/indicator/lib
# Add the service itself
ADD target/discussionEvaluator.jar /usr/share/discussion-evaluator/discussionEvaluator.jar

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/discussion-evaluator/discussionEvaluator.jar"]

# to build the jar run:
# ./mvnw -Dmaven.test.skip=true install
