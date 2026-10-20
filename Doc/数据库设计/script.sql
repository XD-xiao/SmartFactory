create table device_device
(
    id               bigint auto_increment comment '主键，自增'
        primary key,
    sn               varchar(255)                       not null comment '设备唯一标识',
    type             varchar(255)                       not null comment '设备类型。pad、pda、adapter',
    state            int      default 0                 not null comment '状态。0离线；1在线',
    last_online_time datetime                           null comment '最后一次上报上线的时间',
    create_time      datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time      datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uk_sn
        unique (sn)
)
    comment '设备表';

create table device_equipment_run_time
(
    id              bigint auto_increment comment '主键，自增'
        primary key,
    sn              varchar(255)  not null comment '设备sn码',
    seq_no          int           null comment '序号',
    action          int default 0 not null comment '当前状态 0关1开',
    work_begin_time datetime      null comment '开始工作时间',
    work_end_time   datetime      null comment '结束工作时间',
    work_time       float         null comment '工作时长'
)
    comment '产线设备运行数据表';

create index idx_sn
    on device_equipment_run_time (sn);

create table device_group
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    name        varchar(255)                       not null comment '组名',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间'
)
    comment '分组表';

create table device_group_device
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    group_id    bigint                             not null comment '分组id',
    device_sn   varchar(255)                       not null comment '设备唯一编码',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    constraint uk_device_sn
        unique (device_sn)
)
    comment '分组-设备关联表';

create index idx_group_id
    on device_group_device (group_id);

create table device_machine_failure
(
    id                   bigint auto_increment comment '主键，自增'
        primary key,
    sn                   varchar(255)                       not null comment '设备唯一标识',
    reason               varchar(255)                       null comment '故障原因',
    failure_level        int                                null comment '故障级别',
    failure_time         varchar(255)                       null comment '故障时间',
    status               int                                null comment '状态',
    failure_recover_time varchar(255)                       null comment '故障恢复时间',
    create_time          datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '设备故障表';

create index idx_sn
    on device_machine_failure (sn);

create table edubox_sensor_config
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    sn          varchar(64)          null comment '设备sn',
    sensor      bigint               null comment '传感器类型',
    sceneType   bigint               null comment '场景',
    valueType   tinyint(1)           null comment '取值类型: 0-范围; 1-开关',
    max         int                  null comment '传感器上限值',
    min         int                  null comment '传感器下限值',
    max_th      int                  null comment '最大阈值',
    mid_th      int                  null comment '中间阈值',
    min_th      int                  null comment '最小阈值',
    create_time datetime             null comment '创建时间',
    update_time datetime             null comment '更新时间',
    delete_flag tinyint(1) default 0 null comment '删除标志: 0-未删除; 1-已删除',
    sensor_name varchar(64)          null
)
    comment '传感器配置表';

create table edubox_sensor_record
(
    id       bigint auto_increment comment '主键，自增'
        primary key,
    sensorId bigint      null comment '传感器配置表中传感器Id',
    com      varchar(64) null comment '通信模块',
    value    int         null comment '当前值',
    status   int         null comment '当前状态: -1-超出上下限; 0-正常; 1-轻微; 2-警告; 3-危险',
    datetime datetime    null comment '当前日期时间'
)
    comment '传感器记录表';

create index idx_sensorId
    on edubox_sensor_record (sensorId);

create table pm_department
(
    department_id   int auto_increment comment '主键，部门ID'
        primary key,
    department_name varchar(50)          not null comment '部门名称',
    status          tinyint              not null comment '部门状态（0:停用, 1:启用）',
    remark          text                 null comment '备注',
    delete_flag     tinyint(1) default 0 not null,
    constraint department_pk
        unique (department_name)
)
    comment '部门表';

create table pm_position
(
    position_id   int auto_increment comment '主键，职位ID'
        primary key,
    position_name varchar(50)          not null comment '职位名称',
    remark        text                 null comment '职位描述',
    delete_flag   tinyint(1) default 0 not null
)
    comment '职位表';

create table pm_employee
(
    employee_id     int auto_increment comment '主键，员工ID'
        primary key,
    employee_number varchar(50)          not null comment '工号，工厂内唯一',
    name            varchar(50)          not null comment '姓名',
    password        varchar(80)          not null comment '密码',
    gender          char                 null comment '性别',
    birth_date      date                 null comment '出生日期',
    phone           varchar(20)          null comment '联系电话',
    email           varchar(100)         null comment '邮箱',
    address         varchar(200)         null comment '家庭住址',
    id_card         varchar(18)          null comment '身份证号',
    hire_date       date                 null comment '入职日期',
    department_id   int                  null comment '外键，所属部门ID',
    position_id     int                  null comment '外键，职位ID',
    status          int                  null,
    delete_flag     tinyint(1) default 0 not null,
    constraint uk_employee_number
        unique (employee_number),
    constraint fk_employee_department
        foreign key (department_id) references pm_department (department_id),
    constraint fk_employee_position
        foreign key (position_id) references pm_position (position_id)
)
    comment '员工表';

create table pm_attendance_record
(
    attendance_id  int auto_increment comment '主键'
        primary key,
    employee_id    int           not null comment '外键，员工ID',
    date           date          not null comment '考勤日期',
    check_in_time  datetime      null comment '上班打卡时间',
    check_out_time datetime      null comment '下班打卡时间',
    status         tinyint       not null comment '考勤状态（0:正常, 1:迟到, 2:早退）',
    work_hours     decimal(5, 2) null comment '当日工作时长（小时）',
    constraint fk_attendance_employee
        foreign key (employee_id) references pm_employee (employee_id)
)
    comment '考勤记录表';

create index idx_attendance_employee_id
    on pm_attendance_record (employee_id);

create index idx_employee_department_id
    on pm_employee (department_id);

create index idx_employee_position_id
    on pm_employee (position_id);

create table pm_leave_record
(
    leave_id    int auto_increment comment '主键'
        primary key,
    employee_id int           not null comment '外键，员工ID',
    leave_type  tinyint       not null comment '请假类型（1:年假, 2:病假, 3:事假, 4:调休）',
    start_date  date          not null comment '开始日期',
    end_date    date          not null comment '结束日期',
    days        decimal(4, 1) not null comment '请假天数',
    reason      text          null comment '请假原因',
    status      tinyint       not null comment '审批状态（0:待审批, 1:通过, 2:驳回）',
    constraint fk_leave_employee
        foreign key (employee_id) references pm_employee (employee_id)
)
    comment '请假记录表';

create index idx_leave_employee_id
    on pm_leave_record (employee_id);

create table product_model
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    name        varchar(255)                         not null comment '型号名称',
    code        varchar(255)                         not null comment '型号码',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_flag tinyint(1) default 0                 not null,
    constraint uk_code
        unique (code)
)
    comment '产品型号表';

create table product_picture
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    content     longtext                           null comment '图片内容（Base64/URL）',
    create_time datetime default CURRENT_TIMESTAMP null comment '创建时间'
)
    comment '产品图片表';

create table product_type
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    name        varchar(255)                         not null comment '类型名称',
    code        varchar(255)                         not null comment '类型码',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_flag tinyint(1) default 0                 not null,
    constraint uk_code
        unique (code)
)
    comment '产品类型表';

create table product_product
(
    id           bigint auto_increment comment '主键，自增'
        primary key,
    bar_code     varchar(255)                       null comment '条形码编号',
    type_code    varchar(255)                       not null comment '产品类型码',
    model_code   varchar(255)                       not null comment '产品型号码',
    product_code varchar(255)                       null comment '产品代号',
    pic_id       bigint                             null comment '图片id (已修改为BIGINT以匹配父表)',
    device_sn    varchar(255)                       null comment '设备SN',
    create_time  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    delete_flag  tinyint(1)                         not null,
    constraint uk_bar_code
        unique (bar_code),
    constraint fk_product_model
        foreign key (model_code) references product_model (code),
    constraint fk_product_pic
        foreign key (pic_id) references product_picture (id),
    constraint fk_product_type
        foreign key (type_code) references product_type (code)
)
    comment '产品表';

create index idx_device_sn
    on product_product (device_sn);

create index idx_model_code
    on product_product (model_code);

create index idx_type_code
    on product_product (type_code);

create table product_production
(
    id                 bigint auto_increment comment '主键，自增'
        primary key,
    product_id         bigint                             not null comment '产品id',
    planned_production int      default 0                 null comment '计划产量',
    actual_production  int      default 0                 null comment '实际产量',
    create_time        datetime default CURRENT_TIMESTAMP null comment '创建时间',
    store_num          int                                not null,
    qualified_num      int                                not null,
    group_id           bigint                             not null,
    plan_name          varchar(63)                        not null,
    constraint fk_production_product
        foreign key (product_id) references product_product (id)
)
    comment '产品产量表';

create index idx_product_id
    on product_production (product_id);

create table product_store
(
    id          bigint auto_increment comment '主键，自增'
        primary key,
    product_id  bigint            not null comment '产品id',
    action      tinyint default 0 not null comment '0:出库；1:入库',
    device_sn   varchar(255)      null comment '设备SN',
    group_id    bigint            null comment '分组id',
    employee_id bigint            null comment '入库员工id',
    datetime    datetime          null comment '入库时间',
    constraint fk_store_product
        foreign key (product_id) references product_product (id)
)
    comment '产品出入库表';

create index idx_action
    on product_store (action);

create index idx_device_sn
    on product_store (device_sn);

create index idx_group_id
    on product_store (group_id);

create index idx_in_employee_id
    on product_store (employee_id);

create index idx_product_id
    on product_store (product_id);


