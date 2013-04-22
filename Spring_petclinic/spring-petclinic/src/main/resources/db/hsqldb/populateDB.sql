INSERT INTO persons VALUES(1,'James','Carter');
INSERT INTO persons VALUES(2,'Helen','Leary');
INSERT INTO persons VALUES(3,'Linda','Douglas');
INSERT INTO persons VALUES(4,'Rafael','Ortega');
INSERT INTO persons VALUES(5,'Henry','Stevens');
INSERT INTO persons VALUES(6,'Sharon','Jenkins');
INSERT INTO persons VALUES(7,'George','Franklin');
INSERT INTO persons VALUES(8,'Betty','Davis');
INSERT INTO persons VALUES(9,'Eduardo','Rodriquez');
INSERT INTO persons VALUES(10,'Harold','Davis');
INSERT INTO persons VALUES(11,'Peter','McTavish');
INSERT INTO persons VALUES(12,'Jean','Coleman');
INSERT INTO persons VALUES(13,'Jeff','Black');
INSERT INTO persons VALUES(14,'Maria','Escobito');
INSERT INTO persons VALUES(15,'David','Schroeder');
INSERT INTO persons VALUES(16,'Carlos','Estaban');

INSERT INTO users VALUES(1,'james','b4cc344d25a2efe540adbf2678e2304c',1);
INSERT INTO users VALUES(2,'helen','7a2eb41a38a8f4e39c1586649da21e5f',2);
INSERT INTO users VALUES(3,'linda','eaf450085c15c3b880c66d0b78f2c041',3);
INSERT INTO users VALUES(4,'rafael','9135d8523ad3da99d8a4eb83afac13d1',4);
INSERT INTO users VALUES(5,'henry','027e4180beedb29744413a7ea6b84a42',5);
INSERT INTO users VALUES(6,'sharon','215a6517848319b70f3f450da480d888',6);
INSERT INTO users VALUES(7,'george','9b306ab04ef5e25f9fb89c998a6aedab',7);
INSERT INTO users VALUES(8,'betty','82b054bd83ffad9b6cf8bdb98ce3cc2f',8);
INSERT INTO users VALUES(9,'rod','52c59993d8e149a1d70b65cb08abf692',9);
INSERT INTO users VALUES(10,'harold','c57f431343f100b441e268cc12babc34',10);
INSERT INTO users VALUES(11,'peter','51dc30ddc473d43a6011e9ebba6ca770',11);
INSERT INTO users VALUES(12,'jean','b71985397688d6f1820685dde534981b',12);
INSERT INTO users VALUES(13,'jeff','166ee015c0e0934a8781e0c86a197c6e',13);
INSERT INTO users VALUES(14,'maria','263bce650e68ab4e23f28263760b9fa5',14);
INSERT INTO users VALUES(15,'david','172522ec1028ab781d9dfd17eaca4427',15);
INSERT INTO users VALUES(16,'carlos','dc599a9972fde3045dab59dbd1ae170b',16);

INSERT INTO vets VALUES(1);
INSERT INTO vets VALUES(2);
INSERT INTO vets VALUES(3);
INSERT INTO vets VALUES(4);
INSERT INTO vets VALUES(5);
INSERT INTO vets VALUES(6);

INSERT INTO specialties VALUES (1, 'radiology');
INSERT INTO specialties VALUES (2, 'surgery');
INSERT INTO specialties VALUES (3, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (1, 'cat');
INSERT INTO types VALUES (2, 'dog');
INSERT INTO types VALUES (3, 'lizard');
INSERT INTO types VALUES (4, 'snake');
INSERT INTO types VALUES (5, 'bird');
INSERT INTO types VALUES (6, 'hamster');

INSERT INTO owners VALUES(7,'110 W. Liberty St.','Madison','6085551023');
INSERT INTO owners VALUES(8,'638 Cardinal Ave.','Sun Prairie','6085551749');
INSERT INTO owners VALUES(9,'2693 Commerce St.','McFarland','6085558763');
INSERT INTO owners VALUES(10,'563 Friendly St.','Windsor','6085553198');
INSERT INTO owners VALUES(11,'2387 S. Fair Way','Madison','6085552765');
INSERT INTO owners VALUES(12,'105 N. Lake St.','Monona','6085552654');
INSERT INTO owners VALUES(13,'1450 Oak Blvd.','Monona','6085555387');
INSERT INTO owners VALUES(14,'345 Maple St.','Madison','6085557683');
INSERT INTO owners VALUES(15,'2749 Blackhawk Trail','Madison','6085559435');
INSERT INTO owners VALUES(16,'2335 Independence La.','Waunakee','6085555487');

INSERT INTO pets VALUES(1,'Leo','2000-09-07',1,7);
INSERT INTO pets VALUES(2,'Basil','2002-08-06',6,8);
INSERT INTO pets VALUES(3,'Rosy','2001-04-17',2,9);
INSERT INTO pets VALUES(4,'Jewel','2000-03-07',2,9);
INSERT INTO pets VALUES(5,'Iggy','2000-11-30',3,10);
INSERT INTO pets VALUES(6,'George','2000-01-20',4,11);
INSERT INTO pets VALUES(7,'Samantha','1995-09-04',1,12);
INSERT INTO pets VALUES(8,'Max','1995-09-04',1,12);
INSERT INTO pets VALUES(9,'Lucky','1999-08-06',5,13);
INSERT INTO pets VALUES(10,'Mulligan','1997-02-24',2,14);
INSERT INTO pets VALUES(11,'Freddy','2000-03-09',5,15);
INSERT INTO pets VALUES(12,'Lucky','2000-06-24',2,16);
INSERT INTO pets VALUES(13,'Sly','2002-06-08',1,16);

INSERT INTO visits VALUES (1, 7, 1, '2013-01-01', 'rabies shot');
INSERT INTO visits VALUES (2, 8, 1, '2013-01-02', 'rabies shot');
INSERT INTO visits VALUES (3, 8, 2, '2013-01-03', 'neutered');
INSERT INTO visits VALUES (4, 7, 3, '2013-01-04', 'spayed');
