#  pylint: disable=missing-module-docstring
#
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
        # pylint: disable-next=invalid-name
        endTag=" - done",
        # pylint: disable-next=invalid-name
        finalSize=100.0,
        # pylint: disable-next=invalid-name
        finalBarLength=10,
        # pylint: disable-next=invalid-name
        barChar="#",
        stream=sys.stdout,
        # pylint: disable-next=invalid-name
        redrawYN=1,
    ):
        # disabling redrawing of the hash marks. Too many people are
        # complaining.
        redrawYN = 0

        self.size = 0.0
        # pylint: disable-next=invalid-name
        self.barLength = 0
        # pylint: disable-next=invalid-name
        self.barLengthPrinted = 0
        self.prompt = prompt
        # pylint: disable-next=invalid-name
        self.endTag = endTag
        # pylint: disable-next=invalid-name
        self.finalSize = float(finalSize)
        # pylint: disable-next=invalid-name
        self.finalBarLength = int(finalBarLength)
        # pylint: disable-next=invalid-name
        self.barChar = barChar
        self.stream = stream
        # pylint: disable-next=invalid-name
        self.redrawYN = redrawYN
        if self.stream not in [sys.stdout, sys.stderr]:
            self.redrawYN = 0

    def reinit(self):
        self.size = 0.0
        self.barLength = 0
        self.barLengthPrinted = 0

    # pylint: disable-next=invalid-name
    def printAll(self, contextYN=0):
        """Prints/reprints the prompt and current level of hashmarks.
        Eg:             ____________________
            Processing: ###########
        NOTE: The underscores only occur if you turn on contextYN.
        """
        if contextYN:
            self.stream.write(
                # pylint: disable-next=consider-using-f-string
                "%s%s\n"
                % (" " * len(self.prompt), "_" * self.finalBarLength)
            )
        # pylint: disable-next=invalid-name
        toPrint = self.prompt + self.barChar * self.barLength
        if self.redrawYN:
            # self.stream.write('\b'*len(toPrint))
            # backup
            self.stream.write("\b" * 80)  # nuke whole line (80 good 'nuf?)
            # pylint: disable-next=invalid-name
            completeBar = len(self.prompt + self.endTag) + self.finalBarLength
            # erase
            self.stream.write(completeBar * " ")
            # backup again
            self.stream.write(completeBar * "\b")
        self.stream.write(toPrint)
        self.stream.flush()
        self.barLengthPrinted = self.barLength

    # pylint: disable-next=invalid-name
    def printIncrement(self):
        "visually updates the bar."
        if self.redrawYN:
            self.printAll(contextYN=0)
        else:
            self.stream.write(self.barChar * (self.barLength - self.barLengthPrinted))
        self.stream.flush()
        self.barLengthPrinted = self.barLength

    # pylint: disable-next=invalid-name
    def printComplete(self):
        """Completes the bar reguardless of current object status (and then
        updates the object's status to complete)."""
        self.complete()
        self.printIncrement()
        self.stream.write(self.endTag + "\n")
        self.stream.flush()

    # pylint: disable-next=invalid-name
    def update(self, newSize):
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

    # pylint: disable-next=invalid-name
    def addTo(self, additionalSize):
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
