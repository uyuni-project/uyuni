#! /bin/bash

# PUT YOUR PREFIX here
PREFIX="dma-"

export RH_MINION="${PREFIX}mincentos7.tf.local"
export MINION="${PREFIX}minsles12sp1.tf.local"
export TESTHOST=${PREFIX}suma3pg.tf.local
export CLIENT=${PREFIX}clisles12sp1.tf.local
export BROWSER=phantomjs
rake
