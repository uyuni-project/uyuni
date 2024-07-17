#  pylint: disable=missing-module-docstring

# TODO Remove this file (used only for illustration purposes and code review)
import updates_util
from lz_reposync import LzRepoSync

update_info_xml = (
    "cc162e424fa20219fa2c046a9c66c99281fa7908561aca9566fd58265c299f01-updateinfo.xml"
)

channel_label = "development_channel"

notices_type, notices = updates_util.get_updates(update_info_xml)

lzreposync = LzRepoSync(channel_label=channel_label)

lzreposync.import_updates(notices)
