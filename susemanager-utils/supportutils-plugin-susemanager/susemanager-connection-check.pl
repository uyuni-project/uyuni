#! /usr/bin/perl
use strict;
use XML::Simple;
use IPC::Open3 ();
use List::Util qw(min);

my $apachemax = 0;

sub run_query {
    my ($command) = @_;
    $ENV{'LANG'} = 'C';
    my $pid = IPC::Open3::open3(my $wfh, my $rfh, '>&STDERR',
                                'spacewalk-sql', '--select-mode', '-') or return;
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


open(FILE, "< /etc/apache2/server-tuning.conf") and do
{
    my $section = 1;
    while (<FILE>)
    {
        my $line = $_;
        next if($line =~ /^\s*#/);
        $section = 0 if ($line =~ /IfModule worker/);
        $section = 1 if ($section == 0 && /\/IfModule/);
        $apachemax = int($1) if($section && $line =~ /MaxRequestWorkers\s+(\d+)/);
    }
    close FILE;
};

my $ref = XMLin("/etc/tomcat/server.xml");
my $tomcatmax = 0;
foreach my $con (@{$ref->{'Service'}->{'Connector'}})
{
    $tomcatmax = 200 if ($con->{'port'} eq "8009" && ! exists $con->{'maxThreads'} && 200 > $tomcatmax);
    $tomcatmax = int($con->{'maxThreads'}) if ($con->{'port'} eq "8009" && exists $con->{'maxThreads'} && int($con->{'maxThreads'}) > $tomcatmax);
}

if( $apachemax > $tomcatmax ) {
    print STDERR "ERROR: Apache allows more connection ($apachemax) as tomcat do ($tomcatmax). Please align the values\n";
}
else
{
    print "Apache connections: $apachemax\nTomcat connections: $tomcatmax\n";
}

my $javamax = 0;
my $dbbackend = "";
my $saltsshthreads = 0;
open(FILE, "< /etc/rhn/rhn.conf") and do
{
    while (<FILE>)
    {
        my $line = $_;
        next if($line =~ /^\s*#/);
        $javamax = $1 if ($line =~ /hibernate.c3p0.max_size\s*=\s*(\d+)/);
        $dbbackend = $1 if($line =~ /db_backend\s*=\s*(\w+)/);
        $saltsshthreads = $1 if ($line =~ /taskomatic\.sshminion_action_executor\.parallel_threads\s*=\s*(\d+)/);
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
# + every salt mworker (pillar rendering) + mgr_events.py engine + uyuni roster module
# + every reposync subproccess connects for insertions
# + buffer for local connections (including custom Salt code)
my $mindb = (2*$javamax) + $apachemax + 10 + $saltmax + $reposyncmax + 30;
my $dblimit = 0;

$dblimit = run_query(<<EOF);
    show max_connections;
EOF
if (! defined $dblimit)
{
    print "Unable to query the allowed DB connections.\n";
    print "Minimal required DB connection: $mindb\n";
}
elsif ($dblimit < $mindb)
{
    print STDERR "ERROR: SUSE Manager requires more connection ($mindb) as the database provide ($dblimit). Please align the values\n";
}
else
{
    print "Minimal required DB connection: $mindb\nAvailable DB connections: $dblimit\n";
}
