#Delete data based on time range in hbase using scala
import org.apache.spark.sql.SparkSession

import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Delete, Scan, Result, BufferedMutator}
import org.apache.hadoop.hbase.client.ResultScanner
import org.apache.hadoop.hbase.filter.KeyOnlyFilter

import java.io.File
import java.io.IOException
import scala.collection.JavaConversions._

object truncateHbase {

  def main(args: Array[String]): Unit = {
    if (args.length != 3) {
      println("Invalid Ivocation")
      System.exit(1)
    }
    println("Arguments Passed: " + args)

    //Create SparkSession for cluster mode
    //val spark = SparkSession.builder.appName("truncateHbase").getOrCreate()
    var spark = SparkSession.builder.getOrCreate()

    val Array(hbaseTableName, startTime, endTime) = args
    val hbaseConfPath :String = "/etc/hbase/conf/hbase-site.xml"

    //Create hbase connection
    val conf = HBaseConfiguration.create()
    conf.addResource(new File(hbaseConfPath).toURI.toURL)
    val connection: Connection = ConnectionFactory.createConnection(conf)
    val table = connection.getTable(TableName.valueOf(hbaseTableName))

    println("Hbase Config Passed")
    val mutator: BufferedMutator = connection.getBufferedMutator(TableName.valueOf(hbaseTableName))

    val scans = new Scan()
    val start = startTime.toLong
    val end = endTime.toLong
    scans.setTimeRange(start, end)

    println("Parms setup Completed")

    scans.setFilter(new KeyOnlyFilter())
    scans.setCaching(1000)
    scans.setBatch(1000)
    scans.setCacheBlocks(false)

    val rs: ResultScanner = table.getScanner(scans)

    try {
      for (result: Result <- rs) {
        val delete = new Delete(result.getRow)
        mutator.mutate(delete)
      }
      mutator.flush()
      mutator.close()
      table.close()
    }
  catch
      {case e: IOException => e.printStackTrace
      }
  }
}

