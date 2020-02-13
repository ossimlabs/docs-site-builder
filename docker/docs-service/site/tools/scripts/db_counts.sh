#!/bin/sh

DATABASE=blueground
STAT_FILE=db_stats.txt
PASSWORD=blueground

PGPASSWORD=${PASSWORD} psql -Ublueground -f db_stats.sql -o ${STAT_FILE} ${DATABASE}
