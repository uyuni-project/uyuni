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

    # @profile
    @abc.abstractmethod
    def get_packages_metadata(self):
        """
        Download the metadata file(s) from the repository and parse it(them) to retrieve the package's metadata.
        """
