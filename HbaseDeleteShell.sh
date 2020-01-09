# HbaseTimeRangeDelete
#Delete data based on time range in hbase using hbase shell

#Date calculations based on requirements
DATE_LOG=`cat datelog.txt`
hbase_endDate=$(date -d "$DATE_LOG -6 day" +"%Y-%m-%d %H:%M:%S.%3N")
END_EPOCH_TIME=$(date -d "$hbase_endDate" '+%s%3N') 

#Create hbase scan query from date and fetch the rowkey
HBASE_QUERY="scan "\'${hbaseTableName}\'",{COLUMNS => ['api:columFamily'], TIMERANGE => [0,${END_EPOCH_TIME}] }" 
GetRowKey=$(echo ${HBASE_QUERY} | hbase shell -n | awk '{print $1}')

#Create rowkey as list of string
GetRowKeyList=$(echo $GetRowKey | sed 's/ /,/g')
GetRowKeyList=$(echo ${GetRowKeyList} | sed -e "s/^/'/" -e "s/\$/'/" -e "s/,/','/g") 

#Creat deleteall command for each rowkey
deleteallCommand=""
for rowkey in $(echo $GetRowKeyList | tr ',' '\n')
do
 deleteCommand=`echo "deleteall "\'${hbaseTableName}\'",${rowkey}"`
 deleteallCommand+="$deleteCommand "$'\n'
done

#Submit the deleteall commands to hbase shell
fnbulkdelete(){
exec hbase shell -n <<EOF 
$deleteallCommand
EOF
}

fnbulkdelete