#!/bin/bash

echo "🚀 CAIS Alert Service - Comprehensive Test Summary"
echo "=================================================="
echo ""

# Run tests and capture output
echo "⏳ Running all tests..."
mvn test > test-output.log 2>&1

# Extract summary information
echo "📊 TEST EXECUTION SUMMARY"
echo "========================="

# Count successful test classes
SUCCESSFUL_CLASSES=$(grep -c "Tests run:.*Failures: 0, Errors: 0" test-output.log)

# Count failed test classes  
FAILED_CLASSES=$(grep -c "FAILURE!" test-output.log)

# Get overall test counts
TOTAL_TESTS=$(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Tests run: \([0-9]*\).*/\1/')
FAILURES=$(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Failures: \([0-9]*\).*/\1/')
ERRORS=$(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Errors: \([0-9]*\).*/\1/')

echo ""
echo "📈 CLASS-LEVEL RESULTS:"
echo "  ✅ Successful Classes: $SUCCESSFUL_CLASSES"
echo "  ❌ Failed Classes: $FAILED_CLASSES"
echo "  📊 Total Classes: $((SUCCESSFUL_CLASSES + FAILED_CLASSES))"
echo ""

echo "📈 TEST-LEVEL RESULTS:"
echo "  🎯 Total Tests: $TOTAL_TESTS"
echo "  ✅ Passed: $((TOTAL_TESTS - FAILURES - ERRORS))"
echo "  ❌ Failures: $FAILURES"
echo "  💥 Errors: $ERRORS"
echo ""

# Calculate success rate
if [ "$TOTAL_TESTS" -gt 0 ]; then
    SUCCESS_RATE=$(( (TOTAL_TESTS - FAILURES - ERRORS) * 100 / TOTAL_TESTS ))
    echo "📊 Overall Success Rate: $SUCCESS_RATE%"
else
    echo "📊 Overall Success Rate: 0%"
fi

echo ""
echo "📋 DETAILED CLASS BREAKDOWN:"
echo "============================="

# Show detailed class results
echo ""
echo "✅ SUCCESSFUL CLASSES:"
grep -B1 "Tests run:.*Failures: 0, Errors: 0" test-output.log | grep "INFO.*--" | sed 's/.*-- in /  ✅ /' | sort

echo ""
echo "❌ FAILED CLASSES:"
grep -B1 "FAILURE!" test-output.log | grep "ERROR.*--" | sed 's/.*-- in /  ❌ /' | sort

echo ""
echo "🔍 BUILD STATUS:"
if grep -q "BUILD SUCCESS" test-output.log; then
    echo "  🎉 BUILD: SUCCESS"
else
    echo "  💥 BUILD: FAILURE"
fi

echo ""
echo "📄 Full test output saved to: test-output.log"
echo "🌐 For detailed HTML report, run: mvn surefire-report:report"
echo "   Report will be at: target/site/surefire-report.html"
echo ""