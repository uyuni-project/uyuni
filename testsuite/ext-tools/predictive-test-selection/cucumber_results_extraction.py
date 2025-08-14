#!/usr/bin/env python3
"""
Extracts tests/features' results from the Cucumber JSON reports for each PR listed in the CSV file
produced by the PR data extraction script, and outputs a new CSV file with the
feature name, feature category, scenario counts, and pass/fail result added per feature per PR.

Usage:
    python cucumber_results_extraction.py

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
    CUCUMBER_FAILED,
    CUCUMBER_PASSED,
    CUCUMBER_SKIPPED,
    CUCUMBER_RESULTS_COLUMNS,
)
from utilities import setup_logging

logger = setup_logging(logging.DEBUG, log_file="logs/cucumber_results_extraction.log")

def get_feature_name_from_uri(uri: str) -> str:
    """
    Extract the feature name from a Cucumber feature URI.

    Args:
        uri: URI field from the Cucumber JSON (e.g., 'features/secondary/srv_users.feature').

    Returns:
        The feature name (e.g., 'srv_users').
    """
    base = os.path.basename(uri)
    if base.endswith(".feature"):
        base = base[:-len(".feature")]
    return base

def get_feature_category(feature_name: str) -> str:
    """
    Extract the feature category from the feature name using CUCUMBER_FEATURE_CATEGORIES.
    The first matching category (prefix) is returned, or 'uncategorized' if none match.

    Args:
        feature_name: The name of the feature (e.g., 'srv_users').

    Returns:
        The matched category (e.g., srv) or 'uncategorized'.
    """
    for category in CUCUMBER_FEATURE_CATEGORIES:
        if feature_name.startswith(category):
            return category
    return "uncategorized"

def get_entry_result(entry: dict) -> str:
    """
    Determine the result of a step or hook, including nested hooks.

    Args:
        entry: A step or hook object from the Cucumber JSON.

    Returns:
        CUCUMBER_FAILED if the entry or any nested hook failed,
        CUCUMBER_SKIPPED if any were skipped (and none failed), otherwise CUCUMBER_PASSED.
    """
    result_value = entry.get("result", {}).get("status")
    if result_value == CUCUMBER_FAILED:
        return CUCUMBER_FAILED
    found_skipped = result_value == CUCUMBER_SKIPPED
    # Check for nested 'after' and 'before' arrays inside a step or hook
    for nested_section in ("after", "before"):
        for nested_entry in entry.get(nested_section, []):
            nested_result = nested_entry.get("result", {}).get("status")
            if nested_result == CUCUMBER_FAILED:
                return CUCUMBER_FAILED
            if nested_result == CUCUMBER_SKIPPED:
                found_skipped = True
    if found_skipped:
        return CUCUMBER_SKIPPED
    return CUCUMBER_PASSED

def get_scenario_result(scenario: dict, stats: dict | None = None) -> str:
    """
    Determine scenario or background result. Updates stats (if provided) only for real scenarios.

    Args:
        scenario: A scenario or background object from the Cucumber JSON report.
        stats (optional): Dict to accumulate step and scenario counts by status.

    Returns:
        CUCUMBER_FAILED if any step or hook failed,
        CUCUMBER_SKIPPED if any were skipped (and none failed), otherwise CUCUMBER_PASSED.
    """
    scenario_failed = False
    scenario_skipped = False
    for section in ("before", "after"):
        for entry in scenario.get(section, []):
            entry_result = get_entry_result(entry)
            if entry_result == CUCUMBER_FAILED:
                scenario_failed = True
            elif entry_result == CUCUMBER_SKIPPED:
                scenario_skipped = True
    for step in scenario.get("steps", []):
        step_result = get_entry_result(step)
        if stats is not None:
            stats["steps"][step_result] += 1
        if step_result == CUCUMBER_FAILED:
            scenario_failed = True
        elif step_result == CUCUMBER_SKIPPED:
            scenario_skipped = True
    if scenario_failed:
        result = CUCUMBER_FAILED
    elif scenario_skipped:
        result = CUCUMBER_SKIPPED
    else:
        result = CUCUMBER_PASSED
    # Only count scenario-level stats if it's a real scenario, not a background.
    if stats is not None and scenario.get("type") == "scenario":
        stats["scenarios"][result] += 1
    return result

def get_feature_result(feature: dict, stats: dict | None = None) -> str:
    """
    Determine the result of a feature based on its scenarios' results.
    Optionally, update stats dict with step and scenario counts by status.

    Args:
        feature: A feature object from the Cucumber JSON report.
        stats (optional): Dict to accumulate step and scenario counts by status.

    Returns:
        CUCUMBER_FAILED if any scenario failed,
        CUCUMBER_PASSED if any scenario passed (and none failed), otherwise CUCUMBER_SKIPPED.
    """
    scenario_passed = False
    scenario_failed = False
    for scenario in feature.get("elements", []):
        scenario_result = get_scenario_result(scenario, stats)
        if scenario_result == CUCUMBER_FAILED:
            scenario_failed = True
        elif scenario_result == CUCUMBER_PASSED:
            scenario_passed = True
    if scenario_failed:
        return CUCUMBER_FAILED
    if scenario_passed:
        return CUCUMBER_PASSED
    return CUCUMBER_SKIPPED

def make_cucumber_stats_dict() -> dict:
    """Return a new stats dictionary for cucumber steps, scenarios, and features."""
    return {
        "steps": {CUCUMBER_FAILED: 0, CUCUMBER_SKIPPED: 0, CUCUMBER_PASSED: 0},
        "scenarios": {CUCUMBER_FAILED: 0, CUCUMBER_SKIPPED: 0, CUCUMBER_PASSED: 0},
        "features": {CUCUMBER_FAILED: 0, CUCUMBER_SKIPPED: 0, CUCUMBER_PASSED: 0}
    }

def log_pr_cucumber_stats(pr_stats: dict) -> None:
    """Log summary stats for features, scenarios, and steps."""
    total_features = sum(pr_stats["features"].values())
    total_scenarios = sum(pr_stats["scenarios"].values())
    total_steps = sum(pr_stats["steps"].values())
    logger.debug(
        "%d features (%d failed, %d skipped, %d passed)", total_features,
        pr_stats["features"][CUCUMBER_FAILED], pr_stats["features"][CUCUMBER_SKIPPED],
        pr_stats["features"][CUCUMBER_PASSED]
    )
    logger.debug(
        "%d scenarios (%d failed, %d skipped, %d passed)", total_scenarios, 
        pr_stats["scenarios"][CUCUMBER_FAILED], pr_stats["scenarios"][CUCUMBER_SKIPPED],
        pr_stats["scenarios"][CUCUMBER_PASSED]
    )
    logger.debug(
        "%d steps (%d failed, %d skipped, %d passed)", total_steps, 
        pr_stats["steps"][CUCUMBER_FAILED], pr_stats["steps"][CUCUMBER_SKIPPED],
        pr_stats["steps"][CUCUMBER_PASSED]
    )

def extract_results_and_scenario_counts(
    cucumber_folder_path: str, log_stats: bool = True
) -> list[tuple[str, int, str]]:
    """
    Extract features' results and scenario counts from Cucumber JSON reports in the given folder.
    Optionally aggregate and log step/scenario/features counts by status for the PR.

    Args:
        cucumber_folder_path: Path to the folder containing Cucumber JSON reports.
        log_stats (optional): Whether to log the step/scenario/features stats.

    Returns:
        List of (feature_name, scenario_count, result) tuples for all features in all JSON files.
    """
    if not os.path.isdir(cucumber_folder_path):
        logger.warning("Folder not found: %s", cucumber_folder_path)
        return []
    results: list[tuple[str, int, str]] = []
    found_json = False
    pr_stats = make_cucumber_stats_dict()
    for report in os.listdir(cucumber_folder_path):
        if report.endswith(".json") and report.startswith("output_"):
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
                    for status in (CUCUMBER_FAILED, CUCUMBER_SKIPPED, CUCUMBER_PASSED):
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

def main() -> None:
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
            # If not debugging, remove PR number column (assume it's last)
            if DEBUG_MODE:
                new_header = header + CUCUMBER_RESULTS_COLUMNS
            else:
                new_header = header[:-1] + CUCUMBER_RESULTS_COLUMNS
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
                        if result != CUCUMBER_SKIPPED:
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
