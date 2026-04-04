#!/usr/bin/env python3
"""
Predictive Test Selection - Prediction Script

This script operates in two modes:
1. Calibration Mode: Calibrates ScoreCutOff and CountCutOff to meet desired performance metrics
2. Prediction Mode: Applies trained model and calibrated cutoffs to predict tests for a new PR

Usage:
Calibration Mode:
    python predict.py --mode calibration --test-recall 0.90 --change-recall 1 --selection-rate 0.30

Prediction Mode:
    python predict.py --mode prediction --input-csv new_pr.csv --cutoff-params calibration.json
"""
import argparse
import json
import logging
from pathlib import Path

import joblib
import matplotlib.pyplot as plt
from matplotlib.ticker import MultipleLocator
import numpy as np
import pandas as pd
import seaborn as sns

from config import (
    PREPROCESSED_TESTING_CSV_FILENAME,
    XGBOOST_MODEL_FILENAME
)
from utilities import setup_logging

# Set visualization style
sns.set_style("whitegrid")
plt.rcParams['figure.figsize'] = (12, 8)


class PredictiveTestSelector:
    """Core logic for predictive test selection."""

    def __init__(self, model_path, logger):
        """Initialize with trained model."""
        self.logger = logger
        self.logger.info("Loading trained model from %s", model_path)
        self.model = joblib.load(model_path)
        self.logger.info("Trained model loaded successfully")

    def get_feature_columns(self, df):
        """Get feature column names (test names) from dataframe."""
        # Find columns that start with 'feature_' but not 'feature_category_'
        feature_cols = [col for col in df.columns
                       if col.startswith('feature_') and not col.startswith('feature_category_')]
        return feature_cols

    def extract_test_name(self, feature_col):
        """Extract test name from one-hot encoded feature column name."""
        # Remove 'feature_' prefix to get the test name
        return feature_col.replace('feature_', '')

    def predict_probabilities(self, X):
        """Predict failure probabilities for all tests."""
        # Get probabilities of failure (class 1)
        probs = self.model.predict_proba(X)[:, 1]
        return probs

    def apply_cutoffs(self, pr_df, feature_cols, score_cutoff, count_cutoff):
        """
        Apply ScoreCutOff and CountCutOff to select tests for a PR.

        Returns:
            selected_tests: List of (test_name, probability) tuples sorted by probability (desc)
            all_test_probs: List of all (test_name, probability, was_selected, failed) tuples
        """
        # Get features for prediction (exclude metadata)
        X = pr_df.drop(['pr_number', 'failed'], axis=1, errors='ignore')

        # Predict probabilities
        probs = self.predict_probabilities(X)

        # Map each test to its probability and actual result
        test_results = []
        for idx, row in pr_df.iterrows():
            # Find which test this row represents (which feature column is 1)
            for feature_col in feature_cols:
                if row[feature_col] == 1:
                    test_name = self.extract_test_name(feature_col)
                    probability = probs[idx - pr_df.index[0]]  # Get correct index in probs array
                    failed = row.get('failed', 0)
                    test_results.append({
                        'test': test_name,
                        'probability': probability,
                        'failed': failed
                    })
                    break

        # Sort by probability (descending)
        test_results.sort(key=lambda x: x['probability'], reverse=True)

        # Apply ScoreCutOff
        selected_by_score = [t for t in test_results if t['probability'] >= score_cutoff]

        # Apply CountCutOff (ensure minimum tests are selected)
        if len(selected_by_score) < count_cutoff:
            selected_tests = test_results[:count_cutoff]
        else:
            selected_tests = selected_by_score

        # Create list of selected test names and probabilities
        selected_list = [(t['test'], t['probability']) for t in selected_tests]

        # Create comprehensive list with selection flags
        all_test_probs = [(t['test'], t['probability'], t in selected_tests, t['failed'])
                         for t in test_results]

        return selected_list, all_test_probs


class CalibrationModeExecutor:
    """Handles calibration mode: calibrates cutoffs to meet performance targets."""

    def __init__(self, selector, logger):
        self.selector = selector
        self.logger = logger
        self.results = []  # Store (score_cutoff, count_cutoff, metrics) tuples

    def run_calibration(
        self, df_test, target_test_recall, target_change_recall, target_selection_rate):
        """
        Run calibration to find cutoffs meeting target metrics.

        Args:
            df_test: Test dataframe
            target_test_recall: Desired TestRecall (0.0-1.0)
            target_change_recall: Desired ChangeRecall (0.0-1.0)
            target_selection_rate: Desired SelectionRate (0.0-1.0)
        
        Returns:
            best_match: Dictionary with best ScoreCutOff, CountCutOff, TestRecall, ChangeRecall,
            SelectionRate, and OptimizationScore
        """
        self.logger.info("=" * 80)
        self.logger.info("Starting Calibration Process")
        self.logger.info("=" * 80)
        self.logger.info("Target TestRecall: %.2f%%", target_test_recall * 100)
        self.logger.info("Target ChangeRecall: %.2f%%", target_change_recall * 100)
        self.logger.info("Target SelectionRate: %.2f%%", target_selection_rate * 100)

        feature_cols = self.selector.get_feature_columns(df_test)
        total_tests = len(feature_cols)
        self.logger.info("Number of possible tests: %d", total_tests)

        unique_prs = sorted(df_test['pr_number'].unique())
        self.logger.info("Total PRs in test set: %d", len(unique_prs))

        # Define search space
        score_cutoffs = np.arange(0.00, 1.00, 0.05)  # 0.00, 0.05, 0.10, 0.15, 0.20, ..., 0.95
        count_cutoffs = range(1, total_tests//2)  # 1, 2, 3, ..., total_tests/2

        total_combinations = len(score_cutoffs) * len(count_cutoffs)
        self.logger.info("Testing %d combinations of cutoff parameters", total_combinations)
        self.logger.info(
            "ScoreCutOff range: %.2f to %.2f (step=0.05)", min(score_cutoffs), max(score_cutoffs)
        )
        self.logger.info("CountCutOff range: %d to %d", min(count_cutoffs), max(count_cutoffs))

        # Test all combinations
        combination_count = 0
        for score_cutoff in score_cutoffs:
            for count_cutoff in count_cutoffs:
                combination_count += 1

                if combination_count % 50 == 0:
                    self.logger.info(
                        "Progress: %d/%d combinations tested", combination_count, total_combinations
                    )

                metrics = self._evaluate_cutoffs(
                    df_test, unique_prs, feature_cols, score_cutoff, count_cutoff
                )

                self.results.append((score_cutoff, count_cutoff, metrics))

        self.logger.info("=" * 80)
        self.logger.info("Calibration Complete: Tested %d combinations", len(self.results))
        self.logger.info("=" * 80)

        # Find best matching combination
        best_match = self._find_best_match(
            target_test_recall, target_change_recall, target_selection_rate
        )

        self._generate_visualizations(df_test)

        return best_match

    def _evaluate_cutoffs(self, df_test, unique_prs, feature_cols, score_cutoff, count_cutoff):
        """
        Evaluate a specific combination of cutoffs on all PRs.

        Returns:
            metrics: Dictionary with TestRecall, ChangeRecall, SelectionRate
        """
        total_failed_tests = 0
        correctly_predicted_failed_tests = 0
        prs_with_failures = 0
        prs_with_at_least_one_failure_predicted = 0
        total_tests_selected = 0
        total_tests_evaluated = 0

        for pr_number in unique_prs:
            pr_df = df_test[df_test['pr_number'] == pr_number]

            # Apply cutoffs
            selected_tests, all_test_probs = self.selector.apply_cutoffs(
                pr_df, feature_cols, score_cutoff, count_cutoff
            )

            # Calculate statistics for PR
            pr_has_failures = False
            predicted_at_least_one_failure = False

            for _, _, was_selected, failed in all_test_probs:
                if failed:
                    pr_has_failures = True
                    total_failed_tests += 1

                    if was_selected:
                        predicted_at_least_one_failure = True
                        correctly_predicted_failed_tests += 1

            if pr_has_failures:
                prs_with_failures += 1
                if predicted_at_least_one_failure:
                    prs_with_at_least_one_failure_predicted += 1

            # Selection rate calculation
            total_tests_selected += len(selected_tests)
            total_tests_evaluated += len(all_test_probs)

        # Calculate metrics
        def _safe_divide(a, b):
            return a / b if b > 0 else 0.0
        test_recall = _safe_divide(correctly_predicted_failed_tests, total_failed_tests)
        change_recall = _safe_divide(prs_with_at_least_one_failure_predicted, prs_with_failures)
        selection_rate = _safe_divide(total_tests_selected, total_tests_evaluated)

        return {
            'test_recall': test_recall,
            'change_recall': change_recall,
            'selection_rate': selection_rate
        }

    def _find_best_match(self, target_test_recall, target_change_recall, target_selection_rate):
        """
        Find the best cutoff combination that maximizes ChangeRecall and TestRecall
        while minimizing SelectionRate.
        
        Goal: ChangeRecall >= target, TestRecall >= target, SelectionRate <= target
        Optimization: Maximize (ChangeRecall + TestRecall) while minimizing SelectionRate
        """
        self.logger.info("Searching for optimal cutoffs...")
        self.logger.info("Optimization goal: Maximize recall metrics, minimize selection rate")

        # Find candidates that meet or exceed recall targets and meet or are below selection target
        candidates = []

        for score_cutoff, count_cutoff, metrics in self.results:
            # Check if metrics meet targets
            change_recall_meets = metrics['change_recall'] >= target_change_recall
            test_recall_meets = metrics['test_recall'] >= target_test_recall
            selection_rate_meets = metrics['selection_rate'] <= target_selection_rate

            if change_recall_meets and test_recall_meets and selection_rate_meets:
                # Calculate optimization score: maximize recalls, minimize selection rate
                # Higher score is better
                optimization_score = (
                    metrics['change_recall'] * 2.0 +  # ChangeRecall is most important
                    metrics['test_recall'] * 1.5 -     # TestRecall is also important
                    metrics['selection_rate'] * 1.0    # Minimize SelectionRate
                )

                candidates.append({
                    'score_cutoff': score_cutoff,
                    'count_cutoff': count_cutoff,
                    'test_recall': metrics['test_recall'],
                    'change_recall': metrics['change_recall'],
                    'selection_rate': metrics['selection_rate'],
                    'optimization_score': optimization_score
                })

        if candidates:
            # Sort by optimization score (best first)
            candidates.sort(key=lambda x: x['optimization_score'], reverse=True)
            best = candidates[0]

            self.logger.info("Found %d matching combination(s)", len(candidates))
            self.logger.info("Best match:")
            self.logger.info("  ScoreCutOff: %.2f", best['score_cutoff'])
            self.logger.info("  CountCutOff: %d", best['count_cutoff'])
            self.logger.info("  ChangeRecall: %.2f%% (target: ≥%.2f%%)",
                           best['change_recall'] * 100, target_change_recall * 100)
            self.logger.info("  TestRecall: %.2f%% (target: ≥%.2f%%)",
                           best['test_recall'] * 100, target_test_recall * 100)
            self.logger.info("  SelectionRate: %.2f%% (target: ≤%.2f%%)",
                           best['selection_rate'] * 100, target_selection_rate * 100)
            self.logger.info("  Optimization Score: %.4f", best['optimization_score'])

            return best
        else:
            self.logger.warning("No combination found meeting all targets")
            self.logger.info("Searching for best compromise...")

            # Find best compromise: prioritize ChangeRecall, then TestRecall, then SelectionRate
            best_compromise = max(self.results,
                                 key=lambda x: (x[2]['change_recall'],
                                              x[2]['test_recall'],
                                              -x[2]['selection_rate']))
            sc, cc, m = best_compromise

            self.logger.info("Best achievable combination:")
            self.logger.info("  ScoreCutOff: %.2f, CountCutOff: %d", sc, cc)
            self.logger.info("  ChangeRecall: %.2f%% (target: ≥%.2f%%) %s",
                           m['change_recall']*100, target_change_recall*100,
                           "✓" if m['change_recall'] >= target_change_recall else "✗")
            self.logger.info("  TestRecall: %.2f%% (target: ≥%.2f%%) %s",
                           m['test_recall']*100, target_test_recall*100,
                           "✓" if m['test_recall'] >= target_test_recall else "✗")
            self.logger.info("  SelectionRate: %.2f%% (target: ≤%.2f%%) %s",
                           m['selection_rate']*100, target_selection_rate*100,
                           "✓" if m['selection_rate'] <= target_selection_rate else "✗")

            return None

    def _generate_visualizations(self, _df_test):
        """Generate visualization graphs for calibration results."""
        self.logger.info("Generating visualization graphs...")

        # Convert results to dataframe for easier plotting
        results_data = []
        for score_cutoff, count_cutoff, metrics in self.results:
            results_data.append({
                'ScoreCutOff': score_cutoff,
                'CountCutOff': count_cutoff,
                'TestRecall': metrics['test_recall'],
                'ChangeRecall': metrics['change_recall'],
                'SelectionRate': metrics['selection_rate']
            })

        results_df = pd.DataFrame(results_data)

        # Create output directory
        output_dir = Path("visualizations")
        output_dir.mkdir(exist_ok=True)

        self._plot_selection_rate_vs_change_recall(results_df, output_dir)

        self._plot_selection_rate_vs_test_recall(results_df, output_dir)

        self._plot_change_recall_heatmap(results_df, output_dir)

        self._plot_test_recall_heatmap(results_df, output_dir)

        self.logger.info("Visualizations saved to %s/", output_dir)

    def _plot_selection_rate_vs_change_recall(self, results_df, output_dir):
        """
        Shows the minimum SelectionRate needed to achieve each unique ChangeRecall value.
        """
        plt.figure(figsize=(12, 7))

        # For each unique ChangeRecall value, find the minimum SelectionRate
        unique_recalls = results_df.groupby('ChangeRecall')['SelectionRate'].min().reset_index()
        unique_recalls = unique_recalls.sort_values('SelectionRate')

        plt.plot(unique_recalls['SelectionRate'], unique_recalls['ChangeRecall'],
                marker='o', linewidth=2, markersize=6, color='#2E86AB',
                markerfacecolor='#A23B72', markeredgecolor='black', markeredgewidth=0.5)

        plt.xlabel('SelectionRate', fontsize=12, fontweight='bold')
        plt.ylabel('ChangeRecall', fontsize=12, fontweight='bold')
        plt.title('Minimum SelectionRate to Achieve Each ChangeRecall Level',
                 fontsize=14, fontweight='bold')
        plt.grid(True, alpha=0.3)

        ax = plt.gca()
        ax.xaxis.set_major_locator(MultipleLocator(0.05))
        ax.yaxis.set_major_locator(MultipleLocator(0.05))

        plt.tight_layout()
        plt.savefig(output_dir / 'selection_rate_vs_change_recall.png', dpi=300)
        plt.close()

    def _plot_selection_rate_vs_test_recall(self, results_df, output_dir):
        """
        Plot SelectionRate vs TestRecall (minimum selection for each unique recall).
        """
        plt.figure(figsize=(12, 7))

        # For each unique TestRecall value, find the minimum SelectionRate
        unique_recalls = results_df.groupby('TestRecall')['SelectionRate'].min().reset_index()
        unique_recalls = unique_recalls.sort_values('SelectionRate')

        plt.plot(unique_recalls['SelectionRate'], unique_recalls['TestRecall'],
                marker='o', linewidth=2, markersize=6, color='#2E86AB',
                markerfacecolor='#F18F01', markeredgecolor='black', markeredgewidth=0.5)

        plt.xlabel('SelectionRate', fontsize=12, fontweight='bold')
        plt.ylabel('TestRecall', fontsize=12, fontweight='bold')
        plt.title('Minimum SelectionRate to Achieve Each TestRecall Level',
                 fontsize=14, fontweight='bold')
        plt.grid(True, alpha=0.3)

        ax = plt.gca()
        ax.xaxis.set_major_locator(MultipleLocator(0.05))
        ax.yaxis.set_major_locator(MultipleLocator(0.05))

        plt.tight_layout()
        plt.savefig(output_dir / 'selection_rate_vs_test_recall.png', dpi=300)
        plt.close()

    def _plot_change_recall_heatmap(self, results_df, output_dir):
        """
        Plot heatmap of ChangeRecall across ScoreCutOff and CountCutOff.
        
        Shows ChangeRecall for all combinations of cutoff parameters.
        """
        plt.figure(figsize=(16, 10))

        heatmap_data = results_df.pivot_table(
            values='ChangeRecall',
            index='CountCutOff',
            columns='ScoreCutOff'
        )

        ax = sns.heatmap(heatmap_data, cmap='RdYlGn', annot=False,
                        cbar_kws={'label': 'ChangeRecall'},
                        linewidths=0.5, linecolor='gray')

        # Format colorbar
        cbar = ax.collections[0].colorbar
        cbar.set_label('ChangeRecall', fontsize=12, fontweight='bold', rotation=270, labelpad=20)

        # Round the x-axis labels to 2 decimal places for a cleaner look
        x_labels = [f'{float(x):.2f}' for x in heatmap_data.columns]
        ax.set_xticklabels(x_labels, rotation=45, ha='right')

        plt.xlabel('ScoreCutOff', fontsize=12, fontweight='bold')
        plt.ylabel('CountCutOff', fontsize=12, fontweight='bold')
        plt.title('ChangeRecall Heatmap: ScoreCutOff vs CountCutOff',
                 fontsize=14, fontweight='bold')
        plt.tight_layout()
        plt.savefig(output_dir / 'change_recall_heatmap.png', dpi=300)
        plt.close()

    def _plot_test_recall_heatmap(self, results_df, output_dir):
        """
        Plot heatmap of TestRecall across ScoreCutOff and CountCutOff.
        
        Shows TestRecall for all combinations of cutoff parameters.
        Darker green = higher TestRecall (better).
        """
        plt.figure(figsize=(16, 10))

        heatmap_data = results_df.pivot_table(
            values='TestRecall',
            index='CountCutOff',
            columns='ScoreCutOff'
        )

        ax = sns.heatmap(heatmap_data, cmap='RdYlGn', annot=False,
                        cbar_kws={'label': 'TestRecall'},
                        linewidths=0.5, linecolor='gray')

        # Format colorbar
        cbar = ax.collections[0].colorbar
        cbar.set_label('TestRecall', fontsize=12, fontweight='bold', rotation=270, labelpad=20)

        # Round the x-axis labels to 2 decimal places for a cleaner look
        x_labels = [f'{float(x):.2f}' for x in heatmap_data.columns]
        ax.set_xticklabels(x_labels, rotation=45, ha='right')

        plt.xlabel('ScoreCutOff', fontsize=12, fontweight='bold')
        plt.ylabel('CountCutOff', fontsize=12, fontweight='bold')
        plt.title('TestRecall Heatmap: ScoreCutOff vs CountCutOff',
                 fontsize=14, fontweight='bold')
        plt.tight_layout()
        plt.savefig(output_dir / 'test_recall_heatmap.png', dpi=300)
        plt.close()


class PredictionModeExecutor:
    """Handles prediction mode: applies calibrated cutoffs to new PR."""

    def __init__(self, selector, logger):
        self.selector = selector
        self.logger = logger

    def run_prediction(self, input_csv, cutoff_params_file, output_json):
        """
        Run prediction on a new PR using calibrated cutoffs.

        Args:
            input_csv: Path to preprocessed CSV for single PR
            cutoff_params_file: Path to JSON file containing calibrated cutoff parameters
            output_json: Path to output JSON file
        """
        self.logger.info("=" * 80)
        self.logger.info("Prediction Mode: Predicting Tests for New PR")
        self.logger.info("=" * 80)
        self.logger.info("Input CSV: %s", input_csv)
        self.logger.info("Cutoff parameters file: %s", cutoff_params_file)

        # Load cutoff parameters from JSON file
        with open(cutoff_params_file, 'r', encoding='utf-8') as f:
            cutoff_data = json.load(f)

        score_cutoff = cutoff_data['calibrated_cutoffs']['score_cutoff']
        count_cutoff = cutoff_data['calibrated_cutoffs']['count_cutoff']

        self.logger.info("ScoreCutOff: %.2f", score_cutoff)
        self.logger.info("CountCutOff: %d", count_cutoff)

        # Load PR data
        df_pr = pd.read_csv(input_csv)
        self.logger.info("Loaded %d test samples", len(df_pr))

        feature_cols = self.selector.get_feature_columns(df_pr)

        # Predict failure probabilities and apply cutoffs
        selected_tests, _ = self.selector.apply_cutoffs(
            df_pr, feature_cols, score_cutoff, count_cutoff
        )

        self.logger.info(
            "Selected %d tests out of %d total tests", len(selected_tests), len(feature_cols)
        )

        output_data = {
            'cutoff_parameters': {
                'score_cutoff': round(float(score_cutoff), 2),
                'count_cutoff': int(count_cutoff)
            },
            'selected_tests': [
                {
                    'test_name': test_name,
                    'failure_probability': round(float(prob), 2),
                    'rank': rank + 1
                }
                for rank, (test_name, prob) in enumerate(selected_tests)
            ],
            'summary': {
                'total_tests': len(feature_cols),
                'selected_tests': len(selected_tests),
                'selection_rate': len(selected_tests) / len(feature_cols)
            }
        }

        # Save to JSON
        with open(output_json, 'w', encoding='utf-8') as f:
            json.dump(output_data, f, indent=2)

        self.logger.info("Results saved to %s", output_json)
        self.logger.info("=" * 80)

        # Log top 10 selected tests
        self.logger.info("Top 10 Selected Tests:")
        for i, test_data in enumerate(output_data['selected_tests'][:10], 1):
            self.logger.info("  %d. %s (prob=%.4f)",
                           i, test_data['test_name'], test_data['failure_probability'])

        return output_data


def main():
    """Main entry point for the prediction script."""
    parser = argparse.ArgumentParser(
        description='Predictive Test Selection - Prediction Script',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
    Examples:
    Calibration Mode:
    python predict.py --mode calibration --test-recall 0.90 --change-recall 1 --selection-rate 0.30

    Prediction Mode:
    python predict.py --mode prediction --input-csv new_pr.csv --cutoff-params calibration.json
            """
    )

    parser.add_argument('--mode', choices=['calibration', 'prediction'], required=True,
                       help='Operation mode')

    # Calibration mode arguments
    parser.add_argument('--test-recall', type=float,
                       help='Target TestRecall (0.0-1.0) for calibration mode')
    parser.add_argument('--change-recall', type=float,
                       help='Target ChangeRecall (0.0-1.0) for calibration mode')
    parser.add_argument('--selection-rate', type=float,
                       help='Target SelectionRate (0.0-1.0) for calibration mode')

    # Prediction mode arguments
    parser.add_argument('--input-csv', type=str,
                       help='Input PR/Tests CSV file for prediction mode')
    parser.add_argument('--cutoff-params', type=str,
                       help='JSON file containing calibrated cutoff parameters for prediction mode')

    # Optional arguments
    parser.add_argument('--output-json', type=str, default='artifacts/prediction_results.json',
                       help='Output JSON file (default: artifacts/prediction_results.json)')
    parser.add_argument('--log-file', type=str, default='logs/predict.log',
                       help='Log file path (default: logs/predict.log)')

    args = parser.parse_args()

    logger = setup_logging(level=logging.INFO, log_file=args.log_file)

    logger.info("=" * 80)
    logger.info("Predictive Test Selection - Prediction Script")
    logger.info("=" * 80)
    logger.info("Mode: %s", args.mode.upper())

    try:
        selector = PredictiveTestSelector(XGBOOST_MODEL_FILENAME, logger)

        if args.mode == 'calibration':
            if any(v is None for v in (args.test_recall, args.change_recall, args.selection_rate)):
                parser.error(
                    "Calibration mode requires --test-recall, --change-recall, and --selection-rate"
                )

            if not 0.0 <= args.test_recall <= 1.0:
                parser.error("--test-recall must be between 0.0 and 1.0")
            if not 0.0 <= args.change_recall <= 1.0:
                parser.error("--change-recall must be between 0.0 and 1.0")
            if not 0.0 <= args.selection_rate <= 1.0:
                parser.error("--selection-rate must be between 0.0 and 1.0")

            logger.info("Loading test data from %s", PREPROCESSED_TESTING_CSV_FILENAME)
            df_test = pd.read_csv(PREPROCESSED_TESTING_CSV_FILENAME)
            logger.info("Loaded %d samples from %d PRs",
                       len(df_test), df_test['pr_number'].nunique())

            executor = CalibrationModeExecutor(selector, logger)
            best_match = executor.run_calibration(
                df_test,
                args.test_recall,
                args.change_recall,
                args.selection_rate
            )

            if best_match:
                output = {
                    'status': 'success',
                    'calibrated_cutoffs': {
                        'score_cutoff': round(float(best_match['score_cutoff']), 2),
                        'count_cutoff': int(best_match['count_cutoff'])
                    },
                    'achieved_metrics': {
                        'test_recall': round(float(best_match['test_recall']), 3),
                        'change_recall': round(float(best_match['change_recall']), 3),
                        'selection_rate': round(float(best_match['selection_rate']), 3)
                    },
                    'target_metrics': {
                        'test_recall': float(args.test_recall),
                        'change_recall': float(args.change_recall),
                        'selection_rate': float(args.selection_rate)
                    }
                }
            else:
                output = {
                    'status': 'failed',
                    'message': 'Unable to find cutoffs meeting all target metrics',
                    'recommendation': 'Review generated visualizations and adjust target metrics'
                }

            with open(args.output_json, 'w', encoding='utf-8') as f:
                json.dump(output, f, indent=2)

            logger.info("Results saved to %s", args.output_json)

        elif args.mode == 'prediction':
            if args.input_csv is None or args.cutoff_params is None:
                parser.error(
                    "Prediction mode requires --input-csv and --cutoff-params"
                )

            executor = PredictionModeExecutor(selector, logger)
            executor.run_prediction(
                args.input_csv,
                args.cutoff_params,
                args.output_json
            )

        logger.info("=" * 80)
        logger.info("Script Completed Successfully")
        logger.info("=" * 80)
    except Exception as e:
        logger.error("Script failed with error: %s", str(e), exc_info=True)
        raise


if __name__ == "__main__":
    main()
