#! /usr/bin/perl
#
# changeHelpUrl.pl /path/to/idfile
#
use strict;
use File::Find;

my $DEBUG = 0;

my $resourceBase="java/code/";
#my $resourceBase="branding/java";
my $gitRoot=`git -c alias.a='!pwd' a`;
chomp($gitRoot);

sub fail
{
    my $msg = shift;
    print STDERR "$msg\n";
    exit 1;
}

sub replace
{
    my $oldID = shift;
    my $newID = shift;
    my $EXTRA = shift or 0;

    return if($oldID eq "==");

    my $foundFiles = `grep -rl "$oldID" $gitRoot/$resourceBase`;
    foreach my $file (split(/\n/, $foundFiles)) {
        rename($file, "$file.orig");
        open(ORIG, "< $file.orig") or fail("cannot open $file.orig: $!");
        open(NEW, "> $file") or fail("cannot open $file: $!");
        while (my $row = <ORIG>) {
	    if ($row =~ /$oldID\.jsp/) {
		$row =~ s/$oldID\.jsp/$newID.jsp/;
		print "replace $oldID.jsp with $newID.jsp\n" if($DEBUG or $EXTRA);
	    }
	    if ($row =~ /#$oldID"/) {
		$row =~ s/#$oldID"/#$newID"/;
		print "replace #$oldID\" with #$newID\"\n" if($DEBUG or $EXTRA);
	    }
	    $row =~ s/rhn\/help\/\w+\/en-US/rhn\/help\/reference\/en-US/;
	    print NEW $row;
        }
	close NEW;
	close ORIG;
    }
}

sub main
{
    `git status >&/dev/null`;
    if($? != 0) {
        fail('Not in git repo');
    }
    if( ! -d "$gitRoot/$resourceBase" ) {
        fail("Can't find $gitRoot/$resourceBase");
    }
    if(! exists $ARGV[0]) {
	print "usage: changeHelpUrl.pl /path/to/id.file\n";
        fail('Expected path to ID file');
    }
    my $idfile=@ARGV[0];
    if(! -r $idfile) {
        print "usage: changeHelpUrl.pl /path/to/id.file\n";
        fail("File not found: $idfile");
    }
    open(IDFILE, "< $idfile") or fail("Cannot open $idfile: $!");
    while (my $row = <IDFILE>) {
	chomp($row);
        if ($row !~ /^(\S+)\s+(\S+)\s*(\S*)\s*$/) {
		print "line does not match: $row\n";
		continue;
	}
	my $oldID = $1;
	my $newID = $2;
	my $oldID2 = $3 or "";

	replace($oldID, $newID) if($oldID2 ne "xxx");
	if($oldID2 && $oldID2 ne "xxx") {
	    replace($oldID2, $newID);
        }
    }
    close IDFILE;
}

main();
