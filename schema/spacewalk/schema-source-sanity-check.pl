#!/usr/bin/perl

use strict;
use warnings FATAL => 'all';

use File::Find ();
use Getopt::Long ();
use Digest::SHA ();

my %files;
my $show_ignored = 0;
Getopt::Long::GetOptions('I' => \$show_ignored) or exit 9;

my @dirs = qw(common postgres upgrade);
if (scalar @ARGV > 0) {
  @dirs = @ARGV;
}

for my $dir (@dirs) {
        File::Find::find(sub {
                my $name = $File::Find::name;
                if ($name eq $dir) {
                        return;
                }
                if (not -f $_) {
                        return;
                }
                if (substr($name, 0, length($dir) + 1) ne "$dir/") {
                        die "In dir [$dir] we got [$name]\n";
                }
                if ($dir eq 'upgrade') {
                        my $generic = $name;
                        my $db = 'common';
                        if ($generic =~ /\.(oracle|postgresql)$/) {
                                die "Found DB specific files in [upgrade] dir"
                        }
                        $files{$db}{$generic} = $name;
                } else {
                        my $rname = substr($name, length($dir) + 1);
                        $files{$dir}{$rname} = $name;
                }
                }, $dir);
}

my $error = 0;

sub check_file_content {
        my $filename = shift;
        return if $filename =~ /^upgrade/;
        return if $filename =~ /qrtz\.sql$/;
        return if $filename =~ /dual\.sql$/;
        my ($type, $name) = ($filename =~ m!.*/(.+)/(.+?)(_foreignkeys)?\.(sql|pks|pkb)$!);
        return if not defined $type;
        return if $type eq 'class';
        return if $type eq 'packages';

        local *FILE;
        open FILE, '<', $filename or do {
                die "Error reading [$filename]: $!\n";
        };
        my $content;
        {
                local $/ = undef;
                $content = <FILE>;
        }
        close FILE;
        # print "[$filename] [$type] [$name]\n";
        if ($type eq 'tables') {
                if ($filename =~ '\w*_index.sql') {
                        $name =~ s/_index$//g;
                }
                elsif ($filename =~ '\w*_alters.sql') {
                        $name =~ s/_alters$//g;
                }
                if (not $content =~ /^(--.*\n
                                        |\s*\n
                                        |(create|alter|comment\s+on)\s+table\s+$name\b(?:[^;]|';')+;
                                        |create\s+(unique\s+)?index\s+\w+\s+on\s+$name[^;]+;
                                        |create\s+sequence[^;]+;
                                        |comment\s+on\s+column\s+$name\.[^;]+;
                                        )+$/ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } elsif ($type eq 'views') {
                if (not $content =~ /^(--.*\n
                                        |\s*\n
                                        |create(\s+or\s+replace)?\s+view\s+$name\b[^;]+;
                                        )+$/ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } elsif ($type eq 'data') {
                if (not $content =~ /^(--.*\n
                                        |\s*\n
                                        |insert\s+into\s+$name\b[^;]+(values|select)('[^;]+(;[^;]*)*'|[^';])+;
                                        |select\s+[^;()]+\(('[^;]+')*\);
                                        |begin\s+[^;()]+\(('[^;]+')*\);\s+end;\n\/
                                        |commit;
                                        )+$/ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } elsif ($type eq 'procs') {
                if (not $content =~ m!^(--.*\n
                                        |\s*\n
                                        |create(\s+or\s+replace)?\s+(procedure|function)\s+$name\b
                                                ((?s:.+?);\n/\n
                                                |[^\$]+\$\$(?s:.+?)\s\$\$
                                                        \s+language\s+(plpgsql|sql)(\s+(strict\s+)?immutable|\s+stable)?;)
                                        |show\s+errors;?\n
                                        )+$!ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } elsif ($type eq 'synonyms') {
                if (not $content =~ m!^(--.*\n
                                        |\s*\n
                                        |create(\s+or\s+replace)?\s+synonym\s+$name\b\s+for[^;]+;
                                        |create(\s+or\s+replace)?\s+synonym\s+${name}s?_recid_seq\s+for[^;]+;
                                        )+$!ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } elsif ($type eq 'triggers') {
                if (not $content =~ m!^(?:--.*\n
                                        |\s*\n
                                        |create(?:\s+or\s+replace)?\s+function\s+(\w+)(?s:.+?)\s+language\s+plpgsql;
                                                \s+create(\s+or\s+replace)?\s+trigger[^;]+\s+on\s+$name\b[^;]+execute\s+procedure\s+\1\(\);
                                        |create(\s+or\s+replace)?\s+trigger[^;]+\s+on\s+$name\b[^;]+execute\s+procedure\s+no_operation_trig_fun\(\);
                                        |create(\s+or\s+replace)?\s+trigger[^;]+\s+on\s+$name\b(?s:.+?);\n/\n
                                        |show\s+errors;?\n
                                        )+$!ix) {
                        print "Bad $type content [$filename]\n";
                        $error = 1;
                }
        } else {
                print "Unknown type [$type] for [$filename]\n";
        }
}

for my $c (sort keys %{ $files{common} }) {
        next unless $c =~ /\.(sql|pks|pkb)$/;
        check_file_content($files{common}{$c});
        for my $o (qw( postgres )) {
                if (exists $files{$o}{$c}) {
                        print "Common file [$c] is also in $o\n";
                        $error = 1;
                }
        }
}

for my $c (sort keys %{ $files{postgres} }) {
        next unless $c =~ /\.(sql|pks|pkb)$/;
        check_file_content($files{postgres}{$c});
}

exit $error;

