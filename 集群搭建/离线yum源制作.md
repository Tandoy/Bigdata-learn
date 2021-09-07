### 离线yum源制作
#### 前提
```text
yum仓库可以支持三种途径提供给yum在安装的时候下载rpm包
第一种: ftp服务 ftp://
第二种: http服务 http://
第三种: 本地文件 file://
```
#### 步骤(采用本地文件方式)
1.首先在可以联网的服务器上使用下面的命令离线下载需要的包和依赖，下载完成后存放在/home/appuser/tangzhi/packages/目录下
```shell script
yumdownloader --resolve --destdir=/home/appuser/tangzhi/packages/ gcc gcc-c++ make binutils compat-libstdc++-33 glibc glibc-devel libaio libaio-devel libgcc libstdc++ libstdc++-devel unixODBC unixODBC-devel sysstat ksh
```
2.安装createrepo：创建yum仓库
```shell script
sudo yum -y install createrepo
```
3.执行如下命令安装
```shell script
cd /home/appuser/tangzhi/packages/
sudo rpm -iv *
```
4.使用下载的安装包和依赖，创建yum仓库
```shell script
createrepo -v /home/appuser/tangzhi/packages/
```
5.压缩创建好的仓库，从而方便传输
```shell script
cd /home/appuser/tangzhi/
tar -zcvf packages.tar.gz /home/appuser/tangzhi/packages/
```
6.配置离线yum源
```shell script
tar -zxzf packages.tar.gz
```
7.备份/etc/yum.repos.d/下的所有.repo
```shell script
##创建备份文件夹
mkdir -p /etc/yum.repos.d/repo.bak
##转移文件
mv *.repo repo.bak
```
8.制作yum源.repo，指定yum源位置
```shell script
vi /etc/yum.repos.d/packages.repo
```
9.添加
```shell script
#additional packages that extend functionality of existing packages
[packages]
name=CentOS-7 - Plus
baseurl=file:///home/appuser/tangzhi/packages
gpgcheck=0
enabled=0
#gpgkey=file:///etc/pki/rpm-gpg/RPM-GPG-KEY-EPEL-7
```
```shell script
yum clean all
yum makecache
```
10.查询
```shell script
yum list | grep packages
```
11.测试安装
```shell script
yum install -y 包名称
```