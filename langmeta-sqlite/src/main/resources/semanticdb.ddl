create table document(
  id int,
  filename text,
  contents text,
  language text,
  primary key (id)
);

create table name(
  id int,
  document int,
  start_line int,
  start_character int,
  end_line int,
  end_character int,
  symbol int,
  is_definition boolean,
  primary key (id),
  foreign key (document) references document (id),
  foreign key (symbol) references symbol (id)
);

create table message(
  id int,
  document int,
  start_line int,
  start_character int,
  end_line int,
  end_character int,
  severity int,
  text text,
  primary key (id),
  foreign key (document) references document(id)
);

create table symbol(
  id int,
  symbol text,
  flags int,
  name text,
  signature int,
  primary key (id),
  foreign key (signature) references document (id)
);

create table synthetic(
  id int,
  start_line int,
  start_character int,
  end_line int,
  end_character int,
  text int,
  primary key (id),
  foreign key (text) references document (id)
);
