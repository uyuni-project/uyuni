#  pylint: disable=missing-module-docstring

import logging
import os.path
import shutil


#  pylint: disable-next=missing-class-docstring
class TranslationParser:
    def __init__(self, translation_file, cache_dir="./.cache"):
        """
        Note: currently, we're handling uncompressed file-like objects (eg: "_io.TextIOWrapper")
        # TODO: use  uyuni.common.fileutils.decompress_open for different format handling (gz, xz, ect), However! we should close the file manually
        """
        self.translation_file = translation_file
        self.cache_dir = cache_dir
        self.num_parsed_packages = 0
        self.parsed = False

    def parse_translation_file(self):
        """
        Parse the Translation file and cache its content; each package's description in a separate file
        """
        logging.debug("Parsing Translation file...")
        curr_package_md5 = ""
        curr_description = ""
        reading_description = (
            False  # check whether we're currently reading the description content
        )
        for line in self.translation_file:
            if line in ["\n", "\r\n"]:
                # End of package description
                self.cache_pacakge_description(curr_package_md5, curr_description)
                reading_description = False
                self.num_parsed_packages += 1
            elif line.startswith("Description-md5"):
                curr_package_md5 = line.split(": ")[1].rstrip("\n")
            elif line.startswith("Description-en"):
                curr_description = line.split(": ")[1]
                reading_description = True
            elif reading_description:
                curr_description += line.strip(" ")
        return True

    def cache_pacakge_description(self, package_checksum, package_description):
        """
        Cache the given package_description in the cache_dir. The file name will be the package_checksum
        """
        cache_file = os.path.join(self.cache_dir, package_checksum)

        if not os.path.exists(self.cache_dir):
            logging.debug("Creating cache directory: %s", self.cache_dir)
            os.makedirs(self.cache_dir)

        with open(cache_file, "w", encoding="utf-8") as description_file:
            logging.debug("Caching file %s", description_file.name)
            description_file.write(package_description)

    def get_pacakge_description_by_description_md5(self, description_md5):
        """
        Read the package's full description given its checksum
        """
        pacakge_description_path = os.path.join(self.cache_dir, description_md5)

        # Read the cached description file
        if not os.path.exists(pacakge_description_path):
            logging.debug(
                "No description file found for %s. Parsing again..", description_md5
            )
            if not self.parsed:
                logging.debug("Parsing Translation file...")
                self.parsed = self.parse_translation_file()
            else:
                logging.error("Couldn't find description file for %s", description_md5)
                return None

        # Checking again
        if not os.path.exists(pacakge_description_path):
            logging.error("Couldn't find description file for %s", description_md5)
            return None

        with open(pacakge_description_path, "r", encoding="utf-8") as pkg_desc_file:
            return pkg_desc_file.read()

    def clear_cache(self):
        """
        Remove the cached filelist files from the cache directory, including the cache directory
        """
        if os.path.exists(self.cache_dir):
            logging.debug("Removing %s directory and its content", self.cache_dir)
            shutil.rmtree(self.cache_dir)
