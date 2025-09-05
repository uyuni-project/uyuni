#  pylint: disable=missing-module-docstring
#
# Copyright (c) 2013 Novell, Inc
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
## purpose: possibility to upload a PXE configuration file from the SUSE
##          Manager, replaces the hostname of the SUSE Manager with the
##          hostname of the SUSE Manager Proxy on the fly

import os
import re
import logging
import logging.handlers

# pylint: disable=deprecated-module
import cgi
import tempfile

try:
    from cStringIO import OutputType
except ImportError:
    from io import StringIO as OutputType
from io import BytesIO
from spacewalk.common.rhnConfig import CFG, initCFG

initCFG("tftpsync")

# create logger
logger = logging.getLogger("tftpsync_add")
logger.setLevel(logging.INFO)

# create RotatingFileHandler handler and set level to INFO
ch = logging.handlers.RotatingFileHandler(
    "/var/log/tftpsync/tftpsync.log", mode="a", maxBytes=1048576, backupCount=10
)
ch.setLevel(logging.INFO)

# create formatter
formatter = logging.Formatter("%(asctime)s - %(name)s - %(levelname)s - %(message)s")

# add formatter to ch
ch.setFormatter(formatter)

# add ch to logger
logger.addHandler(ch)


class TftpFieldStorage(cgi.FieldStorage):

    def make_file(self, binary=None):
        del binary
        tmpdir = os.path.join(CFG.TFTPBOOT, "tmp")
        if not os.path.exists(tmpdir):
            os.makedirs(tmpdir)

        return tempfile.NamedTemporaryFile(
            mode="w+b", suffix="", prefix="tmp", dir=tmpdir, delete=False
        )

def get_safe_path(base_dir, user_directory, user_filename):
    """
    Return an absolute path under base_dir built from user_directory
    and user_filename. All parameters must not be None.
    """

    abs_base_dir = os.path.abspath(base_dir)
    combined_path = os.path.join(abs_base_dir, user_directory, user_filename)

    abs_final_path = os.path.abspath(combined_path)
    if not abs_final_path.startswith(os.path.join(abs_base_dir, '')):
        return None

    return abs_final_path

def application(environ, start_response):
    status = "500 Server Error"
    content = ""

    form = TftpFieldStorage(
        fp=environ["wsgi.input"], environ=environ, keep_blank_values=1
    )
    file_name = form.getvalue("file_name")
    file_type = form.getvalue("file_type")
    directory = form.getvalue("directory")
    tfpointer = form["file"].file

    if not (file_name and file_type and directory):
        logger.error("'file_name', 'directory' or 'file_type' not specified")
        content = (
            "please provide the parameters 'file_name', 'directory' and 'file_type'"
        )
    elif not (CFG.TFTPBOOT and CFG.SERVER_IP and CFG.PROXY_IP and CFG.SERVER_FQDN and CFG.PROXY_FQDN):
        logger.error("Incomplete configuration")
        content = "Incomplete configuration"
    elif get_safe_path(CFG.TFTPBOOT, directory, file_name) is None:
        logger.error("Insecure path specified")
        content = 'Insecure directory'
    elif form.length == 0:
        logger.error("No file content")
        content = "No file content"
    else:
        path = os.path.join(CFG.TFTPBOOT, directory)
        tfname = None
        try:
            if not os.path.exists(path):
                os.makedirs(path, exist_ok=True)

            rfname = os.path.join(path, file_name)
            tfname = f"{rfname}.tmp"
            if file_type == "pxe" or file_type == "grub":
                tf = open(tfname, "wb")
                file_content = form.getvalue("file")
                file_content = file_content.decode()
                file_content = re.sub(
                    r"\b" + re.escape(CFG.SERVER_IP) + r"\b",
                    CFG.PROXY_IP,
                    file_content,
                )
                file_content = re.sub(
                    r"\b" + re.escape(CFG.SERVER_FQDN) + r"\b",
                    CFG.PROXY_FQDN,
                    file_content,
                )
                if CFG.SERVER_IP6 and CFG.PROXY_IP6:
                    file_content = re.sub(
                        r"\b" + re.escape(CFG.SERVER_IP6) + r"\b",
                        CFG.PROXY_IP6,
                        file_content,
                    )
                tf.write(file_content.encode())
                tf.close()
                os.rename(tfname, rfname)
            elif isinstance(tfpointer, OutputType) or isinstance(tfpointer, BytesIO):
                tf = open(tfname, "wb")
                tf.write(form.getvalue("file"))
                tf.close()
                os.rename(tfname, rfname)
            else:
                if tfpointer and os.path.exists(tfpointer.name):
                    os.chmod(tfpointer.name, 0o644)
                    os.rename(tfpointer.name, rfname)
                else:
                    raise IOError("Source file not found")

            status = "200 OK"
            content = f"setting file '{rfname}' ({file_type}), status: {status}"
            logger.info(content)
        # pylint: disable-next=broad-exception-caught
        except Exception as e:
            # remove tmp file if exists
            if tfname and os.path.exists(tfname):
                os.unlink(tfname)
            logger.error("Writing file failed: %s", e, exc_info=True)
            content = "Writing file failed"

    # remove tmp file if exists
    if (
        tfpointer
        and not isinstance(tfpointer, OutputType)
        and hasattr(tfpointer, "name")
        and os.path.exists(tfpointer.name)
    ):
        os.unlink(tfpointer.name)

    response_headers = [
        ("Content-type", "text/plain;charset=utf-8"),
        ("Content-Length", str(len(content))),
    ]
    start_response(status, response_headers)

    return [content.encode()]
