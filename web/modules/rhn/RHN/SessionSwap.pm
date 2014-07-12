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

package RHN::SessionSwap;
use strict;

use Digest::MD5;
use Digest::HMAC_SHA1 qw/hmac_sha1_hex/;

use PXT::Config;

# this is basically a copy of RHN::Session's key stuff
sub rhn_hmac_data {
  my $class = shift;
  my $hmac_data = join("\0", @_);

  my $hmac_key = join("",
		      PXT::Config->get('session_swap_secret_4'),
		      PXT::Config->get('session_swap_secret_3'),
		      PXT::Config->get('session_swap_secret_2'),
		      PXT::Config->get('session_swap_secret_1')
		  );
  return hmac_sha1_hex($hmac_data, $hmac_key);
}

1;
