1.links在部署时会检查当前服务器是否相关liunx命令,进行安装即可。

    say() {
        printf 'check command fail \n %s\n' "$1"
    }
    
    err() {
        say "$1" >&2
        exit 1
    }
    
    check_cmd() {
        command -v "$1" > /dev/null 2>&1
    }
    
    need_cmd() {
        if ! check_cmd "$1"; then
            err "need '$1' (your linux command not found)"
        fi
    }
    echo "<-----start to check used cmd---->"
    need_cmd yum
    need_cmd java
    need_cmd mysql
    need_cmd unzip
    need_cmd expect
    need_cmd telnet
    need_cmd tar
    need_cmd sed
    need_cmd dos2unix
    
2.在使用官方测试linkis连接案例报错用户密码错误

    默认账号密码都是部署时的liunx用户
    .setAuthTokenKey("appuser").setAuthTokenValue("appuser")
    
3.报错：用户已经连接，请先登出再登陆

    设置引擎时用户，例：EngineType$.MODULE$.HIVE()).setUser("appuser").build()

    
