"""
This module implements a class to parse VEX files in CycloneDX format.
"""

import json
import logging
from .vex_parser import VEX_Parser

class CycloneDXParser(VEX_Parser):
    def parse(self):
        data = json.loads(self.document)
        statements = []

        for vuln in data.get("vulnerabilities", []):
            for component in data.get("components", []):
                statements.append({
                    "product_id": component.get("purl"),
                    "vulnerability_id": vuln.get("id"),
                    "status": vuln.get("analysis", {}).get("state"),
                    "timestamp": data.get("timestamp")
                })

        return statements