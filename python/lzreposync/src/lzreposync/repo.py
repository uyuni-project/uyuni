#  pylint: disable=missing-module-docstring


import abc
import os


#  pylint: disable-next=missing-class-docstring
class Repo(metaclass=abc.ABCMeta):

    def __init__(self, name, cache_path, repository, arch_filter=".*", repo_type="rpm"):
        self.name = name
        self.cache_dir = os.path.join(cache_path, str(name))
        if not os.path.exists(self.cache_dir):
            os.mkdir(self.cache_dir)
        self.repository = repository
        if not self.repository.endswith("/"):
            # TODO there might be a better solution
            self.repository += "/"
        self.arch_filter = arch_filter
        self.metadata_files = None
        self.repo_type = repo_type

    def get_repo_path(self, path):
        #  pylint: disable-next=consider-using-f-string
        return "{}/{}".format(self.repository.rstrip("/"), path.lstrip("/"))

    # @profile
    @abc.abstractmethod
    def get_packages_metadata(self):
        """
        Download the metadata file(s) from the repository and parse it(them) to retrieve the package's metadata.
        """
