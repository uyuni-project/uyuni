# pylint: disable=missing-module-docstring
import sys
import yaml

if len(sys.argv) != 2:
    print("Expecting a list of comma-separated tests as an argument")
    exit(-1)

js_output = sys.argv[1]

# Split the comma-separated output into a list
test_names = js_output.split(",") if js_output else []

# Convert list to YAML format
print(yaml.dump(test_names, default_flow_style=False))
