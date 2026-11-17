INSERT INTO types VALUES (1, 'Argument') ON CONFLICT DO NOTHING;
INSERT INTO types VALUES (2, 'Protiargument') ON CONFLICT DO NOTHING;;

INSERT INTO owners VALUES (1, 'Libor', 'Pavlicek', '123 Hollywood', 'Los Angeles', '6085551023') ON CONFLICT DO NOTHING;
INSERT INTO owners VALUES (2, 'Adam', 'Adam', '123 Hollywood', 'Los Angeles', '6085551023') ON CONFLICT DO NOTHING;

INSERT  INTO pets VALUES (1, 'Chodec věnoval pozornost','Díval se na cestu před sebou', '2010-09-07', 1, 1)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (2, 'Obec porušila standard řádné péče','Neměla zpracovaný plán údržby', '2010-09-07', 1, 1)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (3, 'Porušil povinnost prevence','Neměl vhodnou obuv.', '2010-09-07', 1, 1)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (4, 'Byla výluka z plnění plánu údržby','čas pádu nastal v době, kdy údržba nemohla zajistit odpovídající stav dle plánu údržby v přiměřené lhůtě.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (5, 'Byla výstraha,','ČHMÚ varoval v den pádu o riziku námrazy.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (6, 'Chodec byl fyzicky i psychicky způsobilý,','se jedná o běžného čtyřicátníka, který šel ráno do práce.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (7, 'Chodec věnoval pozornost','se díval před sebe pod nohy.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (8, 'Obec měla zpracovaný plán údržby','protože byl schválen veřejnou vyhláškou.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (9, 'Obuv byla vhodná','podrážka měla hrubý vzorek', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (10, 'Stav chodníku byl vyhovující','v zimě bývá obvykle sníh a led všude.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (11, 'Stav chodníku nebyl vyhovující','bylo náledí.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (12, 'Zvolená trasa byla nebezpečná','obsahovala změnu terénu.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (13, 'Zvolená trasa byla riziková','trasa obsahovala místo vedoucí kolem kontejnerů bez pokrytí úklidem a s viditelnými zmrazky.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (14, 'Zvolená trasa byla známá','tudy chodec chodil do práce.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (15, 'Zvolená trasa nebyla bezpečná','vedla šikmo z vozovky.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (16, 'Závada byla překvapivá','protože neošetřená část byla zjistitelná jen se zvýšeným vizuálním úsilím.', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;
INSERT  INTO pets VALUES (17, 'Závada nebyla ojedinělá','v danou chvíli byla kluzká celá ulice', '2010-09-07', 1, 2)ON CONFLICT DO NOTHING;


INSERT  INTO visits VALUES (1, 1, '2013-01-01', 'Nevěnoval pozornost protože se díval do mobilu')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (2, 2, '2013-01-02', 'Měla plán údržby, který byl schválený zastupitelstvem pod 23/2012.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (3, 3, '2013-01-03', 'Neporusil povinnost presence')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (4, 3, '2013-01-04', 'Mit vhodnou obuv neni obsahem povinnosti prevence.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (5, 3, '2013-01-04', 'Obuv měla hrubou podrážku.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (6, 4, '2013-01-04', 'Nebyla výluka, protože v době pádu již měl být dle plánu údržby stav odpovídající schůdnosti a lhůta k zajištění stavu byla přiměřená.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (7, 5, '2013-01-04', 'Nebyla výstraha, protože varování ČHMÚ nebylo zveřejněno ve zpravodajství.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (8, 6, '2013-01-04', 'Chodec nebyl způsobilý, protože to ráno zaspal, byl rozespalý, byl unavený a chvatně pospíchal do práce.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (9, 7, '2013-01-04', 'Chodec nevěnoval pozornost, protože zrovna používal mobil.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (10, 8, '2013-01-04', 'Obec neměla plán údržby, protože ve skutečnosti podle něj nepostupovala a plán údržby byl pouze formální (tzv. na papíře).')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (11, 8, '2013-01-04', 'Obec měla plán údržby, protože pluhovala a solila chodníky.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (12, 9, '2013-01-04', 'Obuv nebyla vhodná, protože boty byly staré a podrážka byla značně opotřebovaná.')ON CONFLICT DO NOTHING;
INSERT  INTO visits VALUES (13, 17, '2013-01-04', 'Závada byla ojedinělá, protože část chodníků v danou chvíli již byla ošetřena a pouze část v místě nehody nebyla.')ON CONFLICT DO NOTHING;

--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=1);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=2);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=3);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=4);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=5);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=6);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=7);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=8);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=9);
--INSERT INTO owners (first_name, last_name, address, city, telephone) SELECT 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487' WHERE NOT EXISTS (SELECT * FROM owners WHERE id=10);

--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Leo', '2000-09-07', 1, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=1);
--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Basil', '2002-08-06', 6, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=2);
--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Rosy', '2001-04-17', 2, 3 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=3);
--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Jewel', '2000-03-07', 2, 3 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=4);
--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'Iggy', '2000-11-30', 3, 4 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=5);
--INSERT INTO pets (name, birth_date, type_id, owner_id) SELECT 'George', '2000-01-20', 4, 5 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=6);

--INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2008-09-04', 'spayed' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=4);
--INSERT INTO visits (pet_id, visit_date, description) SELECT 7, '2010-03-04', 'rabies shot' WHERE NOT EXISTS (SELECT * FROM visits WHERE id=1);

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

