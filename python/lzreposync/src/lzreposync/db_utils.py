#  pylint: disable=missing-module-docstring

from lzreposync.repo_dto import RepoDTO
from spacewalk.server import rhnSQL


def get_repositories_by_channel_label(channel_label):
    """
    Fetch repositories information of a given channel form the database, and return a list of
    RepoDTO objects
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        select s.id, c_ark.name as channel_arch, s.source_url, s.metadata_signed, s.label as repo_label, cst.label as repo_type_label
        from rhnChannel c,
             rhnChannelArch c_ark,
             rhnContentSource s,
             rhnChannelContentSource cs,
             rhnContentSourceType cst
        where c.label = :channel_label
          and c.channel_arch_id = c_ark.id
          and s.id = cs.source_id
          and cst.id = s.type_id
          and cs.channel_id = c.id"""
    )
    h.execute(channel_label=channel_label)
    sources = h.fetchall_dict()
    repositories = map(
        lambda source: RepoDTO(
            repo_id=source["id"],
            channel_arch=source["channel_arch"],
            repo_label=source["repo_label"],
            repo_type=source["repo_type_label"],
            source_url=source["source_url"],
            metadata_signed=source["metadata_signed"],
        ),
        sources,
    )
    rhnSQL.closeDB()

    return list(repositories)
