#!/usr/bin/env python3
"""
Production-ready preprocessing pipeline for predictive test selection data.

This script implements a comprehensive data preprocessing pipeline that:
1. Checks and handles data quality issues
2. Performs time-ordered train/test split by PR number
3. Applies feature transformations using sklearn Pipeline
4. Saves the fitted pipeline for production use
5. Exports preprocessed datasets as CSVs

Prerequisite:
    Run generate_training_data.py

Usage:
    python preprocessing_pipeline.py
"""
import ast
import logging
from collections import Counter

import pandas as pd
import numpy as np
from sklearn.base import BaseEstimator, TransformerMixin
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, FunctionTransformer
import joblib

from config import (
    TRAINING_CSV_FILENAME,
    PREPROCESSED_TRAINING_CSV_FILENAME,
    PREPROCESSED_TESTING_CSV_FILENAME,
    PREPROCESSING_PIPELINE_FILENAME,
    EXTENSION_COVERAGE_THRESHOLD,
    TEST_SET_SIZE,
    CUCUMBER_PASSED,
    CUCUMBER_FAILED
)
from utilities import setup_logging


class ExtensionTransformer(BaseEstimator, TransformerMixin):
    """
    Custom transformer for parsing and multi-hot encoding file extensions.
    Handles list-like strings, keeps top-K extensions covering X% frequency.
    """

    def __init__(self):
        self.coverage_threshold = EXTENSION_COVERAGE_THRESHOLD
        self.top_extensions = None
        self.extension_columns = None

    def _parse_extensions(self, extension_str):
        """Parse extension string to list."""
        if pd.isna(extension_str) or extension_str == '[]':
            return []

        return ast.literal_eval(extension_str)

    def fit(self, X, _=None):
        """Fit the transformer to find top extensions."""
        if len(X.shape) > 1:
            extension_series = X.iloc[:, 0] if isinstance(X, pd.DataFrame) else X[:, 0]
        else:
            extension_series = X

        # Count all extensions
        all_extensions = []
        for ext_str in extension_series:
            extensions = self._parse_extensions(ext_str)
            all_extensions.extend(extensions)

        extension_counts = Counter(all_extensions)
        total_count = sum(extension_counts.values())

        # Find extensions covering the specified threshold
        cumulative_count = 0
        top_extensions = []

        for ext, count in extension_counts.most_common():
            cumulative_count += count
            top_extensions.append(ext)
            if cumulative_count / total_count >= self.coverage_threshold:
                break

        self.top_extensions = set(top_extensions)
        self.extension_columns = [f'ext_{ext}' for ext in sorted(self.top_extensions)]
        self.extension_columns.append('ext_other')

        return self

    def transform(self, X):
        """Transform extensions to multi-hot encoding."""
        if len(X.shape) > 1:
            extension_series = X.iloc[:, 0] if isinstance(X, pd.DataFrame) else X[:, 0]
        else:
            extension_series = X

        result = np.zeros((len(extension_series), len(self.extension_columns)))

        for i, ext_str in enumerate(extension_series):
            extensions = self._parse_extensions(ext_str)
            has_other = False

            for ext in extensions:
                if ext in self.top_extensions:
                    col_name = f'ext_{ext}'
                    col_idx = self.extension_columns.index(col_name)
                    result[i, col_idx] = 1
                else:
                    has_other = True

            if has_other:
                other_idx = self.extension_columns.index('ext_other')
                result[i, other_idx] = 1

        return result

    def get_feature_names_out(self, _=None):
        """Get output feature names."""
        return np.array(self.extension_columns)


class RunNumberTransformer(BaseEstimator, TransformerMixin):
    """
    Transform run_number: keep numeric + add binary is_first_run.
    """

    def fit(self, _=None, __=None):
        """Nothing to learn/fit here."""
        return self

    def transform(self, X):
        """Transform run_number to numeric + binary is_first_run."""
        if len(X.shape) > 1:
            run_numbers = X.iloc[:, 0] if isinstance(X, pd.DataFrame) else X[:, 0]
        else:
            run_numbers = X

        run_numeric = pd.to_numeric(run_numbers, errors='coerce').fillna(1)
        is_first_run = (run_numeric == 1).astype(int)

        return np.column_stack([run_numeric, is_first_run])

    def get_feature_names_out(self, _=None):
        """Get output feature names."""
        return np.array(['run_number', 'is_first_run'])


def log1p_transform(X):
    """Apply log1p transformation."""
    return np.log1p(np.maximum(X, 0))


def create_preprocessing_pipeline():
    """
    Create sklearn preprocessing pipeline with all transformations.
    """
    preprocessor = ColumnTransformer(
        transformers=[
            # Extensions: multi-hot encode with top-K selection
            ('extensions', ExtensionTransformer(), ['extensions']),

            # Modifications: log1p transform
            ('modifications', FunctionTransformer(log1p_transform),
             ['modifications_3d', 'modifications_14d', 'modifications_56d']),

            # Run number: numeric + binary is_first_run
            ('run_number', RunNumberTransformer(), ['run_number']),

            # Categorical: one-hot encode
            ('categorical', OneHotEncoder(sparse_output=False, handle_unknown='ignore'),
             ['feature', 'feature_category']),

            # Scenario count: log1p transform
            ('scenario_count', FunctionTransformer(log1p_transform), ['scenario_count']),

            # Failures: keep as is
            ('failures', 'passthrough',
             ['failures_7d', 'failures_14d', 'failures_28d', 'failures_56d'])
        ]
    )

    pipeline = Pipeline([
        ('preprocessor', preprocessor)
    ])

    return pipeline


def perform_data_quality_checks(df, logger):
    """
    Check and handle data quality issues.
    """
    logger.info("Performing data quality checks")

    initial_rows_count = len(df)
    logger.info("Initial dataset size: %d rows", initial_rows_count)

    # Check for nulls
    null_counts = df.isnull().sum()
    if null_counts.sum() > 0:
        logger.warning("Found null values:")
        for col, count in null_counts[null_counts > 0].items():
            logger.warning("  %s: %d nulls", col, count)

    # Check result column for valid values
    valid_results = {CUCUMBER_PASSED, CUCUMBER_FAILED}
    invalid_results = ~df['result'].isin(valid_results)
    if invalid_results.sum() > 0:
        logger.warning("Found %d invalid result values", invalid_results.sum())
        logger.info("Removing rows with invalid result values")
        df = df[~invalid_results].copy()

    # Check for valid PR numbers (should be numeric and > 0)
    df['pr_number_numeric'] = pd.to_numeric(df['pr_number'], errors='coerce')
    valid_prs = df['pr_number_numeric'] > 0
    if not valid_prs.all():
        invalid_pr_count = (~valid_prs).sum()
        logger.warning("Found %d rows with invalid PR numbers", invalid_pr_count)
        logger.info("Removing rows with invalid PR numbers")
        df = df[valid_prs].copy()

    # Fill remaining nulls with dummy values, this could be improved by using
    # a more sophisticated approach but since nulls theoretically will never occur,
    # this is fine for now.
    numeric_cols = ['modifications_3d', 'modifications_14d', 'modifications_56d',
                   'run_number', 'scenario_count', 'failures_7d', 'failures_14d',
                   'failures_28d', 'failures_56d']

    for col in numeric_cols:
        if col in df.columns:
            if col == 'run_number':
                df[col] = df[col].fillna(1)
            else:
                df[col] = df[col].fillna(0)

    # Handle extensions nulls
    if 'extensions' in df.columns:
        df['extensions'] = df['extensions'].fillna('[]')

    final_rows_count = len(df)
    logger.info("After data quality checks: %d rows", final_rows_count)
    logger.info(
        "Removed %d rows (%.2f%%)",
        initial_rows_count - final_rows_count,
        100*(initial_rows_count - final_rows_count)/initial_rows_count
    )

    return df


def perform_train_test_split(df, logger, test_size=TEST_SET_SIZE):
    """
    Split data by PR number, time-ordered: latest PRs for test.
    """
    logger.info("Performing time-ordered train/test split by PR number")

    # Get unique PR numbers sorted by descending order (latest first)
    unique_prs = sorted(df['pr_number_numeric'].unique(), reverse=True)
    logger.info("Found %d unique PR numbers", len(unique_prs))
    logger.info("PR range: %d to %d", min(unique_prs), max(unique_prs))

    # Calculate split point
    test_pr_count = max(1, int(len(unique_prs) * test_size))
    test_prs = set(unique_prs[:test_pr_count])
    train_prs = set(unique_prs[test_pr_count:])

    logger.info(
        "Test set PRs: %d PRs (latest: %d, earliest: %d)",
        test_pr_count, max(test_prs), min(test_prs)
    )
    logger.info(
        "Train set PRs: %d PRs (latest: %d, earliest: %d)",
        len(train_prs), max(train_prs), min(train_prs)
    )

    # Split data
    test_mask = df['pr_number_numeric'].isin(test_prs)
    df_test = df[test_mask].copy()
    df_train = df[~test_mask].copy()

    logger.info("Train set size: %d samples", len(df_train))
    logger.info("Test set size: %d samples", len(df_test))
    logger.info(
        "Split ratio: %.3f / %.3f", 
        len(df_train)/(len(df_train)+len(df_test)), len(df_test)/(len(df_train)+len(df_test))
    )

    return df_train, df_test


def preprocess_features(df_train, df_test, logger):
    """
    Apply feature preprocessing using sklearn pipeline.
    """
    logger.info("Preprocessing features")

    feature_columns = [
        col for col in df_train.columns if col not in ['pr_number', 'pr_number_numeric', 'result']
    ]

    X_train = df_train[feature_columns].copy()
    X_test = df_test[feature_columns].copy()

    # Convert result to binary failed label (1 if failed, 0 if passed)
    y_train = (df_train['result'] == 'failed').astype(int)
    y_test = (df_test['result'] == 'failed').astype(int)

    pipeline = create_preprocessing_pipeline()

    logger.info("Fitting preprocessing pipeline on training data")
    pipeline.fit(X_train)

    logger.info("Transforming training data")
    X_train_processed = pipeline.transform(X_train)

    logger.info("Transforming test data")
    X_test_processed = pipeline.transform(X_test)

    # Get feature names from pipeline
    feature_names = []
    for name, transformer, columns in pipeline.named_steps['preprocessor'].transformers_:
        if hasattr(transformer, 'get_feature_names_out'):
            names = transformer.get_feature_names_out()
        elif name == 'failures':  # passthrough
            names = columns
        elif name == 'modifications':
            names = [f'log1p_{col}' for col in columns]
        elif name == 'scenario_count':
            # FunctionTransformer for scenario_count
            names = ['log1p_scenario_count']
        elif name == 'categorical':
            # OneHotEncoder - get feature names
            if hasattr(transformer, 'get_feature_names_out'):
                names = transformer.get_feature_names_out(columns)
            else:
                names = [f"{name}_{col}" for col in columns]
        else:
            # Fallback for any unknown transformer
            names = [f"{name}_{i}" for i in range(len(columns))]
        feature_names.extend(names)

    logger.info("Final feature count: %d", X_train_processed.shape[1])

    return X_train_processed, X_test_processed, y_train, y_test, pipeline, feature_names


def main():
    """Main preprocessing pipeline execution."""

    logger = setup_logging(level=logging.DEBUG, log_file="logs/preprocessing_pipeline.log")

    logger.info("=" * 50)
    logger.info("Starting Preprocessing Pipeline")
    logger.info("=" * 50)

    try:
        logger.info("Loading training data from %s", TRAINING_CSV_FILENAME)
        df = pd.read_csv(TRAINING_CSV_FILENAME)
        logger.info("Loaded %d rows, %d columns", len(df), len(df.columns))

        df = perform_data_quality_checks(df, logger)

        df_train, df_test = perform_train_test_split(df, logger)

        X_train, X_test, y_train, y_test, pipeline, feature_names = preprocess_features(
            df_train, df_test, logger
        )

        logger.info("Saving preprocessing pipeline to %s", PREPROCESSING_PIPELINE_FILENAME)
        joblib.dump(pipeline, PREPROCESSING_PIPELINE_FILENAME)
        logger.info("Pipeline saved successfully")

        # Create output DataFrames
        preprocessed_train_df = pd.DataFrame(X_train, columns=feature_names[:X_train.shape[1]])
        preprocessed_train_df['failed'] = y_train.values

        preprocessed_test_df = pd.DataFrame(X_test, columns=feature_names[:X_test.shape[1]])
        preprocessed_test_df['failed'] = y_test.values

        logger.info("Exporting training data to %s", PREPROCESSED_TRAINING_CSV_FILENAME)
        preprocessed_train_df.to_csv(PREPROCESSED_TRAINING_CSV_FILENAME, index=False)

        logger.info("Exporting test data to %s", PREPROCESSED_TESTING_CSV_FILENAME)
        preprocessed_test_df.to_csv(PREPROCESSED_TESTING_CSV_FILENAME, index=False)

        logger.info("=" * 50)
        logger.info("Preprocessing Pipeline Completed Successfully")
        logger.info("=" * 50)

        # Summary statistics
        logger.info(
            "Training data: %d samples, %d features",
            len(preprocessed_train_df), len(preprocessed_train_df.columns)-1
        )
        logger.info(
            "Test data: %d samples, %d features",
            len(preprocessed_test_df), len(preprocessed_test_df.columns)-1
        )
        logger.info("Class distribution in training data: %s", Counter(y_train))
        logger.info("Class distribution in test data: %s", Counter(y_test))
    except Exception as e:
        logger.error("Pipeline failed with error: %s", str(e), exc_info=True)
        raise


if __name__ == "__main__":
    main()
