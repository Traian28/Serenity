#!/bin/bash

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -eq 7 ] || die "Usage ./redshift_ad_events_load_data.sh [game-id] [app-env] [category] [year] [month] [day] [hour] (eg. ./redshift_load_data.sh pogs stage ad 2013 10 07 15)"

set -x

SCRIPT_NAME=$0

GAME_ID=$1
APP_ENV=$2
CATEGORY=$3
YEAR=$4
MONTH=$5
DAY=$6
HOUR=$7


DBHOST=tnt-serenity.cn69g5jqjqwj.us-east-1.redshift.amazonaws.com
DBPORT=5439
DBNAME=tntserenity
DBTABLE=${GAME_ID}_${CATEGORY}_events_${APP_ENV}
DBTABLE_STAGING=${DBTABLE}_staging
DBUSER=tntserenity
DBPASS=TNT4ever!
BUCKET=serenity-events-${APP_ENV}
S3PATH_IOS=s3://${BUCKET}/${GAME_ID}/ios/tnt/${CATEGORY}/csv/${YEAR}/${MONTH}/${DAY}/${HOUR}/
S3PATH_ANDROID=s3://${BUCKET}/${GAME_ID}/android/tnt/${CATEGORY}/csv/${YEAR}/${MONTH}/${DAY}/${HOUR}/
S3PATH_KINDLE=s3://${BUCKET}/${GAME_ID}/kindle/tnt/${CATEGORY}/csv/${YEAR}/${MONTH}/${DAY}/${HOUR}/

#S3PATH_IOS=s3://${BUCKET}/${GAME_ID}/apple/tnt/csv/2013/12/17/20/
#S3PATH_ANDROID=s3://${BUCKET}/${GAME_ID}/apple/tnt/csv/2013/12/17/20/
#S3PATH_KINDLE=s3://${BUCKET}/${GAME_ID}/apple/tnt/csv/2013/12/17/20/

AWS_ACCESS_KEY_ID=dummyKeyAccessF
AWS_SECRET_ACCESS_KEY=dummyKeySecretF

# Secure temp files
export PGPASSFILE=`mktemp /tmp/pass.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXX`
cmds=`mktemp /tmp/cmds.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXX`
logs=`mktemp /tmp/logs.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXX`

cat >$PGPASSFILE << EOF
$DBHOST:$DBPORT:$DBNAME:$DBUSER:$DBPASS
EOF

cat > $cmds << EOF
begin;
copy $DBTABLE from '$S3PATH_IOS' CREDENTIALS 'aws_access_key_id=$AWS_ACCESS_KEY_ID;aws_secret_access_key=$AWS_SECRET_ACCESS_KEY' removequotes emptyasnull blanksasnull maxerror 200 delimiter ',';
copy $DBTABLE from '$S3PATH_ANDROID' CREDENTIALS 'aws_access_key_id=$AWS_ACCESS_KEY_ID;aws_secret_access_key=$AWS_SECRET_ACCESS_KEY' removequotes emptyasnull blanksasnull maxerror 200 delimiter ',';
copy $DBTABLE from '$S3PATH_KINDLE' CREDENTIALS 'aws_access_key_id=$AWS_ACCESS_KEY_ID;aws_secret_access_key=$AWS_SECRET_ACCESS_KEY' removequotes emptyasnull blanksasnull maxerror 200 delimiter ',';
end;
EOF

#psql -d $DBNAME -h $DBHOST -p $DBPORT -U $DBUSER -f $cmds >$logs 2>&1

psql -d $DBNAME -h $DBHOST -p $DBPORT -U $DBUSER -f $cmds
