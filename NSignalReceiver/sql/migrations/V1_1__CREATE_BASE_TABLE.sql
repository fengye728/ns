CREATE TABLE "public"."option_trade_163" (
  "id" bigserial NOT NULL,
  "stock_symbol" char(6) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "expiration" int2 DEFAULT NULL,
  "call_put" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "strike" numeric(10,3) DEFAULT NULL::numeric,
  "event_day" int2 DEFAULT NULL,
  "event_time" int2 DEFAULT NULL,
  "price" numeric(10,3) DEFAULT NULL::numeric,
  "size" int2 DEFAULT NULL,
  "previous_price" numeric(10,3) DEFAULT NULL::numeric,
  "ask_price" numeric(10,3) DEFAULT NULL::numeric,
  "trade_ask_interval" int2 DEFAULT NULL,
  "ask_ask_interval" int2 DEFAULT NULL,
  "bid_price" numeric(10,3) DEFAULT NULL::numeric,
  "trade_bid_interval" int2 DEFAULT NULL,
  "bid_bid_interval" int2 DEFAULT NULL,
  "report_exg" int2 DEFAULT NULL,
  "condition" int2 DEFAULT NULL,
  "sequence_id" int4 DEFAULT NULL,
  "direction" char(9) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "leg_sequence_id" int4 DEFAULT NULL,
  "big_trade" bool DEFAULT NULL
);

ALTER TABLE "public"."option_trade_163" 
  OWNER TO "postgres";
  
CREATE TABLE "public"."option_open_interest_163" (
  "id" bigserial NOT NULL,
  "stock_symbol" char(6) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "expiration" int2 DEFAULT NULL,
  "call_put" char(1) COLLATE "pg_catalog"."default" DEFAULT NULL::bpchar,
  "strike" numeric(10,3) DEFAULT NULL::numeric,
  "event_day" int2 DEFAULT NULL,
  "last_sale" numeric(10,3) DEFAULT NULL::numeric,
  "volume" int4 DEFAULT NULL,
  "open_interest" int4 DEFAULT NULL
)
;

ALTER TABLE "public"."option_open_interest_163" 
  OWNER TO "postgres";