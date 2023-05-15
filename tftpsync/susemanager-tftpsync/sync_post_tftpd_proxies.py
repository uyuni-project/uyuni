"""
This module is the part that is installed on the Cobbler server and that pushes the content to the proxies.

Format of the ``pxe_cache.json`` file: JSON that has a filepath as a key. Each key has as a value a list/tuple with two
entries. The first entry is a float that is returned by an ``os.stat`` call on the file where the ``st_mtime`` is of
entry, the second entry is the sha1 sum of the file.
"""

#
#    sync_post_tftpd_proxies.py
#    Copyright (C) 2013  Novell, Inc.
#    Copyright (C) 2016  SUSE LLC
#
#    This library is free software; you can redistribute it and/or
#    modify it under the terms of the GNU Lesser General Public
#    License as published by the Free Software Foundation;
#    version 2.1 of the License
#
#    This library is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#    Lesser General Public License for more details.
#
#    You should have received a copy of the GNU Lesser General Public
#    License along with this library; if not, write to the Free Software
#    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA


import logging
import os
import time
import json
import uuid
from concurrent import futures
import threading
from typing import Dict, Optional, Tuple

from urllib.parse import urlencode
from urllib.request import urlopen, build_opener

import cobbler.MultipartPostHandler as MultipartPostHandler
from cobbler import utils

_DEBUG = False

logger = logging.getLogger()


def register():
    # this pure python trigger acts as if it were a legacy shell-trigger, but is much faster.
    # the return of this method indicates the trigger type
    return "/var/lib/cobbler/triggers/sync/post/*"


def run(api, args):
    del args  # unused, required API
    sync_uuid = uuid.uuid4().hex
    logger.info("sync_post_tftp_proxies started - this can take a while (to see the progress check the cobbler logs)")
    settings = api.settings()

    # test if proxies are configured:
    if not settings.proxies:
        # not configured - so we return
        return 0

    tftpbootdir = settings.tftpboot_location
    find_delete_from_proxies(sync_uuid, tftpbootdir, settings)

    push_futures = []
    with futures.ThreadPoolExecutor() as executor:
        for dirname, _dirnames, filenames in os.walk(tftpbootdir):
            for fname in filenames:
                path = os.path.join(dirname, fname)
                if '.link_cache' in path:
                    continue
                push_futures.append(executor.submit(check_push, sync_uuid, path, tftpbootdir, settings))

        _update_pxe_cache([f.result() for f in futures.as_completed(push_futures)])

    return 0


def _update_pxe_cache(thread_results):
    cache: Dict[str, Tuple[float, str]] = {}
    for res in thread_results:
        cache.update(res)
    with open("/var/lib/cobbler/pxe_cache.json", "w") as pxe_cache:
        json.dump(cache, pxe_cache)


def sync_to_proxies(sync_uuid: str, filename: str, tftpbootdir: str, format: str, settings):
    """Sync file to all defined proxies"""
    ret = True
    # Proxy timeout
    try:
        timeout = settings.tftpsync_timeout
    except:
        timeout = 15

    logger.info("uploading %s to all known proxies. Timeout: %s", filename, timeout)

    sync_threads = []
    for proxy in settings.proxies:
        thread = ProxySync(filename, tftpbootdir, format, proxy, timeout, sync_uuid)
        sync_threads.append(thread)
        thread.start()

    for t in sync_threads:
        t.join()

    return ProxySync.Result


def delete_from_proxies(sync_uuid: str, path: str, settings):
    """Delete path from proxies"""
    # Proxy timeout
    try:
        timeout = settings.tftpsync_timeout
    except:
        timeout = 15

    # reset the Result var
    ProxyDelete.ResultLock.acquire()
    ProxyDelete.Result = True
    ProxyDelete.ResultLock.release()

    logger.info("deleting %s from all known proxies. Timeout: %s", path, timeout)

    del_threads = []
    # Handle if settings["proxies"] is not set in yaml config
    for proxy in settings.proxies:
        thr = ProxyDelete(path, proxy, timeout, sync_uuid)
        del_threads.append(thr)
        thr.start()

    for t in del_threads:
        t.join()

    return ProxyDelete.Result


def find_delete_from_proxies(sync_uuid: str, tftpbootdir: str, settings, lcache: str = '/var/lib/cobbler'):
    """Delete files from proxies"""
    db: Dict[str, Tuple[float, str]] = {}
    changed = False
    del_paths = list()
    try:
        dbfile = os.path.join(lcache, 'pxe_cache.json')
        if os.path.exists(dbfile):
            db = json.load(open(dbfile, 'r'))
    except:
        logger.error("Cannot load cachefile")
        return

    for path in db:
        if not path.startswith(tftpbootdir):
            continue
        if os.path.exists(path):
            continue
        relpath = path.replace("%s/" % tftpbootdir, "", 1)
        if delete_from_proxies(sync_uuid, relpath, settings):
            changed = True
            del_paths.append(path)
            logger.info("Delete successful")
        else:
            logger.info("Delete failed")

    if changed:
        for p in del_paths:
            del db[p]
        json.dump(db, open(dbfile, 'w'))


def check_push(sync_uuid: str, fn: str, tftpbootdir: str, settings, lcache: str = '/var/lib/cobbler') -> Dict[str, Tuple[float, str]]:
    """
    Returns the sha1sum of the file
    """

    db: Dict[str, Tuple[float, str]] = {}
    dbfile = os.path.join(lcache, 'pxe_cache.json')
    try:
        if os.path.exists(dbfile):
            db = json.load(open(dbfile, 'r'))
    except:
        pass

    count = 0
    while not os.path.exists(fn) and count < 10:
        count += 1
        if _DEBUG:
            logger.debug("%s does not exist yet - retrying (try %s) (sync uuid: %s)", fn, count, sync_uuid)
        time.sleep(1)

    mtime = os.stat(fn).st_mtime
    key: Optional[str] = None
    needpush = True
    if _DEBUG:
        logger.debug("check_push(%s) (sync uuid: %s)", fn, sync_uuid)
    if fn in db:
        if db[fn][0] < mtime:
            if _DEBUG:
                logger.debug("mtime differ - old: %s new: %s (sync uuid: %s)", db[fn][0], mtime, sync_uuid)
            if os.path.exists(fn):
                cmd = '/usr/bin/sha1sum %s' % fn
                key = utils.subprocess_get(cmd).split(' ')[0]
                if _DEBUG:
                    logger.debug("checking checksum - old: %s new: %s (sync uuid: %s)", db[fn][1], key, sync_uuid)
                if key == db[fn][1]:
                    needpush = False
        else:
            needpush = False
    if key is None:
        if os.path.exists(fn):
            cmd = '/usr/bin/sha1sum %s' % fn
            key = utils.subprocess_get(cmd).split(' ')[0]

    if _DEBUG:
        logger.debug("push(%s) ? %s (sync uuid: %s)", fn, needpush, sync_uuid)
    if needpush:
        # reset the Result var
        ProxySync.ResultLock.acquire()
        ProxySync.Result = True
        ProxySync.ResultLock.release()

        format = 'other'
        if "pxelinux.cfg" in fn:
            format = 'pxe'
        elif "grub/system" in fn:
            format = 'grub'
        if sync_to_proxies(sync_uuid, fn, tftpbootdir, format, settings):
            db[fn] = (mtime, key)
            logger.info("Push successful (sync uuid: %s)", sync_uuid)
        else:
            logger.info("Push failed (sync uuid: %s)", sync_uuid)
    return db


class ProxySync(threading.Thread):
    Result = True
    ResultLock = threading.Lock()

    def __init__(self, filename: str, tftpbootdir: str, format: str, proxy: str, timeout: int, sync_uuid: str):
        threading.Thread.__init__(self)

        self.filename = filename
        self.tftpbootdir = tftpbootdir
        self.format = format
        self.proxy = proxy
        self.timeout = timeout
        self.sync_uuid = sync_uuid

    def run(self):
        """Sync file to proxy"""
        ret = True

        logger.info("uploading %s to proxy %s as %s (sync uuid: %s)", self.filename, self.proxy, os.path.basename(self.filename), self.sync_uuid)
        opener = build_opener(MultipartPostHandler.MultipartPostHandler)
        path = os.path.dirname(self.filename)
        if not path.startswith(self.tftpbootdir):
            logger.error("Invalid path: %s (sync uuid: %s)", path, self.sync_uuid)
            ret = False
        if ret:
            path = path.replace("%s/" % self.tftpbootdir, "", 1)
            params = {
                "file_name": os.path.basename(self.filename),
                "file": open(self.filename, "rb"),
                "file_type": self.format,
                "directory": path,
                "sync_uuid": self.sync_uuid,
            }
            try:
                response = opener.open("http://%s/tftpsync/add/" % self.proxy, params, float(self.timeout))
            except Exception as e:
                ret = False
                logger.error("uploading to proxy %s failed: %s (sync uuid: %s)", self.proxy, e, self.sync_uuid)
        if not ret:
            ProxySync.ResultLock.acquire()
            ProxySync.Result = ret
            ProxySync.ResultLock.release()


class ProxyDelete(threading.Thread):
    Result = True
    ResultLock = threading.Lock()

    def __init__(self, path: str, proxy: str, timeout: int, sync_uuid: str):
        threading.Thread.__init__(self)

        self.path = path
        self.proxy = proxy
        self.timeout = timeout
        self.sync_uuid = sync_uuid

    def run(self):
        """Delete file from proxy"""
        ret = True

        logger.info("removing %s from %s (sync uuid: %s)", self.path, self.proxy, self.sync_uuid)

        p = {
            'file_name': os.path.basename(self.path),
            'directory': os.path.dirname(self.path),
            'file_type': 'other',
            "sync_uuid": self.sync_uuid,
        }
        if "pxelinux.cfg" in self.path:
            p["file_type"] = 'pxe'
        elif "grub" in self.path:
            p["file_type"] = 'grub'

        parameters = urlencode(p)

        try:
            url = "https://%s/tftpsync/delete/?%s" % (self.proxy, parameters)
            data = urlopen(url, None, float(self.timeout))
        except Exception as e:
            ret = False
            logger.info("removal from proxy %s failed (sync uuid: %s): %s", self.proxy, self.sync_uuid, e)

        if not ret:
            ProxyDelete.ResultLock.acquire()
            ProxyDelete.Result = ret
            ProxyDelete.ResultLock.release()
