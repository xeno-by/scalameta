DROP TABLE IF EXISTS `document`;

CREATE TABLE document(
  id INT,
  filename TEXT,
  contents MEDIUMTEXT,
  language TEXT,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS `name`;
CREATE TABLE name(
  id INT,
  document INT,
  start_line INT,
  start_character INT,
  end_line INT,
  end_character INT,
  symbol INT,
  is_definition boolean,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS `message`;
CREATE TABLE message(
  id INT,
  document INT,
  start_line INT,
  start_character INT,
  end_line INT,
  end_character INT,
  severity INT,
  text TEXT,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS `symbol`;
CREATE TABLE symbol(
  id INT,
  symbol TEXT,
  flags INT,
  name TEXT,
  signature INT,
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS `synthetic`;
CREATE TABLE synthetic(
  id INT,
  document INT,
  start_line INT,
  start_character INT,
  end_line INT,
  end_character INT,
  text INT,
  PRIMARY KEY (id)
);
