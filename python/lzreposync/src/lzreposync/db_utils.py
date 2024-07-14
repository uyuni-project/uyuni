#  pylint: disable=missing-module-docstring

from lzreposync.repo_dto import RepoDTO
from spacewalk.server import rhnSQL


def get_repositories_by_channel_id(channel_id):
    """
    Fetch repositories information form the database, and return a list of
    RepoDTO objects
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        select s.id, s.source_url, s.metadata_signed, s.label as repo_label, cst.label as repo_type_label
        from rhnContentSource s,
             rhnChannelContentSource cs,
             rhnContentSourceType cst
        where s.id = cs.source_id
          and cst.id = s.type_id
          and cs.channel_id = :channel_id"""
    )
    h.execute(channel_id=int(channel_id))
    sources = h.fetchall_dict()
    repositories = map(
        lambda source: RepoDTO(
            repo_id=source["id"],
            repo_label=source["repo_label"],
            repo_type=source["repo_type_label"],
            source_url=source["source_url"],
            metadata_signed=source["metadata_signed"],
        ),
        sources,
    )
    rhnSQL.closeDB()

    return list(repositories)
