create table usuario (
    codigo varchar(50) not null,
    nombre varchar(50) not null,
    edad int(3) not null,
    primary key (codigo)
);
create table vegetal (
    id int(5) not null,
    descripcion varchar(100),
    primary key (id)
);
create table usuario_vegetal (
    usuario varchar(50) not null,
    vegetal int(5) not null,
    primary key (usuario,vegetal),
    foreign key (usuario) references usuario(codigo),
    foreign key (vegetal) references vegetal(id)
);