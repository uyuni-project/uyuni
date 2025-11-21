"""
This module implements a template class to parse VEX files.
"""
import logging
import json

logging.basicConfig(level=logging.INFO)

class VEX_Parser:
    """
    Base class to parse VEX (Vulnerability Exploitability eXchange) files.

    Attributes:
        metadata (dict): Stores metadata extracted from the VEX file.
        products (dict): Stores product information extracted from the VEX file.
        vulns (list): List of vulnerabilities extracted from the VEX file.
    """

    def __init__(self):
        """
        Initializes the parser with empty metadata, products, and vulnerabilities.
        """

        self.metadata = {}
        self.product = {}
        self.vulns = []

    def parse(self, filename):
        """
        Parses the given VEX JSON file and extracts metadata, products, and vulnerabilities.

        Args:
            filename (str): Path to the VEX JSON file to parse.

        Raises:
            FileNotFoundError: If the file cannot be opened.
        """

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
        """
        Returns the parsed metadata.

        Returns:
            dict: Metadata dictionary.
        """
        return self.metadata

    def get_products(self):
        """
        Returns the parsed products.

        Returns:
            dict: Products dictionary.
        """

        return self.products
    
    def get_product_name(self, p_name):
        """
        Returns the product information for the given product name.

        Args:
            p_name (str): Product name.

        Returns:
            dict: Product details.
        """

        return self.products[p_name]
    
    def get_product_id_name(self, p_name):
        """
        Returns the product identifier (CPE or PURL) for the given product name.

        Args:
            p_name (str): Product name.

        Returns:
            str: Product identifier or "no" if none found.
        """

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
        """
        Returns the list of parsed vulnerabilities.

        Returns:
            list: List of vulnerabilities.
        """
        return self.vulns

    def persist_data(self):
        """
        Persists parsed data using internal persistence mechanisms.
        """
        self._persist_data()