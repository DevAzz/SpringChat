create table usr
(
    id       bigint  not null
        constraint usr_pkey
            primary key,
    active   boolean not null,
    password varchar(255),
    username varchar(255)
);

alter table usr
    owner to postgres;

create table message
(
    id       integer not null
        constraint message_pkey
            primary key,
    tag      varchar(255),
    text     text,
    user_id  bigint,
    filename varchar(255),
    date date
);

alter table message
    owner to postgres;

alter table message
    add constraint fk_user_user_id
        foreign key (user_id) references usr;

create table user_role
(
    user_id bigint not null,
    roles   varchar(255)
);

alter table user_role
    owner to postgres;

alter table user_role
    add constraint fk_user_user_id
        foreign key (user_id) references usr;