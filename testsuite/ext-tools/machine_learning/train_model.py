import pandas as pd
from sklearn.linear_model import SGDClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import train_test_split
from sklearn.metrics import classification_report
from joblib import dump, load
import base64
import sys
import numpy as np
import json

def decode_base64(encoded_str):
    """Decode Base64 encoded string to its original form."""
    try:
        if encoded_str:
            return base64.b64decode(encoded_str).decode('utf-8')
        return ''
    except Exception as e:
        print(f"Error decoding string: {e}")
        return encoded_str

def decode_description(df):
    """Parse and decode Base64-encoded fields nested in the 'description' column."""
    def decode_nested_fields(description):
        try:
            # Parse the JSON object embedded in the 'description' column
            data = json.loads(description)
            # Decode individual fields if they exist
            if 'description' in data:
                data['description'] = decode_base64(data['description'])
            if 'comments' in data:
                data['comments'] = decode_base64(data['comments'])
            if 'error_message' in data:
                data['error_message'] = decode_base64(data['error_message'])
            if 'logs' in data:
                data['logs'] = decode_base64(data['logs'])
            return data
        except Exception as e:
            print(f"Error processing description: {e}")
            return description  # Return original description on failure

    # Apply decoding logic to the 'description' column
    df['description'] = df['description'].apply(decode_nested_fields)
    return df


def train_model(dataset_path, model_path, vectorizer_path, output_model_path, output_vectorizer_path):
    # Load previous model and vectorizer
    try:
        model = load(model_path)
        print(f"Loaded existing model from {model_path}")
    except FileNotFoundError:
        # Initialize SGDClassifier if no previous model exists
        model = SGDClassifier(loss="log", random_state=42, warm_start=True)
        print("No previous model found, initializing a new model.")

    try:
        vectorizer = load(vectorizer_path)
        print(f"Loaded existing vectorizer from {vectorizer_path}")
    except FileNotFoundError:
        # Initialize a new vectorizer if no previous one exists
        vectorizer = TfidfVectorizer(max_features=5000)
        print("No previous vectorizer found, initializing a new vectorizer.")

    # Load and preprocess the new dataset
    df = pd.read_json(dataset_path)

    # Decode the Base64 encoded fields
    print("Decoding Base64-encoded fields...")
    df = decode_description(df)

    # Vectorize the decoded 'text' field
    X = vectorizer.fit_transform(df['description'])
    y = df['label'].tolist()

    # Incrementally update the model
    print("Updating model incrementally with new data...")
    model.partial_fit(X, y, classes=np.unique(y))  # Incremental fit with new data

    # Evaluate the updated model (optional, for reporting)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)
    y_pred = model.predict(X_test)
    print("Classification Report (Root Cause):")
    print(classification_report(y_test, y_pred))

    # Save the updated model and vectorizer
    dump(model, output_model_path)
    dump(vectorizer, output_vectorizer_path)
    print(f"Updated model saved to {output_model_path}")
    print(f"Updated vectorizer saved to {output_vectorizer_path}")

if __name__ == "__main__":
    if len(sys.argv) != 6:
        print("Usage: python train_model.py <new_dataset_path> <model_path> <vectorizer_path> <output_model_path> <output_vectorizer_path>")
        sys.exit(1)

    dataset_path = sys.argv[1]
    model_path = sys.argv[2]
    vectorizer_path = sys.argv[3]
    output_model_path = sys.argv[4]
    output_vectorizer_path = sys.argv[5]

    train_model(dataset_path, model_path, vectorizer_path, output_model_path, output_vectorizer_path)
