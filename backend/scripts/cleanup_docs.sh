#!/bin/bash

# =====================================================
# ProcureZone Documentation Cleanup Script
# =====================================================
# This script archives outdated documentation files
# Date: November 2025
# =====================================================

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BACKEND_DIR="$(dirname "$SCRIPT_DIR")"

cd "$BACKEND_DIR"

echo "=============================================="
echo "ProcureZone Documentation Cleanup"
echo "=============================================="
echo ""
echo "Backend Directory: $BACKEND_DIR"
echo ""

# Create archive directory
echo "[1/3] Creating archive directories..."
mkdir -p archive/test-results
mkdir -p archive/old-docs

# Archive test results (don't delete - historical value)
echo "[2/3] Archiving test result files..."
if [ -f "api_test_results_priority7_fixes.txt" ]; then
    mv api_test_results_priority7_fixes.txt archive/test-results/
    echo "  ✓ Archived api_test_results_priority7_fixes.txt"
fi

if [ -f "indent_api_test_results_20251015_152112.txt" ]; then
    mv indent_api_test_results_20251015_152112.txt archive/test-results/
    echo "  ✓ Archived indent_api_test_results_20251015_152112.txt"
fi

if [ -f "token_response.json" ]; then
    mv token_response.json archive/test-results/
    echo "  ✓ Archived token_response.json"
fi

# Archive outdated progress/fix summaries
echo "[3/3] Archiving outdated documentation..."

outdated_docs=(
    "FIXES-SUMMARY-SESSION.md"
    "QUICK-FIXES-SUCCESS-SUMMARY.md"
    "USERNAME-EMAIL-BUG-FIX-COMPLETE.md"
    "PRIORITY-3-COMPLETION-SUMMARY.md"
    "PRIORITY-4-APPROVAL-WORKFLOW-COMPLETE.md"
    "REMAINING-FAILURES-AND-MISSING-FEATURES.md"
    "COMPREHENSIVE-TEST-REVIEW.md"
    "CRITICAL-FIXES-APPLIED.md"
    "SPRINT-PROGRESS-DAYS-1-10.md"
    "TEST-PROGRESS-CURRENT.md"
    "TEST-RESULTS-SUMMARY-OCT21.md"
)

for doc in "${outdated_docs[@]}"; do
    if [ -f "$doc" ]; then
        mv "$doc" archive/old-docs/
        echo "  ✓ Archived $doc"
    fi
done

# Remove doc/ outdated files
echo ""
echo "Cleaning doc/ directory..."
cd doc

outdated_doc_files=(
    "TESTING-READINESS-REPORT.md"
    "MISSING-TEST-COVERAGE.md"
    "TEST-EXECUTION-FAILED-REPORT.md"
)

for doc in "${outdated_doc_files[@]}"; do
    if [ -f "$doc" ]; then
        rm "$doc"
        echo "  ✓ Removed doc/$doc"
    fi
done

cd "$BACKEND_DIR"

echo ""
echo "=============================================="
echo "Cleanup Complete!"
echo "=============================================="
echo ""
echo "Summary:"
echo "  ✓ Test results archived to archive/test-results/"
echo "  ✓ Old docs archived to archive/old-docs/"
echo "  ✓ Superseded docs removed from doc/"
echo ""
echo "Master documentation files preserved:"
echo "  • doc/PHASE1-2-COMPLETE-SUMMARY.md"
echo "  • doc/PROJECT-COMPLETION-PLAN.md"
echo "  • doc/FEATURE-IMPLEMENTATION-ROADMAP-15Oct.md"
echo "  • doc/DEVELOPMENT-ACTION-PLAN.md"
echo ""
echo "Next step: Review backend/README.md for navigation"
