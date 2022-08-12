insert into person_role(name)
values ('ROLE_USER');
insert into person_role(name)
values ('ROLE_ADMIN');

insert into store(name, external_id)
values ('Пятёрочка', 's1');
insert into store(name, external_id)
values ('Окей', 's2');
insert into store(name, external_id)
values ('Лента', 's3');

insert into product_category(name, external_id)
values ('Продукты', 'pc1');
insert into product_category(name, external_id)
values ('Хозтовары', 'pc2');
insert into product_category(name, external_id)
values ('Коммунальные услуги', 'pc3');

insert into product(name, category_id, unit, external_id)
values ('Хлеб ржаной', 1, 1, 'p1');
insert into product(name, category_id, unit, external_id)
values ('Хлеб белый', 1, 1, 'p2');
insert into product(name, category_id, unit, external_id)
values ('Кефир', 1, 1, 'p3');
insert into product(name, category_id, unit, external_id)
values ('Молоко', 1, 1, 'p4');
insert into product(name, category_id, unit, external_id)
values ('Колбаса Докторская', 1, 1, 'p5');
insert into product(name, category_id, unit, external_id)
values ('Мыло', 2, 1, 'p6');
insert into product(name, category_id, unit, external_id)
values ('Электроэнергия', 3, 1, 'p7');



