"""
Configuration constants for the project.
"""
# ------------------------------
# General
# ------------------------------
CUCUMBER_REPORTS_PARENT_DIR = "PRs-cucumber-reports"
PR_FEATURES_CSV_FILENAME = "CSVs/pr_features.csv"
CUCUMBER_RESULTS_CSV_FILENAME = "CSVs/pr_features_with_results.csv"
RUN_DATA_FILENAME = "run_data.json"
RUN_FEATURE_RESULTS_FILENAME = "run_feature_results.json"
# Debug mode: 1 = debugging, 0 = production
DEBUG_MODE = 1

# ------------------------------
# PR Data Extraction
# ------------------------------
REPO_FULL_NAME = "uyuni-project/uyuni"
TEST_WORKFLOW_NAME = "TestFlow"
# Time windows (in days) to track recent change frequency of PR modified files
MODIFIED_FILES_RECENT_DAYS = [3, 14, 56]
# Oversample when fetching the latest N PRs to account for those missing Cucumber reports
PR_OVERSAMPLE_MULTIPLIER = 4
# Maximum number of consecutive PRs without Cucumber reports before aborting the extraction loop
MAX_CONSECUTIVE_PRS_WITHOUT_CUCUMBER_REPORTS = 15

# ------------------------------
# Cucumber Results Extraction
# ------------------------------
# Categories must be sorted from most specific to most general.
# The first matching category will be assigned to a feature.
CUCUMBER_FEATURE_CATEGORIES = [
    "srv_salt", "srv_docker", "srv",
    "proxy_salt", "proxy_docker", "proxy_container", "proxy_traditional", "proxy",
    "minssh_salt", "minssh_docker", "minssh",
    "min_rhlike_ssh", "min_deblike_ssh", "min_deblike", "min_rhlike",
    "min_salt", "min_docker", "min",
    "buildhost_salt", "buildhost_docker", "buildhost",
    "allcli"
]
CUCUMBER_FAILED = "failed"
CUCUMBER_PASSED = "passed"
CUCUMBER_SKIPPED = "skipped"
CUCUMBER_RESULTS_COLUMNS = ["feature", "feature_category", "scenario_count", "result"]

# ------------------------------
# Training Data Generation
# ------------------------------
TRAINING_CSV_FILENAME = "CSVs/training_data.csv"
# Time windows (in days) to track recent Cucumber feature failure rates
FEATURE_FAILURE_RECENT_DAYS = [7, 14, 28, 56]

# ------------------------------
# Data Preprocessing Pipeline
# ------------------------------
PREPROCESSED_TRAINING_CSV_FILENAME = "CSVs/preprocessed_training_data.csv"
PREPROCESSED_TESTING_CSV_FILENAME = "CSVs/preprocessed_testing_data.csv"
PREPROCESSING_PIPELINE_FILENAME = "artifacts/preprocessing_pipeline.joblib"
# File extensions frequency threshold to keep (top extensions covering X% frequency)
EXTENSION_COVERAGE_THRESHOLD = 0.95
# Test set size as fraction of total data (0.2 = 20% for testing, 80% for training)
TEST_SET_SIZE = 0.2

# ------------------------------
# XGBoost Classifier Training
# ------------------------------
XGBOOST_MODEL_FILENAME = "artifacts/xgboost_classifier.joblib"
# Random seed for reproducibility
RANDOM_SEED = 42
# Cross-validation configuration
CV_FOLDS = 5
# Number of iterations for RandomizedSearchCV
N_ITER_SEARCH = 50
