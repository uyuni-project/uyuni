#! /usr/bin/perl
use strict;
use XML::Simple;
use IPC::Open3 ();
#use Data::Dumper;
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
        $apachemax = int($1) if($section && $line =~ /MaxClients\s+(\d+)/);
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
open(FILE, "< /etc/rhn/rhn.conf") and do
{
    while (<FILE>)
    {
        my $line = $_;
        next if($line =~ /^\s*#/);
        $javamax = $1 if ($line =~ /hibernate.c3p0.max_size\s*=\s*(\d+)/);
        $dbbackend = $1 if($line =~ /db_backend\s*=\s*(\w+)/);
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

# java web ui, taskomatic and search uses c3p0 pooling
# + every apache process can eat a connection
# + buffer for local connections
my $mindb = (3*$javamax) + $apachemax + 60;
my $dblimit = 0;

if ($dbbackend eq "oracle")
{
    $dblimit = run_query(<<EOF);
        select limit_value from v\$resource_limit where resource_name = 'sessions';
EOF
}
else
{
    $dblimit = run_query(<<EOF);
        show max_connections;
EOF
}
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
