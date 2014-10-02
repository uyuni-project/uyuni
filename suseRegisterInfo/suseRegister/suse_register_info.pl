#! /usr/bin/perl -w

use strict;
#use Data::Dumper;
use Getopt::Long;
use XML::Parser;

my $outfile = undef;
my $ctx = {};
# new credentails file
if ( -e "/etc/redhat-release" )
{
  $ctx->{CREDENTIAL_FILE}  = "/etc/NCCcredentials";
} else {
  $ctx->{CREDENTIAL_FILE}  = "/etc/zypp/credentials.d/NCCcredentials";
}
$ctx->{guid}              = "";
$ctx->{secret}            = "";
$ctx->{ostarget}          = "";
$ctx->{installedProducts} = [];

my $parserTmp  = {};

$parserTmp->{CURRENT}   = {};
$parserTmp->{ELEMENT}   = "";
$parserTmp->{PRODUCTS}  = [];

# use read-only hack
$ENV{'ZYPP_READONLY_HACK'} = 1;

# handles XML reader start tag events
sub handle_start_tag()
{
    my ($expat, $element, %attrs ) = @_;

    if(lc($element) eq "product")
    {
        $parserTmp->{ELEMENT} = "product";
        foreach my $key (keys %attrs)
        {
            $parserTmp->{CURRENT}->{uc($key)} = "$attrs{$key}";
        }
    }
    elsif(lc($element) eq "description")
    {
        $parserTmp->{ELEMENT} = "description";
        $parserTmp->{CURRENT}->{DESCRIPTION} = "";
    }
}

sub handle_char_tag
{
    my ($expat, $string) = @_;

    chomp($string);
    return if($string =~ /^\s*$/);
    $string =~ s/^\s*//;
    $string =~ s/\s*$//;

    return if(! defined $parserTmp->{ELEMENT});

    if($parserTmp->{ELEMENT} eq "description")
    {
        $parserTmp->{CURRENT}->{DESCRIPTION} .= $string;
    }
}

sub handle_end_tag
{
    my ($expat, $element ) = @_;

    if(lc($element) eq "product")
    {
        # first call the callback
        push @{$parserTmp->{PRODUCTS}}, $parserTmp->{CURRENT};

        $parserTmp->{ELEMENT} = "";
        $parserTmp->{CURRENT} = {};
    }
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

open(CRED, "< ".$ctx->{CREDENTIAL_FILE}) or die "Cannot read credentials";
while (<CRED>)
{
    if($_ =~ /^\s*#/)
    {
        next;
    }
    if($_ =~ /username\s*=\s*(.*)$/ && defined $1 && $1 ne "")
    {
        $ctx->{guid} = $1;
    }
    elsif($_ =~ /password\s*=\s*(.*)$/ && defined $1 && $1 ne "")
    {
        $ctx->{secret} = $1;
    }
}

if ( -e "/etc/redhat-release" )
{
    my $name = `/usr/lib/suseRegister/bin/parse_release_info -si`;
    my $version = `/usr/lib/suseRegister/bin/parse_release_info -sr`;
    my $release = `/usr/lib/suseRegister/bin/parse_release_info -sc`;
    my $arch = `uname -m`;
    chomp($name);
    chomp($version);
    chomp($arch);
    chomp($release);
    if($name =~ /redhat/i || $name =~ /centos/i || $name =~ /SLESExpandedSupportplatform/i)
    {
        $name = "RES";
    }
    if( $version =~ /^(\d+)/ && defined $1 )
    {
        $version = $1;
    }

    push @{$ctx->{installedProducts}}, [$name, $version, $release, $arch, 'Y'];

    if($arch eq "i386" || $arch eq "i486" || $arch eq "i586" || $arch eq "i686")
    {
        $ctx->{ostarget} = "i386";
    }
    elsif($arch eq "x86_64")
    {
        $ctx->{ostarget} = "x86_64";
    }
}
else
{
    my $xml = `zypper -x --no-refresh --quiet --non-interactive products --installed-only`;

    my $parser = XML::Parser->new( Handlers =>
                                   {
                                    Start=> \&handle_start_tag,
                                    Char => \&handle_char_tag,
                                    End  => \&handle_end_tag,
                                   });
    eval
    {
        $parser->parse( $xml );
    };
    if ($@) {
       # ignore the errors
    }
    foreach my $p (@{$parserTmp->{PRODUCTS}})
    {
        push @{$ctx->{installedProducts}},
          [$p->{NAME},
           $p->{VERSION},
           $p->{RELEASE},
           $p->{ARCH},
           $p->{ISBASE}?'Y':'N'];
    }
    $ctx->{ostarget} = `zypper --non-interactive targetos`;

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
  my $baseproduct = $product->[4];

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
