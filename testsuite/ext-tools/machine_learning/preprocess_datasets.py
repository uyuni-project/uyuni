import simplejson as json
import pandas as pd

def load_json(dataset_path):
    """Load JSON dataset from a file using json library."""
    with open(dataset_path, 'r', encoding='utf-8') as file:
        data = json.load(file)
    return data


def preprocess_github_issues(gh_issues_data):
    gh_issues_df = pd.DataFrame(gh_issues_data)
    gh_issues_df['test_case'] = gh_issues_df['description'].apply(
        lambda d: f"{d.get('feature', '')} | {d.get('scenario', '')}" if isinstance(d, dict) else ''
    )

    # Combine 'description', 'logs', and 'comments' fields for log-based matching
    gh_issues_df['description'] = (
            'description:' + gh_issues_df['description'].apply(lambda d: d.get('description', '') if isinstance(d, dict) else '') +
            'logs:' + gh_issues_df['description'].apply(lambda d: ''.join(d.get('logs', [])) if isinstance(d, dict) else '') +
            'comments:' + gh_issues_df['description'].apply(lambda d: ''.join(d.get('comments', [])) if isinstance(d, dict) else '')
    )

    gh_issues_df['label'] = gh_issues_df['label'].astype(int)
    return gh_issues_df[['test_case', 'description', 'label']]


def preprocess_cucumber_history(cucumber_history_data):
    cucumber_history_df = pd.DataFrame(cucumber_history_data)
    cucumber_history_df['test_case'] = cucumber_history_df['description'].apply(
        lambda d: f"{d.get('feature', '')} | {d.get('scenario', '')}" if isinstance(d, dict) else ''
    )

    # Combine 'age' and 'failedsince' fields for historical tracking
    cucumber_history_df['description'] = cucumber_history_df['description'].apply(
        lambda d: f"age:{d.get('age', 0)} failed_since:{d.get('failedsince', 0)}" if isinstance(d, dict) else ''
    )

    cucumber_history_df['label'] = cucumber_history_df['label'].astype(int)
    return cucumber_history_df[['test_case', 'description', 'label']]


def preprocess_current_report(current_report_data):
    cucumber_report_df = pd.DataFrame(current_report_data)
    cucumber_report_df['test_case'] = cucumber_report_df['description'].apply(
        lambda d: f"{d.get('feature', '')} | {d.get('scenario', '')}" if isinstance(d, dict) else ''
    )

    # Combine 'description', 'logs', and 'comments' fields for log-based matching
    cucumber_report_df['description'] = (
            'error:' + cucumber_report_df['description'].apply(lambda d: d.get('error_message', '') if isinstance(d, dict) else '') +
            'logs:' + cucumber_report_df['description'].apply(lambda d: ''.join(d.get('logs', [])) if isinstance(d, dict) else '')
    )

    cucumber_report_df['label'] = cucumber_report_df['label'].astype(int)
    return cucumber_report_df[['test_case', 'description', 'label']]


def merge_datasets(gh_issues_df, cucumber_history_df, current_report_df):
    """Merge all datasets into a unified format without duplicates."""

    # Merge on feature-scenario for log-based matching
    combined_logs_df = pd.merge(
        current_report_df,
        gh_issues_df,
        on=['test_case', 'description'],
        suffixes=('_current', '_gh')
    )

    # Concatenate GitHub Issues data for log-based matching
    combined_df = pd.concat([combined_logs_df, cucumber_history_df], axis=0, ignore_index=True)

    # Convert unhashable types (e.g., dict) to strings for deduplication
    for col in ['test_case', 'description', 'label']:
        if col in combined_df.columns:
            combined_df[col] = combined_df[col].apply(lambda x: json.dumps(x) if isinstance(x, dict) else x)

    # Remove duplicates based on key columns
    combined_df = combined_df.drop_duplicates(subset=['test_case', 'description', 'label'], keep='first')

    return combined_df


def main(gh_issues_path, cucumber_history_path, current_report_path, output_path):
    gh_issues_data = load_json(gh_issues_path)
    cucumber_history_data = load_json(cucumber_history_path)
    current_report_data = load_json(current_report_path)

    gh_issues_df = preprocess_github_issues(gh_issues_data)
    cucumber_history_df = preprocess_cucumber_history(cucumber_history_data)
    current_report_df = preprocess_current_report(current_report_data)

    combined_df = merge_datasets(gh_issues_df, cucumber_history_df, current_report_df)

    # Save the combined dataset to a JSON file
    combined_df.to_json(output_path, orient='records')
    print(f"Combined dataset saved to {output_path}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 5:
        print("Usage: python preprocess_datasets.py <github_issues_path> <cucumber_history_path> <current_report_path> <output_path>")
        sys.exit(1)

    gh_issues_path = sys.argv[1]
    cucumber_history_path = sys.argv[2]
    current_report_path = sys.argv[3]
    output_path = sys.argv[4]

    main(gh_issues_path, cucumber_history_path, current_report_path, output_path)
