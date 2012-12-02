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

basepath=`pwd`
basepath=`dirname $basepath`
basepath="$basepath/upgrade/"
echo "P: $basepath"

dirs=(spacewalk-schema-1.7-to-spacewalk-schema-1.8 spacewalk-schema-1.8-to-spacewalk-schema-1.9)

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
		echo "NEED CHECK: $i $dest"
	    fi
	    continue
	fi

    	echo ">cp $i $dest"
    	#cp $i $dest
    done
    incr
done

