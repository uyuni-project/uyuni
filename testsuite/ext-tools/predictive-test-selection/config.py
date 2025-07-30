"""
Configuration constants for the project.
"""
# ------------------------------
# PR Data Extraction
# ------------------------------
REPO_FULL_NAME = "uyuni-project/uyuni"
TEST_WORKFLOW_NAME = "TestFlow"
PR_FEATURES_CSV_FILENAME = "pr_features.csv"
# Time windows (in days) to track recent change frequency of PR modified files
RECENT_DAYS = [3, 14, 56]
# Oversample when fetching the latest N PRs to account for those missing Cucumber reports
PR_OVERSAMPLE_MULTIPLIER = 4
# Maximum number of consecutive PRs without Cucumber reports before aborting the extraction loop
MAX_CONSECUTIVE_PRS_WITHOUT_CUCUMBER_REPORTS = 15
# Parent directory for all PRs Cucumber reports folders
CUCUMBER_REPORTS_PARENT_DIR = "PRs-cucumber-reports"
