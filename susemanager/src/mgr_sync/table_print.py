# -*- coding: utf-8 -*-
#
# Copyright (c) 2014 SUSE
#
# This software is licensed to you under the GNU General Public License,
# version 2 (GPLv2). There is NO WARRANTY for this software, express or
# implied, including the implied warranties of MERCHANTABILITY or FITNESS
# FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
# along with this software; if not, see
# http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
# SUSE trademarks are not licensed under GPLv2. No permission is
# granted to use or replicate SUSE trademarks that are incorporated
# in this software or its documentation.


# This class has been taken as is from the SMDBA.
# Needs to be refactored!

class TablePrint:
    """
    Print table on the CLI.
    """

    def __init__(self, table):
        """
        Table is [(1,2,3,), (4,5,6,),] etc data.
        """
        self.table = table
        self.widths = []

    def _check(self):
        """
        Check if table is consistent grid.
        Header is a leader here.
        """
        if not len(self.table):
            raise Exception("Table is empty!")

        header = None
        for row in self.table:
            if header is None:
                header = len(row)
                continue
            if len(row) != header:
                raise Exception("Table has different row widths.")

    def _get_widths(self):
        """
        Find extra-widths by max width of any value.
        """

        self.widths = [0 for x in self.table[0]]
        for row in self.table:
            for idx in range(len(row)):
                cell_len = len(str(row[idx]))
                if cell_len > self.widths[idx]:
                    self.widths[idx] = cell_len

    def _format(self):
        """
        Format the output.
        """
        out = []
        ftable = []
        for row in self.table:
            frow = []
            for idx in range(len(row)):
                frow.append(str(row[idx]) + (" " * (self.widths[idx] - len(str(row[idx])))))
            ftable.append(frow)

        for idx in range(len(ftable)):
            out.append(' | '.join(ftable[idx]))
            if idx == 0:
                out.append('-+-'.join(["-" * len(item) for item in ftable[idx]]))

        return '\n'.join(out)

    def __str__(self):
        self._check()
        self._get_widths()
        return self._format()

