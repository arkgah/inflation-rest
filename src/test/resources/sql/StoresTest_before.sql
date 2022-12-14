----- DDL
drop table if exists store cascade;
create table store
(
    id          int primary key generated by default as identity,
    name        varchar(100) not null unique check (length(name) >= 2),
    external_id varchar(30)  not null unique
);
----- DML
insert into store(name, external_id)
values ('Пятёрочка', 's1');
insert into store(name, external_id)
values ('Окей', 's2');
insert into store(name, external_id)
values ('Лента', 's3');