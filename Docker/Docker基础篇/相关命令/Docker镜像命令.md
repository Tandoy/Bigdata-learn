## Docker镜像命令

    1.列出本地主机上的镜像
        docker images
            -a :列出本地所有的镜像（含中间映像层）
            -q :只显示镜像ID。
            --digests :显示镜像的摘要信息
            --no-trunc :显示完整的镜像信息
            
    2.搜索某个特定镜像
        docker search 某个XXX镜像名字
            --no-trunc : 显示完整的镜像描述
            -s : 列出收藏数不小于指定值的镜像。
            --automated : 只列出 automated build类型的镜像；
            
    3.下载镜像
        docker pull 某个XXX镜像名字   --默认拉取最新版
            docker pull 镜像名字[:TAG]
     
    4.删除镜像
        docker rmi 某个XXX镜像名字ID
            删除单个
            	docker rmi  -f 镜像ID
            删除多个
            	docker rmi -f 镜像名1:TAG 镜像名2:TAG 
            删除全部
            	docker rmi -f $(docker images -qa)