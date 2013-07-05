#! /bin/bash

nextnum=0
dest=""
sqlname=""

function incr () {
    num=`echo "$nextnum +1" | bc`
    printf -v nextnum "%d" $num
}

function new_path () {

    dest="$basepath/susemanager-schema-1.7.57-to-susemanager-schema-2.0//$nextnum$fname"

}

templatedir=`dirname $0`
templatedir="$templatedir/template"

basepath=`pwd`
basepath=`dirname $basepath`
basepath="$basepath/upgrade/"
echo "P: $basepath"
echo "T: $templatedir"

dirs=(spacewalk-schema-1.7-to-spacewalk-schema-1.8 spacewalk-schema-1.8-to-spacewalk-schema-1.9 spacewalk-schema-1.9-to-spacewalk-schema-1.10)

for d in ${dirs[*]}; do
    for i in $basepath/$d/*; do
    	fname=`basename $i`
	if [ "$fname" == "README" ]; then
            continue
        fi
        if [ "$d" == "spacewalk-schema-1.7-to-spacewalk-schema-1.8" ]; then
            num=`echo "$fname" | sed 's/^\([0-9]\+\)\-.*$/\1/'`
	    if [ $num -lt 114 ]; then
                continue
            fi
	fi
	new_path
    	if [ -z "$dest" ]; then
	    continue
    	fi
	if [ -e "$dest" ]; then
	    src=`sha1sum $i| awk '{print $1}'`
            target=`sha1sum $dest| awk '{print $1}'`
            #echo "SRC: $src TARGET: $target"
            if [ "$src" != "$target" ]; then
                if ! grep "already applied in Manager" "$dest" > /dev/null; then
                    templatename=`basename $dest`
                    if [ -e "$templatedir/$templatename.dif" ]; then
                        DIFF1=`diff -ub $i $dest`
                        DIFF2=`cat "$templatedir/$templatename.dif"`
                        if [ "$DIFF1" = "$DIFF2" ]; then
                            continue
                        fi
                    fi
		    echo "NEED CHECK: diff -ub $i $dest"
                fi
	    fi
	    continue
	fi

    	echo ">cp $i $dest"
    	#cp $i $dest
    done
    incr
done

