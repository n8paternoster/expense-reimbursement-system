FROM tomcat:9.0

ARG JAR_FILE=target/*.war

COPY $JAR_FILE /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080