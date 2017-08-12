CREATE TABLE public.option_trade_171
(
    id bigserial NOT NULL,
    stock_symbol character(6) COLLATE pg_catalog."default" DEFAULT NULL::bpchar,
    expiration integer,
    call_put character(1) COLLATE pg_catalog."default" DEFAULT NULL::bpchar,
    strike numeric(10, 3) DEFAULT NULL::numeric,
    event_day integer,
    event_time integer,
    price numeric(10, 3) DEFAULT NULL::numeric,
    size integer,
    previous_price numeric(10, 3) DEFAULT NULL::numeric,
    ask_price numeric(10, 3) DEFAULT NULL::numeric,
    trade_ask_interval integer,
    ask_ask_interval integer,
    bid_price numeric(10, 3) DEFAULT NULL::numeric,
    trade_bid_interval integer,
    bid_bid_interval integer,
    report_exg smallint,
    condition smallint,
    sequence_id bigint,
    direction character(9) COLLATE pg_catalog."default" DEFAULT NULL::bpchar,
    leg_sequence_id integer,
    big_trade boolean
);
  
CREATE TABLE "public"."option_open_interest_171" (
  "id" bigserial NOT NULL,
  "stock_symbol" char(6) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "expiration" int2 DEFAULT NULL,
  "call_put" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "strike" numeric(10,3) DEFAULT NULL::numeric,
  "event_day" int2 DEFAULT NULL,
  "last_sale" numeric(10,3) DEFAULT NULL::numeric,
  "volume" int4 DEFAULT NULL,
  "open_interest" int4 DEFAULT NULL
);