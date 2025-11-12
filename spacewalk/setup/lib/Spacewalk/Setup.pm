package Spacewalk::Setup;
require Exporter;

use warnings;
use strict;

use English;

use Exporter 'import';
use vars '@EXPORT_OK';
@EXPORT_OK = qw(loc system_debug system_or_exit);

use Getopt::Long qw(GetOptions);
use Symbol qw(gensym);
use IPC::Open3 qw(open3);
use Pod::Usage qw(pod2usage);
use POSIX ":sys_wait_h";
use Fcntl qw(F_GETFD F_SETFD FD_CLOEXEC);
use Socket;

eval {
    require Net::LibIDN2;
    Net::LibIDN2->import();
    sub idn_to_ascii($;$$) { return Net::LibIDN2::idn2_lookup_u8( shift ) }
    1;
} or do {
    my $error = $@;
    require Net::LibIDN;
    Net::LibIDN->import( qw(idn_to_ascii) );
};

use Params::Validate qw(validate);
Params::Validate::validation_options(strip_leading => "-");

=head1 NAME

Spacewalk::Setup, spacewalk-setup

=head1 VERSION

Version 1.1

=cut

our $VERSION = '1.1';

use constant SHARED_DIR => "/usr/share/spacewalk/setup";

use constant DEFAULT_RHN_CONF_LOCATION =>
  '/etc/rhn/rhn.conf';

use constant DEFAULT_PROXY_CONF_LOCATION =>
  '/etc/sysconfig/proxy';

use constant DEFAULT_PROXYAUTH_CONF_LOCATION =>
  '/root/.curlrc';

use constant DEFAULT_SATCON_DICT =>
  '/var/lib/rhn/rhn-satellite-prep/satellite-local-rules.conf';

use constant DEFAULT_RHN_SATCON_TREE =>
  '/var/lib/rhn/rhn-satellite-prep/etc';

use constant DEFAULT_BACKUP_DIR =>
   '/etc/sysconfig/rhn/backup-' . `date +%F-%R`;

use constant INSTALL_LOG_FILE =>
  '/var/log/rhn/rhn_installation.log';

use constant DB_INSTALL_LOG_FILE =>
  '/var/log/rhn/install_db.log';

use constant PG_POP_LOG_SIZE => 156503;

use constant RHN_LOG_DIR =>
  '/var/log/rhn';

use constant DB_INSTALL_LOG_SIZE => 11416;

our $DEFAULT_DOC_ROOT = "/var/www/html";
our $SUSE_DOC_ROOT = "/usr/share/susemanager/www/htdocs";

use constant DEFAULT_SUSEMANAGER_CONF =>
  '/usr/share/rhn/config-defaults/rhn_server_susemanager.conf';

use constant DEFAULT_SCC_URL =>
  'https://scc.suse.com';

use constant SCC_CREDENTIAL_FILE =>
  '/etc/zypp/credentials.d/SCCcredentials';

my $DEBUG;
$DEBUG = 0;

my $DRY_RUN;
$DRY_RUN = 0;

sub parse_options {
  my @valid_opts = (
            "help",
            "skip-initial-configuration",
            "skip-fqdn-test",
            "skip-ssl-vhost-setup",
            "skip-services-check",
            "clear-db",
            "answer-file=s",
            "db-only",
            "scc",
            "disconnected"
                   );

  my $usage = loc("usage: %s %s\n",
                  $0,
                  "[ --help ] [ --answer-file=<filename> ] [ --skip-initial-configuration ] [ --skip-fqdn-test ] [--skip-ssl-vhost-setup] [ --skip-services-check ] [ --clear-db ] [--scc] [--disconnected]" );

  # Terminate if any errors were encountered parsing the command line args:
  my %opts;
  if (not GetOptions(\%opts, @valid_opts)) {
    die("\n");
  }

  if ($opts{help}) {
    ( my $module = __PACKAGE__ ) =~ s!::!/!g;
    pod2usage(-exitstatus => 0, -verbose => 1, -message => $usage, -input => $INC{$module . '.pm'});
  }

  return %opts;
}

# This function is a simple wrapper around sprintf, which I'm using as
# a placeholder until or unless real I18N support is required.  Doing
# it this way should make it easier to identify which strings need
# localization, and help me avoid lazily catting strings together.
sub loc {
  my $string = shift;
  return sprintf($string, @_);
}

sub read_config {
  my $config_file = shift;
  my $options = shift;
  local * CONFIG;
  open(CONFIG, '<', $config_file) or die "Could not open $config_file: $!";

  while (my $line = <CONFIG>) {
    if ($line =~ /^#/ or $line =~ /\[comment\]/ or $line =~ /^\s*$/) {
      next;
    } else {
      chomp($line);
      (my $key, my $value) = split (/=/, $line);
      $key =~ s/^\s*//msg;
      $key =~ s/\s*$//msg;
      $value =~ s/^\s*//msg;
      $value =~ s/\s*$//msg;
      $options->{$key} = $value;
      if ($DEBUG) {
        print("read $key = $value from $config_file\n");
      }
    }
  }
  return;
}

sub write_config {
  my $options = shift;
  my $target = shift;

  my @opt_strings = map { "--option=${_}=" . $options->{$_} } grep { defined $options->{$_} } keys %{$options};

  Spacewalk::Setup::system_or_exit([ "/usr/bin/rhn-config-satellite.pl",
                   "--target=$target",
                   @opt_strings,
                 ],
                 29,
                 'There was a problem setting initial configuration.');

  return 1;
}

sub load_answer_file {
  my $options = shift;
  my $answers = shift;
  my (@skip) = @{(shift)};

  my @files = ();
  push @files, $options->{'answer-file'} if $options->{'answer-file'};

  for my $file (@files) {

    next unless (-r $file or $file eq $options->{'answer-file'});

    if ($options->{'answer-file'} and $file eq $options->{'answer-file'}) {
      print loc("* Loading answer file: %s.\n", $file);
    }
    local * FH;
    open FH, '<', $file or die loc("Could not open answer file: %s\n", $!);

    while (my $line = <FH>) {
      next if substr($line, 0, 1) eq '#';
      $line =~ /([\w\.-]*)\s*=\s*(.*)/;
      my ($key, $value) = ($1, $2);

      next unless $key;

      $answers->{$key} = $value;
    }

    close FH;
  }
  if ($answers->{'db-host'}) {
    $answers->{'db-host'} = idn_to_ascii($answers->{'db-host'}, "utf8");
  }
  return;
}

sub system_debug {
  my @args = @_;

  my $logfile = INSTALL_LOG_FILE;

  if ($DEBUG) {
    print "Command: '" . join(' ', @args) . "'\n";
  }
  if ($DRY_RUN) {
    return 0;
  }
  else {
    local $SIG{'ALRM'};
    if (@args == 1) {
      die "Single parameter system_debug [@args] not supported.\n";
    } else {
      local *LOGFILE;
      open(LOGFILE, ">>", $logfile) or do {
          print "Error writing log file '$logfile': $!\n";
          print STDERR "Error writing log file '$logfile': $!\n";
          return 1;
      };
      my $orig_stdout = select LOGFILE;
      $| = 1;
      select $orig_stdout;
      local *PROCESS_OUT;
      set_spinning_callback();
      my $pid = open3(gensym, \*PROCESS_OUT, \*PROCESS_OUT, @args);
      my ($vecin, $vecout) = ('', '');
      vec($vecin, fileno(PROCESS_OUT), 1) = 1;
      my $ret;
      # Some programs that daemonize themselves do not close their stdout,
      # so doing just while (<PROCESS_OUT>) would block forever. That's why
      # we try to select'n'sysread, to have a chance to see if the child
      # is ready to be reaped, even if we did not get eof.
      while (1) {
        if (select($vecout=$vecin, undef, undef, 10) > 0) {
          my $buffer;
          if (sysread(PROCESS_OUT, $buffer, 4096) > 0) {
            print LOGFILE $buffer;
            redo;
          }
        }
        my $pidout = waitpid($pid, WNOHANG);
        if ($pidout < 0) {
          print LOGFILE "We've lost the child [@args] pid [$pid]\n";
          $ret = -1;
          last;
        }
        if ($pidout) {
          $ret = $?;
          last;
        }
      }
      close PROCESS_OUT;
      close LOGFILE;
      alarm 0;
      return $ret;
    }
  }
}

sub system_or_exit {
  my $command = shift;
  my $exit_code = shift;
  my $error = shift;
  my @args = @_;

  my $ret = system_debug(@{$command});

  if ($ret) {
    my $exit_value = $? >> 8;

    print loc($error . "  Exit value: %d.\n", (@args, $exit_value));
    print "Please examine @{[ INSTALL_LOG_FILE ]} for more information.\n";

    exit $exit_code;
  }

  return 1;
}

my $spinning_callback_count;
my @spinning_pattern = split /\n/, <<EOF;
 (°-  ·  ·  ·  ·  ·
 (°<  ·  ·  ·  ·  ·
    (°-  ·  ·  ·  ·
    (°<  ·  ·  ·  ·
       (°-  ·  ·  ·
       (°<  ·  ·  ·
          (°-  ·  ·
          (°<  ·  ·
             (°-  ·
             (°<  ·
                (°-
EOF

my $spinning_pattern_maxlength = 0;
for (@spinning_pattern) {
        if (length > $spinning_pattern_maxlength) {
                $spinning_pattern_maxlength = length;
        }
}
sub spinning_callback {
        my $old = select STDOUT;
        $| = 1;
        my $index = ($spinning_callback_count++ % scalar(@spinning_pattern));
        print STDOUT $spinning_pattern[$index],
                (' ' x ($spinning_pattern_maxlength - length($spinning_pattern[$index]))),
                "\r";
        select $old;
        alarm 1;
}

sub set_spinning_callback {
        if (not -t STDOUT) {
                return;
        }
        $spinning_callback_count = 0;
        $SIG{'ALRM'} = \&spinning_callback;
        alarm 1;
}

sub init_log_files {
  my $product_name = shift;
  my @args = @_;

  if (not -e RHN_LOG_DIR) {
    mkdir RHN_LOG_DIR;
  }

  log_rotate(INSTALL_LOG_FILE);
  log_rotate(DB_INSTALL_LOG_FILE);

  local * FH;
  open(FH, ">", INSTALL_LOG_FILE)
    or die "Could not open '" . INSTALL_LOG_FILE .
        "': $!";

  my $log_header = "Installation log of $product_name\nCommand: "
    . $0 . " " . join(" ", @args) . "\n\n";

  print FH $log_header;

  close(FH);

  return;
}

sub log_rotate {
  my $file = shift;

  my $counter = 1;
  if (-e $file) {
    while (-e $file . '.' . $counter) {
      $counter++;
    }

    rename $file, $file . '.' . $counter;
  }

  return;
}

sub check_users_exist {
    my @required_users = @_;

    my $missing_a_user;

    foreach my $user (@required_users) {
        if (not getpwnam($user)) {
            print loc("The user '%s' should exist.\n", $user);
            $missing_a_user = 1;
        }

    }

    if ($missing_a_user) {
        exit 7;
    }
}

sub check_groups_exist {
    my @required_groups = @_;

    my $missing_a_group;

    foreach my $group (@required_groups) {
        if (not getgrnam($group)) {
            print loc("The group '%s' should exist.\n", $group);
            $missing_a_group = 1;
        }
    }

    if ($missing_a_group) {
        exit 8;
    }
}

sub clear_db {
    my $answers = shift;

    my $dbh = get_dbh($answers);

    print loc("** Database: Shutting down spacewalk services that may be using DB.\n");

    system_debug('/usr/sbin/spacewalk-service', 'stop');

    print loc("** Database: Services stopped.  Clearing DB.\n");

    my $select_sth = $dbh->prepare(<<EOQ);
  SELECT 'drop ' || UO.object_type ||' '|| UO.object_name AS DROP_STMT
    FROM user_objects UO
   WHERE UO.object_type NOT IN ('TABLE', 'INDEX', 'TRIGGER', 'LOB')
UNION
  SELECT 'drop ' || UO.object_type ||' '|| UO.object_name
         || ' cascade constraints' AS DROP_STMT
    FROM user_objects UO
   WHERE UO.object_type = 'TABLE'
     AND UO.object_name NOT LIKE '%$%'
EOQ

    $select_sth->execute();

    while (my ($drop_stmt) = $select_sth->fetchrow()) {
        my $drop_sth = $dbh->prepare($drop_stmt);
        $drop_sth->execute();
    }

    if ($DRY_RUN) {
        $dbh->rollback();
    }
    else {
        $dbh->commit();
    }

    $dbh->disconnect();

    return;
}

# TODO: Still duplicated in install.pl, didn't move out to module as nicely
# as other routines on account of usage of $opts:
sub ask {
    my %params = validate(@_, {
            question => 1,
            test => 0,
            answer => 1,
            default => 0,
        });

    if (${$params{answer}} and not $params{default}) {
        $params{default} = ${$params{answer}};
    }

    if (not defined ${$params{answer}} or not answered($params{test}, ${$params{answer}})) {
        if (defined ${$params{answer}}) {
            die "The answer '" . ${$params{answer}} . "' provided for '" . $params{question} . "' is invalid.\n";
        }
        else {
            die "No answer provided for '" . $params{question} . "'\n";
        }
    }

    ${$params{answer}} ||= $params{default} || '';

    return;
}

sub answered {
    my $test = shift;
    my $answer = shift;

    my $testsub;
    if (ref $test eq 'CODE') {
        $testsub = $test;
    }
    else {
        $testsub = sub {
            my $param = shift;
            if ($param =~ $test) {
                return 1
            }
            else {
                print loc("'%s' is not a valid response\n", $param);
                return 0
            }
        };
    }

    return $testsub->($answer);
}

sub get_nls_database_parameters {
    my $answers = shift;
    my $dbh = get_dbh($answers);

    my $sth = $dbh->prepare(<<EOQ);
SELECT NDP.parameter, NDP.value
  FROM nls_database_parameters NDP
EOQ

    $sth->execute();
    my %nls_database_parameters;

    while (my ($param, $value) = $sth->fetchrow()) {
        $nls_database_parameters{$param} = $value;
    }

    $sth->finish();
    $dbh->disconnect();

    return %nls_database_parameters;
}

sub postgresql_get_database_answers {
    my $opts = shift;
    my $answers = shift;

    my %config = ();
    read_config(DEFAULT_RHN_CONF_LOCATION, \%config);

    ask(
        -question => "Hostname (leave empty for local)",
        -test => sub { 1 },
        -answer => \$answers->{'db-host'});

    if ($answers->{'db-host'} ne '') {
        $answers->{'db-host'} = idn_to_ascii($answers->{'db-host'}, "utf8");
        ask(
            -question => "Port",
            -test => qr/\d+/,
            -default => 5432,
            -answer => \$answers->{'db-port'});
    } else {
            $answers->{'db-port'} = '';
    }

    ask(
        -question => "Database",
        -test => qr/\S+/,
        -default => $config{'db_name'},
        -answer => \$answers->{'db-name'});

    ask(
        -question => "Username",
        -test => qr/\S+/,
        -default => $config{'db_user'},
        -answer => \$answers->{'db-user'});

    ask(
        -question => "Password",
        -test => qr/\S+/,
        -default => $config{'db_password'},
        -answer => \$answers->{'db-password'});

    return;
}


############################
# PostgreSQL Specific Code #
############################

sub get_dbh {
        my $answers = shift;
        my $reportdb = shift || 0;

        my $dbh_attributes = {
                RaiseError => 1,
                PrintError => 0,
                Taint => 0,
                AutoCommit => 0,
        };

        my $dsn = "dbi:Pg:dbname=";
        $dsn .= $reportdb ? $answers->{'report-db-name'} : $answers->{'db-name'};
        my $dbhost = $reportdb ? $answers->{'report-db-host'} : $answers->{'db-host'};
        my $dbport = $reportdb ? $answers->{'report-db-port'} : $answers->{'db-port'};
        if ($dbhost ne '' && $dbhost ne 'localhost') {
                $dsn .= ";host=$dbhost";
                if ($dbport ne '') {
                        $dsn .= ";port=$dbport";
                }
        }
        my $dbh = DBI->connect($dsn,
                $reportdb ? $answers->{'report-db-user'} : $answers->{'db-user'},
                $reportdb ? $answers->{'report-db-password'} : $answers->{'db-password'},
                $dbh_attributes);

        return $dbh;
}

sub generate_satcon_dict {
        my %params = validate(@_, { conf_file => { default => DEFAULT_SATCON_DICT },
                tree => { default => DEFAULT_RHN_SATCON_TREE },});

        system_or_exit([ "/usr/bin/satcon-build-dictionary.pl",
                "--tree=" . $params{tree},
                "--target=" . $params{conf_file} ],
                28,
                'There was a problem building the satcon dictionary.');

        return 1;
}

sub satcon_deploy {
        my %params = validate(@_, { conf_file => { default => DEFAULT_SATCON_DICT },
                                tree => { default => DEFAULT_RHN_SATCON_TREE },
                                dest => { default => '/etc' },
                                backup => { default => DEFAULT_BACKUP_DIR },
                                });

        $params{backup} =~ s/\s+$//;
        my @opts = ("--source=" . $params{tree}, "--dest=" . $params{dest},
                "--conf=" . $params{conf_file}, "--backupdir=" . $params{backup});

        system_or_exit([ "/usr/bin/satcon-deploy-tree.pl", @opts ],     30,
                'There was a problem deploying the satellite configuration.');

        return 1;
}

sub backup_file {
    my $dir = shift;
    my $file = shift;
    my $backup_suffix = shift || '-swsave';

    system("cp", "--backup=numbered", "$dir/$file", "$dir/$file$backup_suffix");

    if ( $? >> 8 ) {
        die loc("An error ocurred while attempting to back up your original $file\n");
    } else {
        print loc("** $dir/$file has been backed up to $file$backup_suffix\n");
    }
}

# Write subset of $answers to /etc/rhn/rhn.conf.
# Config written here is used only for database population
# and config will be later on replaced by one generated from templates.
sub write_rhn_conf {
        my $answers = shift;
        my %config = ();

        for my $n (@_) {
                if (defined $answers->{$n}) {
                        my $name = $n;
                        $name =~ s!-!_!g;
                        $config{$name} = $answers->{$n};
                }
        }

        write_config(\%config, DEFAULT_RHN_CONF_LOCATION);
        write_config(\%config, Spacewalk::Setup::DEFAULT_SATCON_DICT);
}

=head1 DESCRIPTION

Spacewalk::Setup is a module which provides the guts of the
spacewalk-setup program. In will run the necessary steps to configure
the Spacewalk server.

=head1 OPTIONS

=over 8

=item B<--help>

Print this help message.

=item B<--answer-file=<filename>>

Indicates the location of an answer file to be use for answering
questions asked during the installation process.
See answers.txt for an example.

# todo @

=item B<--clear-db>

Clear any pre-existing database schema before installing.
This will destroy any data in the Satellite database and re-create
empty Satellite schema.

=item B<--skip-fqdn-test>

Do not verify that the system has a valid hostname.  Red Hat Satellite
requires that the hostname be properly set during installation.
Using this option may result in a Satellite server that is not fully
functional.

=item B<--skip-ssl-vhost-setup>

Do not configure the default SSL virtual host for Spacewalk.

Note that if you choose to have Spacewalk setup skip this step,
it's up to you to ensure that the following are included
in the virtual host definition:

RewriteEngine on
RewriteOptions inherit
SSLProxyEngine on

=item B<--skip-services-check>

Proceed with upgrade if services are already stopped.

=back

=head1 SEE ALSO

See documentation at L<https://github.com/spacewalkproject/spacewalk/> for more
details and the Spacewalk server, its configuration and use..

=head1 AUTHOR

Devan Goodwin, C<< <dgoodwin at redhat.com> >>

=head1 BUGS

Please report any bugs using or feature requests using
L<https://bugzilla.redhat.com/enter_bug.cgi?product=Spacewalk>.

=head1 COPYRIGHT & LICENSE

Copyright (c) 2008--2017 Red Hat, Inc.

This software is licensed to you under the GNU General Public License,
version 2 (GPLv2). There is NO WARRANTY for this software, express or
implied, including the implied warranties of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
along with this software; if not, see
http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.

SPDX-License-Identifier: GPL-2.0-only

Red Hat trademarks are not licensed under GPLv2. No permission is
granted to use or replicate Red Hat trademarks that are incorporated
in this software or its documentation.

=cut

1; # End of Spacewalk::Setup
