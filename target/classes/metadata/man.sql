DROP TABLE IF EXISTS `man`;
CREATE table `man`(
	`age` int PRIMARY KEY,
	`name` VARCHAR(32),
	`price` BIGINT
);
insert into `man` values (33,'李彦宏',90);
insert into `man` values (40,'雷军',30.45);
insert into `man` values (48,'吕洞宾',93.45);
insert into `man` values (14,'神童',111.7);

select * from man;