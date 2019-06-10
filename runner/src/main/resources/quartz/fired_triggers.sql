create table qrtz_fired_triggers(
    sched_name varchar(120) not null,
    entry_id varchar(95) not null,
    trigger_name varchar(200) not null,
    trigger_group varchar(200) not null,
    instance_name varchar(200) not null,
    fired_time bigint not null,
    sched_time bigint not null,
    priority integer not null,
    state varchar(16) not null,
    job_name varchar(200),
    job_group varchar(200),
    is_nonconcurrent varchar(5),
    requests_recovery varchar(5),
    primary key (sched_name,entry_id)
)
