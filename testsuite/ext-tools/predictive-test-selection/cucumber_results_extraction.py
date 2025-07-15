#!/usr/bin/env python3
"""
Extracts features' results from Cucumber JSON reports for each PR listed in pr_features.csv.

For each row in pr_features.csv:
- Extracts the PR number.
- Processes all Cucumber JSON reports in the corresponding PR folder.
- For each feature in each JSON file, determines if any step failed (Fail) or all passed (Pass).
- Outputs a new CSV with the original row fields (except PR number), in addition,
for each feature/test than ran on the PR, we add the feature name, scenario count, Pass/Fail result.
"""
import os
import csv
import json
import logging

from config import (
    CUCUMBER_REPORTS_PARENT_DIR,
    PR_FEATURES_CSV_FILENAME,
    CUCUMBER_RESULTS_CSV_FILENAME,
    DEBUG_MODE,
)
from utilities import setup_logging

logger = setup_logging(logging.INFO, log_file="cucumber_results_extraction.log")

def get_feature_name_from_uri(uri):
    """
    Extract the feature name from a Cucumber feature URI.

    Args:
        uri (str): URI field from the Cucumber JSON (e.g., 'features/secondary/srv_users.feature').

    Returns:
        str: The feature name (e.g., 'srv_users').
    """
    base = os.path.basename(uri)
    if base.endswith(".feature"):
        base = base[:-len(".feature")]
    return base

def get_feature_result(feature):
    """
    Determine the result of a feature based on its scenarios' steps and all hooks.

    Args:
        feature (dict): A feature object from the Cucumber JSON report.

    Returns:
        str: 'Fail' if any step or hook failed, 'Skipped' if any were skipped (and none failed),
             otherwise 'Pass'.
    """
    def get_entry_result(entry):
        """
        Determine the result of a step or hook, including nested hooks.

        Args:
            entry (dict): A step or hook object from the Cucumber JSON.
        """
        result_value = entry.get("result", {}).get("status")
        if result_value == "failed":
            return "Fail"
        found_skipped = result_value == "skipped"
        # Check for nested 'after' and 'before' arrays inside a step or hook
        for nested_section in ("after", "before"):
            for nested_entry in entry.get(nested_section, []):
                nested_result = nested_entry.get("result", {}).get("status")
                if nested_result == "failed":
                    return "Fail"
                if nested_result == "skipped":
                    found_skipped = True
        if found_skipped:
            return "Skipped"
        return "Pass"

    found_skipped = False
    for scenario in feature.get("elements", []):
        for section in ("steps", "before", "after"):
            for entry in scenario.get(section, []):
                result = get_entry_result(entry)
                if result == "Fail":
                    return "Fail"
                if result == "Skipped":
                    found_skipped = True
    if found_skipped:
        return "Skipped"
    return "Pass"

def extract_results_and_scenario_counts(cucumber_folder_path):
    """
    Extract feature results and scenario counts from all Cucumber JSON reports in the given folder.

    Args:
        cucumber_folder_path (str): Path to the folder containing Cucumber JSON reports.

    Returns:
        list of (feature_name, result, scenario_count) tuples for all features in all JSON files.
    """
    if not os.path.isdir(cucumber_folder_path):
        logger.warning("Folder not found: %s", cucumber_folder_path)
        return []
    results = []
    found_json = False
    for fname in os.listdir(cucumber_folder_path):
        if fname.endswith(".json"):
            found_json = True
            fpath = os.path.join(cucumber_folder_path, fname)
            try:
                with open(fpath, "r", encoding="utf-8") as f:
                    data = json.load(f)
                for feature in data:
                    feature_name = get_feature_name_from_uri(feature.get("uri", ""))
                    result = get_feature_result(feature)
                    scenario_count = len(feature.get("elements", []))
                    results.append((feature_name, result, scenario_count))
            except json.JSONDecodeError as e:
                logger.error("Failed to parse JSON file %s: %s", fpath, e)
            except Exception as e:
                logger.error("Unexpected error reading %s: %s", fpath, e)
    if not found_json:
        logger.warning("No JSON files found in %s", cucumber_folder_path)
    return results

def main():
    """
    Reads pr_features.csv, processes each PR's Cucumber reports, and writes the output CSV.

    For each PR in pr_features.csv:
    - Extracts the PR number.
    - Processes all Cucumber JSON reports in the corresponding PR folder.
    - For each feature in each Cucumber report, determines the feature result and scenario count.
    - Outputs a new CSV with the original row fields (except PR number), 
    feature name, result, and scenario count.
    """
    try:
        with open(PR_FEATURES_CSV_FILENAME, newline="", encoding="utf-8") as infile, \
             open(CUCUMBER_RESULTS_CSV_FILENAME, "w", newline="", encoding="utf-8") as outfile:
            reader = csv.reader(infile)
            writer = csv.writer(outfile)
            header = next(reader)
            # Remove PR number column (assume it's last) unless debugging
            if DEBUG_MODE:
                new_header = header + ["feature_name", "scenario_count", "result"]
            else:
                new_header = header[:-1] + ["feature_name", "scenario_count", "result"]
            writer.writerow(new_header)
            for row in reader:
                pr_number = row[-1]
                if DEBUG_MODE:
                    base_fields = row
                else:
                    base_fields = row[:-1]
                logger.info("Processing PR #%s", pr_number)
                try:
                    pr_reports_folder = os.path.join(CUCUMBER_REPORTS_PARENT_DIR, f"PR{pr_number}")
                    test_results = extract_results_and_scenario_counts(pr_reports_folder)
                    if not test_results:
                        logger.error("No test results found for PR #%s", pr_number)
                    for feature_name, result, scenario_count in test_results:
                        if result != "Skipped":
                            writer.writerow(base_fields + [feature_name, scenario_count, result])
                        else:
                            logger.debug("Feature %s is skipped on PR %s", feature_name, pr_number)
                except Exception as e:
                    logger.error("Error processing PR #%s: %s", pr_number, e)
    except FileNotFoundError as e:
        logger.critical("Input CSV file not found: %s", e)
    except Exception as e:
        logger.critical("Fatal error: %s", e)

if __name__ == "__main__":
    main()
