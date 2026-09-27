create table configuracao_backup (
    id bigint primary key,
    pasta_local varchar(1000),
    pasta_drive varchar(1000),
    ultimo_backup_em timestamp
);

insert into configuracao_backup (id, pasta_local, pasta_drive, ultimo_backup_em)
values (1, null, null, null);
