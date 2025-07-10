#!/usr/bin/env python3
"""
pr_data_extraction.py

Extracts data from the latest N pull requests (PRs) in the Uyuni repository that triggered 
a GitHub Actions test workflow and generated Cucumber reports.

For each PR (training mode):
- Finds the first completed test run during the PR's lifetime that includes Cucumber reports 
(if none exist, the PR is skipped).
- Downloads all Cucumber JSON reports from this initial test run.
- Extracts and retains only secondary/recommended Cucumber test reports.
- Identifies the files modified that triggered this test run.
- Extracts the unique file extensions of the modified files.
- Retrieves the change history for those files over several recent time windows.
- Outputs a CSV file containing PR features: file extensions, change history, and PR number.
- If the script encounters a configurable number of consecutive PRs without Cucumber reports,
it will stop early and log a warning. This prevents unnecessary API calls when it is unlikely
that more qualifying PRs exist among the candidates.

In prediction mode (single PR), only extracts modified files, their unique file extensions,
and change history features for the PR, Cucumber reports are not downloaded or processed.

Requirements:
- Environment variable GITHUB_TOKEN must be set with a GitHub Access Token.

Usage:
    python pr_data_extraction.py <N>
    python pr_data_extraction.py '#<PR_NUMBER>'
    Where <N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from,
    or '#<PR_NUMBER>' is a specific PR number to extract features for.
"""
import os
import sys
import csv
import logging
import zipfile
import datetime

import requests
from github import Github

from config import (
    REPO_FULL_NAME,
    TEST_WORKFLOW_NAME,
    PR_FEATURES_CSV_FILENAME,
    RECENT_DAYS,
    PR_OVERSAMPLE_MULTIPLIER,
    MAX_CONSECUTIVE_PRS_WITHOUT_CUCUMBER_REPORTS,
)

def setup_logging(level, log_file="script.log"):
    """
    Set up logging to both console and file.

    Args:
        level (int): Logging level (e.g., logging.DEBUG, logging.INFO).
        log_file (str): Path to the file where logs will be written.

    Returns:
        logging.Logger: Configured logger instance.
    """
    if level == logging.DEBUG:
        fmt = "%(levelname)s - %(filename)s:%(lineno)d - %(message)s"
    else:
        fmt = "%(levelname)s - %(message)s"
    formatter = logging.Formatter(fmt)
    my_logger = logging.getLogger(__name__)
    my_logger.setLevel(level)
    if not my_logger.handlers:
        console_handler = logging.StreamHandler()
        console_handler.setFormatter(formatter)
        console_handler.setLevel(level)
        my_logger.addHandler(console_handler)

        file_handler = logging.FileHandler(log_file)
        file_handler.setFormatter(formatter)
        file_handler.setLevel(level)
        my_logger.addHandler(file_handler)
    return my_logger

logger = setup_logging(logging.INFO)

def load_github_token():
    """
    Load the GitHub token from the environment.

    Returns:
        str: GitHub token.

    Exits:
        If GITHUB_TOKEN is not set.
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
    Retrieve the GitHub repository object.

    Args:
        gh (Github): PyGithub Github instance.
        repo_full_name (str): Full repository name.

    Returns:
        github.Repository.Repository: Repository object.

    Exits:
        If repository access fails.
    """
    try:
        return gh.get_repo(repo_full_name)
    except Exception as e:
        logger.critical("Failed to access repository %s: %s", repo_full_name, e)
        sys.exit(1)

def get_test_workflow(repo, workflow_name):
    """
    Fetch the GitHub Actions workflow in the repository matching the specified name.

    Args:
        repo (github.Repository.Repository): Repository object.
        workflow_name (str): Name of the workflow.

    Returns:
        github.Workflow.Workflow: Workflow object.

    Exits:
        If workflow is not found.
    """
    workflows = repo.get_workflows()
    test_workflow = next((wf for wf in workflows if wf.name == workflow_name), None)
    if not test_workflow:
        logger.critical("Workflow %s not found.", workflow_name)
        sys.exit(1)
    return test_workflow

def get_candidate_prs(repo, n):
    """
    Get n recently created PRs, oversampling to account for filtering out irrelevant ones.

    Args:
        repo (github.Repository.Repository): Repository object.
        n (int): Number of PRs to process.

    Returns:
        list: List of PullRequest objects.
    """
    prs = list(
        repo.get_pulls(state="all", sort="created", direction="desc")[:n*PR_OVERSAMPLE_MULTIPLIER]
    )
    if len(prs) < n:
        logger.warning("Only %d PRs found, but %d requested.", len(prs), n)
    return prs

def get_cucumber_initial_test_run(test_workflow, pr):
    """
    Find the first completed test run during the PR's lifetime that includes Cucumber reports.

    Args:
        test_workflow (github.Workflow.Workflow): Test workflow object.
        pr (github.PullRequest.PullRequest): Pull request object.

    Returns:
        github.WorkflowRun.WorkflowRun or None: Initial test run with Cucumber reports, or None.
    """
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
    for run in pr_test_runs:
        run_number = run.run_number
        logger.debug("Checking if run #%s has Cucumber reports", run_number)
        for artifact in run.get_artifacts():
            if artifact.name.lower().startswith("cucumber"):
                logger.info("Using initial test run #%d created at %s", run_number, run.created_at)
                return run

    logger.info("No %s runs with Cucumber reports found for PR #%d", TEST_WORKFLOW_NAME, pr.number)
    return None

def download_secondary_cucumber_reports(run_with_cucumber, pr_number, headers):
    """
    Download all Cucumber artifacts from the given workflow run and save them as ZIP files
    inside a PR-specific folder. Extract and filter secondary/recommended reports.

    Args:
        run_with_cucumber (github.WorkflowRun.WorkflowRun): Workflow run with Cucumber artifacts.
        pr_number (int): PR number.
        headers (dict): HTTP headers for requests.

    Returns:
        bool: True if secondary/recommended reports exist, False otherwise.
    """
    pr_folder = f"PR{pr_number}"
    if os.path.exists(pr_folder):
        logger.info("Folder %s exists, skipping artifact download for PR #%d", pr_folder, pr_number)
        return True

    try:
        os.makedirs(pr_folder, exist_ok=True)
    except Exception as e:
        logger.error("Failed to create folder %s: %s", pr_folder, e)
        return False

    for artifact in run_with_cucumber.get_artifacts():
        if artifact.name.lower().startswith("cucumber"):
            url = artifact.archive_download_url
            zip_filename = os.path.join(pr_folder, f"{artifact.name}.zip")
            try:
                response = requests.get(url, headers=headers, stream=True, timeout=30)
                response.raise_for_status()  # Raise HTTP Error for bad codes
            except requests.RequestException as e:
                logger.warning("Failed to download %s: %s", artifact.name, e)
                continue
            try:
                with open(zip_filename, "wb") as f:
                    f.write(response.content)
                logger.info("Downloaded: %s", zip_filename)
            except OSError as e:
                logger.error("Failed to write file %s: %s", zip_filename, e)
                continue

    return extract_secondary_reports(pr_folder)

def extract_secondary_reports(pr_folder):
    """
    Extract all zip files in the PR folder, then delete all files
    that are not secondary test reports (i.e., files not containing 'secondary'
    or 'recommended' in their name). Also deletes the original zip files.

    Args:
        pr_folder (str): Path to PR folder.

    Returns:
        bool: True if any secondary/recommended files remain, False otherwise.
    """
    if not os.path.exists(pr_folder):
        logger.warning("PR folder %s does not exist for extraction.", pr_folder)
        return False

    extract_all_zip_files(pr_folder)
    has_secondary = delete_non_secondary_reports(pr_folder)
    if not has_secondary:
        try:
            os.rmdir(pr_folder)
            logger.info("Delete folder %s as no secondary/recommended reports exist.", pr_folder)
        except Exception as e:
            logger.warning("Failed to delete empty PR folder %s: %s", pr_folder, e)
    return has_secondary

def extract_all_zip_files(pr_folder):
    """
    Extract all zip files in the given folder and delete them after extraction.

    Args:
        pr_folder (str): Path to PR folder.
    """
    for filename in os.listdir(pr_folder):
        if filename.endswith(".zip"):
            zip_path = os.path.join(pr_folder, filename)
            try:
                with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                    zip_ref.extractall(pr_folder)
                logger.debug("Extracted: %s", zip_path)
            except zipfile.BadZipFile as e:
                logger.error("Failed to extract %s: %s", zip_path, e)
            except Exception as e:
                logger.error("Unexpected error extracting %s: %s", zip_path, e)
            finally:
                try:
                    os.remove(zip_path)
                    logger.debug("Deleted zip file: %s", zip_path)
                except Exception as e:
                    logger.warning("Failed to delete zip file %s: %s", zip_path, e)

def delete_non_secondary_reports(pr_folder):
    """
    Delete all reports not containing 'secondary' or 'recommended' in their name.

    Args:
        pr_folder (str): Path to PR folder.

    Returns:
        bool: True if any secondary/recommended files remain, False otherwise.
    """
    has_secondary = False
    for filename in os.listdir(pr_folder):
        file_path = os.path.join(pr_folder, filename)
        keep = any(word in filename for word in ("secondary", "recommended"))
        if os.path.isfile(file_path) and not keep:
            try:
                os.remove(file_path)
                logger.debug("Deleted non-secondary/recommended cucumber report: %s", file_path)
            except Exception as e:
                logger.warning("Failed to delete cucumber report %s: %s", file_path, e)
        elif os.path.isfile(file_path) and keep:
            has_secondary = True
    return has_secondary

def get_modified_files(repo, pr, test_run):
    """
    Get the list of modified files in the PR that triggered the given test run,
    excluding files that contain '.changes' in their filename.

    Args:
        repo (github.Repository.Repository): Repository object.
        pr (github.PullRequest.PullRequest): Pull request object.
        test_run (github.WorkflowRun.WorkflowRun): Workflow run object.

    Returns:
        list: List of modified file paths.
    """
    # base_commit_sha: the commit the PR is targeting (e.g., main)
    # test_run_commit_sha: the commit the test workflow ran on
    base_commit_sha = pr.base.sha
    test_run_commit_sha = test_run.head_sha

    # Compare the base commit and the test run commit to get the file changes
    # that were present when this test run was triggered.
    comparison = repo.compare(base_commit_sha, test_run_commit_sha)
    return [f.filename for f in comparison.files if ".changes" not in f.filename]

def get_files_change_history(repo, file_list, recent_days):
    """
    For each N in recent_days, return the total number of times the files in file_list
    were modified in the last N days.

    Args:
        repo (github.Repository.Repository): Repository object.
        file_list (list): List of modified file paths.
        recent_days (list): List of integers representing "last N days".

    Returns:
        dict: Mapping each number of days to total number of changes across all files.
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
    Get a list of unique file extensions from the given list of modified file paths.
    Files without an extension are excluded.

    Args:
        modified_files (list): List of modified file paths.

    Returns:
        list: Unique file extensions found in the list.
    """
    return list({
        file.rsplit('.', 1)[-1]
        for file in modified_files
        if '.' in file.split('/')[-1]
    })

def write_pr_features_to_csv(csv_writer, file_extensions, change_history, pr_number):
    """
    Write a single PR's features to the CSV file.

    Args:
        csv_writer (csv.writer): CSV writer object.
        file_extensions (list): List of file extensions in the PR.
        change_history (dict): Mapping of recent_day → change count (ordered).
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
    Initialize the CSV output file and return the file handle and CSV writer.

    Args:
        recent_days (list): List of time windows for change frequency.

    Returns:
        tuple: (csv_file, csv_writer)
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

def extract_pr_data(repo_full_name, n, github_token, csv_writer, single_pr_number=None):
    """
    Coordinate the full PR data extraction process: getting PRs, modified files,
    their change history, test runs, and downloading Cucumber reports.
    If single_pr_number is set, only process that PR and skip downloading cucumber reports.

    The function will stop early if a configurable number of consecutive PRs do not have
    Cucumber reports. This prevents wasting API calls when it is unlikely that more qualifying
    PRs exist among the candidates.

    Args:
        repo_full_name (str): Full repository name.
        n (int): Number of PRs to process.
        github_token (str): GitHub token.
        csv_writer (csv.writer): CSV writer object.
        single_pr_number (int, optional): If set, only process this PR.

    Returns:
        None
    """
    gh = Github(github_token)
    headers = {
        "Authorization": f"Bearer {github_token}",
        "Accept": "application/vnd.github+json"
    }

    repo = get_repository(gh, repo_full_name)
    test_workflow = get_test_workflow(repo, TEST_WORKFLOW_NAME)

    if single_pr_number is not None:
        try:
            pr = repo.get_pull(single_pr_number)
        except Exception as e:
            logger.critical("Could not find PR #%d: %s", single_pr_number, e)
            return
        process_single_pr_mode(repo, pr, csv_writer)
        return

    prs = get_candidate_prs(repo, n)
    processed_prs_with_cucumber_reports = 0
    consecutive_without_cucumber = 0
    for pr in prs:
        if processed_prs_with_cucumber_reports >= n:
            logger.info(
                "Processed the required number of PRs with Cucumber reports: %d.",
                processed_prs_with_cucumber_reports
            )
            break
        if consecutive_without_cucumber >= MAX_CONSECUTIVE_PRS_WITHOUT_CUCUMBER_REPORTS:
            logger.warning(
                "Stopped after %d consecutive PRs without Cucumber reports. "
                "Processed %d PRs with Cucumber reports.",
                MAX_CONSECUTIVE_PRS_WITHOUT_CUCUMBER_REPORTS,
                processed_prs_with_cucumber_reports
            )
            break
        logger.info("Processing PR #%d: %s, created at %s", pr.number, pr.title, pr.created_at)

        pr_processed = process_training_mode(repo, test_workflow, pr, headers, csv_writer)
        if pr_processed:
            processed_prs_with_cucumber_reports += 1
            consecutive_without_cucumber = 0
        else:
            consecutive_without_cucumber += 1

def process_single_pr_mode(repo, pr, csv_writer):
    """
    Process a single PR in prediction mode (no test runs, no cucumber downloads).

    Args:
        repo (github.Repository.Repository): Repository object.
        pr (github.PullRequest.PullRequest): Pull request object.
        csv_writer (csv.writer): CSV writer object.
    """
    try:
        dummy_test_run = type("DummyRun", (), {"head_sha": pr.head.sha})()
        extract_and_write_pr_features(repo, pr, dummy_test_run, csv_writer)
    except Exception as e:
        logger.error("Error extracting features for PR #%d: %s", pr.number, e)

def process_training_mode(repo, test_workflow, pr, headers, csv_writer):
    """
    Process a PR in training mode (with test runs and cucumber downloads).

    Args:
        repo (github.Repository.Repository): Repository object.
        test_workflow (github.Workflow.Workflow): Test workflow object.
        pr (github.PullRequest.PullRequest): Pull request object.
        headers (dict): HTTP headers for requests.
        csv_writer (csv.writer): CSV writer object.

    Returns:
        bool: True if PR was processed and features extracted, False otherwise.
    """
    try:
        initial_test_run_with_cucumber = get_cucumber_initial_test_run(test_workflow, pr)
        if not initial_test_run_with_cucumber:
            return False
    except Exception as e:
        logger.error("Error getting initial test run for PR #%d: %s", pr.number, e)
        return False
    try:
        has_secondary = download_secondary_cucumber_reports(
            initial_test_run_with_cucumber, pr.number, headers
        )
        if not has_secondary:
            logger.info(
                "No secondary cucumber reports for PR #%d, skipping feature extraction.",
                pr.number
            )
            return False
    except Exception as e:
        logger.error("Error downloading secondary cucumber reports for PR #%d: %s", pr.number, e)
        return False
    try:
        extract_and_write_pr_features(
            repo, pr, initial_test_run_with_cucumber, csv_writer
        )
    except Exception as e:
        logger.error("Error extracting features for PR #%d: %s", pr.number, e)
        return False

    return True

def extract_and_write_pr_features(repo, pr, test_run, csv_writer):
    """
    Extract modified files, file extensions, and change history for a PR and write to CSV.

    Args:
        repo (github.Repository.Repository): Repository object.
        pr (github.PullRequest.PullRequest): Pull request object.
        test_run (github.WorkflowRun.WorkflowRun): Workflow run object.
        csv_writer (csv.writer): CSV writer object.
    """
    modified_files = get_modified_files(repo, pr, test_run)
    logger.info("Files modified in PR: %s", modified_files)
    file_extensions = get_file_extensions(modified_files)
    logger.info("File extensions of files modified in PR: %s", file_extensions)
    change_history = get_files_change_history(repo, modified_files, RECENT_DAYS)
    logger.info("Files Change History: %s", change_history)
    write_pr_features_to_csv(csv_writer, file_extensions, change_history, pr.number)

def main():
    """
    Parse CLI arguments, load credentials, set up the CSV output file,
    and start the extraction of PR data.
    """
    if len(sys.argv) != 2:
        logger.critical(
            "Usage: python pr_data_extraction.py <N>|'<#PR_NUMBER>'\n"
            "<N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from.\n"
            "'<#PR_NUMBER>' is a specific PR number (e.g., '#12345') to extract features for."
        )
        sys.exit(1)

    arg = sys.argv[1]
    single_pr_number = None
    if arg.startswith("#"):
        try:
            single_pr_number = int(arg[1:])
            n = 1
        except ValueError:
            logger.critical(
                "In single PR mode, argument must be in the form '#<PR_NUMBER>' (e.g., '#5')."
            )
            sys.exit(1)
    else:
        try:
            n = int(arg)
        except ValueError:
            logger.critical(
                "Argument must be an integer (for N) (e.g., 80) or '#<PR_NUMBER>' (e.g., '#12345')."
            )
            sys.exit(1)

    github_token = load_github_token()
    csv_file, csv_writer = setup_output_csv(RECENT_DAYS)
    try:
        extract_pr_data(
            REPO_FULL_NAME, n, github_token, csv_writer, single_pr_number
        )
    finally:
        csv_file.close()

if __name__ == "__main__":
    main()
