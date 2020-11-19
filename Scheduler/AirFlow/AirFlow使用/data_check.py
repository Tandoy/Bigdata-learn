##sqoop导数前进行相关表的数据量检查
####主方法
import sys
import subprocess
def data_check(**kwargs):

    table = kwargs['table']
    sql = """
         select count(1) from wcl_dwh.{0}
    """.format(table)

   cmd = 'hive -e """' + sql.replace('"',"\'") + '"""'

   p =subprocess.Popen(cmd,shell=True,stdout=subprocess.PIPE)
   while p.poll() is None:
   if p.wait() is not 0:
   print("节点连接状态异常！！！")
   sys.exit(1)
   else:
     re = p.stdout.readlines()[0]
     if int(re) > 0
        print("数据量正常！")
        break
       else:
       print("数据量异常！！！")
       sys.exit(1)