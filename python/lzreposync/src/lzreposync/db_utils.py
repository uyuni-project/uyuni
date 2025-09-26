#  pylint: disable=missing-module-docstring
import logging
import time

from psycopg2 import errors
from psycopg2.errorcodes import UNIQUE_VIOLATION

from lzreposync.repo_dto import RepoDTO
from spacewalk.common.rhnConfig import cfg_component
from spacewalk.server import rhnSQL, rhnChannel


# TODO: move this function to a common place
#  (copied from python/spacewalk/server/test/misc_functions.py)
def _new_channel_dict(**kwargs):
    # pylint: disable-next=invalid-name
    _counter = 0

    label = kwargs.get("label")
    if label is None:
        # pylint: disable-next=consider-using-f-string
        label = "rhn-unittest-%.3f-%s" % (time.time(), _counter)
        # pylint: disable-next=invalid-name
        _counter = _counter + 1

    release = kwargs.get("release") or "release-" + label
    # pylint: disable-next=redefined-outer-name
    os = kwargs.get("os") or "Unittest Distro"
    if "org_id" in kwargs:
        # pylint: disable-next=unused-variable
        org_id = kwargs["org_id"]
    else:
        org_id = "rhn-noc"

    vdict = {
        "label": label,
        "name": kwargs.get("name") or label,
        "summary": kwargs.get("summary") or label,
        "description": kwargs.get("description") or label,
        "basedir": kwargs.get("basedir") or "/",
        "channel_arch": kwargs.get("channel_arch") or "i386",
        "channel_families": [kwargs.get("channel_family") or label],
        "org_id": kwargs.get("org_id"),
        "gpg_key_url": kwargs.get("gpg_key_url"),
        "gpg_key_id": kwargs.get("gpg_key_id"),
        "gpg_key_fp": kwargs.get("gpg_key_fp"),
        "end_of_life": kwargs.get("end_of_life"),
        "dists": [
            {
                "release": release,
                "os": os,
            }
        ],
    }
    return vdict


class ChannelAlreadyExistsException(Exception):
    """
    Exception raised when a channel already exists in the db
    """


def create_channel(channel_label, channel_arch, org_id=1):
    """
    Create a new test channel with label :channel_label using the channel family private-channel-family-1
    :channel_arch: eg: "x86_64"
    """
    rhnSQL.initDB()
    try:
        # Channel family "private-channel-family-1" is automatically created when starting the susemanager docker db
        channel_family_label = "private-channel-family-1"

        # Create a new channel using the channel family info
        vdict = _new_channel_dict(
            label=channel_label,
            channel_family=channel_family_label,
            org_id=org_id,
            channel_arch=channel_arch,
        )
        c = rhnChannel.Channel()
        c.load_from_dict(vdict)
        c.save()
        rhnSQL.commit()
        return c
    except errors.lookup(UNIQUE_VIOLATION) as exc:
        print(f"INFO: Channel {channel_label} already exists!")
        raise ChannelAlreadyExistsException() from exc
    finally:
        rhnSQL.closeDB()


def create_content_source(
    channel_label,
    repo_label,
    source_url,
    metadata_signed="N",
    org_id=1,
    source_type="yum",
    repo_id=1,
):
    """
    Create a new content source and associate it with the given channel
    source_type: yum|deb
    """
    try:
        rhnSQL.initDB()
        fetch_source_type_query = rhnSQL.prepare(
            """
            SELECT id from rhnContentSourceType where label = :source_type_label"""
        )
        fetch_source_type_query.execute(source_type_label=source_type)
        type_id = fetch_source_type_query.fetchone_dict()["id"]

        add_repo_query = rhnSQL.prepare(
            """INSERT INTO rhnContentSource(id, org_id, type_id, source_url, label, metadata_signed) VALUES (:repo_id, :org_id, 
            :type_id, :source_url, :label, :metadata_signed) 
            """
        )
        add_repo_query.execute(
            repo_id=repo_id,
            org_id=org_id,
            type_id=type_id,
            source_url=source_url,
            label=repo_label,
            metadata_signed=metadata_signed,
        )

        fetch_source_id_query = rhnSQL.prepare(
            """
            SELECT id from rhnContentSource LIMIT 1"""
        )
        fetch_source_id_query.execute()
        source_id = fetch_source_id_query.fetchone_dict()["id"]

        # associate the source/repo with the channel
        fetch_channel_id_query = rhnSQL.prepare(
            """
                SELECT id FROM rhnChannel WHERE label = :channel_label"""
        )
        fetch_channel_id_query.execute(channel_label=channel_label)
        channel_id = fetch_channel_id_query.fetchone_dict()["id"]

        associate_repo_channel_query = rhnSQL.prepare(
            """INSERT INTO rhnChannelContentSource(source_id, channel_id) VALUES (:source_id, :channel_id)
            """
        )
        associate_repo_channel_query.execute(source_id=source_id, channel_id=channel_id)
        rhnSQL.commit()
    except errors.lookup(UNIQUE_VIOLATION):
        print(f"INFO: Source {repo_label} already exists!")
    finally:
        rhnSQL.closeDB()


# TODO: move this function to a common place
#  (copied from python/spacewalk/satellite_tools/reposync.py
def get_compatible_arches(channel_label):
    """Return a list of compatible package arch labels for the given channel"""
    rhnSQL.initDB()
    h = rhnSQL.prepare(
        """SELECT pa.label 
            FROM rhnChannelPackageArchCompat AS cpac
            INNER JOIN rhnChannel AS c ON c.channel_arch_id = cpac.channel_arch_id
            INNER JOIN rhnpackagearch AS pa ON cpac.package_arch_id = pa.id
            WHERE c.label = :channel_label"""
    )

    h.execute(channel_label=channel_label)
    res_dict = h.fetchall_dict()
    if not res_dict:
        logging.warning(
            "Couldn't fetch compatible arches for channel: %s", channel_label
        )
        return None
    # pylint: disable-next=invalid-name
    with cfg_component("server.susemanager") as CFG:
        arches = [
            k["label"]
            for k in res_dict
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
    """
    Fetch the channel information from the given label and return
    the result in a dict like object
    """
    rhnSQL.initDB()
    channel = rhnChannel.channel_info(channel_label)
    rhnSQL.closeDB()
    return channel or None


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


if __name__ == "__main__":
    _arches = get_all_arches()
    print(f"All arches: {_arches}")
