"""
Database helper functions
"""

from lzdownload.repo_dto import RepoDTO
from spacewalk.server import rhnSQL


class NoSourceFoundForChannel(Exception):
    """Raised when no source(repository) was found"""

    def __init__(self, channel_label):
        self.msg = f"No resource found for channel {channel_label}"
        super().__init__(self.msg)


def get_repositories_by_channel_label(channel_label):
    """
    Fetch repositories information of a given channel form the database, and return a list of
    RepoDTO objects
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        SELECT c.label as channel_label, c_ark.name as channel_arch, s.id, s.source_url, s.metadata_signed, s.label as repo_label, cst.label as repo_type_label
        FROM rhnChannel c
        INNER JOIN rhnChannelArch c_ark ON c.channel_arch_id = c_ark.id
        INNER JOIN rhnChannelContentSource cs ON c.id = cs.channel_id
        INNER JOIN rhnContentSource s ON cs.source_id = s.id
        INNER JOIN  rhnContentSourceType cst ON s.type_id = cst.id
        WHERE c.label = :channel_label
        """
    )
    h.execute(channel_label=channel_label)
    sources = h.fetchall_dict()
    if not sources:
        raise NoSourceFoundForChannel(channel_label)
    repositories = map(
        lambda source: RepoDTO(
            channel_label=source["channel_label"],
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


def get_all_packages_metadata_from_channel(channel) -> list[dict]:
    """
    return all the packages' metadata that are linked to the given channel
    :channel: channel label
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        SELECT p.remote_path, cks.checksum, ckstype.label as checksum_type, rpm.name source_rpm from rhnpackage p 
        INNER JOIN rhnChannelPackage chpkg on chpkg.package_id = p.id
        INNER JOIN rhnChannel ch on ch.id = chpkg.channel_id 
        INNER JOIN rhnChecksum cks on cks.id = p.checksum_id
        INNER JOIN rhnChecksumType ckstype on ckstype.id = cks.checksum_type_id
        INNER JOIN rhnSourceRpm rpm ON rpm.id = p.source_rpm_id
        WHERE ch.label = :channel_label
        """
    )
    h.execute(channel_label=channel)
    packages_metadata = h.fetchall_dict()
    rhnSQL.closeDB()
    return packages_metadata
