#! /bin/bash

nextnum=000
dest=""
sqlname=""

function incr () {
    num=`echo "$nextnum +1" | bc`
    printf -v nextnum "%03d" $num
}

function new_path () {

    if `echo -n "$fname" | grep -E 'sql\.postgresql$'>/dev/null`; then
        if [ "$fname" != "$sqlname.postgresql" ]; then
	    incr
	fi
    elif `echo -n "$fname" | grep -E 'sql\.oracle$'>/dev/null`; then
        incr
        sqlname=`echo "$fname" | sed 's/\.oracle$//'`
    elif `echo -n "$fname" | grep -E '\.sql$'>/dev/null`; then
        incr
	sqlname=""
    else
        dest=""
        return
    fi

    dest="$basepath/susemanager-schema-next/$nextnum-$fname"

}

basepath=`pwd`
basepath=`dirname $basepath`
basepath="$basepath/spacewalk/upgrade/"
echo "P: $basepath"

dirs=(spacewalk-schema-1.2-to-spacewalk-schema-1.3 spacewalk-schema-1.3-to-spacewalk-schema-1.4 spacewalk-schema-1.4-to-spacewalk-schema-1.5 spacewalk-schema-1.5-to-spacewalk-schema-1.6 spacewalk-schema-1.6-to-spacewalk-schema-1.7 spacewalk-schema-1.7-to-spacewalk-schema-1.8)

for d in ${dirs[*]}; do
    for i in $basepath/$d/*; do
    	fname=`basename $i`
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
done

