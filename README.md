# investments-tracker

Application for tracking investments across multiple broker accounts.

## Build System

This project uses **Gradle** with the **Gradle Wrapper**, which means you **don't need to install Gradle** on your machine. The wrapper (`gradlew` script) will automatically download and use the correct Gradle version.

### Prerequisites

- Java 21 (JDK 21 or later)
- No Gradle installation required (uses Gradle Wrapper)

### Common Gradle Commands

```bash
# Build the project (compile, run tests, create JAR)
./gradlew build

# Clean build artifacts and rebuild from scratch
./gradlew clean build

# Run the application
./gradlew bootRun

# Run tests only
./gradlew test

# Check dependencies
./gradlew dependencies

# View available tasks
./gradlew tasks
```

**Note for Windows users:** Use `gradlew.bat` instead of `./gradlew`

### First Build

The first time you run `./gradlew`, it will:
1. Download the correct Gradle version (8.11.1)
2. Download all project dependencies
3. Build the project

This may take a few minutes. Subsequent builds will be much faster.