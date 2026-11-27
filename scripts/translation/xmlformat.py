#!/usr/bin/env python3
# Copyright (c) 2026 SUSE LLC
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SPDX-License-Identifier: GPL-2.0-only

"""
XML formatter utility script with standard spacing and namespace preservation.
"""

import sys
import xml.etree.ElementTree as ET


def main():
    if len(sys.argv) < 2:
        print("Usage: xmlformat.py <file>", file=sys.stderr)
        sys.exit(1)

    file_path = sys.argv[1]

    # Register namespaces to avoid ns0: prefixes
    ET.register_namespace("", "urn:oasis:names:tc:xliff:document:1.1")
    ET.register_namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance")

    parser = ET.XMLParser(target=ET.TreeBuilder(insert_comments=True))
    try:
        tree = ET.parse(file_path, parser=parser)
    except (OSError, ET.ParseError) as e:
        print(f"Error parsing {file_path}: {e}", file=sys.stderr)
        sys.exit(1)

    root = tree.getroot()
    ET.indent(root, space="  ")

    # Write to stdout
    sys.stdout.buffer.write(ET.tostring(root, encoding="utf-8", xml_declaration=True))
    sys.stdout.buffer.write(b"\n")


if __name__ == "__main__":
    main()
