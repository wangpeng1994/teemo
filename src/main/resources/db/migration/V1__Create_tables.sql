create table NEWS (
    id bigint primary key auto_increment,
    title text,
    content text,
    url varchar(100),
    created_at timestamp default now(),
    updated_at timestamp default now()
);

create table LINKS_TO_BE_PROCESSED (
    link varchar(2000)
);

create table LINKS_ALREADY_PROCESSED (
    link varchar(2000)
);


