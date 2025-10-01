#!/usr/bin/env python3
"""
Extracts feature results from each run folder within PR cucumber report directories
and saves the results to JSON files for further analysis.

This script processes the CSV file produced by the PR data extraction script and
for each PR, processes all run folders to extract feature-level test results.

Usage:
    python runs_feature_result_extraction.py

- Run the PR data extraction script first, this script depends on its output.
- Uses functions from cucumber_results_extraction.py for feature result extraction.
- Creates a JSON file in each run folder with detailed feature results and data.
"""
import csv
import json
import logging
import os
import traceback
from typing import Any, Dict, List

from config import (
    CUCUMBER_REPORTS_PARENT_DIR,
    PR_FEATURES_CSV_FILENAME,
    CUCUMBER_PASSED,
    CUCUMBER_FAILED,
    RUN_DATA_FILENAME,
    RUN_FEATURE_RESULTS_FILENAME,
)
from cucumber_results_extraction import (
    extract_results_and_scenario_counts,
    get_feature_category,
)
from utilities import setup_logging

logger = setup_logging(logging.DEBUG, log_file="logs/runs_feature_result_extraction.log")

def get_run_folders(pr_folder_path: str) -> List[str]:
    """
    Get all run folders within a PR folder.

    Args:
        pr_folder_path: Path to the PR folder containing run directories.

    Returns:
        List of run folder names (e.g., ['run_1', 'run_2']).
    """
    if not os.path.isdir(pr_folder_path):
        logger.warning("PR folder not found: %s", pr_folder_path)
        return []

    run_folders = []
    for item in os.listdir(pr_folder_path):
        item_path = os.path.join(pr_folder_path, item)
        if os.path.isdir(item_path) and item.startswith("run_"):
            run_folders.append(item)

    # Sort run folders numerically
    run_folders.sort(key=lambda x: int(x.split('_')[1]))
    return run_folders

def extract_run_results(run_folder_path: str) -> List[Dict[str, Any]]:
    """
    Process a single run folder and extract feature results.

    Args:
        run_folder_path: Path to the run folder containing Cucumber JSON reports.

    Returns:
        List of dictionaries containing feature data with keys:
        - feature_name: Name of the feature
        - category_name: Category of the feature
        - scenario_count: Number of scenarios in the feature
        - result: Test result (passed/failed)
    """
    logger.debug("Processing run folder: %s", run_folder_path)

    feature_results = extract_results_and_scenario_counts(
        run_folder_path, log_stats=False
    )

    run_results = []
    for feature_name, scenario_count, result in feature_results:
        if result in [CUCUMBER_PASSED, CUCUMBER_FAILED]:
            category_name = get_feature_category(feature_name)
            feature_data = {
                "feature_name": feature_name,
                "category_name": category_name,
                "scenario_count": scenario_count,
                "result": result
            }
            run_results.append(feature_data)

    logger.debug("Extracted %d feature results from run folder", len(run_results))
    return run_results

def save_run_results(run_folder_path: str, run_results: List[Dict[str, Any]]) -> None:
    """
    Save the run feature results to a JSON file in the run folder.

    Args:
        run_folder_path: Path to the run folder where to save the JSON file.
        run_results: List of feature data dictionaries to save.
    """
    output_file = os.path.join(run_folder_path, RUN_FEATURE_RESULTS_FILENAME)

    try:
        with open(output_file, "w", encoding="utf-8") as f:
            json.dump(run_results, f, indent=2, ensure_ascii=False)
        logger.info("Saved run results to: %s", output_file)
    except Exception as e:
        logger.error("Failed to save run results to %s: %s", output_file, e)

def update_run_data_result(run_folder_path: str, run_results: List[Dict[str, Any]]) -> None:
    """
    Update the result in run_data JSON file based on the actual test feature results.
    Sometimes a test run may be marked as failed due to reasons unrelated to the test outcomes 
    (e.g., infrastructure issues, timeouts). Since we only care whether the tests themselves passed, 
    a run should be considered "passed" if all features passed, even if the overall run result 
    was initially marked as "failed" due to other reasons.

    Args:
        run_folder_path: Path to the run folder containing the run_data JSON file.
        run_results: List of feature result dictionaries.
    """
    run_data_path = os.path.join(run_folder_path, RUN_DATA_FILENAME)
    if not os.path.exists(run_data_path):
        logger.warning("%s not found in: %s", RUN_DATA_FILENAME, run_folder_path)
        return

    try:
        with open(run_data_path, "r", encoding="utf-8") as f:
            run_data = json.load(f)

        if not run_results:
            logger.warning("No feature results found, skipping %s result update", RUN_DATA_FILENAME)
            return
        else:
            feature_failed = any(feature.get("result") == CUCUMBER_FAILED
                           for feature in run_results)
            correct_result = CUCUMBER_FAILED if feature_failed else CUCUMBER_PASSED

        current_result = run_data.get("result")
        if current_result != correct_result:
            logger.info("Updating result from '%s' to '%s' in: %s",
                       current_result, correct_result, run_data_path)
            run_data["result"] = correct_result

            with open(run_data_path, "w", encoding="utf-8") as f:
                json.dump(run_data, f, indent=2, ensure_ascii=False)
            logger.info("Updated %s result", RUN_DATA_FILENAME)
        else:
            logger.debug("Result already correct ('%s') in: %s",
                        current_result, run_data_path)
    except (FileNotFoundError, json.JSONDecodeError) as e:
        logger.error("Error updating %s in %s: %s", RUN_DATA_FILENAME, run_folder_path, e)

def process_pr_folder(pr_number: str) -> None:
    """
    Process all run folders within a PR folder.

    Args:
        pr_number: The PR number to process.
    """
    pr_folder_path = os.path.join(CUCUMBER_REPORTS_PARENT_DIR, f"PR{pr_number}")
    logger.info("Processing PR #%s", pr_number)

    run_folders = get_run_folders(pr_folder_path)
    if not run_folders:
        logger.warning("No run folders found for PR #%s", pr_number)
        return

    logger.info("Found %d run folders for PR #%s", len(run_folders), pr_number)

    for run_folder in run_folders:
        run_folder_path = os.path.join(pr_folder_path, run_folder)
        try:
            run_results = extract_run_results(run_folder_path)
            if run_results:
                save_run_results(run_folder_path, run_results)
                update_run_data_result(run_folder_path, run_results)
            else:
                logger.warning("No feature data extracted from %s", run_folder_path)
                # Clean up any existing run feature results JSON file
                cleanup_file = os.path.join(run_folder_path, RUN_FEATURE_RESULTS_FILENAME)
                if os.path.exists(cleanup_file):
                    try:
                        os.remove(cleanup_file)
                        logger.info(
                            "Cleaned up existing %s from %s",
                            RUN_FEATURE_RESULTS_FILENAME, run_folder_path
                        )
                    except OSError as e:
                        logger.warning(
                            "Failed to clean up %s from %s: %s",
                            RUN_FEATURE_RESULTS_FILENAME, run_folder_path, e)
        except Exception as e:
            logger.error("Error processing run folder %s: %s", run_folder_path, e)

def main() -> None:
    """
    Reads the PR features CSV and processes each PR's run folders.

    For each PR in the CSV file:
    - Locates the corresponding folder in PRs-cucumber-reports/
    - Extracts feature results of all runs within the PR folder and saves them to JSON files
    """
    try:
        with open(PR_FEATURES_CSV_FILENAME, newline="", encoding="utf-8") as infile:
            reader = csv.reader(infile)
            # Check if file is empty
            try:
                next(reader)  # Skip header row
            except StopIteration:
                logger.warning("CSV file %s is empty", PR_FEATURES_CSV_FILENAME)
                return

            for row in reader:
                pr_number = row[-1]
                process_pr_folder(pr_number)
    except FileNotFoundError as e:
        logger.critical("Input CSV file %s not found: %s", PR_FEATURES_CSV_FILENAME, e)
    except Exception as e:
        logger.critical("Fatal error: %s", e)
        logger.critical("Traceback: %s", traceback.format_exc())

if __name__ == "__main__":
    main()
