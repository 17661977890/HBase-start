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

# 解决方案： 网上百度，说是删除冲突的jar，我没操作，担心出问题还要备份，貌似不影响程序。 看着不爽就到目录下 rm -f ...jar
```
* (4) 启动成功以后，可以进入shell命令窗口，但是web ui 页面打不开，默认端口，配置端口都不行，未解决。。。

* (5) 停止hbase时，报一个文件不存在：

```bash
stopping hbasecat: /tmp/hbase-root-master.pid: No such file or directory

# 默认情况下hbase的pid文件保存在/tmp目录下，/tmp目录下的文件很容易丢失，所以造成停止集群的时候出现上述错误。解决方式是在hbase-env.sh中修改pid文件  # 的存放路径，配置项如下所示：

# The directory where pid files are stored. /tmp by default.
export HBASE_PID_DIR=/var/hadoop/pids 
```
* （6）安装官网进行伪分布式搭建，jps显示进程了，但是等一会Hmaster没有了 问题： 并且没有在hdfs 创建hbase文件，web ui依然打不开
```bash 
#查看日志报错如下：貌似时zookeeper的问题。暂时这里不解决了，毕竟时运维干的事，我们主要增加了解。
java.net.ConnectException: 拒绝连接
	at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
	at sun.nio.ch.SocketChannelImpl.finishConnect(SocketChannelImpl.java:717)
	at org.apache.zookeeper.ClientCnxnSocketNIO.doTransport(ClientCnxnSocketNIO.java:361)
	at org.apache.zookeeper.ClientCnxn$SendThread.run(ClientCnxn.java:1141)
2019-07-22 11:45:30,197 ERROR [main] zookeeper.RecoverableZooKeeper: ZooKeeper create failed after 4 attempts
2019-07-22 11:45:31,197 INFO  [main-SendThread(sun.com:2181)] zookeeper.ClientCnxn: Opening socket connection to server sun.com/192.168.2.31:2181. Will not attempt to authenticate using SASL (unknown error)
2019-07-22 11:45:31,300 INFO  [main] zookeeper.ZooKeeper: Session: 0x0 closed
2019-07-22 11:45:31,300 ERROR [main] master.HMasterCommandLine: Master exiting
java.lang.RuntimeException: Failed construction of Master: class org.apache.hadoop.hbase.master.HMaster. 
	at org.apache.hadoop.hbase.master.HMaster.constructMaster(HMaster.java:2818)
	at org.apache.hadoop.hbase.master.HMasterCommandLine.startMaster(HMasterCommandLine.java:234)
	at org.apache.hadoop.hbase.master.HMasterCommandLine.run(HMasterCommandLine.java:138)
	at org.apache.hadoop.util.ToolRunner.run(ToolRunner.java:70)
	at org.apache.hadoop.hbase.util.ServerCommandLine.doMain(ServerCommandLine.java:127)
	at org.apache.hadoop.hbase.master.HMaster.main(HMaster.java:2828)
Caused by: org.apache.hadoop.hbase.ZooKeeperConnectionException: master:160000x0, quorum=sun.com:2181, baseZNode=/hbase Unexpected KeeperException creating base node
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher.createBaseZNodes(ZooKeeperWatcher.java:214)
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher.<init>(ZooKeeperWatcher.java:185)
	at org.apache.hadoop.hbase.regionserver.HRegionServer.<init>(HRegionServer.java:611)
	at org.apache.hadoop.hbase.master.HMaster.<init>(HMaster.java:449)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
	at org.apache.hadoop.hbase.master.HMaster.constructMaster(HMaster.java:2811)
	... 5 more
Caused by: org.apache.zookeeper.KeeperException$ConnectionLossException: KeeperErrorCode = ConnectionLoss for /hbase
	at org.apache.zookeeper.KeeperException.create(KeeperException.java:99)
	at org.apache.zookeeper.KeeperException.create(KeeperException.java:51)
	at org.apache.zookeeper.ZooKeeper.create(ZooKeeper.java:783)
	at org.apache.hadoop.hbase.zookeeper.RecoverableZooKeeper.createNonSequential(RecoverableZooKeeper.java:565)
	at org.apache.hadoop.hbase.zookeeper.RecoverableZooKeeper.create(RecoverableZooKeeper.java:544)
	at org.apache.hadoop.hbase.zookeeper.ZKUtil.createWithParents(ZKUtil.java:1218)
	at org.apache.hadoop.hbase.zookeeper.ZKUtil.createWithParents(ZKUtil.java:1196)
	at org.apache.hadoop.hbase.zookeeper.ZooKeeperWatcher.createBaseZNodes(ZooKeeperWatcher.java:201)
	... 13 more
2019-07-22 11:45:31,302 INFO  [main-EventThread] zookeeper.ClientCnxn: EventThread shut down for session: 0x0
```
