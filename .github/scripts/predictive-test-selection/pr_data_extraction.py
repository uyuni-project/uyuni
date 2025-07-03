#!/usr/bin/env python3
"""
Script: pr_data_extraction.py

Description:
    Extracts data from the latest N pull requests in the Uyuni repository
    that triggered a GitHub Actions test workflow and generated Cucumber reports.

    For each such PR:
    - Downloads all Cucumber JSON reports from the initial test run.
    - Logs the files that were modified in the PR at the time it was opened.

Requirements:
    - Environment variable GITHUB_TOKEN must be set with a GitHub Access Token.

Usage:
    python pr_data_extraction.py <N>
    Where <N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from.
"""
import os
import sys
import logging
import datetime
import csv

import requests
from github import Github

from config import REPO_FULL_NAME, TEST_WORKFLOW_NAME, PR_FEATURES_CSV_FILENAME

# ------------------------------
# Constants
# ------------------------------

# Time windows (in days) to track recent change frequency of PR modified files
RECENT_DAYS = [3, 14, 56]

def setup_logging(level, log_file="script.log"):
    """
    Initializes and configures logging for the script, returning a logger at the given level.
    Logs will be written to both the console and a file.
    
    Args:
        level: Logging level (e.g., logging.DEBUG, logging.INFO).
        log_file: Path to the file where logs will be written.
    
    Returns:
        A configured logger instance.
    """

    if level == logging.DEBUG:
        fmt = "%(levelname)s - %(filename)s:%(lineno)d - %(message)s"
    else:
        fmt = "%(levelname)s - %(message)s"

    formatter = logging.Formatter(fmt)

    # Get the logger
    my_logger = logging.getLogger(__name__)
    my_logger.setLevel(level)

    # Avoid adding duplicate handlers if setup_logging is called multiple times
    if not my_logger.handlers:

        # Console handler
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(formatter)
        console_handler.setLevel(level)
        my_logger.addHandler(console_handler)

        # File handler
        file_handler = logging.FileHandler(log_file)
        file_handler.setFormatter(formatter)
        file_handler.setLevel(level)
        my_logger.addHandler(file_handler)

    return my_logger

logger = setup_logging(logging.INFO)

def load_github_token():
    """
    Loads the GitHub token from the environment and exits with an error if it's missing.
    """

    github_token = os.getenv("GITHUB_TOKEN")
    if not github_token:
        logger.critical(
            "GITHUB_TOKEN not set in environment.\n"
            "export GITHUB_TOKEN='your_personal_access_token' and rerun the script"
        )
        sys.exit(1)
    return github_token

def get_repository(gh, repo_full_name):
    """
    Retrieves the GitHub repository object using the provided full name, exits if access fails.
    """

    try:
        return gh.get_repo(repo_full_name)
    except Exception as e:
        logger.critical("Failed to access repository %s: %s", repo_full_name, e)
        sys.exit(1)

def get_test_workflow(repo, workflow_name):
    """
    Fetches the GitHub Actions workflow in the repository matching the specified name.
    """

    workflows = repo.get_workflows()
    test_workflow = next((wf for wf in workflows if wf.name == workflow_name), None)
    if not test_workflow:
        logger.critical("Workflow %s not found.", workflow_name)
        sys.exit(1)
    return test_workflow

def get_candidate_prs(repo, n):
    """
    Returns n recently created PRs, oversampling to account for filtering out irrelevant ones.
    """

    multiplier = 4  # To oversample in case some PRs don't have Cucumber reports
    prs = list(repo.get_pulls(state="all", sort="created", direction="desc")[:n*multiplier])
    if len(prs) < n:
        logger.warning("Only %d PRs found, but %d requested.", len(prs), n)
    return prs

def get_cucumber_initial_test_run(test_workflow, pr):
    """
    Finds the first completed test run during the PR's lifetime that includes Cucumber reports.
    """

    # Gets all test workflow runs that occured on the current PR's branch.
    test_runs_on_pr_branch = test_workflow.get_runs(
        branch=pr.head.ref,
        event="pull_request",
        status="completed",
        exclude_pull_requests=True
    )

    pr_opened_at = pr.created_at
    pr_closed_at = pr.closed_at or datetime.datetime.max.replace(tzinfo=pr.created_at.tzinfo)

    # Filter runs that are:
    # - within the PR lifetime (Addresses edge case of multiple PRs from one branch)
    # - completed (success or failure, not cancelled/skipped)
    pr_test_runs = [
        run for run in test_runs_on_pr_branch
        if pr_opened_at <= run.created_at <= pr_closed_at
        and run.conclusion in ("success", "failure")
    ]

    if not pr_test_runs:
        logger.info("No %s runs found for PR", TEST_WORKFLOW_NAME)
        return None

    for run in pr_test_runs:
        logger.debug("Obtained run #%s created at %s", run.run_number, run.created_at)

    pr_test_runs.sort(key=lambda run: run.created_at)

    # Return initial test run *that has Cucumber reports*
    for run in pr_test_runs:
        run_number = run.run_number
        logger.debug("Checking if run #%s has Cucumber reports", run_number)
        for artifact in run.get_artifacts():
            if artifact.name.lower().startswith("cucumber"):
                logger.info("Using initial test run #%d created at %s", run_number, run.created_at)
                return run

    logger.info("No %s runs with Cucumber reports found for PR #%d", TEST_WORKFLOW_NAME, pr.number)
    return None

def download_cucumber_artifacts(run_with_cucumber, pr_number, headers):
    """
    Downloads all Cucumber artifacts from the given workflow run and saves them as ZIP files.
    """

    for artifact in run_with_cucumber.get_artifacts():
        if artifact.name.lower().startswith("cucumber"):
            url = artifact.archive_download_url
            try:
                response = requests.get(url, headers=headers, stream=True, timeout=30)
                response.raise_for_status()  # Raise HTTP Error for bad codes
            except requests.RequestException as e:
                logger.warning("Failed to download %s: %s", artifact.name, e)
                continue

            filename = f"pr{pr_number}_run{run_with_cucumber.id}_{artifact.name}.zip"
            try:
                with open(filename, "wb") as f:
                    f.write(response.content)
                logger.info("Downloaded: %s", filename)
            except OSError as e:
                logger.error("Failed to write file %s: %s", filename, e)

def get_modified_files(repo, pr, test_run):
    """
    Returns the list of modified files in the PR that triggered the given test run,
    excluding files that contain '.changes' in their filename.
    """

    # base_commit_sha: the commit the PR is targeting (e.g., main)
    # test_run_commit_sha: the commit the test workflow ran on
    # note: this may not be the first test run, it's the earliest one with Cucumber reports
    base_commit_sha = pr.base.sha
    test_run_commit_sha = test_run.head_sha

    # Compare the base commit and the test run commit to get the file changes
    # that were present when this test run was triggered.
    comparison = repo.compare(base_commit_sha, test_run_commit_sha)
    return [f.filename for f in comparison.files if ".changes" not in f.filename]

def get_files_change_history(repo, file_list, recent_days):
    """
    For each N in recent_days,
    returns the total number of times the files in file_list were modified in the last N days.

    Args:
        repo: GitHub Repository object.
        file_list: List of modified file paths.
        recent_days: List of integers (e.g., [3, 14, 56]) representing "last N days".

    Returns:
        Dict mapping each number of days to total number of changes across all files.
    """
    now = datetime.datetime.now(datetime.timezone.utc)
    earliest_date = now - datetime.timedelta(days=max(recent_days))
    day_thresholds = {days: now - datetime.timedelta(days=days) for days in recent_days}
    change_history = {days: 0 for days in recent_days}

    for file_path in file_list:
        logger.debug("Processing change history of file %s", file_path)
        try:
            commits = repo.get_commits(path=file_path, since=earliest_date)
            logger.debug("Edited in %s commits since %s days", len(list(commits)), max(recent_days))
            for commit in commits:
                logger.debug("File edited in commit %s", commit.sha)
                commit_date = commit.commit.committer.date
                for days, threshold_date in day_thresholds.items():
                    if commit_date >= threshold_date:
                        change_history[days] += 1
                        logger.debug("Incremented modifications in the last %d days", days)
        except Exception as e:
            logger.warning("Could not retrieve commits for file %s: %s", file_path, e)

    return change_history

def get_file_extensions(modified_files):
    """
    Returns a list of unique file extensions (without the dot) 
    from the given list of modified file paths. Files without an 
    extension are excluded.

    Args:
        modified_files (List[str]): List of modified file paths.

    Returns:
        List[str]: Unique file extensions found in the list.
    """

    return list({
        file.rsplit('.', 1)[-1]
        for file in modified_files
        if '.' in file.split('/')[-1]
    })

def write_pr_features_to_csv(csv_writer, file_extensions, change_history, pr_number):
    """
    Writes a single PR's features to the CSV file.

    Args:
        csv_writer (csv.writer): CSV writer object.
        file_extensions (List[str]): List of file extensions in the PR.
        change_history (Dict[int, int]): Mapping of recent_day → change count (ordered).
        pr_number (int): The PR number.
    """
    try:
        extensions_str = ",".join(file_extensions)
        change_counts = list(change_history.values())  # Assumes correct and consistent order
        row = [extensions_str] + change_counts + [pr_number]
        csv_writer.writerow(row)
    except Exception as e:
        logger.error("Error writing PR #%d features to CSV: %s", pr_number, e)

def setup_output_csv(recent_days):
    """
    Initializes the CSV output file and returns the file handle and CSV writer.
    """
    try:
        csv_file = open(PR_FEATURES_CSV_FILENAME, mode="w", newline="", encoding="utf-8")
        csv_writer = csv.writer(csv_file)
        # PR number wont be used during training AI models,
        # it only helps later scripts identify the corresponding PR for each row.
        header = (
            ["file_extensions"]
            + [f"changes_last_{d}_days" for d in recent_days]
            + ["pr_number"]
        )
        csv_writer.writerow(header)
        return csv_file, csv_writer
    except Exception as e:
        logger.critical("Failed to initialize PR features CSV file: %s", e)
        sys.exit(1)

def extract_pr_data(repo_full_name, n, github_token, csv_writer):
    """
    Coordinates the full PR data extraction process: 
    getting PRs, test runs, modified files, and downloading Cucumber artifacts.
    """

    gh = Github(github_token)
    headers = {
        "Authorization": f"Bearer {github_token}",
        "Accept": "application/vnd.github+json"
    }

    repo = get_repository(gh, repo_full_name)
    test_workflow = get_test_workflow(repo, TEST_WORKFLOW_NAME)
    prs = get_candidate_prs(repo, n)

    processed_prs_with_cucumber_reports = 0
    for pr in prs:
        if processed_prs_with_cucumber_reports >= n:
            break

        pr_num = pr.number

        logger.info("Processing PR #%d: %s, created at %s", pr_num, pr.title, pr.created_at)

        try:
            initial_test_run_with_cucumber = get_cucumber_initial_test_run(test_workflow, pr)
            if not initial_test_run_with_cucumber:
                continue
        except Exception as e:
            logger.error("Error getting initial test run for PR #%d: %s", pr_num, e)

        try:
            download_cucumber_artifacts(initial_test_run_with_cucumber, pr_num, headers)
        except Exception as e:
            logger.error("Error downloading cucumber artifacts for PR #%d: %s", pr_num, e)

        try:
            modified_files = get_modified_files(repo, pr, initial_test_run_with_cucumber)
            logger.info("Files modified in PR that triggered test run: %s", modified_files)
        except Exception as e:
            logger.error("Error obtaining modified files for PR #%d: %s", pr_num, e)

        try:
            file_extensions = get_file_extensions(modified_files)
            logger.info("File extensions of files modified in PR: %s", file_extensions)
        except Exception as e:
            logger.error("Error obtaining file extensions for PR #%d: %s", pr_num, e)

        try:
            change_history = get_files_change_history(repo, modified_files, RECENT_DAYS)
            logger.info("Files Change History: %s", change_history)
        except Exception as e:
            logger.error("Error getting modified files change history for PR #%d: %s", pr_num, e)

        write_pr_features_to_csv(csv_writer, file_extensions, change_history, pr_num)

        processed_prs_with_cucumber_reports += 1

def main():
    """
    Parses CLI arguments, loads credentials, sets up the CSV output file,
    and starts the extraction of PR data.
    """

    if len(sys.argv) != 2:
        logger.critical(
            "Usage: python pr_data_extraction.py <N>\n"
            "<N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from."
        )
        sys.exit(1)

    try:
        n = int(sys.argv[1])
    except ValueError:
        logger.critical(
            "Argument <N> must be an integer.\n"
            "<N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from."
        )
        sys.exit(1)

    github_token = load_github_token()
    csv_file, csv_writer = setup_output_csv(RECENT_DAYS)
    try:
        extract_pr_data(REPO_FULL_NAME, n, github_token, csv_writer)
    finally:
        csv_file.close()

if __name__ == "__main__":
    main()
