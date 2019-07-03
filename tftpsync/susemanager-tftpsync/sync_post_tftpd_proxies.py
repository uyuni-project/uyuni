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


import os
import cobbler.utils as utils
import time
import cobbler.MultipartPostHandler as MultipartPostHandler
import simplejson
import cobbler.clogger as clogger
import threading

try:
    from urllib.parse import urlencode
    from urllib.request import urlopen, build_opener
except ImportError:
    from urllib import urlencode
    from urllib2 import urlopen, build_opener

_DEBUG = False

def register():
    # this pure python trigger acts as if it were a legacy shell-trigger, but is much faster.
    # the return of this method indicates the trigger type
    return "/var/lib/cobbler/triggers/sync/post/*"

def run(api,args,logger):
    if not logger:
        logger = clogger.Logger()
    logger.info("sync_post_tftp_proxies started")
    settings = api.settings()

    # test if proxies are configured:
    try:
        p = settings.proxies
    except:
        # not configured - so we return
        return 0

    tftpbootdir = "/srv/tftpboot"
    syncstart = os.stat(tftpbootdir).st_mtime

    find_delete_from_proxies(tftpbootdir, settings, logger=logger)

    for root, dirs, files in os.walk(tftpbootdir):
        for fname in files:
            path = os.path.join(root, fname)
            if '.link_cache' in path:
                continue
            check_push(path, tftpbootdir, settings, logger=logger)
    return 0

def sync_to_proxies(filename, tftpbootdir, format, settings, logger):
    """Sync file to all defined proxies"""
    ret = True
    # Proxy timeout
    try:
            timeout = settings.tftpsync_timeout
    except:
            timeout = 15

    if logger:
        logger.info("uploading %s to all known proxies. Timeout: %s" % (filename, timeout))

    sync_threads = []
    for proxy in settings.proxies:
        thread = ProxySync(filename, tftpbootdir, format, proxy, timeout, logger)
        sync_threads.append(thread)
        thread.start()

    for t in sync_threads:
        t.join()

    return ProxySync.Result

def delete_from_proxies(path, settings, logger):
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

    if logger:
        logger.info("deleting %s from all known proxies. Timeout: %s" % (path, timeout))

    del_threads = []
    # Handle if settings["proxies"] is not set in yaml config
    for proxy in settings.proxies:
        thr = ProxyDelete(path, proxy, timeout, logger)
        del_threads.append(thr)
        thr.start()

    for t in del_threads:
        t.join()

    return ProxyDelete.Result


def find_delete_from_proxies(tftpbootdir, settings, lcache='/var/lib/cobbler', logger=None):
    """Delete files from proxies"""
    db = {}
    changed = False
    del_paths = list()
    try:
        dbfile = os.path.join(lcache,'pxe_cache.json')
        if os.path.exists(dbfile):
            db = simplejson.load(open(dbfile, 'r'))
    except:
        logger.error("Cannot load cachefile")
        return

    for path in db:
        if not path.startswith(tftpbootdir):
            continue
        if os.path.exists(path):
            continue
        relpath = path.replace("%s/" % tftpbootdir, "", 1)
        if delete_from_proxies(relpath, settings, logger):
            changed = True
            del_paths.append(path)
            logger.info("Delete successfull")
        else:
            logger.info("Delete failed")

    if changed:
        for p in del_paths:
            del db[p]
        simplejson.dump(db, open(dbfile,'w'))

def check_push(fn, tftpbootdir, settings, lcache='/var/lib/cobbler', logger=None):
    """
    Returns the sha1sum of the file
    """

    db = {}
    try:
        dbfile = os.path.join(lcache,'pxe_cache.json')
        if os.path.exists(dbfile):
            db = simplejson.load(open(dbfile, 'r'))
    except:
        pass

    count = 0
    while not os.path.exists(fn) and count < 10:
        count += 1
        if _DEBUG:
            logger.debug("%s does not exist yet - retrying (try %s)" % (fn, count))
        time.sleep(1)

    mtime = os.stat(fn).st_mtime
    key = None
    needpush = True
    if _DEBUG:
        logger.debug("check_push(%s)" % fn)
    if fn in db:
        if db[fn][0] < mtime:
            if _DEBUG:
                logger.debug("mtime differ - old: %s new: %s" % (db[fn][0], mtime))
            if os.path.exists(fn):
                cmd = '/usr/bin/sha1sum %s'%fn
                key = utils.subprocess_get(None,cmd).split(' ')[0]
                if _DEBUG:
                    logger.debug("checking checksum - old: %s new: %s" % (db[fn][1], key))
                if key == db[fn][1]:
                    needpush = False
        else:
            needpush = False
    if key is None:
        if os.path.exists(fn):
            cmd = '/usr/bin/sha1sum %s'%fn
            key = utils.subprocess_get(None,cmd).split(' ')[0]

    if _DEBUG:
        logger.debug("push(%s) ? %s" % (fn, needpush))
    if needpush:
        # reset the Result var
        ProxySync.ResultLock.acquire()
        ProxySync.Result = True
        ProxySync.ResultLock.release()

        format = 'other'
        if "pxelinux.cfg" in fn:
            format = 'pxe'
        elif "grub" in fn:
            format = 'grub'
        if sync_to_proxies(fn, tftpbootdir, format, settings, logger):
            db[fn] = (mtime,key)
            simplejson.dump(db, open(dbfile,'w'))
            logger.info("Push successfull")
        else:
            logger.info("Push failed")


class ProxySync(threading.Thread):
    Result = True
    ResultLock = threading.Lock()

    def __init__(self, filename, tftpbootdir, format, proxy, timeout, logger):
        threading.Thread.__init__(self)

        self.filename = filename
        self.tftpbootdir = tftpbootdir
        self.format = format
        self.proxy = proxy
        self.timeout = timeout
        self.logger = logger


    def run(self):
        """Sync file to proxy"""
        ret = True

        if self.logger:
            self.logger.info("uploading %s to proxy %s as %s" % (self.filename, self.proxy, os.path.basename(self.filename)))
        opener = build_opener(MultipartPostHandler.MultipartPostHandler)
        path = os.path.dirname(self.filename)
        if not path.startswith(self.tftpbootdir):
            self.logger.error("Invalid path: %s" % path)
            ret = False
        if ret:
            path = path.replace("%s/" % self.tftpbootdir, "", 1)
            params = { "file_name" : os.path.basename(self.filename), "file" : open(self.filename, "rb"), "file_type" : self.format, "directory": path }
            try:
                response = opener.open("http://%s/tftpsync/add/" % self.proxy, params, self.timeout)
            except Exception as e:
                ret = False
                if self.logger:
                    self.logger.error("uploading to proxy %s failed: %s" % (self.proxy, e))
        if not ret:
            ProxySync.ResultLock.acquire()
            ProxySync.Result = ret
            ProxySync.ResultLock.release()


class ProxyDelete(threading.Thread):
    Result = True
    ResultLock = threading.Lock()

    def __init__(self, path, proxy, timeout, logger):
        threading.Thread.__init__(self)

        self.path = path
        self.proxy = proxy
        self.timeout = timeout
        self.logger = logger


    def run(self):
        """Delete file from proxy"""
        ret = True

        if self.logger is not None:
            self.logger.info("removing %s from %s" % (self.path, self.proxy))

        p = {'file_name': os.path.basename(self.path),
             'directory': os.path.dirname(self.path),
             'file_type': 'other'}
        if "pxelinux.cfg" in self.path:
            p["file_type"] = 'pxe'
        elif "grub" in self.path:
            p["file_type"] = 'grub'

        parameters = urlencode(p)

        try:
            url = "https://%s/tftpsync/delete/?%s" % (self.proxy, parameters)
            data = urlopen(url, None, self.timeout)
        except Exception as e:
            ret = False
            if self.logger:
                self.logger.info("delete from proxy %s failed: %s" % (self.proxy, e))

        if not ret:
            ProxyDelete.ResultLock.acquire()
            ProxyDelete.Result = ret
            ProxyDelete.ResultLock.release()


