wtss_jdbc_ip=$2
wtss_jdbc_port=$3
wtss_jdbc_db=$4
wtss_jdbc_username=$5
wtss_jdbc_passwd=$(echo $6 |base64 -d)
sqlscript_version=$1

result_check(){
  if [ "$?" -ne "0" ];then
    echo $1
    exit 1
  fi;
}

#check mysql connection
#校验mysql连接的正确性
mysql -h $wtss_jdbc_ip -u $wtss_jdbc_username -P $wtss_jdbc_port -p$wtss_jdbc_passwd << EOF
exit
EOF

result_check "can not connect mysql connection,please check config!"

#make sure script exists
#确定脚本文件存在与否
if [ ! -f "hdp_wtss_deploy_script.sql" ];then
    echo "hdp_wtss_deploy_script.sql not exists!"
    exit 1
fi;

#start init database
#开始初始化数据库
echo "start init database"
mysql -h $wtss_jdbc_ip -u $wtss_jdbc_username -P $wtss_jdbc_port -p$wtss_jdbc_passwd << EOF
CREATE DATABASE if not exists $wtss_jdbc_db DEFAULT CHARACTER SET utf8;
USE $wtss_jdbc_db;
EOF

result_check "init wtss database failed!"

#start init data
#初始化数据
echo "start init data"
mysql -h $wtss_jdbc_ip -u $wtss_jdbc_username -P $wtss_jdbc_port -p$wtss_jdbc_passwd $wtss_jdbc_db < hdp_wtss_deploy_script.sql

result_check "init wtss data failed!"

echo "init wtss database success!"
