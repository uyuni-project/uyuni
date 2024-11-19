import sys
import json
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report
from sklearn.preprocessing import LabelEncoder
from joblib import dump
from transformers import BertTokenizer, BertForSequenceClassification, Trainer, TrainingArguments
from datasets import Dataset
import torch

def load_dataset(filepath):
    """Load JSON dataset from a file."""
    with open(filepath, 'r') as file:
        data = json.load(file)
    return data

def preprocess_data(data):
    """Combine and clean title, description, and comments."""
    texts, labels = [], []
    for entry in data:
        combined_text = " ".join([
            entry.get('title', ''),
            entry.get('description', ''),
            " ".join(entry.get('comments', []))
        ])
        texts.append(combined_text)
        labels.append(entry['label'])
    return texts, labels

def train_random_forest(texts, labels):
    # Encode labels
    label_encoder = LabelEncoder()
    encoded_labels = label_encoder.fit_transform(labels)

    # Split the dataset
    X_train, X_test, y_train, y_test = train_test_split(
        texts, encoded_labels, test_size=0.2, random_state=42
    )

    # Convert text data to TF-IDF features
    vectorizer = TfidfVectorizer(max_features=5000)
    X_train_vec = vectorizer.fit_transform(X_train)
    X_test_vec = vectorizer.transform(X_test)

    # Train a Random Forest classifier
    model = RandomForestClassifier(n_estimators=100, random_state=42)
    model.fit(X_train_vec, y_train)

    # Evaluate the model
    y_pred = model.predict(X_test_vec)
    unique_labels = np.unique(y_test)
    target_names = [str(label) for label in label_encoder.inverse_transform(unique_labels)]

    print("Classification Report (Random Forest):")
    print(classification_report(y_test, y_pred, labels=unique_labels, target_names=target_names, zero_division=0))

    # Save the model and vectorizer
    dump(model, 'random_forest_model.joblib')
    dump(vectorizer, 'random_forest_vectorizer.joblib')
    dump(label_encoder, 'random_forest_label_encoder.joblib')
    print("Random Forest model, vectorizer, and label encoder saved successfully!")

def train_bert(texts, labels):
    # Split into train and test sets
    train_texts, test_texts, train_labels, test_labels = train_test_split(
        texts, labels, test_size=0.2, random_state=42
    )

    # Convert to Hugging Face Dataset
    train_data = Dataset.from_dict({"text": train_texts, "label": train_labels})
    test_data = Dataset.from_dict({"text": test_texts, "label": test_labels})

    # Tokenize the data
    tokenizer = BertTokenizer.from_pretrained("bert-base-uncased")
    def tokenize_function(example):
        return tokenizer(example["text"], padding="max_length", truncation=True)

    train_data = train_data.map(tokenize_function, batched=True)
    test_data = test_data.map(tokenize_function, batched=True)

    train_data = train_data.remove_columns(["text"])
    test_data = test_data.remove_columns(["text"])
    train_data.set_format("torch")
    test_data.set_format("torch")

    # Load BERT model
    num_labels = len(set(labels))
    model = BertForSequenceClassification.from_pretrained("bert-base-uncased", num_labels=num_labels)

    # Define Training Arguments
    training_args = TrainingArguments(
        output_dir="./bert_results",
        evaluation_strategy="epoch",
        logging_dir="./bert_logs",
        num_train_epochs=3,
        per_device_train_batch_size=8,
        per_device_eval_batch_size=8,
        save_steps=500,
        save_total_limit=2,
        learning_rate=2e-5,
        weight_decay=0.01,
        logging_steps=100,
        load_best_model_at_end=True
    )

    # Define the Trainer
    trainer = Trainer(
        model=model,
        args=training_args,
        train_dataset=train_data,
        eval_dataset=test_data,
        tokenizer=tokenizer
    )

    # Train the Model
    trainer.train()

    # Evaluate the Model
    metrics = trainer.evaluate()
    print("BERT Model Evaluation Metrics:")
    print(metrics)

    # Save the Model
    model.save_pretrained("bert_model")
    tokenizer.save_pretrained("bert_tokenizer")
    print("BERT model and tokenizer saved successfully!")

def main(model_type, filepath):
    # Load and preprocess dataset
    data = load_dataset(filepath)
    texts, labels = preprocess_data(data)

    if model_type == "random_forest":
        train_random_forest(texts, labels)
    elif model_type == "bert":
        train_bert(texts, labels)
    else:
        print("Invalid model type. Please choose 'random_forest' or 'bert'.")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python train_model.py <model_type> <path_to_dataset.json>")
        sys.exit(1)

    model_type = sys.argv[1].lower()
    dataset_path = sys.argv[2].lower()
    main(model_type, dataset_path)
