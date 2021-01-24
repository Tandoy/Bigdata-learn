####1.基本Rest风格命令说明
![image]

####2.ES基于kibana的查询记录

    1.更新数据 PUT，更新时是覆盖更新，没有更新的字段会被覆盖为空
    2.Post _update,==推荐使用这种更新方式!==
    3.must(and) ,所有条件都要符合 where id=1 and name=xxx
    4.返回json中"_score"表示匹配度
    5.查询json中 "_soure":["",""]，表示对结果进行过滤
    6.查询json中 "order":"desc"，对查询结果进行排序
    7.分页查询："from":0,"size":"1"
    8.must(and) ,所有条件都要符合 where id=1 and name=xxx
    9.should (or) ,所有条件都要符合 where id=1 or name = xxx
    10.must not (not)
    11.过滤器 filter：gt 大于、gte 大于等于、lt 小于、lte 小于等于
    12.匹配多个条件时多个条件使用空格隔开，只要满足其中一个条件
    13.term 查询时直接通过倒排索引指定的词条进程精确查找的
    14.match,会使用分词器解析! (先分析分档,然后再通过分析的分档进行查询! )
    15.高亮查询：搜索的高亮条件 "hignlight",会在HTML里面自动的加上标签