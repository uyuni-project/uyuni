package Spacewalk::Setup;
require Exporter;

use warnings;
use strict;

use English;

use Exporter 'import';
use vars '@EXPORT_OK';
@EXPORT_OK = qw(loc system_debug system_or_exit postgresql_clear_db);

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

use constant POSTGRESQL_SCHEMA_FILE => File::Spec->catfile("/usr", "share",
    'susemanager', 'db', 'postgres', 'main.sql');

use constant POSTGRESQL_DEPLOY_FILE => File::Spec->catfile("/usr", "share",
    'susemanager', 'db', 'postgres', 'deploy.sql');

use constant DEFAULT_ANSWER_FILE_GLOB =>
  SHARED_DIR . '/defaults.d/*.conf';

use constant DEFAULT_RHN_CONF_LOCATION =>
  '/etc/rhn/rhn.conf';

use constant DEFAULT_PROXY_CONF_LOCATION =>
  '/etc/sysconfig/proxy';

use constant DEFAULT_PROXYAUTH_CONF_LOCATION =>
  '/root/.curlrc';

use constant DEFAULT_UP2DATE_LOCATION =>
  '/etc/sysconfig/rhn/up2date';

use constant DEFAULT_RHN_ETC_DIR =>
  '/etc/sysconfig/rhn';

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

use constant DB_POP_LOG_FILE =>
  '/var/log/rhn/populate_db.log';

use constant PG_POP_LOG_SIZE => 156503;
use constant ORA_POP_LOG_SIZE => 132243;

use constant RHN_LOG_DIR =>
  '/var/log/rhn';

use constant DB_UPGRADE_LOG_FILE =>
  '/var/log/rhn/upgrade_db.log';

use constant DB_UPGRADE_LOG_SIZE => 22000000;

use constant DB_INSTALL_LOG_SIZE => 11416;

use constant DB_MIGRATION_LOG_FILE =>
  '/var/log/rhn/rhn_db_migration.log';

use constant EMBEDDED_DB_ANSWERS =>
  '/usr/share/spacewalk/setup/defaults.d/embedded-postgresql.conf';
our $DEFAULT_DOC_ROOT = "/var/www/html";
our $SUSE_DOC_ROOT = "/usr/share/susemanager/www/htdocs";

our $CA_TRUST_DIR = '/etc/pki/ca-trust/source/anchors';
our $SUSE_CA_TRUST_DIR = '/etc/pki/trust/anchors';
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
            "skip-system-version-test",
            "skip-selinux-test",
            "skip-fqdn-test",
            "skip-python-test",
            "skip-updates-install",
            "skip-db-install",
            "skip-db-diskspace-check",
            "skip-db-population",
            "skip-reportdb-setup",
            "skip-ssl-cert-generation",
            "skip-ssl-ca-generation",
            "skip-ssl-vhost-setup",
            "skip-services-check",
            "skip-services-restart",
            "skip-logfile-init",
            "clear-db",
            "re-register",
            "answer-file=s",
            "non-interactive",
            "upgrade",
            "run-updater:s",
            "run-cobbler",
            "enable-tftp:s",
            "external-postgresql",
            "external-postgresql-over-ssl",
            "db-only",
            "rhn-http-proxy:s",
            "rhn-http-proxy-username:s",
            "rhn-http-proxy-password:s",
            "managed-db",
            "scc",
            "disconnected"
                   );

  my $usage = loc("usage: %s %s\n",
                  $0,
                  "[ --help ] [ --answer-file=<filename> ] [ --non-interactive ] [ --skip-initial-configuration ] [ --skip-system-version-test ] [ --skip-selinux-test ] [ --skip-fqdn-test ] [ --skip-db-install ] [ --skip-db-diskspace-check ] [ --skip-db-population ] [--skip-reportdb-setup ] [ --skip-ssl-cert-generation ] [--skip-ssl-ca-generation] [--skip-ssl-vhost-setup] [ --skip-services-check ] [ --skip-services-restart ] [ --clear-db ] [ --re-register ] [ --upgrade ] [ --run-updater=<yes|no>] [--run-cobbler] [ --enable-tftp=<yes|no>] [ --external-postgresql [ --external-postgresql-over-ssl ] ] [--scc] [--disconnected]" );

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
  foreach my $afile (glob(DEFAULT_ANSWER_FILE_GLOB)) {
      push @files, $afile if not grep $_ eq $afile, @skip;
  }
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

# Check if we're installing with an embedded database.
sub is_embedded_db {
  my $opts = shift;
  return not (defined($opts->{'external-postgresql'})
           or defined($opts->{'managed-db'}));
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

sub upgrade_stop_services {
  my $opts = shift;
  if ($opts->{'upgrade'} && not $opts->{'skip-services-check'}) {
    print "* Upgrade flag passed.  Stopping necessary services.\n";
    if (-e "/usr/sbin/spacewalk-service") {
      system_or_exit(['/usr/sbin/spacewalk-service', 'stop'], 16,
                      'Could not stop the rhn-satellite service.');
    } else {
      # shutdown pre 3.6 services proerly
      system_or_exit(['/sbin/service', 'apache2', 'stop'], 25,
                      'Could not stop the http service.');
      system_or_exit(['/sbin/service', 'taskomatic', 'stop'], 27,
                      'Could not stop the taskomatic service.');
      if (is_embedded_db($opts)) {
        system_or_exit(['/sbin/service', 'rhn-database', 'stop'], 31,
                        'Could not stop the rhn-database service.');
      }
    }
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
  if (have_selinux()) {
    local *X; open X, '>', INSTALL_LOG_FILE and close X;
    system('/sbin/restorecon', INSTALL_LOG_FILE);
  }
  log_rotate(DB_INSTALL_LOG_FILE);
  log_rotate(DB_POP_LOG_FILE);

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
            noninteractive => 1,
            question => 1,
            test => 0,
            answer => 1,
            password => 0,
            default => 0,
            completion => 0,
        });

    if (${$params{answer}} and not $params{default}) {
        $params{default} = ${$params{answer}};
    }

    while (not defined ${$params{answer}} or
        not answered($params{test}, ${$params{answer}})) {
        if ($params{noninteractive}) {
            if (defined ${$params{answer}}) {
                die "The answer '" . ${$params{answer}} . "' provided for '" . $params{question} . "' is invalid.\n";
            }
            else {
                die "No answer provided for '" . $params{question} . "'\n";
            }
        }

        my $default_string = "";
        if ($params{default}) {
            if ($params{password}) {
                $default_string = " [******]";
            }
            else {
                $default_string = " [" . $params{default} . "]";
            }
        }

        print loc("%s%s? ",
            $params{question},
            $default_string);

        if ($params{password}) {
            my $stty_orig_val = `stty -g`;
            system('stty', '-echo');
            ${$params{answer}} = <STDIN>;
            system("stty $stty_orig_val");
            print "\n";
        }
        else {
            if ($params{completion}) {
                require Term::Completion::Path;
                my $tc = Term::Completion::Path->new();
                ${$params{answer}} = $tc->complete();
            }
            else {
                ${$params{answer}} = <STDIN>;
            }
        }

        chomp ${$params{answer}};
        ${$params{answer}} =~ s/^\s+|\s+$//g;

        ${$params{answer}} ||= $params{default} || '';
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


sub print_progress {
        my %params = validate(@_, { init_message => 1,
                log_file_name => 1,
                log_file_size => 1,
                err_message => 1,
                err_code => 1,
                system_opts => 1,
        });
        print "Running " . join(" ", @{$params{system_opts}}) . "\n";

        local *LOGFILE;
        open(LOGFILE, ">>", $params{log_file_name}) or do {
                print "Error writing log file '$params{log_file_name}': $!\n";
                print STDERR "Error writing log file '$params{log_file_name}': $!\n";
                exit $params{err_code};
        };

        $| = 1;
        my $orig_stdout = select LOGFILE;
        $| = 1;
        select $orig_stdout;
        print loc($params{init_message});
        local *PROCESS_OUT;
        my $progress_hashes_done = 0;
        my $progress_callback_length = 0;
        my $pid = open3(gensym, \*PROCESS_OUT, \*PROCESS_OUT, @{$params{system_opts}});
        while (<PROCESS_OUT>) {
                print LOGFILE $_;
                $progress_callback_length += length;
                if (-t STDOUT and $params{log_file_size}) {
                        my $target_hashes = int(60 * $progress_callback_length / $params{log_file_size});
                        if ($target_hashes > $progress_hashes_done) {
                                print "#" x ($target_hashes - $progress_hashes_done);
                                $progress_hashes_done = $target_hashes;
                        }
                }
        }
        close PROCESS_OUT;
        waitpid($pid, 0);
        my $ret = $?;
        close LOGFILE;
        print "\n";

        if ($ret) {
                print loc($params{err_message});
                exit $params{err_code};
        }
}

sub postgresql_get_database_answers {
    my $opts = shift;
    my $answers = shift;

    my %config = ();
    read_config(DEFAULT_RHN_CONF_LOCATION, \%config);

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Hostname (leave empty for local)",
        -test => sub { 1 },
        -answer => \$answers->{'db-host'});

    if ($answers->{'db-host'} ne '') {
        $answers->{'db-host'} = idn_to_ascii($answers->{'db-host'}, "utf8");
        ask(
            -noninteractive => $opts->{"non-interactive"},
            -question => "Port",
            -test => qr/\d+/,
            -default => 5432,
            -answer => \$answers->{'db-port'});
    } else {
            $answers->{'db-port'} = '';
    }

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Database",
        -test => qr/\S+/,
        -default => $config{'db_name'},
        -answer => \$answers->{'db-name'});

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Username",
        -test => qr/\S+/,
        -default => $config{'db_user'},
        -answer => \$answers->{'db-user'});

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Password",
        -test => qr/\S+/,
        -default => $config{'db_password'},
        -answer => \$answers->{'db-password'},
        -password => 1);

    if ($opts->{'external-postgresql-over-ssl'}) {
      $answers->{'db-ssl-enabled'} = '1';
      ask(
         -noninteractive => $opts->{"non-interactive"},
         -question => "Path to CA certificate for connection to database",
         -test => sub { return (-f shift) },
         -default => $ENV{HOME} . "/.postgresql/root.crt",
         -answer => \$answers->{'db-ca-cert'});
    }

    return;
}

sub postgresql_get_reportdb_answers {
    my $opts = shift;
    my $answers = shift;

    my %config = ();
    read_config(DEFAULT_RHN_CONF_LOCATION, \%config);

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Hostname (leave empty for local)",
        -test => sub { 1 },
        -answer => \$answers->{'report-db-host'});

    if ($answers->{'report-db-host'} ne '') {
        $answers->{'report-db-host'} = idn_to_ascii($answers->{'report-db-host'}, "utf8");
        ask(
            -noninteractive => $opts->{"non-interactive"},
            -question => "Port",
            -test => qr/\d+/,
            -default => 5432,
            -answer => \$answers->{'report-db-port'});
    } else {
            $answers->{'report-db-port'} = '';
    }

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Database",
        -test => qr/\S+/,
        -default => $config{'report_db_name'},
        -answer => \$answers->{'report-db-name'});

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Username",
        -test => qr/\S+/,
        -default => $config{'report_db_user'},
        -answer => \$answers->{'report-db-user'});

    ask(
        -noninteractive => $opts->{"non-interactive"},
        -question => "Password (leave empty for autogenerated password)",
        -test => sub { 1 },
        -answer => \$answers->{'report-db-password'},
        -password => 1);

    ask(
       -noninteractive => $opts->{"non-interactive"},
       -question => "Path to CA certificate to connect to the reporting database",
       -test => sub { return (-f shift) },
       -default => "/etc/pki/trust/anchors/LOCAL-RHN-ORG-TRUSTED-SSL-CERT",
       -answer => \$answers->{'report-db-ca-cert'});
    $answers->{'report-db-ssl-enabled'} = '1';
    return;
}

############################
# PostgreSQL Specific Code #
############################

# Parent PostgreSQL setup function:
sub postgresql_setup_db {
    my $opts = shift;
    my $answers = shift;

    print Spacewalk::Setup::loc("** Database: Setting up database connection for PostgreSQL backend.\n");
    my $connected;

    if (is_embedded_db($opts)) {
      postgresql_start();
    }
    postgresql_setup_embedded_db($opts, $answers);

    while (not $connected) {
        postgresql_get_database_answers($opts, $answers);

        if ($opts->{'external-postgresql-over-ssl'}) {
            $ENV{PGSSLROOTCERT} = $answers->{'db-ca-cert'};
            $ENV{PGSSLMODE} = "verify-full";
        }

        my $dbh;

        eval {
            $dbh = get_dbh($answers);
            $dbh->disconnect();
        };
        if ($@) {
            print Spacewalk::Setup::loc("Could not connect to the database.  Your connection information may be incorrect.  Error: %s\n", $@);

            delete @{$answers}{qw/db-host db-port db-name db-user db-password/};
        }
        else {
            $connected = 1;
        }
    }

    my $populate_db = 0;

    set_hibernate_conf($answers);
    write_rhn_conf($answers, 'db-backend', 'db-host', 'db-port', 'db-name', 'db-user', 'db-password', 'db-ssl-enabled');

    postgresql_populate_db($opts, $answers, $populate_db);
    return 1;
}

sub postgresql_reportdb_setup {
    my $opts = shift;
    my $answers = shift;

    print Spacewalk::Setup::loc("** Database: Setting up report database.\n");
    # check for answers, but use defaults in case the values are not specified

    postgresql_get_reportdb_answers($opts, $answers);

    if ($opts->{"clear-db"}) {
        print Spacewalk::Setup::loc("** Database: --clear-db option used.  Clearing report database.\n");
	postgresql_drop_reportdb($answers);
    }

    $ENV{PGSSLROOTCERT} = $answers->{'report-db-ca-cert'};
    if ($answers->{'report-db-host'} ne 'localhost') {
        $ENV{PGSSLMODE} = "verify-full";
    }

    write_rhn_conf($answers, 'externaldb-admin-user','externaldb-admin-password', 'report-db-backend', 'report-db-host', 'report-db-port', 'report-db-name', 'report-db-user', 'report-db-password', 'report-db-ssl-enabled');

    my @cmd = ('/usr/bin/uyuni-setup-reportdb', 'create', '--db', $answers->{'report-db-name'},
        '--user', $answers->{'report-db-user'}, '--host', $answers->{'report-db-host'});

    if ($answers->{'externaldb'}) {
        push @cmd, "--externaldb-admin-user", $answers->{'externaldb-admin-user'},
        "--externaldb-admin-password", $answers->{'externaldb-admin-password'},
        "--externaldb-root-cert", $answers->{'report-db-ca-cert'};

        if ($answers->{'externaldb-provider'} ne '') {
            push @cmd, "--externaldb-provider", $answers->{'externaldb-provider'};
        }
    }
    else {
        push @cmd, "--address", '*', "--remote", '0.0.0.0/0,::/0';
    }

    if ($answers->{'report-db-password'} ne '') {
        push @cmd, "--password", $answers->{'report-db-password'};
    }
    else {
        push @cmd, "--autogenpw";
    }

    print_progress(-init_message => "*** Progress: #",
                      -log_file_name => DB_INSTALL_LOG_FILE,
                      -log_file_size => DB_INSTALL_LOG_SIZE,
                      -err_message => "Could not install report database.\n",
                      -err_code => 15,
                      -system_opts => \@cmd);
    
    if (-e Spacewalk::Setup::DEFAULT_RHN_CONF_LOCATION) {
        my %dbOptions = ();
        ### uyuni-setup-reportdb writes param in rhn.conf. We need to read them and persists them in satellite-local-rules.conf
        read_config(Spacewalk::Setup::DEFAULT_RHN_CONF_LOCATION, \%dbOptions);
        ### here we need _ instead of - cause we read them from rhn.conf
        write_rhn_conf(\%dbOptions, 'report_db_backend', 'report_db_host', 'report_db_port', 'report_db_name', 'report_db_user', 'report_db_password', 'report_db_ssl_enabled','report_db_sslrootcert');
    }
    print loc("** Database: Installation complete.\n");

    return 1;
}

sub postgresql_start {
    my $pgservice=`systemctl list-unit-files | grep postgresql | cut -f1 -d. | tr -d '\n'`;
    system("service $pgservice status >&/dev/null");
    system("service $pgservice start >&/dev/null") if ($? >> 8);
    return ($? >> 8);
}

sub postgresql_setup_embedded_db {
    my $opts = shift;
    my $answers = shift;

    if (not is_embedded_db($opts)) {
        return 0;
    }

    if ($opts->{"skip-db-install"} or $opts->{"upgrade"}) {
        print loc("** Database: Embedded database installation SKIPPED.\n");
        return 0;
    }

    if (not -x '/usr/bin/spacewalk-setup-postgresql') {
        print loc(<<EOQ);
The spacewalk-setup-postgresql does not seem to be available.
You might want to use --external-postgresql command line option.
EOQ
        exit 24;
    }

    my $pgdata=`runuser -l postgres -c env | grep PGDATA | cut -f2- -d=`;

    if (-d "$pgdata/base" and
        ! system(qq{/usr/bin/spacewalk-setup-postgresql check --db $answers->{'db-name'}})) {
        my $shared_dir = SHARED_DIR;
        print loc(<<EOQ);
The embedded database appears to be already installed. Either rerun
this script with the --skip-db-install option, or use the
'/usr/bin/spacewalk-setup-postgresql remove --db $answers->{'db-name'} --user $answers->{'db-user'}'
script to remove the embedded database and try again.
EOQ

        exit 13;
    }

    if (not $opts->{"skip-db-diskspace-check"}) {
        system_or_exit(['python3', SHARED_DIR .
            '/embedded_diskspace_check.py', '$pgdata', '12288'], 14,
            'There is not enough space available for the embedded database.');
    }
    else {
        print loc("** Database: Embedded database diskspace check SKIPPED!\n");
    }

    printf loc(<<EOQ, DB_INSTALL_LOG_FILE);
** Database: Installing the database:
** Database: This is a long process that is logged in:
** Database:   %s
EOQ

    if (have_selinux()) {
      local *X; open X, '>', DB_INSTALL_LOG_FILE and close X;
      system('/sbin/restorecon', DB_INSTALL_LOG_FILE);
    }
    print_progress(-init_message => "*** Progress: #",
        -log_file_name => DB_INSTALL_LOG_FILE,
                -log_file_size => DB_INSTALL_LOG_SIZE,
                -err_message => "Could not install database.\n",
                -err_code => 15,
                -system_opts => [ "/usr/bin/spacewalk-setup-postgresql",
                                  "create",
                                  "--db", $answers->{'db-name'},
                                  "--user", $answers->{'db-user'},
                                  "--password", $answers->{'db-password'}]);

    print loc("** Database: Installation complete.\n");

    return 1;
}

sub postgresql_populate_db {
    my $opts = shift;
    my $answers = shift;
    my $populate_db = shift;

    print Spacewalk::Setup::loc("** Database: Populating database.\n");

    if ($opts->{"skip-db-population"} or ($opts->{'upgrade'} and not $populate_db)) {
        print Spacewalk::Setup::loc("** Database: Skipping database population.\n");
        return 1;
    }

    if ($opts->{"clear-db"}) {
        print Spacewalk::Setup::loc("** Database: --clear-db option used.  Clearing database.\n");
        my $dbh = get_dbh($answers);
        postgresql_clear_db($dbh, $answers);
    }

    if (postgresql_test_db_schema($answers)) {
        ask(
            -noninteractive => $opts->{"non-interactive"},
            -question => "The Database has schema.  Would you like to clear the database",
            -test => qr/(Y|N)/i,
            -answer => \$answers->{'clear-db'},
            -default => 'Y',
        );

        if ($answers->{"clear-db"} =~ /Y/i) {
            print Spacewalk::Setup::loc("** Database: Clearing database.\n");
            my $dbh = get_dbh($answers);
            postgresql_clear_db($dbh, $answers);
            print Spacewalk::Setup::loc("** Database: Re-populating database.\n");
        }
        else {
            print Spacewalk::Setup::loc("** Database: The database already has schema.  Skipping database population.\n");
            return 1;
        }
    }

    my $sat_schema = POSTGRESQL_SCHEMA_FILE;
    my $sat_schema_deploy = POSTGRESQL_DEPLOY_FILE;

    system_or_exit([ "/usr/bin/rhn-config-schema.pl",
                   "--source=" . $sat_schema,
                   "--target=" . $sat_schema_deploy,
                   "--tablespace-name=None" ],
                   22,
                   'There was a problem populating the deploy.sql file.',
                   );

    my $logfile = DB_POP_LOG_FILE;

    my @opts = ('spacewalk-sql', '--select-mode-direct', $sat_schema_deploy);

    print_progress(-init_message => "*** Progress: #",
        -log_file_name => Spacewalk::Setup::DB_POP_LOG_FILE,
        -log_file_size => Spacewalk::Setup::PG_POP_LOG_SIZE,
        -err_message => "Could not populate database.\n",
        -err_code => 23,
        -system_opts => [@opts]);

    return 1;
}

# Check if the database appears to already have schema loaded:
sub postgresql_test_db_schema {
    my $answers = shift;
    my $dbh = get_dbh($answers);

    # Assumption, if web_customer table exists then schema exists:
    my $sth = $dbh->prepare("SELECT tablename from pg_tables where schemaname='public' and tablename='web_customer'");

    $sth->execute;
    my ($row) = $sth->fetchrow;
    $sth->finish;
    $dbh->disconnect();
    return $row ? 1 : 0;
}

# Clear the PostgreSQL schema by deleting the 'public' schema with cascade,
# then re-creating it. Also delete all the other known schemas that
# Spacewalk might have created.

my @POSTGRESQL_CLEAR_SCHEMA = (
        'drop schema if exists rpm cascade ;',
        'drop schema if exists rhn_exception cascade ;',
        'drop schema if exists rhn_config cascade ;',
        'drop schema if exists rhn_server cascade ;',
        'drop schema if exists rhn_entitlements cascade ;',
        'drop schema if exists rhn_bel cascade ;',
        'drop schema if exists rhn_cache cascade ;',
        'drop schema if exists rhn_channel cascade ;',
        'drop schema if exists rhn_config_channel cascade ;',
        'drop schema if exists rhn_org cascade ;',
        'drop schema if exists rhn_user cascade ;',
        'drop schema if exists logging cascade ;',
);

sub postgresql_clear_db {
        my $dbh = shift;
        my $answers = shift;
        my $do_shutdown = (defined($_[0]) ? shift : 1);

        if ($do_shutdown) {
            print loc("** Database: Shutting down spacewalk services that may be using DB.\n");

            # The --exclude=postgresql is needed for embedded database Satellites.
            system_debug('/usr/sbin/spacewalk-service', '--exclude=postgresql', 'stop');
            print loc("** Database: Services stopped.  Clearing DB.\n");
        }

        local $dbh->{RaiseError} = 0;
        local $dbh->{PrintError} = 1;
        local $dbh->{PrintWarn} = 0;
        local $dbh->{AutoCommit} = 1;
        if (lc $answers->{'externaldb-provider'} ne 'aws') {
                push @POSTGRESQL_CLEAR_SCHEMA, "drop schema if exists public cascade ;", 
                "create schema public authorization postgres ;";
        }
        foreach my $c (@POSTGRESQL_CLEAR_SCHEMA) {
                $dbh->do($c);
        }
        $dbh->disconnect;
        return 1;
}

sub postgresql_drop_reportdb {
        my $answers = shift;
        my @cmd = ('/usr/bin/uyuni-setup-reportdb', 'remove', '--db', $answers->{'report-db-name'},
        '--user', $answers->{'report-db-user'}, '--host', $answers->{'report-db-host'});

        if ($answers->{'externaldb'}) {
            push @cmd, "--externaldb-admin-user", $answers->{'externaldb-admin-user'},
            "--externaldb-admin-password", $answers->{'externaldb-admin-password'};
        }
        system_debug(@cmd);
        return 1;
}

sub get_dbh {
        my $answers = shift;
        my $reportdb = shift || 0;

        my $dbh_attributes = {
                RaiseError => 1,
                PrintError => 0,
                Taint => 0,
                AutoCommit => 0,
        };

        my $backend = $reportdb ? $answers->{'report-db-backend'} : $answers->{'db-backend'};

        if ($backend eq 'postgresql') {
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

        die "Unknown db-backend [$backend]\n";
}

# Function to check that we have SELinux, in the sense that we are on
# system with modular SELinux (> RHEL 4), and the module spacewalk is loaded.
my $have_selinux;
sub have_selinux {
        return $have_selinux if defined $have_selinux;
        if( not -x "/usr/sbin/selinuxenabled") {
                $have_selinux = 0;
        } elsif (system(q!/usr/sbin/selinuxenabled && /usr/sbin/semodule -l 2> /dev/null | grep '^spacewalk\b' 2>&1 > /dev/null!)) {
                $have_selinux = 0;
        } else {
                $have_selinux = 1;
        }
        return $have_selinux;
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

# Set hibernate strings into answers according to DB backend.
sub set_hibernate_conf {
    my $answers = shift;

    if ($answers->{'db-backend'} eq 'postgresql') {
        $answers->{'hibernate.dialect'} = "org.hibernate.dialect.PostgreSQLDialect";
        $answers->{'hibernate.connection.driver_class'} = "org.postgresql.Driver";
        $answers->{'hibernate.connection.driver_proto'} = "jdbc:postgresql";
    }
    write_rhn_conf($answers, 'hibernate.dialect', 'hibernate.connection.driver_class', 'hibernate.connection.driver_proto');
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

=item B<--non-interactive>

For use only with --answer-file.  If the --answer-file doesn't provide
a required response, exit instead of prompting the user.

# todo @
=item B<--re-register>

Register the system with RHN, even if it is already registered.

=item B<--clear-db>

Clear any pre-existing database schema before installing.
This will destroy any data in the Satellite database and re-create
empty Satellite schema. This option implies B<--skip-db-install>.

=item B<--skip-system-version-test>

Do not test the Red Hat Enterprise Linux version before installing.

=item B<--skip-selinux-test>

For the installation and setup to proceed properly, SELinux should
be in Permissive or Enforcing mode. If you are certain that
you are not in Disabled mode or you want to install in
Disabled anyway, re-run the installer with the flag --skip-selinux-test.

=item B<--skip-fqdn-test>

Do not verify that the system has a valid hostname.  Red Hat Satellite
requires that the hostname be properly set during installation.
Using this option may result in a Satellite server that is not fully
functional.

=item B<--skip-db-install>

Do not install the embedded database.  This option may be useful if you
are re-installing the satellite, and do not want to clear the database.

=item B<--skip-db-diskspace-check>

Do not check to make sure there is enough free disk space to install
the embedded database.

=item B<--skip-db-population>

Do not populate the database schema.

=item B<--skip-ssl-cert-generation>

Do not generate the SSL certificates for the Satellite.

=item B<--skip-ssl-ca-generation>

Do not generate the SSL CA, use existing CA to sign certificate for the Satellite.

=item B<--skip-ssl-vhost-setup>

Do not configure the default SSL virtual host for Spacewalk.

Note that if you choose to have Spacewalk setup skip this step,
it's up to you to ensure that the following are included
in the virtual host definition:

RewriteEngine on
RewriteOptions inherit
SSLProxyEngine on

=item B<--upgrade>

Only runs necessary steps for a Satellite upgrade.

=item B<--skip-services-check>

Proceed with upgrade if services are already stopped.

=item B<--skip-services-restart>

Do not restart services at the end of installation.

=item B<--run-updater=<yes|no>>

Set to 'yes' to automatically install needed packages from RHSM, provided the system is registered. Set to 'no' to terminate the installer if any needed packages are missing.

=item B<--run-cobbler>

Only runs the necessary steps to setup cobbler

=item B<--enable-tftp=<yes|no>>

Set to 'yes' to automatically enable tftp and xinetd services needed for Cobbler PXE provisioning functionality. Set to 'no' if you do not want the installer to enable these services.

=item B<--external-postgresql>

Assume the Red Hat Satellite installation uses an external PostgreSQL database (Red Hat Satellite only).

=item B<--external-postgresql-over-ssl>

When used, installation will assume that external PostgreSQL server allows only connections over SSL.
This option is supposed to be used only in conjuction with B<--external-postgresql>.

=item B<--managed-db>

Setup PostgreSQL database for multi-server installation (database and Spacewalk / Red Hat Satellite on different machines).

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

Red Hat trademarks are not licensed under GPLv2. No permission is
granted to use or replicate Red Hat trademarks that are incorporated
in this software or its documentation.

=cut

1; # End of Spacewalk::Setup
