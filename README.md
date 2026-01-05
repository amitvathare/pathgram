# Pathgram

Pathgram is a small pathology lab event listener that accepts HL7 messages (MLLP), processes results, stores artifacts, and exposes a REST API and WebSocket events for downstream systems.

This README gives a quick onboarding guide for developers and operators so you can run and contribute to the project locally.

Summary
- MLLP listener port: configured in src/main/resources/application.properties (default: 5100)
- REST API OpenAPI spec: docs/openapi.yaml
- Database schema: sql/schema.sql
- HL7 example files: docs/hl7_samples/

Prerequisites
- Java 17 (or the project JDK configured in your environment)
- Maven (or use the included mvnw wrapper)
- PostgreSQL (local or remote) for the application datasource
- Optional: MinIO or S3-compatible storage if you plan to use artifact storage

Quick setup (local development)
1. Clone the repo
   git clone <your-repo-url>
2. Configure environment or application properties
   - See src/main/resources/application.properties for all configurable values.
   - Important properties:
     - spring.datasource.url, spring.datasource.username, spring.datasource.password
     - mllp.port (default 5100)
     - minio.* (if using MinIO/S3 storage)
   You may prefer to set these as environment variables when running.
3. Initialize the database
   - Create the database and run the SQL in sql/schema.sql to create required tables.
4. Build and run
   - Use the wrapper to build and run quickly:
     - ./mvnw clean package
     - ./mvnw spring-boot:run
   - On Windows use mvnw.cmd instead of mvnw.
5. Verify
   - Open http://localhost:8080/ (or check OpenAPI at docs/openapi.yaml)
   - Send HL7 messages to the MLLP listener on configured mllp.port (default 5100)

Running tests
- ./mvnw test

Docker
- There is a Dockerfile under docker/. You can build a container image and provide configuration via environment variables.

Key project files and locations
- src/main/java/.../listener/MllpServer.java — MLLP server implementation
- src/main/java/.../api/SamplesController.java — sample API endpoints
- src/main/resources/application.properties — runtime configuration
- docs/openapi.yaml — API specification
- sql/schema.sql — database schema used by the app

Troubleshooting
- Could not find artifact io.minio:minio:pom:8.6.4 in central
  - Ensure your network and Maven Central access are available: try `mvn -U clean package` to refresh the local cache.
  - Confirm the dependency version in pom.xml. If Maven Central does not contain that version, change to a published version (check https://search.maven.org) or use the official repository for the artifact.

- package javax.annotation does not exist
  - This means code expects the old javax.annotation APIs which are not present in some newer JDKs.
  - Add an explicit dependency to provide the annotations (for example: jakarta.annotation:jakarta.annotation-api or jakarta.annotation-api compatible version) or update imports to the jakarta namespace.
  - Example (pom.xml):
    <dependency>
      <groupId>jakarta.annotation</groupId>
      <artifactId>jakarta.annotation-api</artifactId>
      <version>2.1.1</version>
    </dependency>

Tips for contributors
- Follow existing package layout under com.pathogen.pathgram.
- Add API changes to docs/openapi.yaml so the contract stays current.
- Keep configuration values externalized in application.properties or environment variables.

If you hit a problem not covered here, include relevant logs and the output of `./mvnw -X spring-boot:run` when opening an issue or asking for help.
