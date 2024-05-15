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
## purpose: possibility to delete a PXE configuration file on the SUSE Manager
##          Proxy from the SUSE Maanger
##

import os
import re
import logging
import logging.handlers
try:
    import urlparse
except:
    from urllib import parse as urlparse
from uyuni.common.rhnConfig import CFG, initCFG

initCFG("tftpsync")

# create logger
logger = logging.getLogger('tftpsync_delete')
logger.setLevel(logging.INFO)

# create RotatingFileHandler handler and set level to INFO
ch = logging.handlers.RotatingFileHandler("/var/log/tftpsync/tftpsync.log",
                                          mode='a', maxBytes=1048576, backupCount=10)
ch.setLevel(logging.INFO)

# create formatter
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# add formatter to ch
ch.setFormatter(formatter)

# add ch to logger
logger.addHandler(ch)

def application(environ, start_response):
    status = '500 Server Error'
    content = ''
    file_name = None
    file_type = None
    directory = None
    for key, value in urlparse.parse_qsl(environ.get("QUERY_STRING", ""), 0, 0):
        if key == "file_name":
            file_name = value
        elif key == "file_type":
            file_type = value
        elif key == "directory":
            directory = value

    if not (CFG.TFTPBOOT and re.match('^/[\w]+.*$', CFG.TFTPBOOT) and
              os.path.exists(CFG.TFTPBOOT)):
        logger.error("Invalid tftp directory configuration")
        content = 'Invalid tftp directory configuration'
    elif not (file_name and file_type and directory):
        logger.error("'file_name', 'directory' or 'file_type' not specified")
        content = "please provide the parameters 'file_name', 'directory' and 'file_type'"
    elif ".." in directory or not re.match('^[/\:a-zA-Z0-9._-]+$', directory):
        # don't print the parameter because of security concerns
        logger.error("Insecure directory parameter given")
        content = 'Insecure directory'
    elif ".." in file_name or not re.match('^[\:a-zA-Z0-9._-]+$', file_name):
        # don't print the parameter because of security concerns
        logger.error("Insecure file_name parameter given")
        content = 'Insecure file_name'
    elif not (CFG.SERVER_IP and CFG.PROXY_IP and CFG.SERVER_FQDN and CFG.PROXY_FQDN):
        logger.error("Incomplete configuration")
        content = 'Incomplete configuration'
    else:
        path = os.path.join(CFG.TFTPBOOT, directory, file_name)

        logger.info("Removing %s (%s)" % (path, file_type))

        if os.path.exists(path):
            try:
                os.remove(path)
                status = '200 OK'
                content = "removing file '%s', status: %s" % (path, status)
            except Exception as e:
                logger.error("os.remove(%s) failed: %s", path, e, exc_info=True)
                content = "removing %s failed" % path
        else:
            # success, if file not exists: we achieved what we wanted
            status = '200 OK'
            content = "removing file '%s', status: %s" % (path, status)

    response_headers = [('Content-type', 'text/plain;charset=utf-8'),
                        ('Content-Length', str(len(content)))]
    start_response(status, response_headers)

    return [content.encode()]


