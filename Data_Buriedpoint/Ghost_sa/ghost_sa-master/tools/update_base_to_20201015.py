# -*- coding: utf-8 -*
# author: unknowwhite@outlook.com
# wechat: Ben_Xiaobai

# 该程序用来把老版本鬼策升级到支持用户分群的新版本。仅老用户在升级2020年10月15日之后的版本使用。如果新装就是2020年10月15日后的版本，无需运行此程序。
import sys
sys.path.append("./")
sys.setrecursionlimit(10000000)
from component.db_func import select_all_project
from component.db_op import do_tidb_exe

#!用于最早期版本升级到200210226版本。

def update():
    sql_alter_update = """ALTER TABLE `project_list` ADD COLUMN `enable_scheduler` int(4) NULL DEFAULT 1 COMMENT '是否启动定时器支持' AFTER `user_count`;"""
    do_tidb_exe(sql_alter_update)
    print('项目表已更新')
    sql_insert_status_code = """CREATE TABLE IF NOT EXISTS `status_code` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `desc` varchar(255) DEFAULT NULL COMMENT '含义',
    `p_id` int(11) DEFAULT NULL COMMENT '父id',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1;"""
    do_tidb_exe(sql_insert_status_code)
    print('状态码表创建完')
    status_codes = ["INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (1, '分群列表状态', 0);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (2, '创建列表开始', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (3, '分群信息写入中', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (4, '分群写入完成并包含错误', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (5, '分群写入完成', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (6, '分群写入失败', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (7, '生效策略', 0);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (8, '自动', 7);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (9, '手动', 7);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (10, '禁用', 7);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (11, '进入分群队列', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (12, '优先级', 0);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (13, '普通', 12);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (14, '高', 12);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (15, '最高', 12);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (16, '已添加任务队列', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (17, '任务已被选取', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (18, '任务方法加载完', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (19, '任务执行成功', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (20, '分群ETL失败', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (21, '任务执行失败', 1);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (22, '通知方式', 0);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (23, 'email', 22);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (24, '自动分群但不自动应用模板', 7);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (25, '推送状态', 0);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (26, '推送成功', 25);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (27, '推送失败', 25);","INSERT IGNORE INTO `events`.`status_code`(`id`, `desc`, `p_id`) VALUES (28, '自动分群自动应用模板但不自动发送', 7);"]
    for code in status_codes:
        do_tidb_exe(code)
    print('状态码添加完毕')
    sql_scheduler_jobs = """CREATE TABLE IF NOT EXISTS `scheduler_jobs` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '任务id',
    `project` varchar(255) DEFAULT NULL COMMENT '项目id',
    `group_id` int(11) DEFAULT NULL COMMENT 'group_plan的id',
    `list_index` int(11) DEFAULT NULL COMMENT 'group_index任务完成后，补充',
    `datetime` int(11) DEFAULT NULL COMMENT '执行的日期，即要执行的那个任务的时间（不是任务执行时间，是要执行的时间。如周三时执行周一的任务。也用来防止任务重复添加）',
    `data` json DEFAULT NULL COMMENT '其他附带的参数',
    `priority` int(4) DEFAULT NULL COMMENT '优先级',
    `status` int(4) DEFAULT NULL COMMENT '状态',
    `created_at` int(11) DEFAULT NULL COMMENT '创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `ind_task` (`project`,`group_id`,`datetime`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1;"""
    do_tidb_exe(sql_scheduler_jobs)
    print('任务计划表添加完毕')
    project_list,project_count = select_all_project()
    for project in project_list:
        insert_data = """CREATE TABLE IF NOT EXISTS `{project_name}_usergroup_data` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `group_list_id` int(11) DEFAULT NULL COMMENT '分群列表id',
    `data_index` int(11) DEFAULT NULL COMMENT '最新一组数据的index_id',
    `data_key` varchar(255) DEFAULT NULL COMMENT '数据的唯一识别id',
    `data_json` json DEFAULT NULL COMMENT '数据包',
    `enable` int(11) DEFAULT NULL COMMENT '生效策略。参考status_code，p_id=7',
    `created_at` int(11) DEFAULT NULL,
    `updated_at` int(11) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `group_list_id` (`group_list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1;""".format(project_name=project[0])
        do_tidb_exe(insert_data)
        insert_list = """CREATE TABLE IF NOT EXISTS `{project_name}_usergroup_list` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分群列表id',
    `group_id` int(11) DEFAULT NULL COMMENT '分群id',
    `group_list_index` int(11) DEFAULT NULL COMMENT '分群列表顺位',
    `list_init_date` int(11) DEFAULT NULL COMMENT '触发时间',
    `list_desc` varchar(255) DEFAULT NULL COMMENT '清单所描述的',
    `jobs_id` int(4) DEFAULT NULL COMMENT 'scheduler_jbos的id',
    `item_count` int(11) DEFAULT NULL COMMENT '分组条目数',
    `status` int(4) DEFAULT NULL COMMENT '分群状态。参考status_code,p_id=1',
    `complete_at` int(11) DEFAULT NULL COMMENT '分群完成时间',
    `apply_temple_times` int(2) DEFAULT 0 COMMENT '被套用模板的次数',
    `created_at` int(11) DEFAULT NULL COMMENT '条目创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '条目更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_key` (`group_id`,`group_list_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1;""".format(project_name=project[0])
        do_tidb_exe(insert_list)
        insert_plan = """CREATE TABLE IF NOT EXISTS `{project_name}_usergroup_plan` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '分群id',
    `group_title` varchar(255) DEFAULT NULL COMMENT '分群标题',
    `group_desc` varchar(255) DEFAULT NULL COMMENT '分群描述',
    `func` json DEFAULT NULL COMMENT '分群执行方法参考/scheduler_jobs/scheduler_job_creator.py',
    `latest_data_list_index` int(11) DEFAULT NULL COMMENT '最新一组数据的id',
    `repeatable` varchar(20) DEFAULT NULL COMMENT '定时器，分，时，日，月，周。不填的用*代替。跟crontab一个逻辑，不支持1-10的方式表达，多日的需要1,2,3,4,5,6,7,8这样的形式填',
    `priority` int(4) DEFAULT NULL COMMENT '任务执行优先级',
    `latest_data_time` int(11) DEFAULT NULL COMMENT '最新一组数据的完成时间',
    `repeat_times` int(11) DEFAULT 0 COMMENT '分群完成次数',
    `enable_policy` int(11) DEFAULT NULL COMMENT '生效策略。参考status_code，p_id=7',
    `latest_apply_temple_id` int(11) DEFAULT NULL COMMENT '最后一次执行的模板类型',
    `latest_apply_temple_time` int(11) DEFAULT NULL COMMENT '最后一次执行的模型时间',
    `created_at` int(11) DEFAULT NULL COMMENT '创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1;""".format(project_name=project[0]) 
        do_tidb_exe(insert_plan)
        print(project[0]+'的分群附加表表已添加完')
        do_tidb_exe(insert_list)
        insert_noti = """CREATE TABLE IF NOT EXISTS `{project_name}_noti` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `plan_id` int(11) DEFAULT NULL COMMENT '计划id',
    `list_id` int(11) DEFAULT NULL COMMENT '列表id',
    `data_id` int(11) DEFAULT NULL COMMENT '数据id',
    `temple_id` int(4) DEFAULT NULL COMMENT '模板id',
    `noti_group_id` int(11) DEFAULT NULL COMMENT '消息群组id',
    `distinct_id` varchar(512) DEFAULT NULL COMMENT '用户识别id',
    `priority` int(4) DEFAULT NULL COMMENT '优先级',
    `status` int(4) DEFAULT NULL COMMENT '状态',
    `owner` varchar(255) DEFAULT NULL COMMENT '添加人',
    `type` int(4) DEFAULT NULL COMMENT '消息类型',
    `content` json DEFAULT NULL COMMENT '消息内容',
    `send_at` int(11) DEFAULT NULL COMMENT '计划发送时间',
    `recall_result` text DEFAULT NULL COMMENT '发送结果',
    `created_at` int(11) DEFAULT NULL COMMENT '创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `distinct_id` (`distinct_id`),
    KEY `send_plan` (`status`,`priority`,`send_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1001;""".format(project_name=project[0])
        do_tidb_exe(insert_noti)
        insert_noti_group = """CREATE TABLE IF NOT EXISTS `{project_name}_noti_group` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `plan_id` int(11) DEFAULT NULL COMMENT '分群计划id',
    `list_id` int(11) DEFAULT NULL COMMENT '分群列表id',
    `data_id` int(11) DEFAULT NULL COMMENT '分群数据id',
    `temple_id` int(11) DEFAULT NULL COMMENT '应用模板id',
    `priority` int(4) DEFAULT NULL COMMENT '优先级id',
    `status` int(4) DEFAULT NULL COMMENT '状态id',
    `owner` varchar(255) DEFAULT NULL COMMENT '添加人',
    `send_at` int(11) DEFAULT NULL COMMENT '计划发送时间',
    `sent` int(11) DEFAULT NULL COMMENT '已发送数目',
    `total` int(11) DEFAULT NULL COMMENT '该计划总数目',
    `created_at` int(11) DEFAULT NULL COMMENT '创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1001;""".format(project_name=project[0])
        do_tidb_exe(insert_noti_group)
        insert_noti_temple = """CREATE TABLE IF NOT EXISTS `{project_name}_noti_temple` (
    `id` int(11) NOT NULL AUTO_INCREMENT,
    `name` varchar(255) DEFAULT NULL COMMENT '模板名称',
    `temple_desc` varchar(255) DEFAULT NULL COMMENT '模板描述',
    `args` json DEFAULT NULL COMMENT '模板参数',
    `content` json DEFAULT NULL COMMENT '模板内容',
    `apply_times` int(11) DEFAULT 0 COMMENT '应用次数',
    `lastest_apply_time` int(11) DEFAULT NULL COMMENT '最后一次应用时间',
    `lastest_apply_list` int(11) DEFAULT NULL COMMENT '最后一次应用列表',
    `created_at` int(11) DEFAULT NULL COMMENT '创建时间',
    `updated_at` int(11) DEFAULT NULL COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin AUTO_INCREMENT=1001;""".format(project_name=project[0])
        do_tidb_exe(insert_noti_temple)
        print(project[0]+'的消息附加表表已添加完')

if __name__ == "__main__":
    update()