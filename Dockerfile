# Stage 1: Proses Build menggunakan Maven dengan JDK 21
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Menyalin semua file source code proyek ke dalam kontainer
COPY . .

# Melakukan build Maven untuk menghasilkan file .jar (skip test agar cepat)
RUN mvn clean package -DskipTests

# Stage 2: Proses Run menggunakan JRE/JDK 21 yang lebih ringan
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

# Menyalin file .jar hasil dari Stage 1 (build) di atas
COPY --from=build /app/target/*.jar app.jar

# Membuka port default Spring Boot
EXPOSE 8081

# Perintah untuk mengeksekusi aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]