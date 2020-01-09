# HbaseTimeRangeDelete
Delete data based on time range in hbase using hbase shell

DATE_LOG=`cat datelog.txt`
hbase_endDate=$(date -d "$DATE_LOG -6 day" +"%Y-%m-%d %H:%M:%S.%3N")
END_EPOCH_TIME=$(date -d "$hbase_endDate" '+%s%3N') 

HBASE_QUERY="scan "\'${hbaseTableName}\'",{COLUMNS => ['api:DateTime'], TIMERANGE => [0,${END_EPOCH_TIME}] }" 
GetRowKey=$(echo ${HBASE_QUERY} | hbase shell -n | awk '{print $1}')

GetRowKeyList=$(echo $GetRowKey | sed 's/ /,/g')
GetRowKeyList=$(echo ${GetRowKeyList} | sed -e "s/^/'/" -e "s/\$/'/" -e "s/,/','/g") 

deleteallCommand=""
for rowkey in $(echo $GetRowKeyList | tr ',' '\n')
do
 deleteCommand=`echo "deleteall "\'${hbaseTableName}\'",${rowkey}"`
 deleteallCommand+="$deleteCommand "$'\n'
done

fnbulkdelete(){
exec hbase shell -n <<EOF 
$deleteallCommand
EOF
}

fnbulkdelete
