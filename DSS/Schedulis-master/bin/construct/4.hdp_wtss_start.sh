exec_hosts=$1
start_type=$2

result_check(){
  if [ "$?" -ne "0" ];then
    echo $1
    exit 1
  fi;
}

#joint hostname which is used to start process
#拼接hostname用于启动进程
v_hosts=${exec_hosts//,/ }
target_hosts=""
for node in $v_hosts
do
    ip=`echo $node | cut -d : -f 1`
    target_hosts+=$ip","
done

echo ${target_hosts}

if [ "$start_type" == "exec" ];then
	#start wtss exec node
	#启动wtss exec 节点
	/usr/bin/ansible -i ${target_hosts}  all -m shell -a "cd /appcom/Install/AzkabanInstall/wtss-exec;nohup sh bin/start-exec.sh &"
	result_check "Start wtss exec failed! For more detail,seeing log files in directory /appcom/logs/azkaban "
else
	#start wtss web node
	#启动wtss web节点
	/usr/bin/ansible -i ${target_hosts} all -m shell -a "cd /appcom/Install/AzkabanInstall/wtss-web;nohup sh bin/start-web.sh &"
	result_check "Start wtss web failed! For more detail,seeing log files in directory /appcom/logs/azkaban "
fi

echo "start wtss process success!"
