#  pylint: disable=missing-module-docstring


import abc
import os


#  pylint: disable-next=missing-class-docstring
class Repo(metaclass=abc.ABCMeta):

    def __init__(self, name, cache_path, repository, arch_filter=".*"):
        self.name = name
        self.cache_dir = os.path.join(cache_path, str(name))
        self.repository = repository
        self.arch_filter = arch_filter
        self.metadata_files = None

    def get_repo_path(self, path):
        #  pylint: disable-next=consider-using-f-string
        return "{}/{}".format(self.repository, path)

    # @profile
    @abc.abstractmethod
    def get_packages_metadata(self):
        """
        Download the metadata file(s) from the repository and parse it(them) to retrieve the package's metadata.
        """
