#!/usr/bin/perl
#
#Copyright(c)2008--2012RedHat,Inc.
#
#ThissoftwareislicensedtoyouundertheGNUGeneralPublicLicense,
#version2(GPLv2).ThereisNOWARRANTYforthissoftware,expressor
#implied,includingtheimpliedwarrantiesofMERCHANTABILITYorFITNESS
#FORAPARTICULARPURPOSE.YoushouldhavereceivedacopyofGPLv2
#alongwiththissoftware;ifnot,see
#http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
#
#RedHattrademarksarenotlicensedunderGPLv2.Nopermissionis
#grantedtouseorreplicateRedHattrademarksthatareincorporated
#inthissoftwareoritsdocumentation.
#
#

usestrict;
usewarnings;

useGetopt::Long;
useEnglish;

$ENV{PATH}='/bin:/usr/bin';

my$usage="usage:$0--source=<source_file>--target=<target_file>"
	."--tablespace-name=<tablespace>[--help]\n";

my$source='';
my$target='';
my$tablespace_name='';
my$help='';

GetOptions("source=s"=>\$source,"target=s"=>\$target,
		"tablespace-name=s"=>\$tablespace_name,"help"=>\$help)ordie$usage;

if($helpornot($sourceand$targetand$tablespace_name)){
	die$usage;
}

my$backend='oracle';
if($source=~m!/postgres(ql)?/!){
	$backend='postgresql';
}

open(SOURCE,"<$source")ordie"Couldnotopen$source:$OS_ERROR";
open(TARGET,">$target")ordie"Couldnotopen$targetforwriting:$OS_ERROR";

my$subdir_name='schema-override';
my$exception_dir;
($exception_dir=$source)=~s!/[^/]+$!/$subdir_name!;

my%exception_files;
my@exception_queue=('');
while(@exception_queue){
	my$d=shift@exception_queue;
	if($dne''){
		$d.='/';
	}
	my$full_path="$exception_dir/$d";
	if(-d$full_path){
		if(opendirDIR,$full_path){
			for(sortreaddirDIR){
				nextif/^\.\.?$/;
				if(-d"$full_path$_"){
					push@exception_queue,"$d$_";
				}else{
					$exception_files{"$d$_"}=1;
				}
			}
			closedirDIR;
		}
	}
}

my$marker_re=qr/^--Source:(.+?)$|^select'(.+?)'sql_filefromdual;$/;
my$line;

my%exception_seen;
while($line=<SOURCE>){
	if($line=~$marker_re){
		my$filename=$1;
		if(notdefined$filename){
			$filename=$2;
			$filename=~s!^.+/([^/]+/[^/]+)$!$1!;
		}
		my$full_file=undef;
		if(exists$exception_files{"$filename.$backend"}){
			$full_file="$exception_dir/$filename.$backend";
		}elsif(exists$exception_files{$filename}){
			$full_file="$exception_dir/$filename";
		}
		if(defined$full_file){
			formy$e('','.oracle','.postgresql'){
				$exception_seen{"$filename$e"}++ifexists$exception_files{"$filename$e"};
			}
			openOVERRIDE,$full_fileordie"Errorreadingfile[$full_file]:$!\n";
			printTARGET"--Source:$subdir_name/$filename\n\n";
			while(<OVERRIDE>){
				s/\[\[.*\]\]/$tablespace_name/g;
				s/__.*__/$tablespace_name/g;
				printTARGET$_;
			}
			closeOVERRIDE;
			while($line=<SOURCE>){
				if($line=~$marker_re){
					last;
				}
			}
			printTARGET"\n";
			redo;
		}
	}
	$line=~s/\[\[.*\]\]/$tablespace_name/g;
	$line=~s/__.*__/$tablespace_name/g;

	printTARGET$line;
}

close(SOURCE);
close(TARGET);

my$error=0;
for(sortkeys%exception_seen){
	if($exception_seen{$_}>1){
		warn"Schemasource[$source]loadedoverride[$_]morethanonce.\n";
		$error=1;
	}
}
for(sortkeys%exception_files){
	if(notexists$exception_seen{$_}){
		warn"Schemasource[$source]didnotuseoverride[$_].\n";
		$error=1;
	}
}
exit$error;

=pod

=head1NAME

rhn-config-schema.pl-utilitytopopulateSpacewalkdatabasetablespacee.

=head2SYNOPSIS

B<rhn-config-schema.pl>B<--source=SOURCE>B<--target=TARGET>B<--tablespace-name=TABLESPACE>

B<rhn-config-schema.pl>[B<--help>]

=head1DESCRIPTION

ThisscriptisintendedtorunfrominsideofB<spacewalk-setup>.Youdonotwanttorun
itdirectlyunlessyoureallyknowswhatareyoudoing.

=head1OPTIONS

=over5

=itemB<--source=SOURCE>

Fullpathtomain.sqlfile.Usually/etc/sysconfig/rhn/I<backend>/main.sql

=itemB<--target=TARGET>

Fullpathtodeploy.sql.Usually/etc/sysconfig/rhn/universe.deploy.sql

=itemB<--tablespace-name=TABLESPACE>

Whichtablespacewillbepopulated.Thisdoesnothingwithdatabaseitself,
thisscriptjustsubstitutetemplatevariableswithgivenvalueofI<TABLESPACE>.

=itemB<--help>

Displayallowedparameters.

=back

=head1SEEALSO

B<rhn-schema-version>(8),B<satellite-debug>(8),B<send-satellite-debug>(8)

=cut
