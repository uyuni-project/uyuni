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
BEGIN;
    """)

    namespace_map = {}  # Store namespaces and their endpoints
    endpoints_without_namespace = [] #Store endpoints with no namespace

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
                    print(f"Error processing row: {row}")
                    continue

                if namespace and access_mode:
                    namespace_key = (namespace, access_mode)
                    if namespace_key not in namespace_map:
                        namespace_map[namespace_key] = []
                    namespace_map[namespace_key].append((class_method, endpoint, http_method, scope, auth_required))
                else:
                    endpoints_without_namespace.append((class_method, endpoint, http_method, scope, auth_required))

    # Generate INSERT statements, ensuring namespaces are inserted first
    namespace_inserts = []
    endpoint_inserts = []
    endpoint_namespace_inserts = []

    for (namespace, access_mode), endpoints in namespace_map.items():
        namespace_insert = f"""INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('{namespace}', '{access_mode}', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;"""
        namespace_inserts.append(namespace_insert)

        for class_method, endpoint, http_method, scope, auth_required in endpoints:
            endpoint_insert = f"""INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('{class_method}', '{endpoint}', '{http_method}', '{scope}', {auth_required})
    ON CONFLICT (endpoint, http_method) DO NOTHING;"""
            endpoint_inserts.append(endpoint_insert)

            endpoint_namespace_insert = f"""INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = '{namespace}' AND ns.access_mode = '{access_mode}'
    AND ep.endpoint = '{endpoint}' AND ep.http_method = '{http_method}'
    ON CONFLICT DO NOTHING;"""
            endpoint_namespace_inserts.append(endpoint_namespace_insert)

    # Generate insert statements for endpoints without namespaces
    for class_method, endpoint, http_method, scope, auth_required in endpoints_without_namespace:
        endpoint_insert = f"""INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('{class_method}', '{endpoint}', '{http_method}', '{scope}', {auth_required})
    ON CONFLICT (endpoint, http_method) DO NOTHING;"""
        endpoint_inserts.append(endpoint_insert)

    # Output the INSERT statements in the desired order
    for insert in namespace_inserts:
        print(insert)

    for insert in endpoint_inserts:
        print(insert)

    for insert in endpoint_namespace_inserts:
        print(insert)

    print("COMMIT;")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python script.py csv_file1.csv csv_file2.csv...", file=sys.stderr)
        sys.exit(1)

    csv_files = sys.argv[1:]
    process_csv_files(csv_files)
