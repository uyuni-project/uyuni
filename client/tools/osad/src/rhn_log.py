#
# Copyright (c) 2008--2012 Red Hat, Inc.
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

import os
import sys
import time
import string
import traceback

class Logger:
    debug_level = 1
    logfile = "/var/log/osad"

    def set_logfile( self, logfile ):
        Logger.logfile = logfile

    def log_debug(self, debug_level, *args):
        if debug_level <= self.debug_level:
            info_out =  (
                time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(time.time())),
                self.get_caller(),
                string.join(map(str, args))
            )

            outstring = "%s %s: %s\n" % info_out
            sys.stdout.write( outstring )

            if not Logger.logfile is None:
                try:
                    fd = os.open(Logger.logfile, os.O_APPEND | os.O_RDWR | os.O_CREAT, 0600)
                    os.write(fd, outstring)
                    os.close(fd)
                except IOError:
                    raise

    def set_debug_level(self, debug_level):
        Logger.debug_level = debug_level

    def get_caller(self, caller_offset=4):
        tbStack = traceback.extract_stack()
        callid = len(tbStack) - caller_offset
        module = tbStack[callid]
        module_file = os.path.basename(module[0])
        module_file = string.split(module_file, '.', 1)[0]
        return "%s.%s" % (module_file, module[2])

    def log_error(self, *args):
        log_debug(self, 0, *args)
        line = map(str, args)
        sys.stderr.write(string.join(line))
        sys.stderr.write("\n")

    def die(self, error_code, *args):
        self.log_error(args)
        sys.exit(error_code)

def set_logfile(*args):
    return apply( Logger().set_logfile, args )

def set_debug_level(*args):
    return apply(Logger().set_debug_level, args)

def get_debug_level():
    return Logger().debug_level

def log_debug(*args):
    return apply(Logger().log_debug, args)

def log_error(*args):
    return apply(Logger().log_error, args)

def die(error_code, *args):
    apply(Logger().log_error, args)
    sys.exit(error_code)
