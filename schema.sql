-- Drop table

-- DROP TABLE public.coupons

CREATE TABLE public.coupons (
	coupon_id uuid NOT NULL,
	user_id uuid NOT NULL,
	CONSTRAINT coupons_pk PRIMARY KEY (coupon_id)
);
CREATE INDEX coupons_user_id_idx ON public.coupons USING btree (user_id);

-- Permissions

ALTER TABLE public.coupons OWNER TO coupons_app;
GRANT ALL ON TABLE public.coupons TO coupons_app;


-- Drop table

-- DROP TABLE public.bids

CREATE TABLE public.bids (
	duel_id uuid NOT NULL,
	winner varchar NULL,
	rate float8 NOT NULL,
	coupon_id uuid NOT NULL,
	CONSTRAINT bids_pk PRIMARY KEY (coupon_id, duel_id),
	CONSTRAINT bids_coupons_fk FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id) ON UPDATE CASCADE ON DELETE CASCADE
);

-- Permissions

ALTER TABLE public.bids OWNER TO coupons_app;
GRANT ALL ON TABLE public.bids TO coupons_app;



CREATE TYPE duel_result AS ENUM ('home_team_won', 'away_team_won', 'tied');

-- Drop table

-- DROP TABLE public.duels

CREATE TABLE public.duels (
	duel_id uuid NOT NULL,
	home_team varchar NOT NULL,
	away_team varchar NOT NULL,
	"result" duel_result NULL,
	CONSTRAINT duels_pk PRIMARY KEY (duel_id)
);

-- Permissions

ALTER TABLE public.duels OWNER TO coupons_app;
GRANT ALL ON TABLE public.duels TO coupons_app;

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


insert into duels (duel_id, home_team, away_team) values (uuid_generate_v4(),uuid_generate_v4(),uuid_generate_v4()), (uuid_generate_v4(),uuid_generate_v4(),uuid_generate_v4()),(uuid_generate_v4(),uuid_generate_v4(),uuid_generate_v4())
