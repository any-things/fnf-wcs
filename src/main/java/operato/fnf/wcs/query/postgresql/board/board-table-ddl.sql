-- public.expas_building_b_month_volumn definition
-- Drop table
DROP TABLE public.expas_building_b_month_volumn;
CREATE TABLE public.expas_building_b_month_volumn (
	id varchar(40) NOT NULL DEFAULT uuid_generate_v1(),
	work_date varchar(255) NULL,
	pas_pcs_qty float8 NULL,
	das_pcs_qty float8 NULL,
	box_qty float8 NULL,
	domain_id int8 NOT NULL DEFAULT 1,
	creator_id varchar(32) NULL,
	updater_id varchar(32) NULL,
	created_at timestamp DEFAULT now(),
	updated_at timestamp DEFAULT now(),
	CONSTRAINT expas_building_b_month_volumn_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_expas_building_b_month_volumn_0 ON public.expas_building_b_month_volumn USING btree (id);


-- public.expas_building_b_chute definition
-- Drop table
DROP TABLE public.expas_building_b_chute;
CREATE TABLE public.expas_building_b_chute (
	id varchar(40) NOT NULL DEFAULT uuid_generate_v1(),
	chute_no varchar(255) NULL,
	status varchar(255) NULL,
	domain_id int8 NOT NULL DEFAULT 1,
	creator_id varchar(32) NULL,
	updater_id varchar(32) NULL,
	created_at timestamp DEFAULT now(),
	updated_at timestamp DEFAULT now(),
	CONSTRAINT expas_building_b_chute_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_expas_building_b_chute_0 ON public.expas_building_b_chute USING btree (id);


-- public.expas_building_b_process definition
-- Drop table
DROP TABLE public.expas_building_b_process;
CREATE TABLE public.expas_building_b_process (
	id varchar(40) NOT NULL DEFAULT uuid_generate_v1(),
	das_total_shop_cnt float8 NULL,
	das_total_sku_cnt float8 NULL,
	das_total_pcs_cnt float8 NULL,
	das_done_shop_cnt float8 NULL,
	das_done_sku_cnt float8 NULL,
	das_done_pcs_cnt float8 NULL,
	das_done_pcs_rate float8 NULL,
	prev_das_done_pcs_cnt float8 NULL,
	now_das_done_pcs_cnt float8 NULL,
	pas_done_pcs_rate float8 NULL,
	pas_done_pcs_cnt float8 NULL,
	pas_done_sku_cnt float8 NULL,
	pas_done_shop_cnt float8 NULL,
	pas_total_pcs_cnt float8 NULL,
	pas_total_sku_cnt float8 NULL,
	pas_total_shop_cnt float8 NULL,
	increase_rate float8 NULL,
	domain_id int8 NOT NULL DEFAULT 1,
	creator_id varchar(32) NULL,
	updater_id varchar(32) NULL,
	created_at timestamp DEFAULT now(),
	updated_at timestamp DEFAULT now(),
	CONSTRAINT expas_building_b_process_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_expas_building_b_process_0 ON public.expas_building_b_process USING btree (id);


-- public.total_building_b_process definition
-- Drop table
DROP TABLE public.total_building_b_process;
CREATE TABLE public.total_building_b_process (
	id varchar(40) NOT NULL DEFAULT uuid_generate_v1(),
	equip_no varchar(255) NULL,
	order_cnt float8 NULL,
	sku_cnt float8 NULL,
	pcs_qty float8 NULL,
	done_order_cnt float8 NULL,
	done_sku_cnt float8 NULL,
	done_pcs_qty float8 NULL,
	done_box_cnt float8 NULL,
	done_rate float8 NULL,
	domain_id int8 NOT NULL DEFAULT 1,
	creator_id varchar(32) NULL,
	updater_id varchar(32) NULL,
	created_at timestamp DEFAULT now(),
	updated_at timestamp DEFAULT now(),
	CONSTRAINT total_building_b_process_pkey PRIMARY KEY (id)
);
CREATE UNIQUE INDEX ix_total_building_b_process_0 ON public.total_building_b_process USING btree (id);