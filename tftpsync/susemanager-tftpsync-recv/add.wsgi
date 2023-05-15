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
import shutil
import logging
import logging.handlers
import cgi
import tempfile
from io import StringIO as OutputType
from io import BytesIO
from uuid import UUID

from spacewalk.common.rhnConfig import CFG, initCFG

initCFG("tftpsync")

# create logger
logger = logging.getLogger('tftpsync_add')
logger.setLevel(logging.INFO)

# create RotatingFileHandler handler and set level to INFO
ch = logging.handlers.RotatingFileHandler("/var/log/tftpsync/tftpsync.log", mode='a', maxBytes=1048576, backupCount=10)
ch.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
ch.setFormatter(formatter)
logger.addHandler(ch)


def validate_uuid(possible_uuid: str) -> bool:
    """
    Validate if the handed string is a valid UUIDv4 hex representation.

    :param possible_uuid: The str with the UUID.
    :return: True in case it is one, False otherwise.
    """
    if not isinstance(possible_uuid, str):
        return False
    # Taken from: https://stackoverflow.com/a/33245493/4730773
    try:
        uuid_obj = UUID(possible_uuid, version=4)
    except ValueError:
        return False
    return uuid_obj.hex == possible_uuid


def _application(environ, start_response):
    status = '500 Server Error'
    if not (CFG.TFTPBOOT and re.match(r'^/\w+.*$', CFG.TFTPBOOT) and
            os.path.exists(CFG.TFTPBOOT)):
        logger.error("Invalid tftp directory configuration")
        content = 'Invalid tftp directory configuration'
        response_headers = [('Content-type', 'text/plain;charset=utf-8'),
                            ('Content-Length', str(len(content)))]
        start_response(status, response_headers)
        return [content.encode()]

    form = cgi.FieldStorage(fp=environ['wsgi.input'], environ=environ, keep_blank_values=1)
    file_name = form.getvalue('file_name')
    file_type = form.getvalue('file_type')
    directory = form.getvalue('directory')
    sync_uuid = form.getvalue('sync_uuid', None)
    with form['file'].file as temporary_file_pointer:
        if not (file_name and file_type and directory and sync_uuid):
            logger.error("'file_name', 'directory', 'file_type' or 'sync_uuid' not specified")
            content = "please provide the parameters 'file_name', 'directory', 'file_type' and 'sync_uuid'"
        elif not validate_uuid(sync_uuid):
            logger.error("Invalid UUID passed!")
            content = 'Invalid UUID passed'
        elif ".." in directory or not re.match(r'^[/:a-zA-Z0-9._-]+$', directory):
            # don't print the parameter because of security concerns
            logger.error("Insecure directory parameter given. (sync uuid: %s)", sync_uuid)
            content = 'Insecure directory'
        elif ".." in file_name or not re.match(r'^[:a-zA-Z0-9._-]+$', file_name):
            # don't print the parameter because of security concerns
            logger.error("Insecure file_name parameter given. (sync uuid: %s)", sync_uuid)
            content = 'Insecure file_name'
        elif not (CFG.SERVER_IP and CFG.PROXY_IP and CFG.SERVER_FQDN and CFG.PROXY_FQDN):
            logger.error("Incomplete configuration. (sync uuid: %s)", sync_uuid)
            content = 'Incomplete configuration'
        elif form.length == 0:
            logger.error("No file content. (sync uuid: %s)", sync_uuid)
            content = "No file content"
        else:
            path = os.path.join(CFG.TFTPBOOT, directory)
            try:
                if not os.path.exists(path):
                    os.makedirs(path, exist_ok=True)

                real_file_name = os.path.join(path, file_name)
                if file_type == 'pxe' or file_type == 'grub':
                    with open(real_file_name, 'wb') as tf:
                        file_content = form.getvalue('file')
                        file_content = file_content.replace(CFG.SERVER_IP.encode(), CFG.PROXY_IP.encode())
                        file_content = file_content.replace(CFG.SERVER_FQDN.encode(), CFG.PROXY_FQDN.encode())
                        if CFG.SERVER_IP6 and CFG.PROXY_IP6:
                            file_content = file_content.replace(CFG.SERVER_IP6.encode(), CFG.PROXY_IP6.encode())
                        tf.write(file_content)
                elif isinstance(temporary_file_pointer, OutputType) or isinstance(temporary_file_pointer, BytesIO):
                    with open(real_file_name, 'wb') as tf:
                        tf.write(form.getvalue('file'))
                else:
                    if temporary_file_pointer and os.path.exists(temporary_file_pointer.name):
                        os.chmod(temporary_file_pointer.name, 0o644)
                        os.rename(temporary_file_pointer.name, real_file_name)
                    else:
                        raise IOError("Source file not found")

                status = "200 OK"
                content = f"setting file '{real_file_name}' ({file_type}), status: {status}"
                logger.info(content)
            except Exception as e:
                logger.error("Writing file failed: %s", e, exc_info=True)
                content = "Writing file failed"

    response_headers = [('Content-type', 'text/plain;charset=utf-8'),
                        ('Content-Length', str(len(content)))]
    start_response(status, response_headers)

    return [content.encode()]


def application(environ, start_response):
    try:
        return _application(environ, start_response)
    finally:
        # Cleanup tmpdir to cleanup temporary files in case server times out
        tempdir = os.path.join(CFG.TFTPBOOT, "tmp")
        shutil.rmtree(tempdir)
