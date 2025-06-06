"""
A class to represent a remediation described in a VEX file.
"""

class Remediation:

    def __init__(self):
        self.category = "category"
        self.details = "no_detais"
        self.fix_versions_by_product = {}

    def _set_category(self, category):
        self.category = category

    def _set_details(self, details):
        self.details = details

    def _get_category(self):
        return self.category
    
    def _get_details(self):
        return self.details
    
    def add_fixed_package(self, platform, fixversion):
        if platform not in self.fix_versions_by_product:
            self.fix_versions_by_product[platform] = set()
        self.fix_versions_by_product[platform].add(fixversion)
    
    def get_fixed_packages(self):
        return self.fixed_packages

    def get_fix_versions(self, product_id):
        return list(self.fix_versions_by_product.get(product_id, []))
    
    def show_fixversions(self):
        print("Fix_versions:")
        for product, versions in self.fix_versions_by_product.items():
            print(f"- {product}:")
            for version in versions:
                print(f"    - {version}")

    def show_fixversions_product(self, product):
        
        for prod, versions in self.fix_versions_by_product.items():
            if prod == product:
                print(f"Fix_versions for {product}:")
                for version in versions:
                    print(f"    - {version}")