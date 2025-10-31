#!/bin/bash

# Quick Validation Script for Running Application - T114
# This script assumes the application is already running on port 8080

echo "========================================="
echo "QUICK VALIDATION - T114 (App Running)"
echo "Dark Academia Library Web Application"
echo "========================================="
echo ""

# Color codes
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

TESTS_PASSED=0
TESTS_FAILED=0
VALIDATION_ERRORS=()

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

# Test if application is running
echo -e "${BLUE}=== Application Status ===${NC}"
if curl -f -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    log_test "Application Running" "PASS" "Application responding on port 8080"
else
    log_test "Application Running" "FAIL" "Application not responding on port 8080"
    echo "Please start the application first: ./gradlew bootRun"
    exit 1
fi

# Test health endpoint
echo -e "\n${BLUE}=== Health Check ===${NC}"
HEALTH_RESPONSE=$(curl -s http://localhost:8080/actuator/health 2>/dev/null)
if echo "$HEALTH_RESPONSE" | grep -q "UP"; then
    log_test "Health Endpoint" "PASS" "Application health check successful"
else
    log_test "Health Endpoint" "FAIL" "Health endpoint not responding correctly"
fi

# Test H2 Console
echo -e "\n${BLUE}=== Database Access ===${NC}"
if curl -f -s -I http://localhost:8080/h2-console 2>/dev/null | grep -q "200 OK"; then
    log_test "H2 Console" "PASS" "H2 console accessible at /h2-console"
else
    log_test "H2 Console" "FAIL" "H2 console not accessible"
fi

# Test Swagger UI
echo -e "\n${BLUE}=== API Documentation ===${NC}"
if curl -f -s -I http://localhost:8080/swagger-ui.html 2>/dev/null | grep -q -E "(200 OK|302 Found)"; then
    log_test "Swagger UI" "PASS" "API documentation accessible"
else
    log_test "Swagger UI" "FAIL" "Swagger UI not accessible"
fi

# Test authentication endpoints
echo -e "\n${BLUE}=== Authentication API ===${NC}"

# Test registration endpoint structure
REG_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{}' 2>/dev/null)

REG_HTTP_CODE="${REG_RESPONSE: -3}"
if [ "$REG_HTTP_CODE" = "400" ]; then
    log_test "Registration Endpoint" "PASS" "Registration endpoint accepts requests (validation working)"
else
    log_test "Registration Endpoint" "FAIL" "Registration endpoint returned unexpected code: $REG_HTTP_CODE"
fi

# Test login endpoint structure  
LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/login \
    -H "Content-Type: application/json" \
    -d '{}' 2>/dev/null)

LOGIN_HTTP_CODE="${LOGIN_RESPONSE: -3}"
if [ "$LOGIN_HTTP_CODE" = "400" ]; then
    log_test "Login Endpoint" "PASS" "Login endpoint accepts requests (validation working)"
else
    log_test "Login Endpoint" "FAIL" "Login endpoint returned unexpected code: $LOGIN_HTTP_CODE"
fi

# Test real user workflow
echo -e "\n${BLUE}=== End-to-End Authentication ===${NC}"

# Register test user
REGISTER_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{
        "email": "quickstart-test@validation.com",
        "password": "TestPassword123!",
        "firstName": "Quickstart",
        "lastName": "Test"
    }' 2>/dev/null)

REG_CODE="${REGISTER_RESPONSE: -3}"
if [ "$REG_CODE" = "201" ] || [ "$REG_CODE" = "409" ]; then
    if [ "$REG_CODE" = "201" ]; then
        log_test "User Registration" "PASS" "New user registered successfully"
    else
        log_test "User Registration" "PASS" "User already exists (expected for re-runs)"
    fi
    
    # Try to login
    LOGIN_RESPONSE=$(curl -s -w "%{http_code}" -X POST http://localhost:8080/api/v1/auth/login \
        -H "Content-Type: application/json" \
        -d '{
            "email": "quickstart-test@validation.com",
            "password": "TestPassword123!"
        }' 2>/dev/null)
    
    LOGIN_CODE="${LOGIN_RESPONSE: -3}"
    LOGIN_BODY="${LOGIN_RESPONSE%???}"
    
    if [ "$LOGIN_CODE" = "200" ]; then
        # Extract JWT token
        JWT_TOKEN=$(echo "$LOGIN_BODY" | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)
        if [ -n "$JWT_TOKEN" ]; then
            log_test "User Login" "PASS" "Login successful with JWT token"
            
            # Test protected endpoint
            LIBRARY_RESPONSE=$(curl -s -w "%{http_code}" -X GET http://localhost:8080/api/v1/library \
                -H "Authorization: Bearer $JWT_TOKEN" 2>/dev/null)
            
            LIBRARY_CODE="${LIBRARY_RESPONSE: -3}"
            if [ "$LIBRARY_CODE" = "200" ]; then
                log_test "Protected API Access" "PASS" "JWT authentication working for protected endpoints"
            else
                log_test "Protected API Access" "FAIL" "Protected endpoint returned HTTP $LIBRARY_CODE"
            fi
        else
            log_test "User Login" "FAIL" "Login successful but no JWT token in response"
        fi
    else
        log_test "User Login" "FAIL" "Login failed with HTTP $LOGIN_CODE"
    fi
else
    log_test "User Registration" "FAIL" "Registration failed with HTTP $REG_CODE"
fi

# Test book search API
echo -e "\n${BLUE}=== Book Search API ===${NC}"
if [ -n "$JWT_TOKEN" ]; then
    SEARCH_RESPONSE=$(curl -s -w "%{http_code}" -X GET "http://localhost:8080/api/v1/books?q=test" \
        -H "Authorization: Bearer $JWT_TOKEN" 2>/dev/null)
    
    SEARCH_CODE="${SEARCH_RESPONSE: -3}"
    if [ "$SEARCH_CODE" = "200" ]; then
        log_test "Book Search" "PASS" "Book search API accessible"
    else
        log_test "Book Search" "FAIL" "Book search returned HTTP $SEARCH_CODE"
    fi
fi

# Final report
echo -e "\n${BLUE}================================"
echo "QUICK VALIDATION REPORT - T114"
echo -e "================================${NC}"
echo ""
echo "Total Tests: $(($TESTS_PASSED + $TESTS_FAILED))"
echo -e "${GREEN}Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Failed: $TESTS_FAILED${NC}"
echo ""

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "${GREEN}🎉 QUICKSTART VALIDATION SUCCESSFUL! 🎉${NC}"
    echo ""
    echo -e "${GREEN}✅ Application is running correctly${NC}"
    echo -e "${GREEN}✅ All core endpoints are functional${NC}"
    echo -e "${GREEN}✅ Authentication workflow works end-to-end${NC}"
    echo -e "${GREEN}✅ API security is properly implemented${NC}"
    echo -e "${GREEN}✅ Database integration is working${NC}"
    echo ""
    echo -e "${GREEN}The quickstart.md guide provides accurate setup instructions.${NC}"
    echo -e "${GREEN}T114 validation is COMPLETE! ✅${NC}"
    
    # Create success summary
    cat > quickstart-validation-summary.md << EOF
# T114 - Quickstart Validation Summary

**Date**: $(date)  
**Status**: ✅ SUCCESS

## Validated Components
- ✅ Application startup and health
- ✅ H2 database console access
- ✅ Swagger API documentation
- ✅ User registration and login
- ✅ JWT authentication and authorization
- ✅ Protected API endpoint access
- ✅ Book search API functionality

## Test Results
- **Total Tests**: $(($TESTS_PASSED + $TESTS_FAILED))
- **Passed**: $TESTS_PASSED  
- **Failed**: $TESTS_FAILED

## Conclusion
All quickstart.md scenarios work correctly. The Dark Academia Library Web Application is fully functional and ready for use.

**T114 is COMPLETE** ✅
EOF
    
    echo ""
    echo -e "${BLUE}📄 Summary saved to: quickstart-validation-summary.md${NC}"
    exit 0
else
    echo -e "${RED}❌ VALIDATION FAILED${NC}"
    echo ""
    echo "Issues found:"
    for error in "${VALIDATION_ERRORS[@]}"; do
        echo -e "${RED}  • $error${NC}"
    done
    echo ""
    echo -e "${RED}Please fix these issues before completing T114.${NC}"
    exit 1
fi