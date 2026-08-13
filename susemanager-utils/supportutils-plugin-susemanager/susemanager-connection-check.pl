#! /usr/bin/perl
use strict;
use XML::Simple;
use IPC::Open3 ();
use List::Util qw(min);

my $apachemax = 0;

# match double quoted non-empty string, or unquoted non-empty string
# reject empty or without closing double quotes
my $VALUE_REGEXP = '\s*(?|"(.*?[\S].*?)"|([^\s"].*?)\s*$)';

# default tomcat acceptCount value
# these connections are on top of tomcat's maxThreads which can wait in TCP queue without triggering error response.
# we can use them for apache static content not going through tomcat without fear of overloading tomcat.
my $acceptCount = 100;

sub run_query {
    my ($command, $is_reportdb) = @_;
    $ENV{'LANG'} = 'C';
    my @sql_args = ('spacewalk-sql');
    push(@sql_args, '--reportdb') if $is_reportdb;
    push(@sql_args, '--select-mode', '-');
    my $pid = IPC::Open3::open3(my $wfh, my $rfh, '>&STDERR',
                                @sql_args) or return;
    print $wfh $command;
    print $wfh "\n";
    close $wfh;

    my $out;
    my $seen_dashes = 0;
    while (<$rfh>) {
        if (not defined $out and $seen_dashes) {
            $out = $_;
            last;
        }
        if (/---/) {
            $seen_dashes = 1;
        }
    }
    close $rfh;
    waitpid $pid, 0;
    if ($?) {
        return;
    }

    $out =~ s/^\s+|\s+$//g if defined $out;
    # psql print '----' even if no rows were selected
    $out = undef if defined $out && $out =~ /^\(0 rows\)$/;
    return $out;
}


sub get_apache_max_request_workers {
    my @server_flags = ("-DSYSCONFIG");
    my $mpm = "prefork"; # default MPM

    # Read APACHE_SERVER_FLAGS and APACHE_MPM
    if (open(my $fh, '<', '/etc/sysconfig/apache2')) {
        while (<$fh>) {
            if (/^\s*APACHE_SERVER_FLAGS\s*=$VALUE_REGEXP/) {
                my $flags = $1;
                $flags =~ s/"//g;
                foreach my $flag (split(/\s+/, $flags)) {
                    next unless $flag;
                    if ($flag =~ /^-D/) {
                        push @server_flags, $flag;
                    } else {
                        push @server_flags, "-D$flag";
                    }
                }
            }
            if (/^\s*APACHE_MPM\s*=$VALUE_REGEXP/) {
                $mpm = $1;
                $mpm =~ s/"//g;
            }
        }
        close($fh);
    }

    # Determine binary based on active MPM
    my $apache_bin = "/usr/sbin/httpd";
    if (-x "/usr/sbin/httpd-$mpm") {
        $apache_bin = "/usr/sbin/httpd-$mpm";
    }

    my $mod_path = "/usr/lib64/apache2-$mpm/mod_info.so";
    $mod_path = "/usr/lib64/apache2/mod_info.so" unless -e $mod_path;

    # Construct and execute the direct binary call
    my @cmd = ($apache_bin,
        @server_flags,
        '-C', 'Include /etc/apache2/sysconfig.d/loadmodule.conf',
        '-C', 'Include /etc/apache2/sysconfig.d/global.conf',
        '-f', '/etc/apache2/httpd.conf',
        '-c', 'Include /etc/apache2/sysconfig.d/include.conf',
        '-c', "LoadModule info_module $mod_path",
        '-D', 'DUMP_CONFIG');

    my $max_clients = 0;
    my $out;
    my $pid = IPC::Open3::open3(undef, $out, '>', @cmd);
    while (my $line = <$out>) {
        if ($line =~ /^\s*(?:MaxRequestWorkers|MaxClients)\s+(\d+)/i) {
            $max_clients = int($1);
        }
    }
    waitpid( $pid, 0);

    # Fallback to static parsing if dynamic extraction failed
    if ($max_clients == 0) {
        print STDERR "WARN: Unable to check Apache variables, trying conf files directly\n";
        if (open(my $sfh, "<", "/etc/apache2/server-tuning.conf")) {
            my $in_active_mpm_section = 0;
            while (<$sfh>) {
                my $line = $_;
                next if ($line =~ /^\s*#/);

                if ($line =~ /<IfModule\s+${mpm}\.c>/) {
                    $in_active_mpm_section = 1;
                }
                elsif ($line =~ /<\/IfModule>/) {
                    $in_active_mpm_section = 0;
                }

                if ($in_active_mpm_section && $line =~ /(?:MaxRequestWorkers|MaxClients)\s+(\d+)/) {
                    $max_clients = int($1);
                }
            }
            close($sfh);
        }
    }
    if ($max_clients == 0) {
        print STDERR "ERROR: Unable to determine Apache connection limits.\n";
    }
    return $max_clients;
}

$apachemax = get_apache_max_request_workers();

my $ref = XMLin("/etc/tomcat/server.xml");
my $tomcatmax = 0;
foreach my $con (@{$ref->{'Service'}->{'Connector'}})
{
    $tomcatmax = 200 if ($con->{'port'} eq "8009" && ! exists $con->{'maxThreads'} && 200 > $tomcatmax);
    $tomcatmax = int($con->{'maxThreads'}) if ($con->{'port'} eq "8009" && exists $con->{'maxThreads'} && int($con->{'maxThreads'}) > $tomcatmax);
}

my $hardmax = $tomcatmax + $acceptCount;

if( $apachemax < $tomcatmax ) {
    print STDERR "ERROR: Apache allows fewer connections ($apachemax) than Tomcat does ($tomcatmax). Please align the values.\n";
}
elsif( $apachemax > $hardmax ) {
    # 100 is the default acceptCount value for tomcat. Meaning tomcat accepts up to acceptCount in a TCP queue, see $acceptCount.
    print STDERR "ERROR: Apache allows significantly more connections ($apachemax) than Tomcat ($tomcatmax). Please align the values.\n";
}
else {
    print "Apache connections: $apachemax\nTomcat connections: $tomcatmax\n";
}

my $javamax = 0;
my $reporting_javamax = 0;
my $dbbackend = "";
my $saltsshthreads = 0;
my $report_db_name = "";
my $db_host = "";
my $db_port = "5432";
my $report_db_host = "";
my $report_db_port = "5432";
open(FILE, "< /etc/rhn/rhn.conf") and do
{
    while (<FILE>)
    {
        my $line = $_;
        next if($line =~ /^\s*#|^\s*$/);
        $javamax = $1 if ($line =~ /hibernate.c3p0.max_size\s*=\s*(\d+)/);
        $reporting_javamax = $1 if ($line =~ /reporting.hibernate.c3p0.max_size\s*=\s*(\d+)/);
        $dbbackend = $1 if($line =~ /db_backend\s*=\s*(\w+)/);
        $saltsshthreads = $1 if ($line =~ /taskomatic\.sshminion_action_executor\.parallel_threads\s*=\s*(\d+)/);
        $report_db_name = $1 if ($line =~ /report_db_name\s*=\s*(\w+)/);
        $db_host = $1 if ($line =~ /db_host\s*=\s*(\S+)/);
        $db_port = $1 if ($line =~ /db_port\s*=\s*(\d+)/);
        $report_db_host = $1 if ($line =~ /report_db_host\s*=\s*(\S+)/);
        $report_db_port = $1 if ($line =~ /report_db_port\s*=\s*(\d+)/);
    }
    close FILE;
};
if ($javamax == 0)
{
    open(FILE, "< /usr/share/rhn/config-defaults/rhn_hibernate.conf") and do
    {
        while (<FILE>)
        {
            my $line = $_;
            next if($line =~ /^\s*#/);
            $javamax = $1 if ($line =~ /hibernate.c3p0.max_size\s*=\s*(\d+)/);
        }
        close FILE;
    };
}
if ($reporting_javamax == 0)
{
    open(FILE, "< /usr/share/rhn/config-defaults/rhn_reporting_hibernate.conf") and do
    {
        while (<FILE>)
        {
            my $line = $_;
            next if($line =~ /^\s*#/);
            $reporting_javamax = $1 if ($line =~ /reporting.hibernate.c3p0.max_size\s*=\s*(\d+)/);
        }
        close FILE;
    };
}
if ($saltsshthreads == 0)
{
    open(FILE, "< /usr/share/rhn/config-defaults/rhn_java.conf") and do
    {
        while (<FILE>)
        {
            my $line = $_;
            next if($line =~ /^\s*#/);
            $saltsshthreads = $1 if ($line =~ /taskomatic\.sshminion_action_executor\.parallel_threads\s*=\s*(\d+)/);
        }
        close FILE;
    };
}
# reposync computes subprocess count with min(os.cpu_count() * 2, 32)
my $cpucount = 0;
open(FILE, "< /proc/cpuinfo") and do
{
    $cpucount = scalar (map /^processor/, <FILE>);
    close FILE;
};
my $reposyncmax = min($cpucount * 2, 32);
print "Reposync connections: $reposyncmax\n";

sub parse_salt_worker_threads {
    my @config_files = sort {$b cmp $a} glob("/etc/salt/master.d/*.conf");
    push(@config_files, "/etc/salt/master");

    my $worker_threads = 0;
    CONFIG: foreach my $config (@config_files) {
        open(FILE, $config) and do
        {
            while (<FILE>)
            {
                my $line = $_;
                next if($line =~ /^\s*#/);
                $worker_threads = $1 if ($line =~ /worker_threads\s*:\s*(\d+)/);
            }
            close FILE;
            last CONFIG if $worker_threads > 0;
        };
    };
    return $worker_threads;
};
my $worker_threads = parse_salt_worker_threads();
print "Salt worker threads: $worker_threads\n";
# add one for mgr_events.py and for uyuni roster module
# custom engines are not counted here as we don't know if they use (and
# potentially leak) DB connections
my $saltmax = $worker_threads + $saltsshthreads + 2;

# java web ui, taskomatic uses c3p0 pooling
# search used a fixed number of connections (10)
# + every apache process can eat a connection
# + every salt worker (pillar rendering) + mgr_events.py engine + uyuni roster module
# + every reposync subprocess connects for insertions
# + buffer for local connections (including custom Salt code)
my $mindb = (2*$javamax) + $apachemax + 10 + $saltmax + $reposyncmax + 30;
my $min_report_db = (2 * $reporting_javamax) + 10;

my $dblimit = run_query(<<EOF, 0);
    show max_connections;
EOF

my $norm_db_host = $db_host || "localhost";
$norm_db_host = "localhost" if $norm_db_host eq "127.0.0.1";
my $norm_report_db_host = $report_db_host || "localhost";
$norm_report_db_host = "localhost" if $norm_report_db_host eq "127.0.0.1";

my $shared_db = 0;
if ($report_db_name ne "" && $norm_db_host eq $norm_report_db_host && $db_port eq $report_db_port) {
    $shared_db = 1;
}

if ($shared_db) {
    my $combined_min = $mindb + $min_report_db;
    if (! defined $dblimit) {
        print "Unable to query the allowed DB connections.\n";
        print "Minimal required DB connections: $mindb\n";
        print "Minimal required Report DB connections: $min_report_db\n";
    } elsif ($dblimit < $combined_min) {
        print STDERR "ERROR: SUSE Multi-Linux Manager requires more connections ($combined_min) than the database provides ($dblimit). Please align the values\n";
    } else {
        print "Minimal required DB connections: $mindb\n";
        print "Minimal required Report DB connections: $min_report_db\n";
        print "Minimal required connections: $combined_min\nAvailable DB connections: $dblimit\n";
    }
} else {
    if (! defined $dblimit) {
        print "Unable to query the allowed DB connections.\n";
        print "Minimal required DB connections: $mindb\n";
    } elsif ($dblimit < $mindb) {
        print STDERR "ERROR: SUSE Multi-Linux Manager requires more connections ($mindb) than the database provides ($dblimit). Please align the values\n";
    } else {
        print "Minimal required DB connections: $mindb\nAvailable DB connections: $dblimit\n";
    }

    if ($report_db_name ne "") {
        my $report_dblimit = run_query(<<EOF, 1);
            show max_connections;
EOF
        if (! defined $report_dblimit) {
            print "Unable to query the allowed Report DB connections.\n";
            print "Minimal required Report DB connections: $min_report_db\n";
        } elsif ($report_dblimit < $min_report_db) {
            print STDERR "ERROR: SUSE Multi-Linux Manager Report DB requires more connections ($min_report_db) than the database provides ($report_dblimit). Please align the values\n";
        } else {
            print "Minimal required Report DB connections: $min_report_db\nAvailable Report DB connections: $report_dblimit\n";
        }
    }
}
