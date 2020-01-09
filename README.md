# HbaseTimeRangeDelete

When a Delete command is issued through the HBase client, data is not deleted instead a tombstone marker is set. The cells will get deleted during compaction happening periodically.

Delete data based on time range in hbase was achieved by hbase shell and scala (running both in cdh client and cluster mode).

