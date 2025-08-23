"""
Processes test run data from a GitHub Actions workflow and inserts it into the database.

This script is triggered by a GitHub Actions job.
It performs the following steps:
1.  Collects run metadata (run ID, PR number, commit SHA, modified files) from workflow arguments.
2.  Creates a `test_run/` directory and saves the metadata to a run data JSON file.
3.  Filters downloaded Cucumber reports, keeping only secondary/recommended reports.
4.  If secondary reports exist, it generates a run feature results JSON file.
5.  Updates the run data JSON file with the final result (passed/failed) based on feature results.
6.  Connects to the PostgreSQL database using a connection string from an environment variable.
7.  Inserts the test run data, including modified files and feature results, into the database.
"""
import os
import sys
import json
import logging
import argparse
from pathlib import Path
from datetime import datetime, timezone

from sqlalchemy import select, func

# Add predictive-test-selection directory to the python path to import required modules
uyuni_dir = Path(__file__).resolve().parents[2]
predictive_test_selection_dir = uyuni_dir / "testsuite/ext-tools/predictive-test-selection"
sys.path.append(str(predictive_test_selection_dir))

from config import RUN_DATA_FILENAME
from insert_test_runs_into_db import DatabaseManager, TestRunInserter, TestRun
from pr_data_extraction import delete_non_secondary_reports
from runs_feature_result_extraction import (
    extract_run_results,
    save_run_results,
    update_run_data_result,
)

logging.basicConfig(
    level=logging.DEBUG, format="%(levelname)s - %(filename)s:%(lineno)d - %(message)s"
)

TEST_RUN_FOLDER = Path("test_run")


def get_next_run_number(session, pr_number: int) -> int:
    """
    Calculates the next run number for a given pull request.

    If the PR does not exist in the database, the run number is 1.
    If the PR exists, the run number is 1 + the maximum existing run number for that PR.
    """
    stmt = select(func.max(TestRun.run_number)).where(TestRun.pr_number == pr_number)
    max_run_number = session.execute(stmt).scalar_one_or_none()

    if max_run_number is None:
        logging.debug("No existing runs for PR #%d, starting with run_number=1", pr_number)
        return 1

    next_run_number = max_run_number + 1
    logging.debug("Next run number for PR #%d: %d", pr_number, next_run_number)
    return next_run_number

def store_run_data(args: argparse.Namespace):
    """
    Stores the run data obtained from the workflow in a JSON file.

    Args:
        args: The command-line arguments containing run data.
    """
    TEST_RUN_FOLDER.mkdir(exist_ok=True)
    run_data = {
        "github_id": args.run_id,
        "commit_sha": args.commit_sha,
        "result": "to_be_determined",
        "execution_timestamp": datetime.now(timezone.utc).isoformat(),
        "modified_files": json.loads(args.modified_files),
    }
    run_data_path = TEST_RUN_FOLDER / RUN_DATA_FILENAME
    with open(run_data_path, "w", encoding="utf-8") as f:
        json.dump(run_data, f, indent=2)
    logging.info("Run data saved to %s", run_data_path)


def main():
    """
    Main function to orchestrate the processing and insertion of test run data.
    """
    parser = argparse.ArgumentParser(description="Process test run data and insert into database.")
    parser.add_argument("--run-id", required=True, type=int, help="GitHub Actions Run ID.")
    parser.add_argument("--pr-number", required=True, type=int, help="Pull Request number.")
    parser.add_argument("--commit-sha", required=True, help="Commit SHA.")
    parser.add_argument("--modified-files", required=True, help="JSON string of modified files.")
    parser.add_argument("--cucumber-reports-path", required=True, help="Path to cucumber reports.")
    args = parser.parse_args()

    try:
        # Create run data JSON file
        store_run_data(args)

        # Filter cucumber reports
        cucumber_reports_path = Path(args.cucumber_reports_path)
        if not delete_non_secondary_reports(str(cucumber_reports_path)):
            logging.warning("No secondary cucumber reports found. Stopping insertion.")
            return

        # Generate run feature results JSON file
        results_dict = extract_run_results(str(TEST_RUN_FOLDER))
        save_run_results(str(TEST_RUN_FOLDER), results_dict)
        update_run_data_result(str(TEST_RUN_FOLDER), results_dict)
        logging.info("Run feature results JSON file generated and run data result field updated.")

        # Database insertion
        database_connection_string = os.getenv("TEST_RUNS_DATABASE_CONNECTION_STRING")
        if not database_connection_string:
            logging.critical("TEST_RUNS_DATABASE_CONNECTION_STRING not set. Cannot insert into DB.")
            sys.exit(1)

        db_manager = DatabaseManager(database_connection_string)
        with db_manager.get_session() as session:
            try:
                with session.begin():
                    run_number = get_next_run_number(session, args.pr_number)
                    inserter = TestRunInserter(db_manager)
                    inserter.insert_run(
                        session=session,
                        pr_number=args.pr_number,
                        run_number=run_number,
                        run_folder_path=str(TEST_RUN_FOLDER)
                    )
                logging.info("Successfully inserted test run")
            except Exception:
                logging.exception("Error inserting test run")
                sys.exit(1)
    except Exception:
        logging.exception("Error during test run data processing and insertion")
        sys.exit(1)


if __name__ == "__main__":
    main()
