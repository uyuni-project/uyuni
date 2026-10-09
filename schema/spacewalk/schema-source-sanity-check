#!/usr/bin/env python3
# pylint: disable=invalid-name
"""
Sanity check script for schema source files.
"""

import os
import re
import sys
import argparse


def main():
    parser = argparse.ArgumentParser(add_help=False)
    parser.add_argument("-I", action="store_true")
    _, unknown = parser.parse_known_args()

    dirs = ["common", "postgres", "upgrade"]
    if unknown:
        dirs = unknown

    files = {"common": {}, "postgres": {}}

    for d in dirs:
        if not os.path.exists(d):
            continue
        for root, _, filenames in os.walk(d):
            for filename in filenames:
                full_path = os.path.join(root, filename)
                if not os.path.isfile(full_path):
                    continue

                rname = os.path.relpath(full_path, d)

                if d == "upgrade" or os.path.basename(d) == "upgrade":
                    generic = full_path
                    db = "common"
                    if generic.endswith(".oracle") or generic.endswith(".postgresql"):
                        sys.stderr.write(
                            f"Found DB specific files in [upgrade] dir: {generic}\n"
                        )
                        sys.exit(1)
                    files[db][generic] = full_path
                else:
                    parts = full_path.split(os.sep)
                    if "common" in parts:
                        common_index = parts.index("common")
                        sub_rname = os.path.join(*parts[common_index + 1 :])
                        files["common"][sub_rname] = full_path
                    elif "postgres" in parts:
                        postgres_index = parts.index("postgres")
                        sub_rname = os.path.join(*parts[postgres_index + 1 :])
                        files["postgres"][sub_rname] = full_path
                    else:
                        if d not in files:
                            files[d] = {}
                        files[d][rname] = full_path

    error = 0

    def check_file_content(filename):
        nonlocal error
        parts = filename.split(os.sep)
        if "upgrade" in parts or filename.startswith("upgrade"):
            return
        if "docs" in parts or "docs" in filename:
            return
        if filename.endswith("qrtz.sql") or filename.endswith("dual.sql"):
            return

        m = re.match(r".*/([^/]+)/([^/]+?)(?:_foreignkeys)?\.(sql|pks|pkb)$", filename)
        if not m:
            return
        type_name, name, _ = m.groups()
        if type_name in ("class", "packages"):
            return

        # Only check known database object directory types
        if type_name not in (
            "tables",
            "views",
            "data",
            "procs",
            "synonyms",
            "triggers",
            "schemas",
        ):
            return

        try:
            with open(filename, "r", encoding="utf-8", errors="ignore") as f:
                content = f.read()
        except OSError as e:
            sys.stderr.write(f"Error reading [{filename}]: {e}\n")
            sys.exit(1)

        if type_name == "tables":
            name_norm = name
            if filename.endswith("_index.sql"):
                name_norm = re.sub(r"_index$", "", name_norm)
            elif filename.endswith("_alters.sql"):
                name_norm = re.sub(r"_alters$", "", name_norm)

            escaped_name = re.escape(name_norm)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |(?:create|alter|comment\s+on)\s+table\s+(?:\w+\.)?{escaped_name}\b(?:[^;]|'[^']*'|--[^\n]*\n)+;
                |create\s+(?:unique\s+)?index\s+(?:if\s+not\s+exists\s+)?\w+\s+on\s+(?:\w+\.)?{escaped_name}[^;]+;
                |create\s+sequence[^;]+;
                |comment\s+on\s+column\s+{escaped_name}\.[^;]+;
            )+$"""
            if not re.match(pat, content, re.I | re.X):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "views":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |create(?:\s+or\s+replace)?\s+view\s+(?:\w+\.)?{escaped_name}\b(?:[^;]|'[^']*'|--[^\n]*\n)+;
            )+$"""
            if not re.match(pat, content, re.I | re.X):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "data":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |insert\s+into\s+(?:\w+\.)?{escaped_name}\b[^;]+(?:values|select)(?:'[^;]+(?:;[^;]*)*'|[^';])+;
                |delete\s+from\s+(?:\w+\.)?{escaped_name}\b[^;]+;
                |select\s+[^;()]+\((?:'[^;]+')*\);
                |begin\s+[^;()]+\((?:'[^;]+')*\);\s+end;\n/
                |commit;
            )+$"""
            if not re.match(pat, content, re.I | re.X):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "procs":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |create(?:\s+or\s+replace)?\s+(?:procedure|function)\s+{escaped_name}\b
                    (?:(?:.+?);\n/\n
                    |[^\$]+\$\$(?:.+?)\s\$\$
                        \s+language\s+(?:plpgsql|sql)(?:\s+(?:strict\s+)?immutable|\s+stable)?;)
                |show\s+errors;?\n
            )+$"""
            if not re.match(pat, content, re.I | re.X | re.S):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "synonyms":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |create(?:\s+or\s+replace)?\s+synonym\s+{escaped_name}\b\s+for(?:[^;]|'[^']*'|--[^\n]*\n)+;
                |create(?:\s+or\s+replace)?\s+synonym\s+{escaped_name}s?_recid_seq\s+for(?:[^;]|'[^']*'|--[^\n]*\n)+;
            )+$"""
            if not re.match(pat, content, re.I | re.X):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "triggers":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |create(?:\s+or\s+replace)?\s+function\s+(\w+)(?:.+?)\s+language\s+plpgsql;
                    \s+create(?:\s+or\s+replace)?\s+trigger[^;]+\s+on\s+{escaped_name}\b[^;]+execute\s+procedure\s+\1\(\);
                |create(?:\s+or\s+replace)?\s+trigger[^;]+\s+on\s+{escaped_name}\b[^;]+execute\s+procedure\s+no_operation_trig_fun\(\);
                |create(?:\s+or\s+replace)?\s+trigger[^;]+\s+on\s+{escaped_name}\b(?:.+?);\n/\n
                |show\s+errors;?\n
            )+$"""
            if not re.match(pat, content, re.I | re.X | re.S):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        elif type_name == "schemas":
            escaped_name = re.escape(name)
            pat = rf"""^(?:
                --.*\n
                |\s*\n
                |create\s+schema\s+{escaped_name}\b\s*;
                |comment\s+on\s+schema\s+{escaped_name}\b\s+is\s+(?:[^;]|'[^']*'|--[^\n]*\n)+;
            )+$"""
            if not re.match(pat, content, re.I | re.X):
                print(f"Bad {type_name} content [{filename}]")
                error = 1

        else:
            print(f"Unknown type [{type_name}] for [{filename}]")

    for c in sorted(files["common"].keys()):
        if not (c.endswith(".sql") or c.endswith(".pks") or c.endswith(".pkb")):
            continue
        check_file_content(files["common"][c])
        for o in ("postgres",):
            if c in files.get(o, {}):
                print(f"Common file [{c}] is also in {o}")
                error = 1

    for c in sorted(files.get("postgres", {}).keys()):
        if not (c.endswith(".sql") or c.endswith(".pks") or c.endswith(".pkb")):
            continue
        check_file_content(files["postgres"][c])

    sys.exit(error)


if __name__ == "__main__":
    main()
