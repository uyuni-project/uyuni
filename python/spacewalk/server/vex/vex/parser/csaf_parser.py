"""
This module extends vex_parser class to parse VEX files in CSAF format.

Credits to [@anthonyharrison](https://github.com/anthonyharrison/csaf/blob/main/csaf/parser.py#L154)
"""

import json
from .vex_parser import *
from vex.data.vulnerability import Vulnerability
from vex.data.remediation import Remediation
# from vex.data.justification import Justification
from vex.data.statuses_enum import Status
from vex.persistence.vex_persistence import VEXDatabaseManager
from packageurl import PackageURL

class CSAFParser(VEX_Parser):

    """
    Parser class to handle CSAF-formatted VEX files.

    Inherits from VEX_Parser and overrides methods to extract CSAF-specific metadata, products, and vulnerabilities.
    """

    def __init__(self):
        """
        Initializes the CSAF parser by calling the base class constructor.
        """

        super().__init__()
        # self.fixversions = {}

    def _extract_vulns(self):

        """
        Extracts vulnerabilities from the CSAF VEX data and populates the vulnerabilities list.

        Parses the vulnerabilities section of the CSAF document, mapping fields such as CVE IDs, descriptions, product statuses, and remediations into Vulnerability objects.
        """

        if len(self.vex_data) == 0:
            return None
        
        for vulnerability in self.vex_data["vulnerabilities"]:
            vuln = Vulnerability(validation="csaf")
            vuln.initialise() #Initialise vulnerability lib4sbom object

            if vulnerability["cve"]:
                vuln.set_id(vulnerability["cve"])
            else:
                vuln.set_id(vulnerability["ids"])

            if "notes" in vulnerability:
                for note in vulnerability["notes"]:
                    vuln.set_description(note["text"])

            # These fields are not required as can be accessed throught the official CVE page
            #if "cwe" in vulnerability:
            #    vuln.set_value("cwe",f"{vulnerability['cwe']['id']} - {vulnerability['cwe']['name']}")
            #if "discovery_date" in vulnerability:
            #    vuln.set_value("discovery_date", vulnerability["discovery_date"])
            #if "references" in vulnerability:
            #    for reference in vulnerability["references"]:
            #        vuln.set_value(reference["category"], [reference.get("summary",""), reference.get("url","")])
            #if "release_date" in vulnerability:
            #    vuln.set_value("release_date", vulnerability["release_date"])

            if "product_status" in vulnerability:
                # Extract lists with products for each status
                statuses = vulnerability.get("product_status", {})

                for status, products in statuses.items():
                    
                    product_list = []
                    
                    for product in products:

                        product_list.append(product)

                    vuln.set_status(status, products)
                
            if "remediations" in vulnerability:  
                for remediation in vulnerability["remediations"]:

                    if remediation.get("category") != "vendor_fix":
                        continue

                    rem = Remediation()
                    if "category" in remediation:
                        rem._set_category(remediation["category"])
                    if "details" in remediation:
                        rem._set_details(remediation["details"])

                    for pid in remediation.get("product_ids", []):
                        # pid looks like "SUSE Liberty Linux 7:squid-3.5.20-17.el7_9.4.x86_64"
                        split = pid.split(":", 1)
                        if len(split) > 1:
                            platform, fixversion = split

                            # OPTIONAL - drop the trailing ".<arch>"
                            # pkg_ver = pkg_ver_arch.rsplit(".", 1)[0]
                            # now pkg_ver is "squid-3.5.20-17.el7_9.4"

                            rem.add_fixed_package(platform, fixversion)

                    vuln.add_remediation(rem)

            # Optional, additional information MAY also be provide through
            # /vulnerabilities[]/notes and /vulnerabilities[]/threats.

            # # Human readable justification in /vulnerabilities[]/threats. For the latter one, the category 
            # # value for such a statement MUST be impact and the details field SHALL contain a a description why
            # # the vulnerability cannot be exploited.
            # if "threats" in vulnerability:
            #     for threat in vulnerability["threats"]:
            #         if threat["category"] == "impact":
            #             justification = Justification()
            #             if "category" in threat:
            #                 justification._set_category(threat["category"])
            #             if "details" in threat:
            #                 justification._set_details(threat["details"])
            #             if "product_ids" in threat:
            #                 justification._set_products(threat["product_ids"])
            #             vuln.add_justification(justification)

            # # TODO - An impact statement SHALL exist as machine readable flag in /vulnerabilities[]/flags
            # if "flags" in vulnerability:
            #     for flag in vulnerability["flags"]:
            #         for product in flag["product_ids"]:
            #             justification = vuln.get_justification_product(product)
            #             if justification != None:
            #                 if "label" in flag:
            #                     justification._set_type(flag["label"])
                
                            
            self.vulns.append(vuln)

    def _extract_metadata(self):
        """
        Extracts metadata from the CSAF document header.

        Populates metadata fields such as version, title, category, dates, publisher info,
        references, and distribution info from the CSAF JSON structure.
        """

        if len(self.vex_data) == 0:
            return
        
        # Key attributes from the CSAF header
        document = self.vex_data.get("document")

        if document is None:
            # Doesn't look like a CSAF document
            self.vex_data = []
            return
        
        self.metadata["version"] = document["csaf_version"]
        self.metadata["title"] = document["title"]
        self.metadata["category"] = document["category"]
        self.metadata["date"] = document["tracking"]["current_release_date"]
        if "aggregate_severity" in document:
            self.metadata["severity"] = document["aggregate_severity"]["text"]
        if "notes" in document:
            notes = []
            for note in document["notes"]:
                note_ref = {'title': note["title"], 'text' : note['text'], 'category': note['category']}
                notes.append(note_ref)
            self.metadata["notes"] = notes
        if "publisher" in document:
            publisher_info = (
                f"{self.vex_data['document']['publisher']['name']} "
                f"{self.vex_data['document']['publisher']['namespace']}"
            )
            self.metadata["publisher"] = publisher_info
            self.metadata["author"] = self.vex_data['document']['publisher']['name']
            self.metadata["author_url"] = self.vex_data['document']['publisher']['namespace']
            if "contact_details" in self.vex_data['document']['publisher']:
                self.metadata["contact_details"] = self.vex_data['document']['publisher']['contact_details']
        if "tracking" in document:
            if "generator" in document["tracking"]:
                generator_version = "UNKNOWN"
                if (
                        "version"
                        in document["tracking"]["generator"]["engine"]
                ):
                    generator_version = document["tracking"]["generator"][
                        "engine"
                    ]["version"]
                self.metadata["generator"] = f"{self.vex_data['document']['tracking']['generator']['engine']['name']} version {generator_version}"
            self.metadata["id"] = document["tracking"]["id"]
            self.metadata["initial_release_date"] = document["tracking"]["initial_release_date"]
            if "revision_history" in document["tracking"]:
                revision_data=[]
                for revision in document["tracking"]["revision_history"]:
                    revision_ref={'date' : revision["date"], 'number' : revision["number"], 'summary' : revision["summary"]}
                    revision_data.append(revision_ref)
                self.metadata["revision"] = revision_data
            self.metadata["tracking_status"] = document["tracking"]["status"]
            self.metadata["tracking_version"] = document["tracking"]["version"]
        if "references" in document:
            for reference in document["references"]:
                if "category" in reference:
                    self.metadata["reference_category"] = reference["category"]
                self.metadata["reference_url"] = reference["url"]
        if "distribution" in document:
            distribution_info = ""
            if "text" in document["distribution"]:
                distribution_info = f"{self.vex_data['document']['distribution']['text']}"
            if "tlp" in document["distribution"]:
                distribution_info = (
                        distribution_info
                        + f" TLP: {self.vex_data['document']['distribution']['tlp']['label']}"
                )
            self.metadata["distribution"] = distribution_info

    def _extract_products(self):
        """
        Extracts product information from the CSAF product_tree section.

        Processes product branches recursively to build a product dictionary with
        product IDs and related details such as vendor, product name, and version.
        """

        #logging.info("Extracting products") #debug
        if len(self.vex_data) == 0:
            return
        product = self.vex_data["product_tree"]
        #logging.info(product) #debug
        for d in product["branches"]:
            element = {}
            self._process_branch(d, element)
        
        #logging.info(self.product) #debug

    def _process_branch_element(self, branch_element, element):
        """
        Processes a single branch element in the CSAF product tree to extract category and name.

        Args:
            branch_element (dict): A branch element from the CSAF product_tree.
            element (dict): A dictionary accumulating extracted information.

        Returns:
            dict: Updated element dictionary with extracted category-name pair.
        """

        category = branch_element.get("category", None)
        name = branch_element.get("name", None)
        if category is not None:
            element[category] = name
        return element

    def _process_branch(self, branch_element, element):
        """
        Recursively processes branches of the CSAF product tree to extract product details.

        Args:
            branch_element (dict): Current branch element.
            element (dict): Dictionary accumulating product info.

        Returns:
            dict: Updated element with product information.
        """

        element = self._process_branch_element(branch_element, element)
        if "branches" in branch_element:
            for branch in branch_element["branches"]:
                element = self._process_branch(branch, element)
                if "product" in branch:
                    element["product_id"] = branch["product"]["product_id"]
                    item = {}
                    if "product_identification_helper" in branch["product"]:
                        pid = branch["product"]["product_identification_helper"]
                        if "cpe" in pid:
                            cpe_info = pid["cpe"]
                            item["cpe"] = cpe_info
                            cpe_items = cpe_info.split(":")
                            if cpe_items[1] == "/a" and len(cpe_items) > 4:
                                # Example is cpe:/a:redhat:rhel_eus:8.2::realtime
                                element["product_version"] = cpe_items[4]
                            elif cpe_items[1] == "2.3" and len(cpe_items) > 5:
                                # Example is cpe:2.3:a:redhat:rhel_eus:8.2::realtime
                                element["product_version"] = cpe_items[5]
                        elif "purl" in pid:
                            purl_info = PackageURL.from_string(pid["purl"])
                            item["purl"] = purl_info
                            element["product_version"] = purl_info.to_dict()["version"]
                    item["vendor"] = element.get("vendor", None)
                    item["product"] = element.get("product_name", "Not defined")
                    item["version"] = element.get("product_version", None)
                    if item["version"] is None:
                        item["version"] = element.get("product_version_range", None)
                    item["family"] = element.get("product_family", "")
                    id = element.get("product_id", None)
                    if id is not None and id not in self.products:
                        self.products[id] = item
                    # element = {}
        return element
    

    def _persist_data(self):
        """
        Persists the extracted vulnerabilities and related product annotations into a database.

        Uses VEXDatabaseManager to insert CVEs, products, and status annotations such as affected,
        patched, not affected, and under investigation.
        """

        vulns =  self.get_vulnerabilities()

        db_manager = VEXDatabaseManager()

        for vuln in vulns:

            try:
                db_manager.connect()
                
                cve_id = vuln.get_id()
                vuln_id = db_manager.insert_cve(cve_id) # Insert CVE if necessary

                if Status.AFFECTED in vuln.get_statuses():
                    logging.debug("Persisting known_affected")
                    #logging.info(f"Products -> {vuln.get_value('known_affected')}") # DEBUG
                    for platform, packages in vuln.get_products_status(Status.AFFECTED).items():
                        platform_id = db_manager.insert_oval_platform(self.get_product_id_name(platform))
                        for package in packages:
                            package_id = db_manager.insert_vulnerable_package(package)
                            db_manager.insert_vex_annotation(platform_id, vuln_id, package_id, Status.AFFECTED.value)

                if Status.NOT_AFFECTED in vuln.get_statuses():
                    logging.debug("Persisting known_not_affected")
                    for platform, packages in vuln.get_products_status(Status.NOT_AFFECTED).items():
                        platform_id = db_manager.insert_oval_platform(self.get_product_id_name(platform))
                        for package in packages:
                            package_id = db_manager.insert_vulnerable_package(package)
                            db_manager.insert_vex_annotation(platform_id, vuln_id, package_id, Status.NOT_AFFECTED.value)

                if Status.PATCHED in vuln.get_statuses():
                    logging.debug("Persisting fixed")
                    for platform, packages in vuln.get_products_status(Status.PATCHED).items():
                        platform_id = db_manager.insert_oval_platform(self.get_product_id_name(platform))
                        for package in packages:
                            package_id = db_manager.insert_vulnerable_package(package)
                            db_manager.insert_vex_annotation(platform_id, vuln_id, package_id, Status.PATCHED.value)

                if Status.UNDER_INVESTIGATION in vuln.get_statuses():
                    logging.debug("Persisting under_investigation")
                    for platform, packages in vuln.get_products_status(Status.UNDER_INVESTIGATION).items():
                        platform_id = db_manager.insert_oval_platform(self.get_product_id_name(platform))
                        for package in packages:
                            package_id = db_manager.insert_vulnerable_package(package)
                            db_manager.insert_vex_annotation(platform_id, vuln_id, package_id, Status.UNDER_INVESTIGATION.value)

            except Exception as e:
                logging.warning(f"File not valid: {cve_id}: {str(e)}")

            finally:
                db_manager.close()
