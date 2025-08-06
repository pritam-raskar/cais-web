#!/bin/bash

# Generate JSON test summary report
echo "Generating JSON test summary..."

mvn test > test-output.log 2>&1

# Parse results and create JSON
cat > test-summary.json << EOF
{
  "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "project": "CAIS Alert Service",
  "summary": {
    "successful_classes": $(grep -c "Tests run:.*Failures: 0, Errors: 0" test-output.log),
    "failed_classes": $(grep -c "FAILURE!" test-output.log),
    "total_tests": $(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Tests run: \([0-9]*\).*/\1/' || echo 0),
    "failures": $(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Failures: \([0-9]*\).*/\1/' || echo 0),
    "errors": $(grep "Tests run:" test-output.log | tail -1 | sed 's/.*Errors: \([0-9]*\).*/\1/' || echo 0),
    "build_status": "$(grep -q "BUILD SUCCESS" test-output.log && echo "SUCCESS" || echo "FAILURE")"
  },
  "successful_classes": [
$(grep -B1 "Tests run:.*Failures: 0, Errors: 0" test-output.log | grep "INFO.*--" | sed 's/.*-- in /    "/' | sed 's/$/",/' | sed '$ s/,$//')
  ],
  "failed_classes": [
$(grep -B1 "FAILURE!" test-output.log | grep "ERROR.*--" | sed 's/.*-- in /    "/' | sed 's/$/",/' | sed '$ s/,$//')
  ]
}
EOF

echo "✅ JSON summary generated: test-summary.json"
cat test-summary.json