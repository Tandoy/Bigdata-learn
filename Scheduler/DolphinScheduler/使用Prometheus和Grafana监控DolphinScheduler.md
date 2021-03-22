##使用Prometheus和Grafana监控DolphinScheduler

    1.通过 Prometheus 中 push gateway 的方式采集监控指标数据。
        需要借助 push gateway 一起，然后将数据发送到 push gateway 地址中，比如地址为 http://10.25x.xx.xx:8085，那么就可以写一个 shell 脚本，通过 crontab 调度或者 DolphinScheduler 调度，定期运行 shell 脚本，来发送指标数据到Prometheus中。
        
        1.1 失败任务数脚本：
        
        #!/bin/bash
        failedTaskCounts=`mysql -h 10.25x.xx.xx -u username -ppassword -e "select 'failed' as failTotal ,count(distinct(process_definition_id))
        as failCounts from dolphinscheduler.t_ds_process_instance where state=6 and start_time>='${datetimestr} 00:00:00'" |grep "failed"|awk -F " " '{print $2}'`
        echo "failedTaskCounts:${failedTaskCounts}"
        job_name="Scheduling_system"
        instance_name="dolphinscheduler"
        cat <<EOF | curl --data-binary @- http://10.25x.xx.xx:8085/metrics/job/$job_name/instance/$instance_name
        failedSchedulingTaskCounts $failedTaskCounts
        EOF
        
        这段脚本中 failedSchedulingTaskCounts 就是定义的 Prometheus 中的一个指标。脚本通过 sql 语句查询出失败的任务数，然后发送到 Prometheus 中。
        
        1.2 然后在 Grafana 中就可以选择数据源为 Prometheus，并且选择对应的指标。
        
        1.3 脚本实例：
        
            1.3.1 正在运行的任务数
            
            #!/bin/bash
            runningTaskCounts=`mysql -h 10.25x.xx.xx -u username -ppassword -e "select 'running' as runTotal ,count(distinct(process_definition_id))  as runCounts from dolphinscheduler.t_ds_process_instance where state=1" |grep "running"|awk -F " " '{print $2}'`
            echo "runningTaskCounts:${runningTaskCounts}"
            job_name="Scheduling_system"
            
            instance_name="dolphinscheduler"
            if [ "${runningTaskCounts}yy" == "yy" ];then
            runningTaskCounts=0
            fi
            cat <<EOF | curl --data-binary @- http://10.25x.xx.xx:8085/metrics/job/$job_name/instance/$instance_name
            runningSchedulingTaskCounts $runningTaskCounts
            EOF
            
            1.3.2 失败的工作流实例数
            
            #!/bin/bash
            failedInstnceCounts=`mysql -h 10.25x.xx.xx -u username-ppassword -e "select 'failed' as failTotal ,count(1) as failCounts from dolphinscheduler.t_ds_process_instance where state=6 and start_time>='${datetimestr} 00:00:00'" |grep "failed"|awk -F " " '{print $2}'`
            echo "failedInstnceCounts:${failedInstnceCounts}"
            job_name="Scheduling_system"
            instance_name="dolphinscheduler"
            cat <<EOF | curl --data-binary @- http://10.25x.xx.xx:8085/metrics/job/$job_name/instance/$instance_name
            failedSchedulingInstanceCounts $failedInstnceCounts
            EOF
            
            1.3.3 等待中的工作任务流数
            
            #!/bin/bash
            waittingTaskCounts=`mysql -h 10.25x.xx.xx -u username -ppassword -e "select 'waitting' as waitTotal ,count(distinct(process_definition_id)) as waitCounts from dolphinscheduler.t_ds_process_instance where state in(10,11) and start_time>='${sevenDayAgo} 00:00:00'" |grep "waitting"|awk -F " " '{print $2}'`
            echo "waittingTaskCounts:${waittingTaskCounts}"
            job_name="Scheduling_system"
            instance_name="dolphinscheduler"
            cat <<EOF | curl --data-binary @- http://10.25x.xx.xx:8085/metrics/job/$job_name/instance/$instance_name
            waittingSchedulingTaskCounts $waittingTaskCounts
            EOF
            
            1.3.4 运行中的工作流实例数
            
            #!/bin/bash
            runningInstnceCounts=`mysql -h 10.25x.xx.xx -u username -ppassword -e "select 'running' as runTotal ,count(1)  as runCounts from dolphinscheduler.t_ds_process_instance where state=1" |grep "running"|awk -F " " '{print $2}'`
            echo "runningInstnceCounts:${runningInstnceCounts}"
            job_name="Scheduling_system"
            instance_name="dolphinscheduler"
            if [ "${runningInstnceCounts}yy" == "yy" ];then
            runningInstnceCounts=0
            fi
            cat <<EOF | curl --data-binary @- http://10.25x.xx.xx:8085/metrics/job/$job_name/instance/$instance_name
            runningSchedulingInstnceCounts $runningInstnceCounts
            EOF
            
            
    2. 通过 Grafana 直接查询 DolphinScheduler自身的 Mysql 数据库（也可以是别的数据库）
    
        2.1 首先需要在 Grafana 中定义一个数据源，这个数据源就是 DolphinScheduler 自身的 Mysql 数据库。
        
        2.2 在grafana中实例sql：
        
            统计本周以及当日正在运行的调度任务的情况：
            
            select d.*,ifnull(f.today_runCount,0) as today_runCount,ifnull(e.today_faildCount,0) as today_faildCount,ifnull(f.today_avg_timeCosts,0) as today_avg_timeCosts,ifnull(f.today_max_timeCosts,0) as today_max_timeCosts,
            ifnull(g.week_runCount,0) as week_runCount,ifnull(h.week_faildCount,0) as week_faildCount,ifnull(g.week_avg_timeCosts,0) as week_avg_timeCosts,ifnull(g.week_max_timeCosts,0) as week_max_timeCosts from
            (select a.id,c.name as project_name,a.name as process_name,b.user_name,a.create_time,a.update_time from t_ds_process_definition a,t_ds_user b, t_ds_project c  where a.user_id=b.id and c.id=a.project_id and a.release_state=$status) d
            left join
            (select count(1) as today_faildCount,process_definition_id from
            t_ds_process_instance where state=6 and start_time>=DATE_FORMAT(NOW(),'%Y-%m-%d 00:00:00') and  start_time<=DATE_FORMAT(NOW(),'%Y-%m-%d 23:59:59') group by process_definition_id ) e  on d.id=e.process_definition_id
            left join 
            (select count(1) as today_runCount,avg(UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time)) as today_avg_timeCosts,max(UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time)) as today_max_timeCosts,process_definition_id from
            t_ds_process_instance  where start_time>=DATE_FORMAT(NOW(),'%Y-%m-%d 00:00:00') and  start_time<=DATE_FORMAT(NOW(),'%Y-%m-%d 23:59:59') group by process_definition_id ) f on d.id=f.process_definition_id
            left join
            (select count(1) as week_runCount,avg(UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time)) as week_avg_timeCosts,max(UNIX_TIMESTAMP(end_time)-UNIX_TIMESTAMP(start_time)) as week_max_timeCosts,process_definition_id from
            t_ds_process_instance  where start_time>=DATE_FORMAT(SUBDATE(CURDATE(),DATE_FORMAT(CURDATE(),'%w')-1), '%Y-%m-%d 00:00:00') and  start_time<=DATE_FORMAT(SUBDATE(CURDATE(),DATE_FORMAT(CURDATE(),'%w')-7), '%Y-%m-%d 23:59:59') group by process_definition_id ) g
            on d.id=g.process_definition_id  left join 
            (select count(1) as week_faildCount,process_definition_id from
            t_ds_process_instance where state=6 and start_time>=DATE_FORMAT(SUBDATE(CURDATE(),DATE_FORMAT(CURDATE(),'%w')-1), '%Y-%m-%d 00:00:00')  and  start_time<=DATE_FORMAT( SUBDATE(CURDATE(),DATE_FORMAT(CURDATE(),'%w')-7), '%Y-%m-%d 23:59:59') group by process_definition_id ) h
            on d.id=h.process_definition_id 
            
            任务耗时：
            
            select (UNIX_TIMESTAMP(a.end_time)-UNIX_TIMESTAMP(a.start_time)) as timeCosts, UNIX_TIMESTAMP(a.end_time) as time   from t_ds_process_instance a,t_ds_process_definition b where end_time>=DATE_FORMAT( DATE_SUB(CURDATE(), INTERVAL 1 MONTH), '%Y-%m-01 00:00:00')  and end_time is not null and a.process_definition_id=b.id and b.name='$process_name'
