#!/bin/bash

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -ge 1 ] || die "Usage ./s3_script_runner.sh [script-path] [param 1] [param 2] [param 3] [..] (eg. ./s3_script_runner.sh s3://scripts-bucket/my_script.sh param-1 param-2 param-3 ..)"

set -x

SCRIPT_NAME=$0

S3_SCRIPT_PATH=$1


S3_SCRIPT="script_to_run.sh"

s3cmd get --force ${S3_SCRIPT_PATH} ${S3_SCRIPT}
[ $? -eq 0 ] && [ -s $S3_SCRIPT ] || die "Error getting script ${S3_SCRIPT_PATH} from S3"

chmod +x script_to_run.sh

# shift the first argument and pass the rest to the bash script

shift
./${S3_SCRIPT} "$@"
[ $? -eq 0 ] || die "Error running script ${S3_SCRIPT}."

exit 0


