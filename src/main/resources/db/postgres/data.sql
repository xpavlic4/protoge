INSERT INTO types VALUES (1, 'Argument') ON CONFLICT DO NOTHING;
INSERT INTO types VALUES (2, 'Protiargument') ON CONFLICT DO NOTHING;;

insert into source_types values (0, '')ON CONFLICT DO NOTHING;;
insert into source_types values (1, 'žaloba')ON CONFLICT DO NOTHING;;
insert into source_types values (2, 'odvolalní')ON CONFLICT DO NOTHING;;
insert into source_types values (3, 'replika')ON CONFLICT DO NOTHING;;
insert into source_types values (4, 'duplika')ON CONFLICT DO NOTHING;;
insert into source_types values (5, 'rozsudek')ON CONFLICT DO NOTHING;;

INSERT INTO ARGUMENT_TYPES VALUES  (1, 'Rebutting') ON CONFLICT DO NOTHING;
INSERT INTO ARGUMENT_TYPES VALUES  (2, 'Undercutting')ON CONFLICT DO NOTHING;
INSERT INTO ARGUMENT_TYPES VALUES  (3, 'Undermining') ON CONFLICT DO NOTHING;

INSERT INTO roles (id, name) VALUES (10,'ROLE_PLAINTIFF')ON CONFLICT DO NOTHING;;
INSERT INTO roles (id, name) VALUES (11, 'ROLE_DEFENDANT')ON CONFLICT DO NOTHING;;
INSERT INTO roles (id, name) VALUES (12, 'ROLE_JUDGE')ON CONFLICT DO NOTHING;;

