#!/usr/bin/env python3
"""
Generates training CSV file from our PostgreSQL test runs database.

This script connects to the database, preprocesses feature data, and loops over pull requests
test runs to create a training CSV file.

Prerequisites:
    - Environment variable DATABASE_CONNECTION_STRING must be set.
    - Environment variable GITHUB_TOKEN must be set with a GitHub Access Token.

Usage:
    python generate_training_data.py
"""
import csv
import json
import logging
from datetime import datetime, timedelta
from typing import Any, Dict, List

from sqlalchemy import create_engine, select, func, and_
from sqlalchemy.orm import Session, sessionmaker
from github import Github

from config import (
    TRAINING_CSV_FILENAME,
    MODIFIED_FILES_RECENT_DAYS,
    FEATURE_FAILURE_RECENT_DAYS,
    REPO_FULL_NAME
)
from backfill_test_runs_into_db import (
    Feature, FeatureCategory, FeatureResult, TestRun, RunModifiedFile, File,
    get_database_connection_string
)
from pr_data_extraction import (
    get_file_extensions, get_files_change_history, get_repository, load_github_token
)
from utilities import setup_logging

logger = setup_logging(logging.INFO, "logs/generate_training_data.log")


class DatabaseManager:
    """Manages database operations for generating training data."""

    def __init__(self, database_connection_string: str):
        """Initialize database manager with connection string."""
        logger.debug("Initializing database manager with connection string")
        self.engine = create_engine(database_connection_string)
        self.session_local = sessionmaker(autocommit=False, autoflush=False, bind=self.engine)

    def get_session(self) -> Session:
        """Get a database session."""
        logger.debug("Creating new database session")
        return self.session_local()

    def get_feature_failure_rates_before_run(self, session: Session, feature_id: int,
                        recent_days: List[int], run_execution_date: datetime) -> Dict[int, int]:
        """
        Calculate the historical failure rates for a given feature over several day ranges before
        the execution date of the test run, this ensures no future data leakage occurs and the
        training data correctly mirrors the production data.
        """
        failure_rates = {days: 0 for days in recent_days}

        for days in recent_days:
            start_date = run_execution_date - timedelta(days=days)
            end_date = run_execution_date
            stmt = (
                select(func.count(FeatureResult.run_id))
                .join(TestRun, FeatureResult.run_id == TestRun.id)
                .where(
                    and_(
                        FeatureResult.feature_id == feature_id,
                        FeatureResult.passed.is_(False),
                        TestRun.executed_at >= start_date,
                        TestRun.executed_at < end_date,
                    )
                )
            )
            failure_count = session.execute(stmt).scalar_one()
            failure_rates[days] = failure_count

        return failure_rates

    def get_test_runs_by_pr(self, session: Session, pr_number: int) -> List[TestRun]:
        """Get all test runs for a given PR number."""
        stmt = select(TestRun).where(TestRun.pr_number == pr_number)
        stmt = (
            select(TestRun)
            .where(TestRun.pr_number == pr_number)
            .order_by(TestRun.run_number.asc())
        )
        return session.execute(stmt).scalars().all()

    def get_modified_files_for_run(self, session: Session, run_id: int) -> List[str]:
        """Get the modified files paths for a given test run."""
        stmt = (
            select(File.path)
            .join(RunModifiedFile, File.id == RunModifiedFile.file_id)
            .where(RunModifiedFile.run_id == run_id)
        )
        return session.execute(stmt).scalars().all()

    def get_feature_results_for_run(self, session: Session, run_id: int) -> List[FeatureResult]:
        """Get the feature results for a given test run."""
        stmt = select(FeatureResult).where(FeatureResult.run_id == run_id)
        return session.execute(stmt).scalars().all()


class TrainingDataGenerator:
    """Generates training data from the database."""

    def __init__(self, db_manager: DatabaseManager):
        """Initialize the generator with a database manager."""
        self.db_manager = db_manager
        self.repo = None  # Initialized when needed

    def _get_repo(self):
        """Initializes and returns the GitHub repository object."""
        if self.repo is None:
            github_token = load_github_token()
            gh = Github(github_token)
            self.repo = get_repository(gh, REPO_FULL_NAME)
        return self.repo

    def generate_training_data(self, include_passed_prs: bool = True):
        """
        Generate the training data and write it to a CSV file.
        """
        with self.db_manager.get_session() as session:

            pr_numbers = session.execute(
                select(TestRun.pr_number)
                .distinct()
                .order_by(TestRun.pr_number.desc())
            ).scalars().all()

            with open(TRAINING_CSV_FILENAME, "w", newline="", encoding="utf-8") as csvfile:
                csv_writer = self._setup_csv_writer(csvfile)

                for pr_number in pr_numbers:
                    self._process_pr(
                        session, pr_number, csv_writer, include_passed_prs
                    )

    def _setup_csv_writer(self, csvfile: Any) -> Any:
        """Sets up the CSV writer with the correct header."""
        header = (
            ["extensions"]
            + [f"modifications_{d}d" for d in MODIFIED_FILES_RECENT_DAYS]
            + ["pr_number", "run_number", "feature", "feature_category", "scenario_count"]
            + [f"failures_{d}d" for d in FEATURE_FAILURE_RECENT_DAYS]
            + ["result"]
        )
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(header)
        return csv_writer

    def _process_pr(self, session: Session, pr_number: int,
                    csv_writer: Any, include_passed_prs: bool):
        """Process a single pull request."""
        logger.info("Processing PR #%d", pr_number)
        test_runs = self.db_manager.get_test_runs_by_pr(session, pr_number)

        if not test_runs:
            return

        failed_runs = [run for run in test_runs if not run.passed]
        if failed_runs:
            run_to_process = failed_runs[0]
        elif include_passed_prs:
            run_to_process = test_runs[0]
        else:
            return

        modified_files = self.db_manager.get_modified_files_for_run(
            session, run_to_process.id
        )
        file_extensions = get_file_extensions(modified_files)
        extensions_field = json.dumps(file_extensions, ensure_ascii=False)
        change_history = get_files_change_history(
            self._get_repo(), modified_files, MODIFIED_FILES_RECENT_DAYS, run_to_process.executed_at
        )
        change_history_values = [change_history.get(days, 0) for days in MODIFIED_FILES_RECENT_DAYS]

        feature_results = self.db_manager.get_feature_results_for_run(
            session, run_to_process.id
        )

        for feature_result in feature_results:
            feature = session.get(Feature, feature_result.feature_id)
            category = session.get(FeatureCategory, feature.category_id)
            failure_values = list(
                self.db_manager.get_feature_failure_rates_before_run(
                    session, feature.id, FEATURE_FAILURE_RECENT_DAYS, run_to_process.executed_at
                ).values()
            )

            row = (
                [extensions_field]
                + change_history_values
                + [pr_number, run_to_process.run_number, feature.name, category.name,
                   feature.scenario_count]
                + failure_values
                + ["failed" if not feature_result.passed else "passed"]
            )
            csv_writer.writerow(row)


def main():
    """Main function to run the training data generation process."""
    database_connection_string = get_database_connection_string()
    try:
        db_manager = DatabaseManager(database_connection_string)
        generator = TrainingDataGenerator(db_manager)
        generator.generate_training_data()
        logger.info("Training data generation completed successfully")
    except Exception as e:
        logger.critical("Fatal error during training data generation: %s", e)
        raise


if __name__ == "__main__":
    main()
