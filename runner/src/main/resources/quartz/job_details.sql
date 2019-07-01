create table qrtz_job_details (
    sched_name varchar(120) not null,
    job_name varchar(200) not null,
    job_group varchar(200) not null,
    description varchar(250) ,
    job_class_name varchar(250) not null,
    is_durable varchar(5) not null,
    is_nonconcurrent varchar(5) not null,
    is_update_data varchar(5) not null,
    requests_recovery varchar(5) not null,
    job_data blob,
    primary key (sched_name,job_name,job_group)
)
