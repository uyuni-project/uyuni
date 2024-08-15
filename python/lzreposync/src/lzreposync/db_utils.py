#  pylint: disable=missing-module-docstring

from lzreposync.repo_dto import RepoDTO
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.server import rhnSQL, rhnChannel


# Stolen from python/spacewalk/satellite_tools/reposync.py
def get_compatible_arches(channel_id):
    """Return a list of compatible package arch labels for this channel"""
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """select pa.label
                          from rhnChannelPackageArchCompat cpac,
                          rhnChannel c,
                          rhnpackagearch pa
                          where c.id = :channel_id
                          and c.channel_arch_id = cpac.channel_arch_id
                          and cpac.package_arch_id = pa.id"""
    )
    h.execute(channel_id=channel_id)
    # pylint: disable-next=invalid-name
    with cfg_component("server.susemanager") as CFG:
        arches = [
            k["label"]
            for k in h.fetchall_dict()
            if CFG.SYNC_SOURCE_PACKAGES or k["label"] not in ["src", "nosrc"]
        ]
    rhnSQL.closeDB()
    return arches


def get_all_arches():
    """
    return the list of all compatible packages' arches regardless of the channel arch
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
    SELECT pa.label FROM rhnchannelpackagearchcompat cpac, rhnpackagearch pa 
    WHERE pa.id = cpac.package_arch_id"""
    )
    h.execute()
    all_arches = list(map(lambda d: d.get("label"), h.fetchall_dict()))
    rhnSQL.closeDB()
    return all_arches


def get_channel_info_by_label(channel_label):
    # TODO: possible exception handling
    rhnSQL.initDB()
    channel = rhnChannel.channel_info(channel_label)
    print(
        f"===> HAROUNE fetched channel = {channel}, for channel label = {channel_label}"
    )
    rhnSQL.closeDB()
    return channel or None


def get_repositories_by_channel_label(channel_label):
    """
    Fetch repositories information of a given channel form the database, and return a list of
    RepoDTO objects
    """
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """
        select c.label as channel_label, s.id, c_ark.name as channel_arch, s.source_url, s.metadata_signed, s.label as repo_label, cst.label as repo_type_label
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


if __name__ == "__main__":
    _arches = get_all_arches()
    print(f"All arches: {_arches}")
