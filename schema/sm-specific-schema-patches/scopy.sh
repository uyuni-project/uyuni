#! /bin/bash

#
# create a template with the diff command provided by "NEED CHECK:" message
# pipe it through "| tail -n +3" and store it in the template dir
#

nextnum=0
dest=""
sqlname=""

function incr () {
    num=`echo "$nextnum +1" | bc`
    printf -v nextnum "%d" $num
}

function new_path () {

    dest="$basepath/susemanager-schema-2.1.51-to-susemanager-schema-3.0/$nextnum$fname"

}

function find_source ()  {
    local s=$1
    local bp="$2.."
    local db=""

    local imp_packages=(rhn_entitlements.pkb rhn_channel.pks rhn_channel.pkb rhn_server.pkb)
    local imp_procs=(pg_dblink_exec create_first_org create_new_org)

    if [[ "$s" == *oracle ]]; then
	db="oracle"
    elif [[ "$s" == *postgresql ]]; then
	db="postgres"
    else
        echo "$s"
	return 0
    fi

    for p in ${imp_packages[*]}; do
	if [[ "$s" == *$p* ]]; then
            #echo "return new source: $bp/$db/packages/$p" >&2
            echo "$bp/$db/packages/$p"
	    return 0
        fi
    done
    for p in ${imp_procs[*]}; do
        if [[ "$s" == *$p* ]]; then
            #echo "return new source: $bp/$db/procs/$p.sql" >&2
            echo "$bp/$db/procs/$p.sql"
            return 0
        fi
    done
    echo "$s"
    return 0
}

templatedir=`dirname $0`
templatedir="$templatedir/template"

basepath=`pwd`
basepath=`dirname $basepath`
basepath="$basepath/upgrade/"
echo "P: $basepath"
echo "T: $templatedir"

dirs=(spacewalk-schema-2.1-to-spacewalk-schema-2.2 spacewalk-schema-2.2-to-spacewalk-schema-2.3 spacewalk-schema-2.3-to-spacewalk-schema-2.4 spacewalk-schema-2.4-to-spacewalk-schema-2.5)

for d in ${dirs[*]}; do
    for i in $basepath/$d/*; do
    	fname=`basename $i`
	if [ "$fname" == "README" ]; then
            continue
        fi
        #if [ "$d" == "spacewalk-schema-2.1-to-spacewalk-schema-2.2" ]; then
        #    num=`echo "$fname" | sed 's/^\([0-9]\+\)\-.*$/\1/'`
	#    if [ $num -lt 23 ]; then
        #        continue
        #    fi
	#fi
	new_path
    	if [ -z "$dest" ]; then
	    continue
    	fi
	if [ -e "$dest" ]; then
	    src=`sha1sum $i| awk '{print $1}'`
            target=`sha1sum $dest| awk '{print $1}'`
            #echo "SRC: $src TARGET: $target"
            if [ "$src" != "$target" ]; then
                if grep "already applied in Manager" "$dest" > /dev/null; then
                    continue
                fi
                if grep "own migration available" "$dest" > /dev/null; then
                    continue
                fi
                templatename=`basename $dest`
                #echo "Searching for template: $templatedir/$templatename.dif"
                if [ -e "$templatedir/$templatename.dif" ]; then
                    DIFF1=`diff -ub $i $dest | tail -n +3`
                    DIFF2=`cat "$templatedir/$templatename.dif"`
                    if [ "$DIFF1" = "$DIFF2" ]; then
                        continue
                    fi
                fi
		echo "NEED CHECK: diff -ub $i $dest"
	    fi
	    continue
	fi
        source=`find_source $i $basepath`
    	#echo ">cp $source $dest"
    	cp -v $source $dest
        #if grep "oracle equivalent source" $source; then
        #    echo "-- oracle equivalent source sha1 6c3a7d18a2df3aaabff00dedafa5fe70caeaa219" > $dest
        #fi
        #echo "-- This file is intentionally left empty." >> $dest
        #echo "-- own migration available" >> $dest
    done
    incr
done

