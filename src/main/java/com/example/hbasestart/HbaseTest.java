package com.example.hbasestart;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HbaseTest {

    public static Connection connection;
    public static Configuration configuration;
    static { 
        //获取配置
        configuration = HBaseConfiguration.create();
        //设置连接参数：HBase数据库使用的端口
        configuration.set("hbase.zookeeper.property.clientPort", "2181"); 
        //设置连接参数：HBase数据库所在的主机IP
        configuration.set("hbase.zookeeper.quorum", "192.168.2.31");
//        configuration.set("hbase.client.retries.number", "3");
//        configuration.set("hbase.rpc.timeout", "2000");
//        configuration.set("hbase.client.operation.timeout", "3000");
//        configuration.set("hbase.client.scanner.timeout.period", "10000");
        // configuration.addResource("hbase-site.xml");
        try {
      //取得一个数据库连接对象
      connection = ConnectionFactory.createConnection(configuration);
    } catch (IOException e) {
      e.printStackTrace();
    }
        
    } 
    public static void main(String[] args) throws Exception {
//      createTable("mytable", "CF1","CF2");
//      deleteTable("mytable");
//        insterRow("mytable");
        getData("mytable","101","CF1","sex");
    }

    /**
     * 创建表
     * @param tableName
     * @param cf1
     * @throws IOException
     */
    public static void createTable(String tableName,String... cf1) throws IOException { 
        Admin admin = connection.getAdmin();
        //（1）定义表名：HTD需要TableName类型的tableName，创建TableName类型的tableName
        TableName tbName = TableName.valueOf(tableName);
        //判断表述否已存在，不存在则创建表
        if(admin.tableExists(tbName)){
            System.err.println("表" + tableName + "已存在！");
            return;
        }
        //（2）创建表：通过HTableDescriptor创建一个HTableDescriptor将表的描述传到createTable参数中
        HTableDescriptor HTD = new HTableDescriptor(tbName);
        //（3）定义列族：为描述器添加表的详细参数
        for(String cf : cf1){
            //（4）执行建表操作： 创建HColumnDescriptor对象添加表的详细的描述
            HColumnDescriptor HCD =new HColumnDescriptor(cf);
            HTD.addFamily(HCD);
        }
        //（5）调用createtable方法创建表
        admin.createTable(HTD);
        System.out.println("创建成功");
        admin.close();
        connection.close();
    }

    /**
     * 删除表
     * @param tableName
     * @throws IOException
     */
    public static void deleteTable(String tableName) throws IOException {
        Admin admin = connection.getAdmin();
        TableName tableName1 = TableName.valueOf(tableName);
        if(admin.tableExists(tableName1)){
            //停用表
            admin.disableTable(tableName1);
            //获取并删除列族---只有一个列族不能删除！！！！？？
            List<byte[]> byteList = getRowName(tableName);
            for (byte[] b:byteList) {
                admin.deleteColumn(tableName1,b);
            }
            //删除表
            admin.disableTable(tableName1);
            System.out.println("删除表成功");
        }else {
            System.out.println("表不存在");
        }
        admin.close();
        connection.close();
    }

    /**
     * 插入数据
     */
    public static void insterRow(String tableName)
            throws IOException {
        Admin admin = connection.getAdmin();
        Table table = connection.getTable(TableName.valueOf(tableName));
        // 批量插入
        List<Put> putList = new ArrayList<Put>();
        Put put = new Put(Bytes.toBytes("101"));
        put.addColumn(Bytes.toBytes("name"), Bytes.toBytes("sex"), Bytes.toBytes(2));
        putList.add(put);
        put = new Put(Bytes.toBytes("103"));
        put.addColumn(Bytes.toBytes("name"), Bytes.toBytes("sex"), Bytes.toBytes(3));
        putList.add(put);
        table.put(putList);

        table.close();
        admin.close();
        connection.close();
    }

    /**
     * 根据rowkey查找数据
     * @param tableName
     * @param rowkey
     * @param colFamily
     * @param col
     * @throws IOException
     */
    public static void getData(String tableName, String rowkey, String colFamily, String col) throws IOException {
        Admin admin = connection.getAdmin();
        Table table = connection.getTable(TableName.valueOf(tableName));
        // 根据rowkey 查
        Get get = new Get(Bytes.toBytes(rowkey));
        // 追加查询条件：获取指定列族数据
        get.addFamily(Bytes.toBytes(colFamily));
        // 追加查询条件：获取指定列数据
        get.addColumn(Bytes.toBytes(colFamily),Bytes.toBytes(col));
        Result result = table.get(get);

        showCell(result);
        table.close();
        admin.close();
        connection.close();
    }

    // 格式化输出
    public static void showCell(Result result) {
        Cell[] cells = result.rawCells();
        for (Cell cell : cells) {
            System.out.println("RowName:" + new String(CellUtil.cloneRow(cell)) + " ");
            System.out.println("Timetamp:" + cell.getTimestamp() + " ");
            System.out.println("column Family:" + new String(CellUtil.cloneFamily(cell)) + " ");
            System.out.println("row Name:" + new String(CellUtil.cloneQualifier(cell)) + " ");
            System.out.println("value:" + new String(CellUtil.cloneValue(cell)) + " ");
        }
    }

    /**
     * 获取表列族
     * @param tableName
     * @throws IOException
     */
    public static List<byte[]>  getRowName(String tableName)throws IOException{
        Table table=connection.getTable(TableName.valueOf(tableName));
        List<String> list=new ArrayList<>();
        HTableDescriptor hTableDescriptor=table.getTableDescriptor();
        List<byte[]> byteList = new ArrayList<>();
        for(HColumnDescriptor fdescriptor : hTableDescriptor.getColumnFamilies()){
            list.add(fdescriptor.getNameAsString());
        }
        System.out.println("该表列族有："+ Arrays.toString(list.toArray()));
        for(int i=0;i<list.size();i++) {
            byte[] bytes = list.get(i).getBytes("UTF-8");
            byteList.add(bytes);
        }
        return byteList;
    }

}