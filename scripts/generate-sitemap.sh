#!/bin/bash

if [[ $# -eq 0 ]] ; then
    echo 'Please, specify the root path of the project!!!'
    exit 1
fi

echo 'Generating sitemap, please wait..'

ROOT=$1
FILE_NAME="sitemap.txt"
STRUTS_TEMP="sitemap-struts.txt"
SPARK_TEMP="sitemap-spark.txt"

# extract all routes handled by struts
grep -Pro --exclude=*sitemap* '<action.*path="([\w\d\/]*)"' $ROOT/* | grep -Po --exclude=*sitemap* '".*"' >> $STRUTS_TEMP
awk -i inplace '{ gsub("\"", "", $0); print $0}' $STRUTS_TEMP # remove all double quotes first
awk -i inplace '$0=$0"\.do"' $STRUTS_TEMP # add the .do suffix
awk -i inplace '!x[$0]++' $STRUTS_TEMP # deduplicate routes
sed -i 's/.*/"\/rhn&",/' $STRUTS_TEMP # add the "/rhn prefix and the suffix ",

# extract all routes handeld by spark
grep -Pro --exclude=*sitemap* '(?:(?:get|put|post|delete)\().*\"(/manager/.*)\"' $ROOT/* | grep -Po --exclude=*sitemap* '".*"' >> $SPARK_TEMP
awk -i inplace '{ gsub("\"", "", $0); print $0}' $SPARK_TEMP # remove all double quotes first
awk -i inplace '{ gsub(r"(:[^/]*)", r"([^/]+)", $0); print $0}' $SPARK_TEMP # replace all placeholder of the route (e.g.: '/../:id/../:name') with a regex pattern
awk -i inplace '!x[$0]++' $SPARK_TEMP # deduplicate routes
sed -i 's/.*/"\/rhn&",/' $SPARK_TEMP # add the "/rhn prefix and the suffix ",

cat $STRUTS_TEMP >> "$FILE_NAME.tmp"
cat $SPARK_TEMP >> "$FILE_NAME.tmp"

if [ -f "$FILE_NAME" ]; then
  mv "$FILE_NAME" "$FILE_NAME.bk"
fi

mv "$FILE_NAME.tmp" $FILE_NAME
rm $STRUTS_TEMP $SPARK_TEMP "$FILE_NAME.bk"
