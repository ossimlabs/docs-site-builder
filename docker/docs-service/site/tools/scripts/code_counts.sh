#!/bin/sh

CODE_DIR=/c/projects/blueground
STAT_FILE=code_stats.txt

java_code_count=`find ${CODE_DIR} -name '*.java' -exec cat {} \; | grep -v "^[ 	]*$" | grep -v "^[ 	]*\/\/" | grep -v "^[ 	]*\/\*" | grep -v "^[ 	]*\*" | wc -l`
scala_code_count=`find ${CODE_DIR} -name '*.scala' -exec cat {} \; | grep -v "^[ 	]*$" | grep -v "^[ 	]*\/\/" | grep -v "^[ 	]*\/\*" | grep -v "^[ 	]*\*" | wc -l`
xml_code_count=`find ${CODE_DIR} -name '*.xml' -exec cat {} \; | grep -v "^[ 	]*$" | grep -v "^[ 	]*\/\/" | grep -v "^[ 	]*\/\*" | grep -v "^[ 	]*\*" | grep -v "^[ 	]*<\!" | wc -l`
javascript_code_count=`find ${CODE_DIR} -name '*.js' -exec cat {} \; | grep -v "^[ 	]*$" | grep -v "^[ 	]*\/\/" | grep -v "^[ 	]*\/\*" | grep -v "^[ 	]*\*" | wc -l`
properties_code_count=`find ${CODE_DIR} -name '*.properties' -exec cat {} \; | grep -v "^[ 	]*$" | grep -v "^[ 	]*\/\/" | grep -v "^[ 	]*\/\*" | grep -v "^[ 	]*\*" | wc -l`

echo "JAVA: $java_code_count" > $STAT_FILE
echo "SCALA: $scala_code_count" >> $STAT_FILE
echo "XML: $xml_code_count" >> $STAT_FILE
echo "JAVASCRIPT: $javascript_code_count" >> $STAT_FILE
echo "PROPERTIES: $properties_code_count" >> $STAT_FILE
