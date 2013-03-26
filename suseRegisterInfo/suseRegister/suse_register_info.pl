#! /usr/bin/perl -w

use strict;
use SUSE::SRPrivate;
#use Data::Dumper;
use Time::HiRes qw(gettimeofday tv_interval);
use Getopt::Long;

my $outfile = undef;
my $oldstyle = 0;

my $ctx = {};
$ctx->{errorcode} = 0;
$ctx->{errormsg} = "";
$ctx->{time} = 0;
$ctx->{timeindent} = 0;
$ctx->{logfile}     = undef;
$ctx->{debug}       = 0;
$ctx->{LOGDESCR}    = undef;
$ctx->{programStartTime} = [gettimeofday];
$ctx->{printnodebug} = 0;
$ctx->{version} = "1.0";

$ctx->{configFile}      = "/etc/suseRegister.conf";
$ctx->{sysconfigFile}   = "/etc/sysconfig/suse_register";

# these are the old files.
# if they still exists, we need to convert them
# to the new format
$ctx->{GUID_FILE}       = "/etc/zmd/deviceid";
$ctx->{SECRET_FILE}     = "/etc/zmd/secret";

# new credentails file
if ( -e "/etc/redhat-release" )
{
  $ctx->{CREDENTIAL_DIR}  = "/etc/";
} else {
  $ctx->{CREDENTIAL_DIR}  = "/etc/zypp/credentials.d/";
}
$ctx->{CREDENTIAL_FILE} = "NCCcredentials";

$ctx->{SYSCONFIG_CLOCK} = "/etc/sysconfig/clock";
$ctx->{CA_PATH}         = "/etc/ssl/certs";

$ctx->{URL}             = "https://secure-www.novell.com/center/regsvc/";

$ctx->{URLlistParams}   = "command=listparams";
$ctx->{URLregister}     = "command=register";
$ctx->{URLlistProducts} = "command=listproducts";

$ctx->{guid}      = undef;
$ctx->{secret}    = undef;
$ctx->{locale}    = undef;
$ctx->{encoding}  = "utf-8";
$ctx->{lang}      = "en-US";

$ctx->{listParams}  = 0;
$ctx->{xmlout}      = 0;
$ctx->{nooptional}  = 0;
$ctx->{acceptmand}  = 0;
$ctx->{forcereg}    = 0;
$ctx->{nohwdata}    = 0;
$ctx->{batch}       = 0;
$ctx->{interactive} = 0;
$ctx->{noproxy}     = 0;
$ctx->{serverKnownProducts} = [];
$ctx->{installedProducts}   = [];
$ctx->{products} = [];

$ctx->{installedPatterns} = [];

$ctx->{extraCurlOption} = [];

$ctx->{hostGUID} = undef;
$ctx->{virtType} = "";
$ctx->{FallbackHostGUID} = undef;

$ctx->{zmdConfig} = {};
$ctx->{ostarget} = "";

$ctx->{norefresh} = 1;

$ctx->{zypper}        = "/usr/bin/zypper";
$ctx->{lsb_release}   = "/usr/bin/lsb_release";
if( -e "/usr/lib/suseRegister/bin/parse_release")
{
    $ctx->{lsb_release}   = "/usr/lib/suseRegister/bin/parse_release";
}
$ctx->{uname}         = "/bin/uname";
$ctx->{hwinfo}        = "/usr/sbin/hwinfo";
$ctx->{curl}          = "/usr/bin/curl";

$ctx->{xenstoreread}  = "/usr/bin/xenstore-read";
$ctx->{xenstorewrite} = "/usr/bin/xenstore-write";
$ctx->{xenstorechmod} = "/usr/bin/xenstore-chmod";
$ctx->{lscpu}         = "/usr/bin/lscpu";

$ctx->{createGuid}    = "/usr/bin/uuidgen";

$ctx->{zmdcache} = "/var/cache/SuseRegister/lastzmdconfig.cache";
$ctx->{restoreRepos} = 0;
$ctx->{warnUnrestoredRepos} = 0;
# check for xen tools
if(! -e $ctx->{xenstoreread} &&
  -e "/bin/xenstore-read")
{
  $ctx->{xenstoreread} = "/bin/xenstore-read";
}
if(! -e $ctx->{xenstorewrite} &&
  -e "/bin/xenstore-write" )
{
  $ctx->{xenstorewrite} = "/bin/xenstore-write";
}
if(! -e $ctx->{xenstorechmod} &&
  -e "/bin/xenstore-chmod" )
{
  $ctx->{xenstorechmod} = "/bin/xenstore-chmod";
}

my $help = 0;
my $result = GetOptions ("outfile|o=s" => \$outfile,
                         "help|h"      => \$help
                        );

if($help)
{
  print STDERR "usage: $ARGV[0] [--outfile|o=/path/to/output]\n";
  exit 1;
}

#
# For debugging
# 
#open($ctx->{LOGDESCR}, ">> /var/log/suse_register_info.log");
#$ctx->{debug} = 3;

# use read-only hack 
$ENV{'ZYPP_READONLY_HACK'} = 1;

# call this as soon as possible.
my ($code, $msg) = SUSE::SRPrivate::initGUID($ctx);
if($code != 0)
{
  exit 1;
}

($code, $msg) = SUSE::SRPrivate::getProducts($ctx);
if($code != 0)
{
  exit 1;
}
eval
{
  ($code, $msg) = SUSE::SRPrivate::zypperOSTarget($ctx);
  if($code != 0)
  {
    exit 1;
  }
};
if($@)
{
  # try old interface
  eval
  {
    $ctx->{ostarget} = SUSE::SRPrivate::getOSTarget($ctx);
    $oldstyle = 1;
  };
  if($@)
  {
    print STDERR "Cannot get the ostarget\n";
    exit 1;
  }
}

if($outfile)
{
  open(OUT, "> $outfile") or die "Cannot open $outfile:$!";
}
else
{
  open(OUT, ">&STDOUT") or die "Cannot open STDOUT:$!";
}

print OUT "[system]\n";;
print OUT "guid=".$ctx->{guid}."\n";
print OUT "secret=".$ctx->{secret}."\n";
print OUT "ostarget=".$ctx->{ostarget}."\n";

foreach my $product (@{$ctx->{installedProducts}})
{
  my $name    = $product->[0];
  my $version = $product->[1];
  my $release = $product->[2];
  my $arch    = $product->[3];
  my $baseproduct = (($oldstyle)?'Y':'N');

  if ( -e '/etc/products.d/baseproduct')
  {
    open(P, "< /etc/products.d/baseproduct") and do
    {
      my $fm = 0;
      while($_ = <P>)
      {
        if($_ =~ /name>$name</)
        {
          $fm += 1;
          next;
        }
        elsif($_ =~ /<version>$version</)
        {
          $fm += 1;
          next;
        }
        if($fm >= 2)
        {
          $baseproduct = 'Y';
          last;
        }
      }
    };
  }
  my $fullname = $name;
  $fullname .= "-$version" if($version);
  $fullname .= "-$release" if($release);
  $fullname .= ".$arch" if($arch);
  print OUT "[$fullname]\n";;
  print OUT "name=$name\n";
  print OUT "version=$version\n";
  print OUT "release=$release\n";
  print OUT "arch=$arch\n";
  print OUT "baseproduct=$baseproduct\n";
  print OUT "\n";
}

