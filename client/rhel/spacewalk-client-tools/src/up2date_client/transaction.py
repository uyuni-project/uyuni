# pylint: disable=missing-module-docstring
#
# Client code for Update Agent
# Copyright (c) 1999--2016 Red Hat, Inc.  Distributed under GPLv2.
#
#         Adrian Likins <alikins@redhat.com
#
#
# a couple of classes wrapping up transactions so that we
#    can share transactions instead of creating new ones all over
#

import rpm

read_ts = None
ts = None

# ************* NOTE: ************#
# for the sake of clarity, the names "added/removed" as used here
# are indicative of what happened when the original transaction was
# ran. Aka, if you "up2date foobar" and it updates foobar-1-0 with
# foobar-2-0, you added foobar-2-0 and removed foobar-1-0
#
# The reason I mention this explicitly is the trouble of describing
# what happens when you rollback the transaction, which is basically
# the opposite, and leads to plenty of confusion
#


# pylint: disable-next=missing-class-docstring
class TransactionData:
    # simple data structure designed to transport info
    # about rpm transactions around
    def __init__(self):
        self.data = {}
        # a list of tuples of pkg info, and mode ('e', 'i', 'u')
        # the pkgInfo is tuple of [name, version, release, epoch, arch]
        # size is never used directly for this, it's here as a place holder
        # arch is optional, if the server specifies it, go with what
        # removed packages only need [n,v,r,e,arch]
        self.data["packages"] = []
        # list of flags to set for the transaction
        self.data["flags"] = []
        self.data["vsflags"] = []
        self.data["probFilterFlags"] = []

    def display(self):
        out = ""
        removed = []
        installed = []
        updated = []
        misc = []
        # pylint: disable-next=invalid-name
        for pkgInfo, mode in self.data["packages"]:
            if mode == "u":
                updated.append(pkgInfo)
            elif mode == "i":
                installed.append(pkgInfo)
            elif mode == "e":
                removed.append(pkgInfo)
            else:
                misc.append(pkgInfo)
        # pylint: disable-next=invalid-name
        for pkgInfo in removed:
            # pylint: disable-next=consider-using-f-string
            out = out + "\t\t[e] %s-%s-%s:%s\n" % (
                pkgInfo[0],
                pkgInfo[1],
                pkgInfo[2],
                pkgInfo[3],
            )
        # pylint: disable-next=invalid-name
        for pkgInfo in installed:
            # pylint: disable-next=consider-using-f-string
            out = out + "\t\t[i] %s-%s-%s:%s\n" % (
                pkgInfo[0],
                pkgInfo[1],
                pkgInfo[2],
                pkgInfo[3],
            )
        # pylint: disable-next=invalid-name
        for pkgInfo in updated:
            # pylint: disable-next=consider-using-f-string
            out = out + "\t\t[u] %s-%s-%s:%s\n" % (
                pkgInfo[0],
                pkgInfo[1],
                pkgInfo[2],
                pkgInfo[3],
            )
        # pylint: disable-next=invalid-name
        for pkgInfo in misc:
            # pylint: disable-next=consider-using-f-string
            out = out + "\t\t[%s] %s-%s-%s:%s\n" % (
                pkgInfo[5],
                pkgInfo[0],
                pkgInfo[1],
                pkgInfo[2],
                pkgInfo[3],
            )
        return out


# wrapper/proxy class for rpm.Transaction so we can
# instrument it, etc easily
# pylint: disable-next=missing-class-docstring
class Up2dateTransaction:
    def __init__(self):
        self.ts = rpm.TransactionSet()
        self._methods = [
            "dbMatch",
            "check",
            "order",
            "addErase",
            "addInstall",
            "run",
            "IDTXload",
            "IDTXglob",
            "rollback",
            "pgpImportPubkey",
            "pgpPrtPkts",
            "Debug",
            "setFlags",
            "setVSFlags",
            "setProbFilter",
            "hdrFromFdno",
        ]
        self.tsflags = []

    def __getattr__(self, attr):
        if attr in self._methods:
            return self.getMethod(attr)
        else:
            raise AttributeError(attr)

    # pylint: disable-next=invalid-name
    def getMethod(self, method):
        # in theory, we can override this with
        # profile/etc info
        return getattr(self.ts, method)

    # push/pop methods so we dont lose the previous
    # set value, and we can potentiall debug a bit
    # easier
    # pylint: disable-next=invalid-name
    def pushVSFlags(self, flags):
        self.tsflags.append(flags)
        self.ts.setVSFlags(self.tsflags[-1])

    # pylint: disable-next=invalid-name
    def popVSFlags(self):
        del self.tsflags[-1]
        self.ts.setVSFlags(self.tsflags[-1])


# pylint: disable-next=invalid-name
def initReadOnlyTransaction():
    global read_ts
    # pylint: disable-next=singleton-comparison
    if read_ts == None:
        read_ts = Up2dateTransaction()
        # FIXME: replace with macro defination
        read_ts.pushVSFlags(-1)
    return read_ts
