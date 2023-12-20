# pylint: disable=missing-module-docstring
# Copyright (c) 2008--2016 Red Hat, Inc.
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

import sys
import time


class ProgressBar:

    """A simplete progress bar class. See example in main below."""

    def __init__(
        self,
        prompt="working: ",
        endTag=" - done",  #  pylint: disable=invalid-name
        finalSize=100.0,  #  pylint: disable=invalid-name
        finalBarLength=10,  #  pylint: disable=invalid-name
        barChar="#",  #  pylint: disable=invalid-name
        stream=sys.stdout,
        redrawYN=1,  #  pylint: disable=invalid-name
    ):
        # disabling redrawing of the hash marks. Too many people are
        # complaining.
        redrawYN = 0

        self.size = 0.0
        self.barLength = 0  #  pylint: disable=invalid-name
        self.barLengthPrinted = 0  #  pylint: disable=invalid-name
        self.prompt = prompt
        self.endTag = endTag  #  pylint: disable=invalid-name
        self.finalSize = float(finalSize)  #  pylint: disable=invalid-name
        self.finalBarLength = int(finalBarLength)  #  pylint: disable=invalid-name
        self.barChar = barChar  #  pylint: disable=invalid-name
        self.stream = stream
        self.redrawYN = redrawYN  #  pylint: disable=invalid-name
        if self.stream not in [sys.stdout, sys.stderr]:
            self.redrawYN = 0

    def reinit(self):
        self.size = 0.0
        self.barLength = 0
        self.barLengthPrinted = 0

    def printAll(self, contextYN=0):  #  pylint: disable=invalid-name,invalid-name
        """Prints/reprints the prompt and current level of hashmarks.
        Eg:             ____________________
            Processing: ###########
        NOTE: The underscores only occur if you turn on contextYN.
        """
        if contextYN:
            self.stream.write(
                "%s%s\n" % (" " * len(self.prompt), "_" * self.finalBarLength)  #  pylint: disable=consider-using-f-string
            )
        toPrint = self.prompt + self.barChar * self.barLength  #  pylint: disable=invalid-name
        if self.redrawYN:
            # self.stream.write('\b'*len(toPrint))
            # backup
            self.stream.write("\b" * 80)  # nuke whole line (80 good 'nuf?)
            completeBar = len(self.prompt + self.endTag) + self.finalBarLength  #  pylint: disable=invalid-name
            # erase
            self.stream.write(completeBar * " ")
            # backup again
            self.stream.write(completeBar * "\b")
        self.stream.write(toPrint)
        self.stream.flush()
        self.barLengthPrinted = self.barLength

    def printIncrement(self):  #  pylint: disable=invalid-name
        "visually updates the bar."
        if self.redrawYN:
            self.printAll(contextYN=0)
        else:
            self.stream.write(self.barChar * (self.barLength - self.barLengthPrinted))
        self.stream.flush()
        self.barLengthPrinted = self.barLength

    def printComplete(self):  #  pylint: disable=invalid-name
        """Completes the bar reguardless of current object status (and then
        updates the object's status to complete)."""
        self.complete()
        self.printIncrement()
        self.stream.write(self.endTag + "\n")
        self.stream.flush()

    def update(self, newSize):  #  pylint: disable=invalid-name
        "Update the status of the class to the newSize of the bar."
        newSize = float(newSize)
        if newSize >= self.finalSize:
            newSize = self.finalSize
        self.size = newSize
        if self.finalSize == 0:
            self.barLength = self.finalBarLength
        else:
            self.barLength = int((self.size * self.finalBarLength) / self.finalSize)
            if self.barLength >= self.finalBarLength:
                self.barLength = self.finalBarLength

    def addTo(self, additionalSize):  #  pylint: disable=invalid-name,invalid-name
        "Update the object's status to an additional bar size."
        self.update(self.size + additionalSize)

    def complete(self):
        self.update(self.finalSize)


# ------------------------------------------------------------------------------

if __name__ == "__main__":
    print("An example:")
    bar_length = 40
    items = 200
    pb = ProgressBar("standby: ", " - all done!", items, bar_length, "o")
    pb.printAll(1)
    for i in range(items):
        # pb.update(i)
        pb.addTo(1)
        time.sleep(0.005)
        pb.printIncrement()
    pb.printComplete()

# ------------------------------------------------------------------------------
