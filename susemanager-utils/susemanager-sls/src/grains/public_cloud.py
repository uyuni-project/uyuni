# -*- coding: utf-8 -*-
'''
Copyright (c) 2019 SUSE LLC

This software is licensed to you under the GNU General Public License,
version 2 (GPLv2). There is NO WARRANTY for this software, express or
implied, including the implied warranties of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
along with this software; if not, see
http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

This grain module is only loaded in case of a public cloud instance.

Supported Instances: AWS EC2, Azure and Google Compute Engine instances

Returns a grain called "instance_id" containing the virtual instance ID
according to the Public Cloud provider. The data is gathered using the
internal API available from within the instance.

Author: Pablo Suárez Hernández <psuarezhernandez@suse.com>
Based on: https://docs.saltstack.com/en/latest/ref/grains/all/salt.grains.metadata.html
'''
from __future__ import absolute_import, print_function, unicode_literals

# Import python libs
import os
import socket
from multiprocessing.pool import ThreadPool
import logging

# Import salt libs
import salt.utils.http as http

# Internal metadata API information
INTERNAL_API_IP = '169.254.169.254'
HOST = 'http://{0}/'.format(INTERNAL_API_IP)

INSTANCE_ID = None

AMAZON_URL_PATH = 'latest/meta-data/'
AZURE_URL_PATH = 'metadata/instance/compute/'
AZURE_API_ARGS = '?api-version=2017-08-01&format=text'
GOOGLE_URL_PATH = 'computeMetadata/v1/instance/'

log = logging.getLogger(__name__)


def __virtual__():
    global INSTANCE_ID
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(0.1)
    result = sock.connect_ex((INTERNAL_API_IP, 80))
    if result != 0:
        return False

    def _do_api_request(data):
        opts = {
            'http_connect_timeout': 0.1,
            'http_request_timeout': 0.1,
        }
        try:
            ret = {
                data[0]: http.query(data[1],
                                    status=True,
                                    header_dict=data[2],
                                    raise_error=False,
                                    opts=opts)
            }
        except:
            ret = { data[0]: dict() }
        return ret

    api_check_dict = [
        ('amazon', os.path.join(HOST, AMAZON_URL_PATH), None),
        ('google', os.path.join(HOST, GOOGLE_URL_PATH), {"Metadata-Flavor": "Google"}),
        ('azure', os.path.join(HOST, AZURE_URL_PATH) + AZURE_API_ARGS, {"Metadata":"true"}),
    ]

    api_ret = {}
    results = []

    try:
       pool = ThreadPool(3)
       results = pool.map(_do_api_request, api_check_dict)
       pool.close()
       pool.join()
    except Exception as exc:
       import traceback
       log.error(traceback.format_exc())
       log.error("Exception while creating a ThreadPool for accessing metadata API: %s", exc)

    for i in results:
        api_ret.update(i)

    if api_ret['amazon'].get('status', 0) == 200 and "instance-id" in api_ret['amazon']['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, AMAZON_URL_PATH, 'instance-id'), raise_error=False)['body']
        return True
    elif api_ret['azure'].get('status', 0) == 200 and "vmId" in api_ret['azure']['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, AZURE_URL_PATH, 'vmId') + AZURE_API_ARGS, header_dict={"Metadata":"true"}, raise_error=False)['body']
        return True
    elif api_ret['google'].get('status', 0) == 200 and "id" in api_ret['google']['body']:
        INSTANCE_ID = http.query(os.path.join(HOST, GOOGLE_URL_PATH, 'id'), header_dict={"Metadata-Flavor": "Google"}, raise_error=False)['body']
        return True

    return False


def instance_id():
    global INSTANCE_ID
    ret = {}
    if INSTANCE_ID:
        ret['instance_id'] = INSTANCE_ID
    return ret
