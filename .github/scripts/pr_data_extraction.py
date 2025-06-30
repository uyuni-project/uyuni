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
    - Optionally, TEST_WORKFLOW_NAME can be set to override the default workflow name ("TestFlow").

Usage:
    python pr_data_extraction.py <N>
    Where <N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from.
"""
import os
import sys
import logging
import requests
import datetime
from github import Github

# Constants
REPO_FULL_NAME = "uyuni-project/uyuni"

# Can be overridden using the 'TEST_WORKFLOW_NAME' environment variable
TEST_WORKFLOW_NAME = os.getenv("TEST_WORKFLOW_NAME", "TestFlow")

def setup_logging(level):
    if level == logging.DEBUG:
        fmt = "%(levelname)s - %(filename)s:%(lineno)d - %(message)s"
    else:
        fmt = "%(levelname)s - %(message)s"

    # Set root logger level to WARNING to suppress noisy third-party logs
    logging.basicConfig(
        level=logging.WARNING,
        format=fmt,
    )

    # Set this script’s logger to the desired level
    logger = logging.getLogger(__name__)
    logger.setLevel(level)
    return logger

logger = setup_logging(logging.DEBUG)

def load_github_token():
    github_token = os.getenv("GITHUB_TOKEN")
    if not github_token:
        logger.critical(
            "GITHUB_TOKEN not set in environment.\n"
            "export GITHUB_TOKEN='your_personal_access_token' and rerun the script"
        )
        sys.exit(1)
    return github_token

def get_repository(gh, repo_full_name):
    try:
        return gh.get_repo(repo_full_name)
    except Exception as e:
        logger.critical("Failed to access repository %s: %s", repo_full_name, e)
        sys.exit(1)

def get_test_workflow(repo, workflow_name):
    workflows = repo.get_workflows()
    test_workflow = next((wf for wf in workflows if wf.name == workflow_name), None)
    if not test_workflow:
        logger.critical("Workflow %s not found.", workflow_name)
        sys.exit(1)
    return test_workflow

def get_candidate_prs(repo, N):
    multiplier = 3  # To oversample in case some PRs don't have Cucumber reports
    prs = list(repo.get_pulls(state="all", sort="created", direction="desc")[:N*multiplier])
    if len(prs) < N:
        logger.warning("Only %d PRs found, but %d requested.", len(prs), N)
    return prs

def get_initial_test_run(test_workflow, pr):
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
        logger.debug("Matched %s run ID %s created at %s", TEST_WORKFLOW_NAME, run.id, run.created_at)

    pr_test_runs.sort(key=lambda run: run.created_at)

    # Return initial test run *that has Cucumber reports*
    for run in pr_test_runs:
        logger.debug("Checking if run ID %s has Cucumber reports", run.id)
        for artifact in run.get_artifacts():
            if artifact.name.lower().startswith("cucumber"):
                logger.info("Using initial test run #%d (that has Cucumber reports) created at %s", run.id, run.created_at)
                return run

    logger.info("No %s runs with Cucumber reports found for PR #%d", TEST_WORKFLOW_NAME, pr.number)
    return None

def download_cucumber_artifacts(run_with_cucumber, pr_number, headers):
    for artifact in run_with_cucumber.get_artifacts():
        if artifact.name.lower().startswith("cucumber"):
            url = artifact.archive_download_url
            response = requests.get(url, headers=headers, stream=True)
            if response.ok:
                filename = f"pr{pr_number}_run{run_with_cucumber.id}_{artifact.name}.zip"
                with open(filename, "wb") as f:
                    f.write(response.content)
                logger.info("Downloaded: %s", filename)
            else:
                logger.warning("Failed to download %s (HTTP %d)", artifact.name, response.status_code)

def get_modified_files_at_pr_open(repo, pr, initial_test_run):
    # base_commit_sha: the commit the PR is targeting (e.g., main)
    # initial_test_run_commit_sha: the commit the initial workflow ran on when the PR was opened
    base_commit_sha = pr.base.sha
    initial_test_run_commit_sha = initial_test_run.head_sha

    # Compare the base commit and the commit of the first workflow run 
    # to get all changes introduced by the PR when it was first opened.
    comparison = repo.compare(base_commit_sha, initial_test_run_commit_sha)
    return [f.filename for f in comparison.files]

def extract_pr_data(repo_full_name, N, github_token):
    gh = Github(github_token)
    headers = {
        "Authorization": f"Bearer {github_token}",
        "Accept": "application/vnd.github+json"
    }

    repo = get_repository(gh, repo_full_name)
    test_workflow = get_test_workflow(repo, TEST_WORKFLOW_NAME)
    prs = get_candidate_prs(repo, N)

    processed_prs_with_cucumber_reports = 0
    for pr in prs:
        if processed_prs_with_cucumber_reports >= N:
            break

        logger.info("Processing PR #%d: %s", pr.number, pr.title)

        try:
            initial_test_run_with_cucumber = get_initial_test_run(test_workflow, pr)
            if not initial_test_run_with_cucumber:
                continue
        except Exception as e:
            logger.error("Error getting initial test run for PR #%d: %s", pr.number, e)
        
        try:
            download_cucumber_artifacts(initial_test_run_with_cucumber, pr.number, headers)
        except Exception as e:
            logger.error("Error downloading cucumber artifacts for PR #%d: %s", pr.number, e)
        
        try:
            modified_files = get_modified_files_at_pr_open(repo, pr, initial_test_run_with_cucumber)
            logger.info("Files modified in PR when opened: %s", modified_files)
        except Exception as e:
            logger.error("Error obtaining modified files for PR #%d: %s", pr.number, e)

        processed_prs_with_cucumber_reports += 1

def main():    
    if len(sys.argv) != 2:
        logger.critical(
            "Usage: python pr_data_extraction.py <N>\n"
            "<N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from."
        )
        sys.exit(1)

    try:
        N = int(sys.argv[1])
    except ValueError:
        logger.critical(
            "Argument <N> must be an integer.\n"
            "<N> is the number of latest Uyuni PRs (with Cucumber reports) to extract data from."
        )
        sys.exit(1)

    github_token = load_github_token()
    extract_pr_data(REPO_FULL_NAME, N, github_token)

if __name__ == "__main__":
    main()
