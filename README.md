# HBase-start
大数据学习-之-HBase模块（在虚拟机单机模式搭建遇到的问题）


## 官网：https://hbase.apache.org/book.html
## 参考链接： 
  * https://github.com/17661977890/interview-docs/blob/master/docs/hadoop/hbase01.md
  * https://github.com/17661977890/technology-talk/blob/master/middle-software/Hbase.md

## hbase shell 常用命令以及数据模型：
  * http://c.biancheng.net/view/3587.html  https://cloud.tencent.com/developer/article/1336648
  * http://c.biancheng.net/view/3586.html
  
  * 貌似没有修改列族名的命令，而且删除列族，如果该表只有一个列族是不能删的。

#### 单机搭建遇到的问题及解决方案：

* (1) 启动弹出警告 jvm问题：

```bash
[root@sun hbase-1.4.10]# ./bin/start-hbase.sh
OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N
running master, logging to /home/admin/bd/hbase/hbase-1.4.10/bin/../logs/hbase-admin-master-sun.com.out
OpenJDK 64-Bit Server VM warning: ignoring option PermSize=128m; support was removed in 8.0
OpenJDK 64-Bit Server VM warning: ignoring option MaxPermSize=128m; support was removed in 8.0
OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N

# 解决方案：
# 1、因为我是用的虚拟机，所以我将虚拟机的的处理器数量修改成了2  以前是1 （处理器 内核数），就解决了第一个警告了
# 2、中间几个警告：需要我们取hbase-env.sh 文件中进行修改，如下：
原文是这样的：
# Configure PermSize. Only needed in JDK7. You can safely remove it for JDK8+
export HBASE_MASTER_OPTS="$HBASE_MASTER_OPTS -XX:PermSize=128m -XX:MaxPermSize=128m -XX:ReservedCodeCacheSize=256m"
export HBASE_REGIONSERVER_OPTS="$HBASE_REGIONSERVER_OPTS -XX:PermSize=128m -XX:MaxPermSize=128m -XX:ReservedCodeCacheSize=256m"
可以看到，他是默认是jdk1.7的 如果你用的是jdk1.8 需要将下面的两个export 注释掉。
```

* (2) 启动成功以后，bin/hbase shell 进入shell命令窗口。输入status 报错：

```bash
hbase(main):001:0> status

ERROR: org.apache.hadoop.hbase.ipc.ServerNotRunningYetException: Server is not running yet
	at org.apache.hadoop.hbase.master.HMaster.checkServiceStarted(HMaster.java:2654)
	at org.apache.hadoop.hbase.master.MasterRpcServices.isMasterRunning(MasterRpcServices.java:980)
	at org.apache.hadoop.hbase.protobuf.generated.MasterProtos$MasterService$2.callBlockingMethod(MasterProtos.java:63372)
	at org.apache.hadoop.hbase.ipc.RpcServer.call(RpcServer.java:2380)
	at org.apache.hadoop.hbase.ipc.CallRunner.run(CallRunner.java:124)
	at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:297)
	at org.apache.hadoop.hbase.ipc.RpcExecutor$Handler.run(RpcExecutor.java:277)
  
# 解决方案：[root@sun hadoop]# hadoop dfsadmin -safemode leave 退出hadoop的安全模式，之前hadoop非正常关闭造成的。
```

* (3) 进入shell命令窗口，有jar包冲突问题：

```bash
[root@sun hbase-1.4.10]# bin/hbase shell
SLF4J: Class path contains multiple SLF4J bindings.
SLF4J: Found binding in [jar:file:/home/admin/bd/hbase/hbase-1.4.10/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: Found binding in [jar:file:/usr/local/hadoop/share/hadoop/common/lib/slf4j-log4j12-1.7.10.jar!/org/slf4j/impl/StaticLoggerBinder.class]
SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
HBase Shell
Use "help" to get list of supported commands.
Use "exit" to quit this interactive shell.
Version 1.4.10, r76ab087819fe82ccf6f531096e18ad1bed079651, Wed Jun  5 16:48:11 PDT 2019

# 解决方案： 网上百度，说是删除冲突的jar，我没操作，担心出问题还要备份，貌似不影响程序。
```
* (4) 启动成功以后，可以进入shell命令窗口，但是web ui 页面打不开，默认端口，配置端口都不行，未解决。。。
