#  pylint: disable=missing-module-docstring

from typing import List

from lzreposync.repo_dto import RepoDTO


class ChannelDTO:
    """
    A temporary data structure to hold some minor channel information
    """

    def __init__(self, label, repositories: List[RepoDTO], channel_arch=None):
        self.label = label
        self.repositories = repositories
        self.channel_arch = channel_arch
