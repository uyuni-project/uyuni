#
# Copyright (c) 2008--2010 Red Hat, Inc.
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

use strict;

package Sniglets::Sets;

use Carp;

sub register_tags {
  my $class = shift;
  my $pxt = shift;

  $pxt->register_tag('rhn-set-totals' => \&set_totals);
}

sub set_totals {
  my $pxt = shift;
  my %params = @_;

  my $block = $params{__block__};
  my @set_stats = $pxt->user->selection_details;
  my %sets = map {$_->[0] => $_->[1]} @set_stats;

  if ($params{set}) {
    if ($params{noun}) {

      return sprintf(qq{<span id="spacewalk-set-%s-counter" class="badge">%s</span>%s%s selected},
         $params{set},
                     $sets{$params{set}} || "0",
                     $params{noun},
                     ((not exists $sets{$params{set}}) || $sets{$params{set}} > 1) ? "s" : "");
    }
    else {
      return sprintf(qq{<span id="spacewalk-set-%s-counter" class="badge">%s</span>},
         $params{set},
         $sets{$params{set}} || "0");
    }
  }

  foreach my $set (qw/system_list server_group_list user_group_list user_list package_upgradable_list package_installable_list package_removable_list errata_list channel_list/) {

    $block =~ s/\{${set}_set_count\}/$sets{$set} ? $sets{$set} : '0'/eg;
  }

  return $block;
}

1;
