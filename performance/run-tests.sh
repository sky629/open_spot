#!/bin/bash

###############################################################################
# Run Performance Tests for Open-Spot
#
# Usage:
#   ./performance/run-tests.sh [test-name] [report-output-dir]
#
# Examples:
#   ./performance/run-tests.sh                    # Run all tests
#   ./performance/run-tests.sh quick              # Quick test (1 min)
#   ./performance/run-tests.sh full ./reports     # Full test with custom report dir
###############################################################################

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Configuration
TEST_NAME="${1:-full}"
REPORT_DIR="${2:-./performance/reports}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_JSON="$REPORT_DIR/report_${TEST_NAME}_${TIMESTAMP}.json"
REPORT_HTML="$REPORT_DIR/report_${TEST_NAME}_${TIMESTAMP}.html"

echo -e "${YELLOW}========================================${NC}"
echo -e "${YELLOW}Open-Spot Performance Tests${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Test: $TEST_NAME"
echo "Report: $REPORT_HTML"
echo ""

# Create report directory
mkdir -p "$REPORT_DIR"

# Check if Artillery is installed
if ! command -v artillery &> /dev/null; then
    echo -e "${RED}Error: Artillery is not installed${NC}"
    echo ""
    echo "Install Artillery globally:"
    echo "  npm install -g artillery"
    echo ""
    echo "Or use npx (no installation needed):"
    echo "  npx artillery run performance/artillery-config.yml"
    exit 1
fi

# Check if services are running
echo -n "Checking if services are running... "
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/v1/auth/health | grep -q "200"; then
    echo -e "${GREEN}✓ OK${NC}"
else
    echo -e "${RED}✗ FAILED${NC}"
    echo ""
    echo -e "${RED}Error: Services are not running on localhost:8080${NC}"
    echo ""
    echo "Start services first:"
    echo "  ./start-infrastructure.sh"
    echo "  ./start-services.sh"
    echo ""
    echo "Or use Docker Compose:"
    echo "  docker-compose -f docker-compose.prod.yml up -d"
    exit 1
fi

echo ""

# Run performance test based on test name
case "$TEST_NAME" in
    quick)
        echo -e "${YELLOW}Running quick performance test (1 minute)...${NC}"
        artillery quick --duration 60 --rate 10 http://localhost:8080/api/v1/auth/health \
            --output "$REPORT_JSON"
        ;;

    stress)
        echo -e "${YELLOW}Running stress test (high load)...${NC}"
        artillery run performance/artillery-config.yml \
            --overrides '{"config":{"phases":[{"duration":60,"arrivalRate":100}]}}' \
            --output "$REPORT_JSON"
        ;;

    spike)
        echo -e "${YELLOW}Running spike test (sudden traffic spike)...${NC}"
        artillery run performance/artillery-config.yml \
            --overrides '{"config":{"phases":[{"duration":10,"arrivalRate":5},{"duration":20,"arrivalRate":200},{"duration":10,"arrivalRate":5}]}}' \
            --output "$REPORT_JSON"
        ;;

    full|*)
        echo -e "${YELLOW}Running full performance test suite...${NC}"
        artillery run performance/artillery-config.yml \
            --output "$REPORT_JSON"
        ;;
esac

# Generate HTML report
echo ""
echo -n "Generating HTML report... "
artillery report "$REPORT_JSON" --output "$REPORT_HTML"
echo -e "${GREEN}✓ Done${NC}"

echo ""
echo -e "${YELLOW}========================================${NC}"
echo -e "${GREEN}Performance test completed!${NC}"
echo -e "${YELLOW}========================================${NC}"
echo ""
echo "Reports:"
echo "  JSON: $REPORT_JSON"
echo "  HTML: $REPORT_HTML"
echo ""
echo "Open report:"
echo "  open $REPORT_HTML"
echo ""

# Print summary
echo -e "${YELLOW}Test Summary:${NC}"
artillery report "$REPORT_JSON" | grep -A 20 "Summary report"

exit 0
