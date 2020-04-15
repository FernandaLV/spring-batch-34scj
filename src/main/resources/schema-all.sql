drop table if exists alunos;

create table alunos(
   id long not null primary key,
   nome varchar(255) not null,
   matricula varchar (20) not null,
   turma varchar (20) not null
)

