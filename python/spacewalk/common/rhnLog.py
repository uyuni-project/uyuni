# rhnLog.py                                            - Logging functions. pylint: disable=missing-module-docstring,invalid-name
# ------------------------------------------------------------------------------
# This module contains the necessary functions for producing log messages to
# stderr, stdout or a specified filename. Used by all server-side code.
#
# USAGE: For general purposes, simply import the log_debug function and use it
#        as log_debug(min_level, *args)
#
# NOTE ON LOG LEVELS (rough descriptions):
# 1 - generally for 1 line log items and/or of relative importance
# 2 - shorter multi-line log items
# 3 - longer multi-line log items and/or of lesser importance
# 4 - excessive stuff
# 5 - really excessive stuff
#
# ------------------------------------------------------------------------------
#
# Copyright (c) 2008--2017 Red Hat, Inc.
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# Red Hat trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate Red Hat trademarks that are incorporated
# in this software or its documentation.
#

# system module imports
import os
import sys
import traceback
import time
import fcntl
import atexit
from uyuni.common.fileutils import getUidGid
from uyuni.common.context_managers import cfg_component

LOG = None

# helper function to format the current time in the log format


def log_time():
    if time.daylight:
        # altzone provides the DST-corrected time
        tz_offset = time.altzone
    else:
        # DST is not in effect
        tz_offset = time.timezone
    # Unfortunately, -3601 / 3600 == 2
    # Also, tz_offset's sign is reverted: it is positive west of GMT
    if tz_offset < 0:
        sign = "+"
    else:
        sign = "-"
    hours, secs = divmod(abs(tz_offset), 3600)
    mins = secs / 60

    tz_offset_string = " %s%02d:%02d" % (sign, hours, mins)  #  pylint: disable=consider-using-f-string
    t = time.strftime("%Y/%m/%d %H:%M:%S", time.localtime(time.time()))
    return t + tz_offset_string


# function for setting the close-on-exec flag


def set_close_on_exec(fd):
    s = fcntl.fcntl(fd, fcntl.F_GETFD)
    fcntl.fcntl(fd, fcntl.F_SETFD, s | fcntl.FD_CLOEXEC)


# pylint: disable=W0702

# Init the log


def initLOG(log_file="stderr", level=0, component=""):  #  pylint: disable=invalid-name
    global LOG

    # check if it already setup
    if LOG is not None:
        # We already have a logging object
        if log_file is None or LOG.file == log_file:
            # Keep the same logging object, change only the log level
            LOG.level = level
            return
        # We need a different type, so destroy the old one
        LOG = None
    elif log_file is None:
        log_file = "/dev/null"

    # attempt to create the path to the log file if neccessary
    log_path = os.path.dirname(log_file)
    if (
        log_file not in ("stderr", "stdout")
        and log_path
        and not os.path.exists(os.path.dirname(log_file))
    ):
        log_stderr(
            "{} WARNING: log path not found; attempting to create {}".format(  #  pylint: disable=consider-using-f-string
                component, log_path
            ),
            sys.exc_info()[:2],
        )

        # fetch uid, gid so we can do a "chown ..."
        with cfg_component(component=None) as CFG:  #  pylint: disable=invalid-name
            apache_uid, apache_gid = getUidGid(
                CFG.get("httpd_user", "wwwrun"), CFG.get("httpd_group", "www")
            )

        try:
            os.makedirs(log_path)
            os.chown(log_path, apache_uid, apache_gid)
        except:
            log_stderr(
                "{} ERROR: unable to create log file path {}".format(  #  pylint: disable=consider-using-f-string
                    component, log_path
                ),
                sys.exc_info()[:2],
            )
            return

    # At this point, LOG is None and log_file is not None
    # Get a new LOG
    LOG = rhnLog(log_file, level, component)
    return 0


# Convenient macro-type debugging function


def log_debug(level, *args):
    # Please excuse the style inconsistencies.
    if LOG and LOG.level >= level:
        LOG.logMessage(*args)


# Dump some information to stderr.


def log_stderr(*args):
    pid = os.getpid()
    for arg in args:
        sys.stderr.write("SUSE Manager %s %s: %s\n" % (pid, log_time(), arg))  #  pylint: disable=consider-using-f-string
    sys.stderr.flush()


# Convenient error logging function


def log_error(*args):
    if not args:
        return
    if LOG:
        LOG.logMessage("ERROR", *args)
    # log to stderr too
    log_stderr(str(args))


# Log a string with no extra info.


def log_clean(level, msg):
    if LOG and LOG.level >= level:
        LOG.writeToLog(msg)


# set the request object for the LOG so we don't have to expose the
# LOG object externally


def log_setreq(req):
    if LOG:
        LOG.set_req(req)


# The base log class


class rhnLog:  #  pylint: disable=missing-class-docstring,invalid-name
    def __init__(self, log_file, level, component):
        self.level = level
        self.component = component
        self.log_info = "0.0.0.0: "
        self.file = log_file
        self.pid = os.getpid()
        self.real = 0
        if self.file in ["stderr", "stdout"]:
            self.fd = getattr(sys, self.file)
            self.log_info = ""
            return

        newfileYN = 0  #  pylint: disable=invalid-name
        if not os.path.exists(self.file):
            newfileYN = 1  # just used for the chown/chmod  #  pylint: disable=invalid-name

        # else, open it as a real file, with locking and stuff
        try:
            # try to open it in line buffered mode
            self.fd = open(self.file, "a", 1)  #  pylint: disable=unspecified-encoding
            set_close_on_exec(self.fd)
            if newfileYN:
                with cfg_component(component=None) as CFG:  #  pylint: disable=invalid-name
                    apache_uid, apache_gid = getUidGid(
                        CFG.get("httpd_user", "wwwrun"), CFG.get("httpd_group", "www")
                    )
                os.chown(self.file, apache_uid, apache_gid)
                os.chmod(self.file, int("0660", 8))
        except:
            log_stderr(
                "ERROR LOG FILE: Couldn't open log file %s" % self.file,  #  pylint: disable=consider-using-f-string
                sys.exc_info()[:2],
            )
            self.file = "stderr"
            self.fd = sys.stderr
        else:
            self.real = 1

    # Main logging method.
    def logMessage(self, *args):  #  pylint: disable=invalid-name
        tbStack = traceback.extract_stack()  #  pylint: disable=invalid-name
        callid = len(tbStack) - 3
        module = ""
        try:  # So one can debug from the commandline.
            module = tbStack[callid][0]
            arr = module.split("/")
            if len(arr) > 1:
                lastDir = arr[-2] + "/"  #  pylint: disable=invalid-name
            else:
                lastDir = ""  #  pylint: disable=invalid-name
            filename = arr[-1]
            filename = filename[: filename.rindex(".")]
            module = lastDir + filename
            del lastDir
        except:
            module = ""

        msg = "%s%s.%s" % (self.log_info, module, tbStack[callid][2])  #  pylint: disable=consider-using-f-string
        if args:
            msg = "%s%s" % (msg, repr(args))  #  pylint: disable=consider-using-f-string
        if self.component:
            msg = "%s %s" % (self.component, msg)  #  pylint: disable=consider-using-f-string
        self.writeMessage(msg)

    # send a message to the log file w/some extra data (time stamp, etc).
    def writeMessage(self, msg):  #  pylint: disable=invalid-name
        if self.real:
            msg = "%s %d %s" % (log_time(), self.pid, msg)  #  pylint: disable=consider-using-f-string
        else:
            msg = "%s %s" % (log_time(), msg)  #  pylint: disable=consider-using-f-string
        self.writeToLog(msg)

    # send a message to the log file.
    def writeToLog(self, msg):  #  pylint: disable=invalid-name
        # this is for debugging in case of errors
        # fd = self.fd # no-op, but useful for dumping the current data
        self.fd.write("%s\n" % msg)  #  pylint: disable=consider-using-f-string

    # Reinitialize req info if req has changed.
    def set_req(self, req=None):
        remoteAddr = "0.0.0.0"  #  pylint: disable=invalid-name
        if req:
            if "X-Forwarded-For" in req.headers_in:
                remoteAddr = req.headers_in["X-Forwarded-For"]  #  pylint: disable=invalid-name
            else:
                remoteAddr = req.connection.remote_ip  #  pylint: disable=invalid-name
        self.log_info = "%s: " % (remoteAddr,)  #  pylint: disable=consider-using-f-string

    # shutdown the log
    def __del__(self):
        if self.real:
            self.fd.close()
        self.level = self.log_info = None
        self.pid = self.file = self.real = self.fd = None


def _exit():
    global LOG
    if LOG:
        del LOG
        LOG = None


atexit.register(_exit)


# ------------------------------------------------------------------------------
if __name__ == "__main__":
    print("You can not run this module by itself")
    sys.exit(-1)
# ------------------------------------------------------------------------------
