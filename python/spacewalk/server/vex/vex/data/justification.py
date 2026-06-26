"""
A class to represent a justification described in a VEX file for known_not_affected products.
"""

class Justification:

    def __init__(self):
        self.type = "type"
        self.category = "category"
        self.details = "no_detais"
        self.products = []

    def _set_type(self, type):
        self.type = type

    def _set_category(self, category):
        self.category = category

    def _set_details(self, details):
        self.details = details

    def _set_products(self, products):
        self.products = products

    def _add_product(self, product):
        self.products.append(product)

    def _get_type(self):
        return self.type

    def _get_category(self):
        return self.category
    
    def _get_details(self):
        return self.details
    
    def _get_products(self):
        return self.products
    
    def _applied_product(self, product_id):
        """
        Checks if this remediations applies for an specifica product.

        Return:
            bool: True if the remediations apply for the specified product id
        """
        return product_id in self.products