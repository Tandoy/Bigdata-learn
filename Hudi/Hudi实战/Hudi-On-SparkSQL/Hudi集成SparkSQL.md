##Hudi集成SparkSQL

###1.环境准备

    首先需要将[HUDI-1659](https://github.com/apache/hudi/pull/2645)拉取到本地打包，生成SPARK_BUNDLE_JAR(hudi-spark-bundle_2.11-0.9.0-SNAPSHOT.jar)包
        git remote add upstream https://github.com/apache/hudi
        git fetch upstream pull/2645/head:2645
        
        如果想保存这个 PR 到自己到 Fork 里：
        git push origin pr2645