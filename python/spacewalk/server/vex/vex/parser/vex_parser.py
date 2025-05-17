"""
This module implements a base class to parse VEX files.
"""
import logging
import json

logging.basicConfig(level=logging.INFO)

class VEX_Parser:

    def __init__(self):

        self.metadata = {}
        self.product = {}
        self.vulns = []

    def parse(self, filename):
        try:
                self.document = open(filename, "r")
        except:
            raise FileNotFoundError
        
        self.vex_data = json.load(self.document)
        self.metadata = {}
        self.products = {}
        self.vulns = []

        self._extract_metadata()
        self._extract_products()
        self._extract_vulns()

    def get_metadata(self):
        return self.metadata

    def get_products(self):
        return self.products
    
    def get_product_name(self, p_name):
        return self.products[p_name]
    
    def get_product_id_name(self, p_name):
        try:
            if self.products[p_name]["cpe"]:
                id = self.products[p_name]["cpe"]
            elif self.products[p_name]["purl"]:
                id = self.products[p_name]["purl"]
            return id
        
        except:
            logging.debug(f"No CPE found for {p_name}")
            return "no"

    def get_vulnerabilities(self):
        return self.vulns

    def persist_data(self):
        self._persist_data()