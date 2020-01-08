"""
libmod operations
"""

class MLLibmodAPI:
    """
    Libmod API operations.
    """

    def set_repodata(self, repodata: str) -> MLLibmodAPI:
        """
        set_repodata sets the repository data from the input JSON.

        :param repodata: JSON string of the input object.
        :type repodata: str
        """
        return self

    def get_all_modules(self):
        pass

    def get_all_packages(self):
        pass