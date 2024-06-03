"""
This is a minimal implementation of the lazy reposync parser.
It downloads the target repository's metadata file(s) from the
given url and parses it(them)
"""
import abc
import os


class Repo(metaclass=abc.ABCMeta):

    def __init__(self, name, cache_path, repository, handler):
        self.name = name
        self.cache_dir = os.path.join(cache_path, str(name))
        self.repository = repository
        self.handler = handler  # The sax handler/parser
        self.metadata_files = None

    def get_repo_path(self, path):
        return "{}/{}".format(self.repository, path)

    @abc.abstractmethod
    def get_metadata_files(self):
        """
        Return a datastructure containing the metadata files' information
        """

    @abc.abstractmethod
    def find_metadata_file_url(self, file_name) -> (str, str):
        """
        Return the corresponding metadata file's url given its name.
        An example of these files can be 'primary', 'filelists', 'other', etc...
        """

    @abc.abstractmethod
    def find_metadata_file_checksum(self, file_name):
        """
        Return the corresponding metadata file's checksum given its name.
        """

    # @profile
    @abc.abstractmethod
    def get_packages_metadata(self):
        """
        Download the metadata file(s) from the repository and parse it(them) to retrieve the package's metadata.
        """
