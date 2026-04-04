#!/usr/bin/perl
#
# Copyright (c) 2008--2015 Red Hat, Inc.
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
#

use strict;
use warnings;
use Time::Piece;
use Time::HiRes;


use Getopt::Long;
use English;

my $usage = "usage: $0 --target=<target_file> --option=<key,value> "
  . "[ --option=<key,value> ] [ --remove=<key>] [ --help ]\n";

my $target = '';
my @options = ();
my @removals = ();
my $help = '';
# bsc#1190040
my @allowed_target_files = qw(/etc/rhn/rhn.conf /var/lib/rhn/rhn-satellite-prep/satellite-local-rules.conf /var/lib/rhn/rhn-satellite-prep/etc/rhn/rhn.conf);

GetOptions("target=s" => \$target, "option=s" => \@options, "remove=s" => \@removals, "help" => \$help) or die $usage;

if ($help) {
  die $usage;
}

unless ($target and (@options || @removals)) {
  die $usage;
}

my %options = map { split(/=/,$_, 2) } @options;

my $tmpfile = $target . ".bak.${PID}";

if (! grep { $_ eq $target} @allowed_target_files) {
  die("Cannot modify a file that is not a spacewalk config file: " . $target);
}

my ($seconds,$microseconds) = Time::HiRes::gettimeofday;
my $current_time = sprintf '%s.%06d', gmtime($seconds)->strftime('%Y-%m-%d_%H:%M:%S'), $microseconds;

if (-e $target) {
  link($target, $target . "." . $current_time) or die "Could not rename $target to ${target}.${current_time}: $OS_ERROR";
  open(TARGET, "< $target") or die "Could not open $target: $OS_ERROR";
}

unlink $tmpfile if -e $tmpfile;
umask 0027;
open(TMP, "> $tmpfile") or die "Could not open $tmpfile for writing: $OS_ERROR";
if ($tmpfile =~ m!^/etc/rhn/!) {
  # Chown for different potential apache group names (SUSE/RHEL)
  my $apache_group = getgrnam(`grep -hsoP "(?<=Group ).*" /etc/httpd/conf/*.conf /etc/apache2/*.conf | tr -d '\n'`);
  chown 0, $apache_group, $tmpfile;
}

foreach my $opt_name (keys %options) {
  my $val = $options{$opt_name};
  if ($val =~ /^[A-Z0-9_]+$/) {
    my $envkey = "UYUNICFG_" . $val;
    if (exists $ENV{$envkey} && defined $ENV{$envkey}) {
      $options{$opt_name} = $ENV{$envkey};
    }
  }
}

while (my $line = <TARGET>) {
  my $removed = 0;
  if ($line =~ /\[prompt\]/ or $line =~ /^#/) {
    print TMP $line;
    next;
  }

  foreach my $opt_name (keys %options) {
    if ($line =~ /^(\S*)\Q$opt_name\E( *)=( *)/) {
      my $prefix = defined $1 ? $1 : '';
      my $s1 = $2 || '';
      my $s2 = $3 || '';
      chomp($options{$opt_name});
      $line = "${prefix}${opt_name}${s1}=${s2}" . $options{$opt_name} . "\n";
      delete $options{$opt_name};
    }
  }

  foreach (@removals) {
    if ($line =~ /^(\S*)\Q$_\E( *)=( *)/) {
      $removed = 1;
      delete $options{$_};
    }
  }

  if (!$removed) {
    print TMP $line;
  }
}

# For the options that didn't exist in the file
# we need to append these to the end.
foreach my $opt_name (keys %options) {
    print $opt_name . "\n";
    chomp($options{$opt_name});
    my $line = "$opt_name=$options{$opt_name}\n\n";
    print TMP "#option generated from rhn-config-satellite.pl\n";
    print TMP $line;

}

close(TMP);
close(TARGET);

rename($tmpfile, $target) or die "Could not rename $tmpfile to $target: $OS_ERROR";;

exit 0;

=pod

=head1 NAME

rhn-config-satellite.pl - generate config files for Spacewalk server

=head1 SYNOPSIS

B<rhn-config-satellite.pl> --target=<target_file> --option=<key,value> [ --option=<key,value> ...] [ --help ]

=head1 DESCRIPTION

This script will make sure that in F<target_file> are present configuration variables in format C<key=value>. If such key already exist there, it is removed and new variables are put at the end of the F<target_file>. Original file is preserved as F<target_file.CURRENT_TIME>

This script is used internally by B<spacewalk-setup>(1)
=head1 OPTIONS

=over 5

=item B<--option=<key,value>>

Specify configuration variable and its value. It will be written to config file as C<key=value>.

=item B<--help>

Write out short help.

=back

=head1 SEE ALSO

B<spacewalk-setup>(1)

=cut
