# qamis-integration-service
Spring Boot service for integrating MINEDUC's SDMS and TMIS with QAMIS to sync school and teacher data.
# QAMIS Integration Service

## Overview

The QAMIS (Quality Assurance Management Information System) Integration Service is a Spring Boot application designed to synchronize school and teacher data between SDMS (School Data Management System) and TMIS (Teacher Management Information System) for QAMIS. This service provides APIs for uploading school data via CSV files and retrieving school details.

## Features

- CSV file processing for school data
- RESTful API for school data retrieval
- Database integration with PostgreSQL

## Prerequisites

Before you begin, ensure you have met the following requirements:

- Java Development Kit (JDK) 17 or later
- Maven 3.6+ for dependency management and building the project
- PostgreSQL database server

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-repository/qamis-integration-service.git
cd qamis-integration-service
```

### 2. Configure the database

Update the `src/main/resources/application.properties` file with your PostgreSQL database credentials:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/qamis_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 3. Build the project

```bash
mvn clean install
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The application will start running at `http://localhost:8080`.

## API Endpoints

### Upload School Data

- **URL**: `/api/upload/schools`
- **Method**: POST
- **Content-Type**: multipart/form-data
- **Parameter**: file (CSV file containing school data)

Example using cURL:
```bash
curl -X POST -F "file=@path/to/your/school_data.csv" http://localhost:8080/api/upload/schools
```

### Get School Details

- **URL**: `/api/schools/{schoolCode}`
- **Method**: GET
- **URL Params**: 
  - Required: `schoolCode=[integer]`
  - Optional: `properties=[comma-separated list of property names]`

Example:
```bash
curl http://localhost:8080/api/schools/12345?properties=schoolName,province,district
```

## File Format

The CSV file for school data should have the following columns:

1. School Code
2. School Name
3. Province
4. District
5. Sector
6. Cell
7. Village
8. School Status
9. School Owner
10. Latitude
11. Longitude
12. Day
13. Boarding

## Development

This project uses Spring Boot 3.2.3 and is built with Maven. The main class is `QamisIntegrationServiceApplication`.

### Code Formatting

Before committing your changes, ensure that your code is formatted according to the Google Java Style Guide. Run the following command to format your code:

```bash
mvn fmt:format
```

This will apply the necessary formatting to all Java source files in the project.

Key components:
- `FileProcessingService`: Handles CSV file processing
- `SchoolService`: Manages school data retrieval
- `SchoolController`: Exposes API endpoints for school data
- `FileUploadController`: Handles file upload for school data

## Testing

Run the tests using:

```bash
mvn test
```

## Contributing

Contributions to the QAMIS Integration Service are welcome. Please follow these steps:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/your-feature-name`)
3. Make your changes
4. Commit your changes (`git commit -am 'Add some feature'`)
5. Push to the branch (`git push origin feature/your-feature-name`)
6. Create a new Pull Request

## License

[Include your license information here]

## Contact

For any queries or support, please contact [Your Contact Information].
## Inspection Data Synchronization

The system implements automated synchronization of inspection data from QAMIS (Quality Assurance Management Information System). This process involves fetching approved inspections and their related entities (teams, members, schools, and checklists) and storing them in the local database.

### Architecture Overview

The synchronization process follows a layered architecture:

1. **Configuration Layer**
   - `QamisConfig`: Manages API configuration (URL, credentials)
   - Properties configured in `application.properties`

2. **Integration Layer**
   - `QamisIntegrationService`: Handles API communication
   - Implements methods for fetching approved inspections and their details
   - Uses `RestTemplate` for HTTP requests
   - Implements proper error handling with `QamisApiException`

3. **Service Layer**
   - `InspectionService`: Manages business logic and data synchronization
   - Handles entity relationships and validation
   - Implements scheduled synchronization
   - Manages transaction boundaries

4. **Domain Model**
   - `Inspection`: Parent entity for inspection data
   - `InspectionTeam`: Represents inspection teams
   - `TeamMember`: Team member details
   - `TeamSchool`: Schools assigned to teams
   - `InspectionChecklist`: Inspection checklists

### Synchronization Process

The synchronization follows these steps:

1. Scheduled task triggers (`@Scheduled`)
2. Fetch list of approved inspections
3. For each approved inspection:
   - Fetch full inspection details
   - Convert DTO to domain entities
   - Validate all entities
   - Sync teams and their members
   - Sync school assignments
   - Sync checklists
   - Save all relationships

### Error Handling

- Custom exceptions for API and sync errors
- Proper transaction management
- Detailed logging at each step
- Validation for all entities

### Configuration

Configure the synchronization in `application.properties`:

```properties
qamis.apiUrl=http://qamis.localhost:8000
inspection.syncCron=0 0/30 * * * ?  # Every 30 minutes
```
