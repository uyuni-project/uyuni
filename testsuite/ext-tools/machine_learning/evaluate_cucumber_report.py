import pandas as pd
from joblib import load
import json

def evaluate_current_report(current_report_path, model_path, vectorizer_path, output_path):
    # Load data
    with open(current_report_path, 'r') as file:
        current_report = json.load(file)
    df = pd.DataFrame(current_report)

    # Load model and vectorizer
    model = load(model_path)
    vectorizer = load(vectorizer_path)

    # Preprocess and predict
    X = vectorizer.transform(df['text'])
    df['predicted_root_cause'] = model.predict(X)

    # Save predictions
    df.to_csv(output_path, index=False)
    print(f"Predictions saved to {output_path}")

if __name__ == "__main__":
    import sys
    if len(sys.argv) != 5:
        print("Usage: python evaluate_current_report.py <current_report_path> <model_path> <vectorizer_path> <output_path>")
        sys.exit(1)

    current_report_path = sys.argv[1]
    model_path = sys.argv[2]
    vectorizer_path = sys.argv[3]
    output_path = sys.argv[4]

    evaluate_current_report(current_report_path, model_path, vectorizer_path, output_path)
