#!/usr/bin/python
#
# Tool to update the uptime and kernel version
# Adapted from wrapper.py
# Copyright (c) 2012 SUSE LLC
#
# Authors:
#       Michael Calmer <mc@suse.de>

import os
import sys

sys.path.append("/usr/share/rhn/")

from up2date_client import up2dateAuth
from up2date_client import rhncli, rhnserver

class StatusCli(rhncli.RhnCli):

    def main(self):
        if not up2dateAuth.getSystemId():
            sys.exit(1)

        if not self._testRhnLogin():
            sys.exit(1)

        s = rhnserver.RhnServer()
        if s.capabilities.hasCapability('queue.update_status'):
            status_report = StatusCli.__build_status_report()
            s.queue.update_status(up2dateAuth.getSystemId(), status_report)

    @staticmethod
    def __build_status_report():
        """Copied from rhn_check"""
        status_report = {}
        status_report["uname"] = tuple(os.uname())

        if os.access("/proc/uptime", os.R_OK):
            uptime = open("/proc/uptime", "r").read().split()
            try:
                status_report["uptime"] = list(map(int, map(float, uptime)))
            except (TypeError, ValueError):
                status_report["uptime"] = list(map(lambda a: a[:-3], uptime))
            except:
                pass

        return status_report

if __name__ == "__main__":
    cli = StatusCli()
    cli.run()
