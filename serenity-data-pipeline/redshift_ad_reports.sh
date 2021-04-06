#!/bin/bash

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -ge 5 ] || die "Usage ./redshift_ad_reports.sh [game-id] [app-env] [year] [month] [day] [hour] (eg. ./redshift_ad_reports.sh pogs stage 2013 10 07 15)"

set -x

SCRIPT_NAME=$0

GAME_ID=$1
APP_ENV=$2
YEAR=$3
MONTH=$4
DAY=$5

HOURLY=0

if [ "$#" -eq 5 ]; then
	echo "Generating daily ad reports for $@\n"
else
	HOUR=$6
	echo "Generating hourly ad reports for $@\n"
	HOURLY=1
fi


DBHOST=tnt-serenity.cn69g5jqjqwj.us-east-1.redshift.amazonaws.com
DBPORT=5439
DBNAME=tntserenity
CATEGORY=ad
DBTABLE=${GAME_ID}_${CATEGORY}_events_${APP_ENV}
DBUSER=tntserenity
DBPASS=TNT4ever!
BUCKET=serenity-reports-${APP_ENV}
AD_EVENTS_COUNT_REPORT_NAME="ad_events_count"

if [ $HOURLY -eq 1 ]; then
	S3PATH_AD_COUNTS_REPORT=s3://${BUCKET}/${GAME_ID}/${CATEGORY}/${YEAR}/${MONTH}/${DAY}/${HOUR}/${AD_EVENTS_COUNT_REPORT_NAME}_
	QUERY_HOUR=$HOUR:00:00
	QUERY_INTERVAL_SECS=3600
else
	S3PATH_AD_COUNTS_REPORT=s3://${BUCKET}/${GAME_ID}/${CATEGORY}/${YEAR}/${MONTH}/${DAY}/${AD_EVENTS_COUNT_REPORT_NAME}_daily_
	QUERY_HOUR=00:00:00
	QUERY_INTERVAL_SECS=86400
fi


AWS_ACCESS_KEY_ID=dummyKeyAccessF
AWS_SECRET_ACCESS_KEY=dummyKeySecretF

# Secure temp files
export PGPASSFILE=`mktemp /tmp/pass.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXXXX`
cmds=`mktemp /tmp/cmds.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXXXX`
logs=`mktemp /tmp/logs.${GAME_ID}.${APP_ENV}.${CATEGORY}.${YEAR}.${MONTH}.${DAY}.${HOUR}.XXXXXXXXXX`

cat >$PGPASSFILE << EOF
$DBHOST:$DBPORT:$DBNAME:$DBUSER:$DBPASS
EOF

cat > $cmds << EOF
unload ('select platform, adprovider, adtype, name as event_name, count(*) as count from
(select platform, userId, sessionId, name, adtype, adprovider
from $DBTABLE where ((timestamp >= extract(epoch from timestamp \'$YEAR-$MONTH-$DAY $QUERY_HOUR UTC\')::bigint * 1000) 
and (timestamp <  (extract(epoch from timestamp \'$YEAR-$MONTH-$DAY $QUERY_HOUR UTC\')::bigint + $QUERY_INTERVAL_SECS) * 1000))
group by platform, userId, sessionId, name, adtype, adprovider)
group by platform, adprovider, adtype, event_name') 
to '$S3PATH_AD_COUNTS_REPORT' CREDENTIALS 'aws_access_key_id=$AWS_ACCESS_KEY_ID;aws_secret_access_key=$AWS_SECRET_ACCESS_KEY' delimiter ',' ALLOWOVERWRITE manifest;
EOF

cat $cmds

#psql -d $DBNAME -h $DBHOST -p $DBPORT -U $DBUSER -f $cmds >$logs 2>&1

psql -d $DBNAME -h $DBHOST -p $DBPORT -U $DBUSER -f $cmds
