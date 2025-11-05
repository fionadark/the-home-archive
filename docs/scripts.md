# Development Scripts

This document describes the development and validation scripts for the Dark Academia Library Web Application.

## Scripts Location

All development scripts are located in the `scripts/` directory in the project root.

## Available Scripts

### `quickstart-validation.sh`
**Purpose**: Comprehensive validation script for T114 quickstart validation  
**Description**: Performs full end-to-end validation of the application including:
- Prerequisites verification (Java, Gradle, directories)
- Build validation (compilation, test compilation)
- Application startup and health checks
- API endpoint testing
- Database connectivity verification
- Cleanup and reporting

**Usage**:
```bash
cd /path/to/the-home-archive
./scripts/quickstart-validation.sh
```

**Requirements**:
- Java 21+ LTS
- Gradle (or wrapper)
- Write permissions for creating validation report

### `quickstart-quick-validation.sh`
**Purpose**: Quick application status check  
**Description**: Lightweight script that checks if the application is currently running on port 8080. Useful for development workflow to quickly verify application status.

**Usage**:
```bash
cd /path/to/the-home-archive
./scripts/quickstart-quick-validation.sh
```

**Requirements**:
- Basic shell utilities (curl, lsof)

## Development Workflow

1. **Development**: Use `quickstart-quick-validation.sh` to quickly check if your application is running
2. **Testing**: Use `quickstart-validation.sh` for comprehensive validation before commits or releases
3. **CI/CD**: Integration `quickstart-validation.sh` into automated testing pipelines

## Output

Both scripts provide colored output and detailed error reporting:
- ✓ PASS: Test passed successfully
- ✗ FAIL: Test failed with error details
- Final status report with summary

## Notes

- Scripts are designed to be run from the project root directory
- Both scripts have been tested on macOS and should work on Linux systems
- Windows users may need to use Git Bash or WSL
- Scripts automatically clean up any processes they start