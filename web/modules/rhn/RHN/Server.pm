#
# Copyright (c) 2008--2014 Red Hat, Inc.
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

# Server - Object
use strict;

package RHN::Server;

use PXT::HTML ();
use PXT::Utils ();
use RHN::Action ();

use RHN::DB::Server;
our @ISA = qw/RHN::DB::Server/;

sub lookup {
  my $class = shift;
  my $first_arg = $_[0];

  die "No argument to $class->lookup"
    unless $first_arg;

  if (substr($first_arg,0,1) eq '-') {
    return $class->SUPER::lookup(@_);
  }
  else {
    warn "deprecated use of unparameterized $class->lookup from (" . join(', ', caller) . ")\n";
    return $class->SUPER::lookup(-id => $first_arg);
  }
}

1;

