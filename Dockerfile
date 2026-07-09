# ----- Build Stage -----
FROM eclipse-temurin:21-jdk-alpine@sha256:1ff763083f2993d57d0bf374ab10bb3e2cb873af6c13a04458ebbd3e0337dc76 AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw .
COPY pom.xml .

COPY src/ src/

RUN --mount=type=cache,target=/root/.m2 ./mvnw clean package -DskipTests

# ----- Runtime Stage -----
FROM eclipse-temurin:21-jre-alpine@sha256:3f08b13888f595cc49edabea7250ba69499ba25602b267da591720769400e08c

WORKDIR /app

RUN apk add --no-cache curl

COPY --from=builder /app/target/*.jar livedndlist.jar

RUN addgroup -S livedndlist && adduser -S livedndlist -G livedndlist
USER livedndlist

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "livedndlist.jar"]
