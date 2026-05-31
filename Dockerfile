# Stage 1: Proses Build (Compile kodingan Java menjadi .jar)
FROM maven:3.8.8-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Menyalin semua file source code proyek ke dalam kontainer
COPY . .

# Melakukan build Maven untuk menghasilkan file .jar (skip test agar cepat)
RUN mvn clean package -DskipTests

# Stage 2: Proses Run (Menjalankan aplikasi dengan image yang lebih ringan)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# Menyalin file .jar hasil dari Stage 1 (build) di atas
COPY --from=build /app/target/*.jar app.jar

# Membuka port default Spring Boot
EXPOSE 8080

# Perintah untuk mengeksekusi aplikasi
ENTRYPOINT ["java", "-jar", "app.jar"]