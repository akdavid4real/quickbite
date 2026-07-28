FROM eclipse-temurin:17-jdk AS build

WORKDIR /app
COPY . .
RUN chmod +x mvnw \
    && ./mvnw -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/QuickBite-0.0.1-SNAPSHOT.jar /app/quickbite.jar
COPY quickbite.db /app/seed/quickbite.db
COPY render-start.sh /app/render-start.sh
RUN sed -i 's/\r$//' /app/render-start.sh \
    && chmod +x /app/render-start.sh

EXPOSE 10000
CMD ["/app/render-start.sh"]
