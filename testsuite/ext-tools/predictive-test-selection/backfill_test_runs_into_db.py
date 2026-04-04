#!/usr/bin/env python3
"""
Uses extracted test run data and feature results to backfill test runs into a relational database.

This script reads the CSV file produced by the PR data extraction script and for each PR,
uses run data and feature results from all run folders and backfills them into the database.

Prerequisites:
    - Run runs_feature_result_extraction.py
    - Environment variable DATABASE_CONNECTION_STRING must be set.

The script works with any PostgreSQL database, and probably any relational database,
but is tested with Neon Postgres.

Usage:
    python backfill_test_runs_into_db.py
"""
import os
import re
import sys
import csv
import json
import logging
from datetime import datetime
from typing import Any, Dict, List, Optional, Set

from sqlalchemy import (
    Boolean, Column, DateTime, ForeignKey, BigInteger, Integer, SmallInteger, Text,
    create_engine, select
)
from sqlalchemy.orm import Session, sessionmaker, declarative_base

from config import (
    CUCUMBER_PASSED, CUCUMBER_REPORTS_PARENT_DIR, PR_FEATURES_CSV_FILENAME,
    RUN_DATA_FILENAME, RUN_FEATURE_RESULTS_FILENAME
)
from utilities import setup_logging

logger = setup_logging(logging.INFO, "logs/backfill_test_runs_into_db.log")

# SQLAlchemy setup
Base = declarative_base()


class FeatureCategory(Base):
    """Feature category table model."""
    __tablename__ = 'feature_categories'

    id = Column(SmallInteger, primary_key=True)
    name = Column(Text, nullable=False, unique=True)


class Feature(Base):
    """Feature table model."""
    __tablename__ = 'features'

    id = Column(SmallInteger, primary_key=True)
    name = Column(Text, nullable=False, unique=True)
    category_id = Column(SmallInteger, ForeignKey('feature_categories.id'),
                        nullable=False)
    scenario_count = Column(SmallInteger, nullable=False)


class File(Base):
    """File table model."""
    __tablename__ = 'files'

    id = Column(Integer, primary_key=True)
    path = Column(Text, nullable=False, unique=True)


class TestRun(Base):
    """Test run table model."""
    __tablename__ = 'test_runs'

    id = Column(Integer, primary_key=True)
    pr_number = Column(Integer, nullable=False)
    run_number = Column(SmallInteger, nullable=False)
    github_id = Column(BigInteger, nullable=False, unique=True)
    commit_sha = Column(Text, nullable=False)
    passed = Column(Boolean, nullable=False)
    executed_at = Column(DateTime(timezone=True), nullable=False)


class FeatureResult(Base):
    """Feature result table model."""
    __tablename__ = 'feature_results'

    run_id = Column(Integer, ForeignKey('test_runs.id'), primary_key=True)
    feature_id = Column(SmallInteger, ForeignKey('features.id'), primary_key=True)
    passed = Column(Boolean, nullable=False)


class RunModifiedFile(Base):
    """Run modified file table model."""
    __tablename__ = 'run_modified_files'

    run_id = Column(Integer, ForeignKey('test_runs.id'), primary_key=True)
    file_id = Column(Integer, ForeignKey('files.id'), primary_key=True)


class DatabaseManager:
    """Manages database operations for inserting test run data."""

    def __init__(self, database_connection_string: str):
        """Initialize database manager with connection string."""
        logger.debug("Initializing database manager with connection string")
        self.engine = create_engine(database_connection_string)
        self.session_local = sessionmaker(autocommit=False, autoflush=False, bind=self.engine)

    def get_session(self) -> Session:
        """Get a database session."""
        logger.debug("Creating new database session")
        return self.session_local()

    def get_category_id(self, session: Session, category_name: str) -> int:
        """Get or create a feature category and return its ID."""
        logger.debug("Getting category ID for: %s", category_name)
        stmt = select(FeatureCategory.id).where(FeatureCategory.name == category_name)
        category_id = session.execute(stmt).scalar_one_or_none()

        if category_id is not None:
            logger.debug("Found existing category ID: %d", category_id)
            return category_id

        new_category = FeatureCategory(name=category_name)
        session.add(new_category)
        session.flush()
        logger.debug("Created category with ID: %d", new_category.id)
        return new_category.id

    def get_feature_id(self, session: Session, feature_name: str,
                      category_id: int, scenario_count: int) -> int:
        """Get or create a feature and return its ID, updating scenario count if needed.

        Behavior when feature exists:
            - If the stored scenario count differs from the provided value, update it.
            - Otherwise, leave it unchanged and log accordingly.
        """
        logger.debug("Getting feature ID for: %s", feature_name)
        stmt = select(Feature).where(Feature.name == feature_name)
        existing_feature = session.execute(stmt).scalar_one_or_none()

        if existing_feature:
            logger.debug("Found existing feature ID: %d", existing_feature.id)
            if existing_feature.scenario_count != scenario_count:
                logger.info(
                    "Updating scenario count for feature '%s' from %d to %d",
                    feature_name, existing_feature.scenario_count, scenario_count
                )
                existing_feature.scenario_count = scenario_count
            else:
                logger.debug(
                    "Scenario count unchanged for feature '%s': %d",
                    feature_name, existing_feature.scenario_count
                )
            return existing_feature.id

        new_feature = Feature(
            name=feature_name,
            category_id=category_id,
            scenario_count=scenario_count
        )
        session.add(new_feature)
        session.flush()
        logger.debug("Created feature with ID: %d", new_feature.id)
        return new_feature.id

    def get_file_id(self, session: Session, file_path: str) -> int:
        """Get or create a file and return its ID."""
        logger.debug("Getting file ID for: %s", file_path)
        stmt = select(File.id).where(File.path == file_path)
        file_id = session.execute(stmt).scalar_one_or_none()

        if file_id is not None:
            logger.debug("Found existing file ID: %d", file_id)
            return file_id

        new_file = File(path=file_path)
        session.add(new_file)
        session.flush()
        logger.debug("Created file with ID: %d", new_file.id)
        return new_file.id

    def test_run_exists(self, session: Session, github_id: int) -> bool:
        """Check if a test run already exists by GitHub ID."""
        logger.debug("Checking if test run exists with github_id: %d", github_id)
        stmt = select(TestRun.id).where(TestRun.github_id == github_id)
        test_run_id = session.execute(stmt).scalar_one_or_none()
        exists = test_run_id is not None
        logger.debug("Test run exists: %s", exists)
        return exists

    def insert_test_run(self, session: Session, pr_number: int, run_number: int,
                       github_id: int, commit_sha: str, passed: bool,
                       executed_at: datetime) -> int:
        """Insert a new test run and return its ID."""
        logger.debug("Inserting run: run=%d, github_id=%d", run_number, github_id)
        new_run = TestRun(
            pr_number=pr_number,
            run_number=run_number,
            github_id=github_id,
            commit_sha=commit_sha,
            passed=passed,
            executed_at=executed_at
        )
        session.add(new_run)
        session.flush()
        logger.debug("Inserted test run with ID: %d", new_run.id)
        return new_run.id

    def insert_feature_result(self, session: Session, run_id: int, feature_id: int,
                            passed: bool) -> None:
        """Insert a feature result."""
        logger.debug("Inserting feature result: run_id=%d, feature_id=%d, passed=%s",
                    run_id, feature_id, passed)
        new_result = FeatureResult(
            run_id=run_id,
            feature_id=feature_id,
            passed=passed
        )
        session.add(new_result)
        logger.debug("Added feature result to session")

    def insert_run_modified_files(self, session: Session, run_id: int, file_ids: Set[int]) -> None:
        """Insert run modified files relationships."""
        logger.debug("Inserting %d file relationships for run_id: %d", len(file_ids), run_id)
        for file_id in file_ids:
            new_relationship = RunModifiedFile(run_id=run_id, file_id=file_id)
            session.add(new_relationship)
        logger.debug("Added all modified file relationships to session")


class TestRunInserter:
    """Main inserter for test run data."""

    def __init__(self, db_manager: DatabaseManager):
        """Initialize the inserter with database manager."""
        self.db_manager = db_manager

    def get_run_folders(self, pr_folder_path: str) -> List[str]:
        """Get all run folders within a PR folder, sorted by natural numeric order."""
        if not os.path.isdir(pr_folder_path):
            logger.warning("PR folder not found: %s", pr_folder_path)
            return []

        run_folders = []
        for item in os.listdir(pr_folder_path):
            item_path = os.path.join(pr_folder_path, item)
            if os.path.isdir(item_path) and item.startswith("run_"):
                run_folders.append(item)

        # Sort by natural numeric order (run_1, run_2, run_10)
        def natural_sort_key(folder_name: str) -> int:
            match = re.search(r'run_(\d+)', folder_name)
            return int(match.group(1)) if match else 0

        run_folders.sort(key=natural_sort_key)
        return run_folders

    def load_run_data(self, run_folder_path: str) -> Optional[Dict[str, Any]]:
        """Load run data JSON file from a run folder."""
        run_data_path = os.path.join(run_folder_path, RUN_DATA_FILENAME)

        if not os.path.exists(run_data_path):
            logger.warning("%s not found in: %s", RUN_DATA_FILENAME, run_folder_path)
            return None

        try:
            with open(run_data_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError) as e:
            logger.error("Error loading %s from %s: %s", RUN_DATA_FILENAME, run_folder_path, e)
            return None

    def load_feature_results(self, run_folder_path: str) -> Optional[List[Dict[str, Any]]]:
        """Load run feature results JSON file from a run folder."""
        feature_results_path = os.path.join(run_folder_path, RUN_FEATURE_RESULTS_FILENAME)

        if not os.path.exists(feature_results_path):
            logger.warning("%s not found in: %s", RUN_FEATURE_RESULTS_FILENAME, run_folder_path)
            return None

        try:
            with open(feature_results_path, 'r', encoding='utf-8') as f:
                return json.load(f)
        except (FileNotFoundError, json.JSONDecodeError) as e:
            logger.error(
                "Error loading %s from %s: %s", RUN_FEATURE_RESULTS_FILENAME, run_folder_path, e
            )
            return None

    def insert_run_feature_results(self, session: Session, run_id: int,
                                 feature_results: List[Dict[str, Any]]) -> None:
        """Insert feature results for a specific test run."""
        logger.debug("Inserting %d feature results for run_id: %d", len(feature_results), run_id)

        for feature_result in feature_results:
            feature_name = feature_result.get('feature_name')
            category_name = feature_result.get('category_name')
            scenario_count = feature_result.get('scenario_count')
            result = feature_result.get('result')

            if not all([feature_name, category_name, scenario_count, result]):
                logger.warning("Incomplete feature result data: %s", feature_result)
                continue

            logger.debug("Processing feature: %s (category: %s)", feature_name, category_name)
            category_id = self.db_manager.get_category_id(session, category_name)
            feature_id = self.db_manager.get_feature_id(
                session, feature_name, category_id, scenario_count
            )

            passed = result == CUCUMBER_PASSED
            self.db_manager.insert_feature_result(session, run_id, feature_id, passed)

        logger.debug("Completed inserting feature results for run_id: %d", run_id)

    def insert_run(self, session: Session, pr_number: int, run_number: int,
                  run_folder_path: str) -> bool:
        """Insert a single test run and its associated data into the database.
            
        Returns:
            True if run was successfully inserted, False otherwise
        """
        logger.info(
            "Inserting run: run_number=%d, path=%s", run_number, run_folder_path
        )

        # Load run data
        run_data = self.load_run_data(run_folder_path)
        if run_data is None:
            return False

        # Check if run already exists
        github_id = run_data.get('github_id')
        if self.db_manager.test_run_exists(session, github_id):
            logger.info("Test run with github_id %d already exists, skipping", github_id)
            return True

        # Parse run data
        commit_sha = run_data.get('commit_sha', '')
        result = run_data.get('result', '')
        execution_timestamp = run_data.get('execution_timestamp', '')
        modified_files = run_data.get('modified_files', [])

        # Convert result to boolean
        passed = result == CUCUMBER_PASSED

        # Parse execution timestamp
        try:
            executed_at = datetime.fromisoformat(execution_timestamp)
        except (ValueError, AttributeError):
            logger.warning("Invalid execution_timestamp in %s: %s", run_folder_path,
                          execution_timestamp)
            return False

        # Obtain modified files, initialize file_ids as a set to avoid duplicates
        file_ids = set()
        for file_path in modified_files:
            file_id = self.db_manager.get_file_id(session, file_path)
            file_ids.add(file_id)
        if not file_ids:
            return False

        feature_results = self.load_feature_results(run_folder_path)
        if not feature_results:
            return False

        run_id = self.db_manager.insert_test_run(
            session, pr_number, run_number, github_id, commit_sha, passed, executed_at
        )

        self.db_manager.insert_run_modified_files(session, run_id, file_ids)

        self.insert_run_feature_results(session, run_id, feature_results)

        logger.info("Successfully inserted run: %s", run_folder_path)
        return True

    def insert_runs_in_pr(self, session: Session, pr_number: str) -> None:
        """Insert all test runs within a PR folder."""
        pr_folder_path = os.path.join(CUCUMBER_REPORTS_PARENT_DIR, f"PR{pr_number}")
        logger.info("Inserting runs for PR #%s", pr_number)

        run_folders = self.get_run_folders(pr_folder_path)
        if not run_folders:
            logger.warning("No run folders found for PR #%s", pr_number)
            return

        logger.info("Found %d run folders for PR #%s", len(run_folders), pr_number)

        # Insert each run folder with sequential run numbers
        for run_number, run_folder in enumerate(run_folders, 1):
            run_folder_path = os.path.join(pr_folder_path, run_folder)
            try:
                success = self.insert_run(session, int(pr_number), run_number, run_folder_path)
                if not success:
                    logger.warning("Failed to insert run: %s", run_folder_path)
            except Exception as e:
                logger.error("Error inserting run %s: %s", run_folder_path, e)

    def insert_runs_in_all_prs(self) -> None:
        """Insert test runs from all PRs in the CSV file."""
        try:
            with open(PR_FEATURES_CSV_FILENAME, newline='', encoding='utf-8') as infile:
                reader = csv.reader(infile)
                # Skip header row
                try:
                    next(reader)
                except StopIteration:
                    logger.warning("CSV file %s is empty", PR_FEATURES_CSV_FILENAME)
                    return

                with self.db_manager.get_session() as session:
                    for row in reader:
                        if not row:
                            continue
                        pr_number = row[-1]  # Last column contains PR number
                        try:
                            self.insert_runs_in_pr(session, pr_number)
                            session.commit()
                        except Exception as e:
                            logger.error("Error inserting runs for PR #%s: %s", pr_number, e)
                            session.rollback()
        except FileNotFoundError as e:
            logger.critical("Input CSV file %s not found: %s", PR_FEATURES_CSV_FILENAME, e)
        except Exception as e:
            logger.critical("Fatal error: %s", e)


def get_database_connection_string() -> str:
    """Get database connection string from environment variable"""
    database_connection_string = os.getenv('DATABASE_CONNECTION_STRING')
    if not database_connection_string:
        logger.critical(
            "DATABASE_CONNECTION_STRING not set in environment.\n"
            "export DATABASE_CONNECTION_STRING='database_connection_string' and rerun the script"
        )
        sys.exit(1)
    return database_connection_string


def main() -> None:
    """Main function to run the test run insertion process."""
    database_connection_string = get_database_connection_string()
    try:
        # Initialize database manager and inserter
        db_manager = DatabaseManager(database_connection_string)
        inserter = TestRunInserter(db_manager)

        inserter.insert_runs_in_all_prs()
        logger.info("Test run insertion completed successfully")
    except Exception as e:
        logger.critical("Fatal error during test run insertion: %s", e)
        raise


if __name__ == "__main__":
    main()
