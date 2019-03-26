# LabWork 4
## Tutorial

* If your cluster doesn’t have the requisite software you will need to install it : 

```
$ sudo apt-get install ssh
$ sudo apt-get install rsync
```

* Edit the file etc/hadoop/hadoop-env.sh to define some parameters as follows

```
# set to the root of your Java installation
export JAVA_HOME=/usr/java/latest
```

* Compile WordCount.java and create a jar

```
$ bin/hadoop com.sun.tools.javac.Main WordCount.java
$ jar cf wc.jar WordCount*.class
```
* make dir input 

```
$ bin/hadoop fs -mkdir input
```

* Run the application

```
$ bin/hadoop jar wc.jar WordCount input output

```


