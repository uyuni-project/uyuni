#!/usr/bin/env python3
"""
Extracts tests/features' results from the Cucumber JSON reports for each PR listed in the CSV file
produced by the PR data extraction script, and outputs a new CSV file with the
feature name, feature category, scenario counts, and pass/fail result added per feature per PR.

- Run the PR data extraction script first, this script depends on its output and should not be used
independently.
- The extract_results_and_scenario_counts function is reusable and can help track
historical test failure rates in the workflow we plan to implement.
- Cucumber statistics are logged for verification and debugging purposes.
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
    CUCUMBER_FEATURE_CATEGORIES,
)
from utilities import setup_logging

logger = setup_logging(logging.DEBUG, log_file="cucumber_results_extraction.log")

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

def get_feature_category(feature_name):
    """
    Extract the feature category from the feature name using CUCUMBER_FEATURE_CATEGORIES.
    The first matching category (prefix) is returned, or 'uncategorized' if none match.

    Args:
        feature_name (str): The name of the feature (e.g., 'srv_users').

    Returns:
        str: The matched category (e.g., srv) or 'uncategorized'.
    """
    for category in CUCUMBER_FEATURE_CATEGORIES:
        if feature_name.startswith(category):
            return category
    return "uncategorized"

def get_entry_result(entry):
    """
    Determine the result of a step or hook, including nested hooks.

    Args:
        entry (dict): A step or hook object from the Cucumber JSON.

    Returns:
        str: 'failed' if the entry or any nested hook failed, 
        'skipped' if any were skipped (and none failed), otherwise 'passed'.
    """
    result_value = entry.get("result", {}).get("status")
    if result_value == "failed":
        return "failed"
    found_skipped = result_value == "skipped"
    # Check for nested 'after' and 'before' arrays inside a step or hook
    for nested_section in ("after", "before"):
        for nested_entry in entry.get(nested_section, []):
            nested_result = nested_entry.get("result", {}).get("status")
            if nested_result == "failed":
                return "failed"
            if nested_result == "skipped":
                found_skipped = True
    if found_skipped:
        return "skipped"
    return "passed"

def get_scenario_result(scenario, stats=None):
    """
    Determine scenario or background result. Updates stats (if provided) only for real scenarios.

    Args:
        scenario (dict): A scenario or background object from the Cucumber JSON report.
        stats (dict, optional): Dict to accumulate step and scenario counts by status.

    Returns:
        str: 'failed' if any step or hook failed, 'skipped' if any were skipped (and none failed),
            otherwise 'passed'.
    """
    scenario_failed = False
    scenario_skipped = False
    for section in ("before", "after"):
        for entry in scenario.get(section, []):
            entry_result = get_entry_result(entry)
            if entry_result == "failed":
                scenario_failed = True
            elif entry_result == "skipped":
                scenario_skipped = True
    for step in scenario.get("steps", []):
        step_result = get_entry_result(step)
        if stats is not None:
            stats["steps"][step_result] += 1
        if step_result == "failed":
            scenario_failed = True
        elif step_result == "skipped":
            scenario_skipped = True
    if scenario_failed:
        result = "failed"
    elif scenario_skipped:
        result = "skipped"
    else:
        result = "passed"
    # Only count scenario-level stats if it's a real scenario, not a background.
    if stats is not None and scenario.get("type") == "scenario":
        stats["scenarios"][result] += 1
    return result

def get_feature_result(feature, stats=None):
    """
    Determine the result of a feature based on its scenarios' results.
    Optionally, update stats dict with step and scenario counts by status.

    Args:
        feature (dict): A feature object from the Cucumber JSON report.
        stats (dict, optional): Dict to accumulate step and scenario counts by status.

    Returns:
        str: 'failed' if any scenario failed, 'skipped' if any were skipped (and none failed),
             otherwise 'passed'.
    """
    scenario_skipped = False
    scenario_failed = False
    for scenario in feature.get("elements", []):
        scenario_result = get_scenario_result(scenario, stats)
        if scenario_result == "failed":
            scenario_failed = True
        elif scenario_result == "skipped":
            scenario_skipped = True
    if scenario_failed:
        return "failed"
    if scenario_skipped:
        return "skipped"
    return "passed"

def make_cucumber_stats_dict():
    """Return a new stats dictionary for cucumber steps, scenarios, and features."""
    return {
        "steps": {"failed": 0, "skipped": 0, "passed": 0},
        "scenarios": {"failed": 0, "skipped": 0, "passed": 0},
        "features": {"failed": 0, "skipped": 0, "passed": 0}
    }

def log_pr_cucumber_stats(pr_stats):
    """Log summary stats for features, scenarios, and steps."""
    total_features = sum(pr_stats["features"].values())
    total_scenarios = sum(pr_stats["scenarios"].values())
    total_steps = sum(pr_stats["steps"].values())
    logger.debug(
        "%d features (%d failed, %d skipped, %d passed)", total_features,
        pr_stats["features"]["failed"], pr_stats["features"]["skipped"],
        pr_stats["features"]["passed"]
    )
    logger.debug(
        "%d scenarios (%d failed, %d skipped, %d passed)", total_scenarios, 
        pr_stats["scenarios"]["failed"], pr_stats["scenarios"]["skipped"],
        pr_stats["scenarios"]["passed"]
    )
    logger.debug(
        "%d steps (%d failed, %d skipped, %d passed)", total_steps, 
        pr_stats["steps"]["failed"], pr_stats["steps"]["skipped"], pr_stats["steps"]["passed"]
    )

def extract_results_and_scenario_counts(cucumber_folder_path, log_stats=True):
    """
    Extract features' results and scenario counts from Cucumber JSON reports in the given folder.
    Optionally aggregate and log step/scenario/features counts by status for the PR.

    Args:
        cucumber_folder_path (str): Path to the folder containing Cucumber JSON reports.
        log_stats (bool, optional): Whether to log the step/scenario/features stats.

    Returns:
        list of (feature_name, scenario_count, result) tuples for all features in all JSON files.
    """
    if not os.path.isdir(cucumber_folder_path):
        logger.warning("Folder not found: %s", cucumber_folder_path)
        return []
    results = []
    found_json = False
    pr_stats = make_cucumber_stats_dict()
    for report in os.listdir(cucumber_folder_path):
        if report.endswith(".json"):
            found_json = True
            report_path = os.path.join(cucumber_folder_path, report)
            try:
                with open(report_path, "r", encoding="utf-8") as f:
                    cucumber_report = json.load(f)
                for feature in cucumber_report:
                    feature_name = get_feature_name_from_uri(feature.get("uri", ""))
                    feature_stats = make_cucumber_stats_dict()
                    result = get_feature_result(feature, stats=feature_stats)
                    scenario_count = sum(feature_stats["scenarios"].values())
                    # Aggregate feature_stats into pr_stats
                    for status in ("failed", "skipped", "passed"):
                        pr_stats["steps"][status] += feature_stats["steps"][status]
                        pr_stats["scenarios"][status] += feature_stats["scenarios"][status]
                    pr_stats["features"][result] += 1
                    results.append((feature_name, scenario_count, result))
            except json.JSONDecodeError as e:
                logger.error("Failed to parse JSON file %s: %s", report_path, e)
            except Exception as e:
                logger.error("Unexpected error reading %s: %s", report_path, e)
    if not found_json:
        logger.warning("No JSON files found in %s", cucumber_folder_path)
    if log_stats:
        log_pr_cucumber_stats(pr_stats)
    return results

def main():
    """
    Reads PR features CSV, for each PR in the CSV file:
    - Processes all Cucumber JSON reports in the corresponding PR folder.
    - For each feature in each Cucumber report,
      determines the feature category, scenario count, and result.
    - Outputs a new CSV with original row fields (PR number based on DEBUG_MODE), in addition to:
      feature name, feature category, scenario count, and result (pass/fail), skipped is ignored.
    """
    try:
        with open(PR_FEATURES_CSV_FILENAME, newline="", encoding="utf-8") as infile, \
             open(CUCUMBER_RESULTS_CSV_FILENAME, "w", newline="", encoding="utf-8") as outfile:
            reader = csv.reader(infile)
            writer = csv.writer(outfile)
            header = next(reader)
            new_columns = ["feature", "feature_category", "scenario_count", "result"]
            # If not debugging, remove PR number column (assume it's last)
            if DEBUG_MODE:
                new_header = header + new_columns
            else:
                new_header = header[:-1] + new_columns
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
                    for feature_name, scenario_count, result in test_results:
                        if result != "skipped":
                            feature_category = get_feature_category(feature_name)
                            new_fields = [feature_name, feature_category, scenario_count, result]
                            writer.writerow(base_fields + new_fields)
                except Exception as e:
                    logger.error("Error processing PR #%s: %s", pr_number, e)
    except FileNotFoundError as e:
        logger.critical("Input CSV file %s not found: %s", PR_FEATURES_CSV_FILENAME, e)
    except Exception as e:
        logger.critical("Fatal error: %s", e)

if __name__ == "__main__":
    main()
