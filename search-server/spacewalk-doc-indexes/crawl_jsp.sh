#!/bin/bash

if [ "${JAVA_HOME}" = "" ]; then
    export JAVA_HOME=/usr/lib/jvm/java
    echo "Setting JAVA_HOME to: ${JAVA_HOME}"
fi

export NUTCH_HOME=/usr/share/nutch-core

python create_urls_per_language.py --docs-dir $1

for lang in `ls ./conf`; do
    if [ ${lang} = "." ]; then
       echo;
    else
        echo "Looking at ${lang}"
        export URLS_DIR=`pwd`/conf/${lang}/urls
        export NUTCH_CONF_DIR=`pwd`/conf/${lang}/nutch_conf
        export NUTCH_OPTS=
        export NUTCH_LOG_DIR=`pwd`/logs/${lang}
        export OUTPUT_DIR=`pwd`/crawl_output/${lang}

        echo "NUTCH_HOME = ${NUTCH_HOME}"
        echo "NUTCH_CONF_DIR = ${NUTCH_CONF_DIR}"
        echo "NUTCH_OPTS = ${NUTCH_OPTS}"
        echo "NUTCH_LOG_DIR = ${NUTCH_LOG_DIR}"
        echo "OUTPUT_DIR = ${OUTPUT_DIR}"

        if [ ! -d ${OUTPUT_DIR} ]; then
            echo "Creating output directory ${OUTPUT_DIR}"
            mkdir -p ${OUTPUT_DIR}
        fi

        if [ ! -d ${NUTCH_LOG_DIR} ]; then
            echo "Creating output directory ${NUTCH_LOG_DIR}"
            mkdir -p ${NUTCH_LOG_DIR}
        fi

        if [ ! -d ${NUTCH_LOG_DIR} ]; then
            echo "Creating output directory ${NUTCH_LOG_DIR}"
            mkdir -p ${NUTCH_LOG_DIR}
        fi


        ${NUTCH_HOME}/bin/nutch crawl ${URLS_DIR} -dir ${OUTPUT_DIR} 2>&1 | tee ${NUTCH_LOG_DIR}/crawl.log
    fi
done
