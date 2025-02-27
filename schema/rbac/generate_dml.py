import csv
import sys

def process_csv_files(csv_files):
    """
    Processes a list of CSV files to generate INSERT statements for PostgreSQL tables,
    handling namespace lookups, duplicate rows, reporting missing namespaces, and
    generating sequential keys using sequences.

    Args:
      csv_files: A list of CSV file paths.
    """

    # Print schema creation and search_path setting
    print("""
SET search_path TO access, public;
BEGIN;
    """)

    for csv_file in csv_files:
        with open(csv_file, 'r', encoding='utf-8') as file:
            reader = csv.DictReader(filter(lambda row: row[0] != '#', file))
            for row in reader:
                try:
                    class_method = row['class_method']
                    endpoint = row['endpoint']
                    http_method = row['http_method']
                    scope = row['scope']
                    auth_required = row['auth_required'].lower() == 't'
                    namespace = row.get('namespace')
                    access_mode = row.get('access_mode')
                except Exception as e:
                    print(row)

                # Generate INSERT statement for endpoint table with sequence for id and duplicate handling
                endpoint_insert = f"""
                    INSERT INTO endpoint (class_method, endpoint, http_method, scope, auth_required)
                    VALUES ('{class_method}', '{endpoint}', '{http_method}', '{scope}', {auth_required})
                    ON CONFLICT (endpoint, http_method) DO NOTHING;
                """
                print(endpoint_insert)

                # Generate INSERT statement for endpointNamespace table with duplicate handling
                if namespace and access_mode:
                    check_namespace_exists = f"""
                        DO $$
                        BEGIN
                            IF NOT EXISTS (SELECT 1 FROM namespace WHERE namespace = '{namespace}' AND access_mode = '{access_mode}') THEN
                                INSERT INTO namespace (namespace, access_mode, description)
                                VALUES ('{namespace}', '{access_mode}', NULL)
                                ON CONFLICT DO NOTHING;
                            END IF;
                        END $$;
                    """
                    print(check_namespace_exists)

                    endpoint_namespace_insert = f"""
                        INSERT INTO endpointNamespace (namespace_id, endpoint_id)
                        SELECT n.id, e.id
                        FROM namespace n, endpoint e
                        WHERE n.namespace = '{namespace}' AND n.access_mode = '{access_mode}' AND e.endpoint = '{endpoint}' AND e.http_method = '{http_method}'
                        ON CONFLICT DO NOTHING;
                    """
                    print(endpoint_namespace_insert)

    print("COMMIT;")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py csv_file1.csv csv_file2.csv...", file=sys.stderr)
        sys.exit(1)

    csv_files = sys.argv[1:]
    process_csv_files(csv_files)
