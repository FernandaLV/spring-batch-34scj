drop table if exists TB_PESSOA;

create table TB_PESSOA(
   id long not null auto_increment primary key,
   nome varchar(255) not null,
   cpf varchar (20) not null
)

