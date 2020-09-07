create table menu_item
(
	menu_item_id bigserial not null
	constraint menu_item_pkey
		primary key,
	name varchar(36) not null,
	owner varchar(36) not null
);