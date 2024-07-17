#  pylint: disable=missing-module-docstring
from dataclasses import dataclass
from typing import List

from lzreposync.repo_dto import RepoDTO


@dataclass
class ChannelDTO:
    """
    A temporary data structure to hold some minor channel information
    """
    label: str
    repositories: List[RepoDTO]
    channel_arch: str = None
