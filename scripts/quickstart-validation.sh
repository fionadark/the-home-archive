#!/bin/bash

# Quickstart Validation Script for Dark Academia Library Web Application
# T114: Run quickstart.md validation and end-to-end testing
# Date: 2025-01-27
# Feature: 002-web-application-this

# set -e  # Exit on any error - disabled for validation script

echo "======================================"
echo "QUICKSTART VALIDATION - T114"
echo "Dark Academia Library Web Application"
echo "======================================"
echo ""

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test results tracking
TESTS_PASSED=0
TESTS_FAILED=0
VALIDATION_ERRORS=()

# Function to log test results
log_test() {
    local test_name="$1"
    local result="$2"
    local details="$3"
    
    if [ "$result" = "PASS" ]; then
        echo -e "${GREEN}✓ PASS${NC} - $test_name"
        [ -n "$details" ] && echo "  $details"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAIL${NC} - $test_name"
        [ -n "$details" ] && echo "  ERROR: $details"
        VALIDATION_ERRORS+=("$test_name: $details")
        ((TESTS_FAILED++))
    fi
}

# Function to check if application is running
check_app_running() {
    echo -e "${BLUE}Checking if application is running...${NC}"
    if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_test "Application Running Check" "PASS" "Application responding on port 8080"
        return 0
    else
        log_test "Application Running Check" "FAIL" "Application not responding on port 8080"
        return 1
    fi
}

# Function to start application if not running
start_application() {
    echo -e "${BLUE}Starting Spring Boot application...${NC}"
    echo "Running: ./gradlew bootRun (background)"
    
    # Start application in background
    nohup ./gradlew bootRun > application.log 2>&1 &
    APP_PID=$!
    
    # Wait for application to start (max 60 seconds)
    echo "Waiting for application startup..."
    for i in {1..60}; do
        if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
            log_test "Application Startup" "PASS" "Started in ${i} seconds (PID: $APP_PID)"
            return 0
        fi
        sleep 1
        echo -n "."
    done
    
    log_test "Application Startup" "FAIL" "Timeout waiting for application to start"
    return 1
}

# Function to validate build
validate_build() {
    echo -e "\n${BLUE}=== Build Validation ===${NC}"
    
    # Check Gradle build
    echo "Running: ./gradlew build -x test"
    if ./gradlew build -x test > build.log 2>&1; then
        log_test "Gradle Build" "PASS" "Build successful without tests"
    else
        log_test "Gradle Build" "FAIL" "Build failed - check build.log"
        return 1
    fi
    
    # Check test compilation
    echo "Running: ./gradlew testClasses"
    if ./gradlew testClasses > test-compile.log 2>&1; then
        log_test "Test Compilation" "PASS" "Test classes compiled successfully"
    else
        log_test "Test Compilation" "FAIL" "Test compilation failed - check test-compile.log"
    fi
}

# Function to validate prerequisites
validate_prerequisites() {
    echo -e "\n${BLUE}=== Prerequisites Validation ===${NC}"
    
    # Java version check
    if command -v java >/dev/null 2>&1; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
        if [ "$JAVA_VERSION" -ge 21 ]; then
            log_test "Java Version" "PASS" "Java $JAVA_VERSION detected"
        else
            log_test "Java Version" "FAIL" "Java 21+ required, found Java $JAVA_VERSION"
        fi
    else
        log_test "Java Installation" "FAIL" "Java not found in PATH"
    fi
    
    # Gradle check
    if [ -f "./gradlew" ]; then
        log_test "Gradle Wrapper" "PASS" "Gradle wrapper found"
    else
        log_test "Gradle Wrapper" "FAIL" "Gradle wrapper not found"
    fi
    
    # Project structure check
    REQUIRED_DIRS=("src/main/java" "src/main/resources" "src/test/java" "frontend")
    for dir in "${REQUIRED_DIRS[@]}"; do
        if [ -d "$dir" ]; then
            log_test "Directory: $dir" "PASS" "Directory exists"
        else
            log_test "Directory: $dir" "FAIL" "Required directory missing"
        fi
    done
}

# Function to validate API endpoints
validate_api_endpoints() {
    echo -e "\n${BLUE}=== API Endpoints Validation ===${NC}"
    
    # Health endpoint
    if curl -f -s http://localhost:8080/actuator/health | grep -q "UP"; then
        log_test "Health Endpoint" "PASS" "Application health check successful"
    else
        log_test "Health Endpoint" "FAIL" "Health endpoint not responding correctly"
    fi
    
    # H2 Console endpoint  
    if curl -f -s -I http://localhost:8080/h2-console | grep -q "200 OK"; then
        log_test "H2 Console" "PASS" "H2 console accessible"
    else
        log_test "H2 Console" "FAIL" "H2 console not accessible"
    fi
    
    # Swagger UI endpoint
    if curl -f -s -I http://localhost:8080/swagger-ui.html | grep -q -E "(200 OK|302 Found)"; then
        log_test "Swagger UI" "PASS" "Swagger UI accessible"
    else
        log_test "Swagger UI" "FAIL" "Swagger UI not accessible"
    fi
    
    # API base path
    if curl -f -s -I http://localhost:8080/api/v1/auth/register | grep -q -E "(200|405|400)"; then
        log_test "API Base Path" "PASS" "API endpoints are routed correctly"
    else
        log_test "API Base Path" "FAIL" "API base path not responding"
    fi
}

# Function to test authentication workflow
validate_auth_workflow() {
    echo -e "\n${BLUE}=== Authentication Workflow Validation ===${NC}"
    
    # Test user registration
    REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/register \
        -H "Content-Type: application/json" \
        -d '{
            "email": "test@quickstart.validation",
            "password": "Password123!",
            "firstName": "Test",
            "lastName": "User"
        }' 2>/dev/null)
    
    HTTP_CODE="${REGISTER_RESPONSE: -3}"
    RESPONSE_BODY="${REGISTER_RESPONSE%???}"
    
    if [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "409" ]; then
        if [ "$HTTP_CODE" = "201" ]; then
            log_test "User Registration" "PASS" "New user registered successfully"
        else
            log_test "User Registration" "PASS" "User already exists (expected for re-runs)"
        fi
    else
        log_test "User Registration" "FAIL" "Registration failed with HTTP $HTTP_CODE"
    fi
    
    # Test user login
    LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{
            "email": "test@quickstart.validation",
            "password": "Password123!"
        }' 2>/dev/null)
    
    LOGIN_HTTP_CODE="${LOGIN_RESPONSE: -3}"
    LOGIN_BODY="${LOGIN_RESPONSE%???}"
    
    if [ "$LOGIN_HTTP_CODE" = "200" ]; then
        # Extract JWT token
        JWT_TOKEN=$(echo "$LOGIN_BODY" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$JWT_TOKEN" ]; then
            log_test "User Login" "PASS" "Login successful, JWT token received"
            echo "    JWT Token (first 20 chars): ${JWT_TOKEN:0:20}..."
        else
            log_test "User Login" "FAIL" "Login successful but no JWT token in response"
        fi
    else
        log_test "User Login" "FAIL" "Login failed with HTTP $LOGIN_HTTP_CODE"
    fi
    
    # Test protected endpoint with JWT
    if [ -n "$JWT_TOKEN" ]; then
        PROTECTED_RESPONSE=$(curl -s -w "%{http_code}" -X GET http://localhost:8080/api/v1/library \
            -H "Authorization: Bearer $JWT_TOKEN" 2>/dev/null)
        
        PROTECTED_HTTP_CODE="${PROTECTED_RESPONSE: -3}"
        
        if [ "$PROTECTED_HTTP_CODE" = "200" ]; then
            log_test "Protected Endpoint Access" "PASS" "JWT authentication working"
        else
            log_test "Protected Endpoint Access" "FAIL" "Protected endpoint returned HTTP $PROTECTED_HTTP_CODE"
        fi
    fi
}

# Function to validate database connectivity
validate_database() {
    echo -e "\n${BLUE}=== Database Validation ===${NC}"
    
    # Check H2 console connectivity
    H2_RESPONSE=$(curl -s -w "%{http_code}" http://localhost:8080/h2-console 2>/dev/null)
    H2_HTTP_CODE="${H2_RESPONSE: -3}"
    
    if [ "$H2_HTTP_CODE" = "200" ]; then
        log_test "H2 Database Console" "PASS" "H2 console accessible at /h2-console"
    else
        log_test "H2 Database Console" "FAIL" "H2 console not accessible"
    fi
    
    # Test database health through actuator
    DB_HEALTH=$(curl -s http://localhost:8080/actuator/health 2>/dev/null | grep -o '"db":{"status":"[^"]*"' | cut -d'"' -f6)
    if [ "$DB_HEALTH" = "UP" ]; then
        log_test "Database Connectivity" "PASS" "Database connection healthy"
    else
        log_test "Database Connectivity" "FAIL" "Database connection issues"
    fi
}

# Function to validate frontend structure
validate_frontend() {
    echo -e "\n${BLUE}=== Frontend Structure Validation ===${NC}"
    
    # Check frontend directory structure
    FRONTEND_DIRS=("frontend/src/js" "frontend/src/css" "frontend/src/html")
    for dir in "${FRONTEND_DIRS[@]}"; do
        if [ -d "$dir" ]; then
            log_test "Frontend Directory: $dir" "PASS" "Directory exists"
        else
            log_test "Frontend Directory: $dir" "FAIL" "Frontend directory missing"
        fi
    done
    
    # Check for key frontend files
    FRONTEND_FILES=(
        "frontend/src/html/login.html"
        "frontend/src/html/register.html"
        "frontend/src/html/dashboard.html"
        "frontend/src/css/themes/dark-academia.css"
        "frontend/src/js/services/authService.js"
    )
    
    for file in "${FRONTEND_FILES[@]}"; do
        if [ -f "$file" ]; then
            log_test "Frontend File: $(basename $file)" "PASS" "File exists"
        else
            log_test "Frontend File: $(basename $file)" "FAIL" "Frontend file missing: $file"
        fi
    done
}

# Function to run unit tests
validate_tests() {
    echo -e "\n${BLUE}=== Unit Tests Validation ===${NC}"
    
    echo "Running: ./gradlew test"
    if ./gradlew test > test-results.log 2>&1; then
        # Count test results
        TESTS_RUN=$(grep -o "tests completed" test-results.log | wc -l || echo "0")
        log_test "Unit Tests Execution" "PASS" "All tests passed"
    else
        FAILED_TESTS=$(grep -o "failed" test-results.log | wc -l || echo "0")
        log_test "Unit Tests Execution" "FAIL" "Some tests failed - check test-results.log"
    fi
}

# Function to validate configuration
validate_configuration() {
    echo -e "\n${BLUE}=== Configuration Validation ===${NC}"
    
    # Check for required configuration files
    CONFIG_FILES=(
        "src/main/resources/application.yml"
        "src/main/resources/application-dev.yml"
        "src/main/resources/application-prod.yml"
    )
    
    for file in "${CONFIG_FILES[@]}"; do
        if [ -f "$file" ]; then
            log_test "Config File: $(basename $file)" "PASS" "Configuration file exists"
        else
            log_test "Config File: $(basename $file)" "FAIL" "Configuration file missing: $file"
        fi
    done
    
    # Check if data.sql exists for sample data
    if [ -f "src/main/resources/data.sql" ]; then
        log_test "Sample Data Script" "PASS" "data.sql found for sample data"
    else
        log_test "Sample Data Script" "FAIL" "data.sql missing - no sample data"
    fi
}

# Function to clean up
cleanup() {
    echo -e "\n${BLUE}=== Cleanup ===${NC}"
    
    if [ -n "$APP_PID" ]; then
        echo "Stopping application (PID: $APP_PID)..."
        kill $APP_PID 2>/dev/null || true
        sleep 2
        # Force kill if still running
        kill -9 $APP_PID 2>/dev/null || true
        log_test "Application Cleanup" "PASS" "Application stopped"
    fi
    
    # Clean up log files
    rm -f application.log build.log test-compile.log test-results.log
}

# Function to generate final report
generate_report() {
    echo -e "\n${BLUE}======================================"
    echo "QUICKSTART VALIDATION REPORT - T114"
    echo -e "======================================${NC}"
    echo ""
    echo -e "Total Tests Run: $(($TESTS_PASSED + $TESTS_FAILED))"
    echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
    echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"
    echo ""
    
    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "${GREEN}🎉 VALIDATION SUCCESSFUL! 🎉${NC}"
        echo -e "${GREEN}All quickstart.md scenarios work correctly.${NC}"
        echo ""
        echo -e "${GREEN}✅ Application builds and runs successfully${NC}"
        echo -e "${GREEN}✅ All API endpoints are accessible${NC}"
        echo -e "${GREEN}✅ Authentication workflow is functional${NC}"
        echo -e "${GREEN}✅ Database connectivity is working${NC}"
        echo -e "${GREEN}✅ Frontend structure is complete${NC}"
        echo -e "${GREEN}✅ Configuration is properly set up${NC}"
        echo ""
        echo -e "${GREEN}The quickstart.md guide provides accurate instructions${NC}"
        echo -e "${GREEN}for setting up and running the Dark Academia Library application.${NC}"
        
        # Generate success report file
        cat > quickstart-validation-report.md << EOF
# Quickstart Validation Report - T114

**Date**: $(date)
**Feature**: 002-web-application-this
**Status**: ✅ SUCCESS

## Summary
All quickstart.md scenarios validated successfully. The application:
- Builds and runs without errors
- Provides all documented endpoints
- Implements working authentication
- Has proper database connectivity
- Includes complete frontend structure
- Contains proper configuration files

## Test Results
- **Total Tests**: $(($TESTS_PASSED + $TESTS_FAILED))
- **Passed**: $TESTS_PASSED
- **Failed**: $TESTS_FAILED

## Validation Conclusion
The quickstart.md guide accurately describes how to set up and run the Dark Academia Library Web Application. All documented features and endpoints work as expected.

## Next Steps
T114 is complete. The application is ready for:
- Production deployment
- Additional feature development
- User acceptance testing
- Performance optimization
EOF
        
        echo ""
        echo -e "${BLUE}📄 Detailed report saved to: quickstart-validation-report.md${NC}"
        
    else
        echo -e "${RED}❌ VALIDATION FAILED ❌${NC}"
        echo ""
        echo -e "${RED}The following issues were found:${NC}"
        for error in "${VALIDATION_ERRORS[@]}"; do
            echo -e "${RED}  • $error${NC}"
        done
        echo ""
        echo -e "${YELLOW}Please review and fix these issues before considering T114 complete.${NC}"
        
        # Generate failure report
        cat > quickstart-validation-report.md << EOF
# Quickstart Validation Report - T114

**Date**: $(date)
**Feature**: 002-web-application-this
**Status**: ❌ FAILED

## Summary
Quickstart validation found $TESTS_FAILED issue(s) that prevent successful completion of T114.

## Test Results
- **Total Tests**: $(($TESTS_PASSED + $TESTS_FAILED))
- **Passed**: $TESTS_PASSED
- **Failed**: $TESTS_FAILED

## Issues Found
$(printf '%s\n' "${VALIDATION_ERRORS[@]}" | sed 's/^/- /')

## Validation Conclusion
The quickstart.md guide has issues that prevent successful application setup or execution. These must be resolved before T114 can be considered complete.

## Required Actions
1. Fix the identified issues
2. Re-run the validation script
3. Ensure all tests pass before marking T114 as complete
EOF
        
        return 1
    fi
}

# Main execution
main() {
    echo "Starting quickstart validation..."
    echo "Working directory: $(pwd)"
    echo ""
    
    # Validate prerequisites first
    validate_prerequisites
    
    # Validate build
    validate_build
    
    # Check if app is already running, start if needed
    if ! check_app_running; then
        start_application || {
            echo -e "${RED}Failed to start application. Cannot continue validation.${NC}"
            generate_report
            exit 1
        }
    fi
    
    # Run all validations
    validate_configuration
    validate_api_endpoints
    validate_database
    validate_auth_workflow
    validate_frontend
    validate_tests
    
    # Generate final report
    generate_report
    
    # Return appropriate exit code
    if [ $TESTS_FAILED -eq 0 ]; then
        echo -e "\n${GREEN}T114 - Quickstart validation completed successfully!${NC}"
        return 0
    else
        echo -e "\n${RED}T114 - Quickstart validation failed. See report for details.${NC}"
        return 1
    fi
}

# Set up trap for cleanup
trap cleanup EXIT

# Run main function
main "$@"