#!/usr/bin/env python3
"""
XGBoost Classifier Training Script for Predictive Test Selection.

This script trains an XGBoost classifier on preprocessed training data with
severe class imbalance. It handles the imbalance using scale_pos_weight
and performs hyperparameter tuning with cross-validation.

Prerequisite:
    Run preprocessing_pipeline.py

Usage:
    python train_xgboost_classifier.py
"""
import logging
from collections import Counter

import pandas as pd
import numpy as np
import xgboost as xgb
from sklearn.model_selection import RandomizedSearchCV, TimeSeriesSplit
import joblib

from config import (
    PREPROCESSED_TRAINING_CSV_FILENAME,
    XGBOOST_MODEL_FILENAME,
    RANDOM_SEED,
    CV_FOLDS,
    N_ITER_SEARCH
)
from utilities import setup_logging


def load_training_data(logger):
    """
    Load preprocessed training data and sort by PR number for time-based CV.

    Sorting ensures TimeSeriesSplit treats older PRs as training data
    and newer PRs as validation data.
    """
    logger.info("Loading training data from %s", PREPROCESSED_TRAINING_CSV_FILENAME)
    df = pd.read_csv(PREPROCESSED_TRAINING_CSV_FILENAME)
    logger.info("Loaded %d rows, %d columns", len(df), len(df.columns))

    # Sort by PR number to ensure temporal ordering (oldest first)
    df = df.sort_values('pr_number').reset_index(drop=True)
    logger.info("Sorted data by PR number (oldest first)")

    # Separate features, target, and pr_number
    pr_numbers = df['pr_number'].values
    X = df.drop(['failed', 'pr_number'], axis=1)
    y = df['failed'].values

    logger.info("Target distribution: %s", Counter(y))
    logger.info("PR number range: %d to %d", pr_numbers.min(), pr_numbers.max())

    return X, y, pr_numbers


def calculate_scale_pos_weight(y, logger):
    """Calculate scale_pos_weight to improve training on our heavily imbalanced data."""
    neg_count = (y == 0).sum()
    pos_count = (y == 1).sum()

    scale_pos_weight = neg_count / pos_count

    logger.info("Class distribution:")
    logger.info("  Negative (passed): %d (%.2f%%)", neg_count, 100 * neg_count / len(y))
    logger.info("  Positive (failed): %d (%.2f%%)", pos_count, 100 * pos_count / len(y))
    logger.info("  Imbalance ratio: %.2f:1", scale_pos_weight)
    logger.info("Setting scale_pos_weight to: %.2f", scale_pos_weight)

    return scale_pos_weight


def create_param_grid():
    """
    Create hyperparameter search space for XGBoost.

    Key parameters for imbalanced classification:
    - max_depth: control model complexity
    - learning_rate: smaller values with more estimators are often better
    - min_child_weight: higher values prevent learning rare patterns
    - subsample/colsample_bytree: regularization to prevent overfitting
    - gamma: minimum loss reduction for split (regularization)
    """
    param_distributions = {
        'max_depth': [3, 4, 5, 6, 7],
        'learning_rate': [0.01, 0.05, 0.1, 0.2],
        'n_estimators': [100, 200, 300, 500],
        'min_child_weight': [1, 3, 5, 7],
        'subsample': [0.6, 0.7, 0.8, 0.9, 1.0],
        'colsample_bytree': [0.6, 0.7, 0.8, 0.9, 1.0],
        'gamma': [0, 0.1, 0.2, 0.3, 0.5],
        'reg_alpha': [0, 0.01, 0.1, 0.5],  # L1 regularization
        'reg_lambda': [1, 1.5, 2, 3]  # L2 regularization
    }

    return param_distributions


def train_with_hyperparameter_tuning(X, y, pr_numbers, scale_pos_weight, logger):
    """
    Train XGBoost classifier with hyperparameter tuning using RandomizedSearchCV.

    Uses sklearn's TimeSeriesSplit for time-based cross-validation:
    - Data is pre-sorted by PR number (oldest first)
    - Each fold trains on all data up to a point, validates on the next chunk
    - Ensures training on past, validation on future (predictive test selection production scenario)
    """
    logger.info("=" * 70)
    logger.info("Starting Hyperparameter Tuning with RandomizedSearchCV")
    logger.info("=" * 70)

    base_model = xgb.XGBClassifier(
        objective='binary:logistic',
        scale_pos_weight=scale_pos_weight,
        random_state=RANDOM_SEED,
        n_jobs=-1,
        tree_method='hist',
        eval_metric='aucpr'
    )

    # Parameter distributions for random search
    param_distributions = create_param_grid()

    cv = TimeSeriesSplit(n_splits=CV_FOLDS)

    logger.info("Configuration:")
    logger.info("  CV strategy: TimeSeriesSplit (train on past, validate on future)")
    logger.info("  CV folds: %d", CV_FOLDS)
    logger.info("  Random search iterations: %d", N_ITER_SEARCH)
    logger.info("  Primary metric: Average Precision (PR-AUC)")
    logger.info("  Parameter space size: ~%d combinations",
                np.prod([len(v) for v in param_distributions.values()]))

    # Log fold details for transparency
    logger.info("  Time-based CV fold structure:")
    for fold_idx, (train_idx, test_idx) in enumerate(cv.split(X), 1):
        train_pr_range = f"{pr_numbers[train_idx].min():.0f}-{pr_numbers[train_idx].max():.0f}"
        test_pr_range = f"{pr_numbers[test_idx].min():.0f}-{pr_numbers[test_idx].max():.0f}"
        logger.info("    Fold %d: Train PRs[%s] → Test PRs[%s]",
                    fold_idx, train_pr_range, test_pr_range)

    # Randomized search with time-based CV
    random_search = RandomizedSearchCV(
        estimator=base_model,
        param_distributions=param_distributions,
        n_iter=N_ITER_SEARCH,
        scoring='average_precision',
        cv=cv,
        verbose=2,
        random_state=RANDOM_SEED,
        n_jobs=-1,
        return_train_score=True
    )

    logger.info("Starting hyperparameter search...")
    random_search.fit(X, y)

    logger.info("=" * 70)
    logger.info("Hyperparameter Tuning Complete")
    logger.info("=" * 70)

    return random_search


def log_best_model_info(random_search, best_model, X, logger):
    """Log information about the best model found by hyperparameter search."""
    logger.info("Best parameters found:")
    for param, value in random_search.best_params_.items():
        logger.info("  %s: %s", param, value)

    logger.info("Best cross-validation PR-AUC: %.4f", random_search.best_score_)

    # Get CV results for the best model
    best_index = random_search.best_index_
    cv_results = random_search.cv_results_

    logger.info("Cross-validation metrics for best model:")
    logger.info("  Mean CV PR-AUC: %.4f (+/- %.4f)",
                cv_results['mean_test_score'][best_index],
                cv_results['std_test_score'][best_index])
    logger.info("  Mean train PR-AUC: %.4f (+/- %.4f)",
                cv_results['mean_train_score'][best_index],
                cv_results['std_train_score'][best_index])

    # Feature importance
    feature_importance = pd.DataFrame({
        'feature': X.columns,
        'importance': best_model.feature_importances_
    }).sort_values('importance', ascending=False)

    logger.info("Top 15 Most Important Features:")
    for _, row in feature_importance.head(15).iterrows():
        logger.info("  %s: %.4f", row['feature'], row['importance'])


def save_model(model, logger):
    """Save the trained model to disk."""
    logger.info("Saving model to %s", XGBOOST_MODEL_FILENAME)
    joblib.dump(model, XGBOOST_MODEL_FILENAME)
    logger.info("Model saved successfully")


def main():
    """Main training pipeline execution."""
    logger = setup_logging(level=logging.INFO, log_file="logs/train_xgboost_classifier.log")

    logger.info("=" * 70)
    logger.info("XGBoost Classifier Training for Predictive Test Selection")
    logger.info("=" * 70)

    try:
        X, y, pr_numbers = load_training_data(logger)

        scale_pos_weight = calculate_scale_pos_weight(y, logger)

        random_search = train_with_hyperparameter_tuning(X, y, pr_numbers, scale_pos_weight, logger)

        best_model = random_search.best_estimator_

        log_best_model_info(random_search, best_model, X, logger)

        save_model(best_model, logger)

        logger.info("=" * 70)
        logger.info("Training Pipeline Completed Successfully")
        logger.info("=" * 70)
        logger.info("Model ready for inference at: %s", XGBOOST_MODEL_FILENAME)
    except Exception as e:
        logger.error("Training pipeline failed with error: %s", str(e), exc_info=True)
        raise


if __name__ == "__main__":
    main()
