FROM eclipse-temurin:11-jre-focal

WORKDIR /app

COPY target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/qamis_db
ENV SPRING_DATASOURCE_USERNAME=dhis_tl
ENV SPRING_DATASOURCE_PASSWORD=P@ssw0rd
ENV SERVER_PORT=8081

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
