# Copyright (c) 2026 SUSE LLC
# SPDX-License-Identifier: MIT
#
# All modifications and additions to the file contributed by third parties
# remain the property of their copyright owners, unless otherwise agreed
# upon. The license for this file, and modifications and additions to the
# file, is the same license as for the pristine package itself (unless the
# license for the pristine package is not an Open Source License, in which
# case the license is the MIT License). An "Open Source License" is a
# license that conforms to the Open Source Definition (Version 1.9)
# published by the Open Source Initiative.

# Please submit bugfixes or comments via https://bugs.opensuse.org/
#

"""Run the PAYG product collector with mocked installed product files."""

import json
import runpy
import sys
import xml.etree.ElementTree as ET
from unittest.mock import patch


def main():
    """Collect a synthetic product using the production script at the supplied path."""
    namespace = runpy.run_path(sys.argv[1])
    product = ET.Element("product")
    for key, value in {
        "name": sys.argv[2],
        "vendor": sys.argv[3],
        "version": "15.7",
        "arch": "x86_64",
    }.items():
        ET.SubElement(product, key).text = value
    with patch.object(
        namespace["glob"], "glob", return_value=["test.prod"]
    ), patch.object(namespace["ET"], "parse", return_value=ET.ElementTree(product)):
        print(json.dumps(namespace["_get_installed_suse_products"]()))


if __name__ == "__main__":
    main()
