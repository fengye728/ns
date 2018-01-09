CREATE TABLE option_trade_161
(
    id bigserial PRIMARY KEY,
    stock_symbol character(6) DEFAULT NULL,
    expiration integer,
    call_put character(1) DEFAULT NULL,
    strike numeric(10, 3) DEFAULT NULL,
    event_day integer,
    event_time integer,
    price numeric(10, 3) DEFAULT NULL,
    size integer,
    previous_price numeric(10, 3) DEFAULT NULL,
    ask_price numeric(10, 3) DEFAULT NULL,
    trade_ask_interval integer,
    ask_ask_interval integer,
    bid_price numeric(10, 3) DEFAULT NULL,
    trade_bid_interval integer,
    bid_bid_interval integer,
    report_exg smallint,
    condition smallint,
    sequence_id bigint,
    direction character(9) DEFAULT NULL,
    leg_sequence_id integer,
    big_trade boolean
);
  
CREATE TABLE "option_open_interest_161" (
  "id" bigserial PRIMARY KEY,
  "stock_symbol" char(6) DEFAULT NULL,
  "expiration" int2 DEFAULT NULL,
  "call_put" char(1) DEFAULT NULL,
  "strike" numeric(10,3) DEFAULT NULL,
  "event_day" int2 DEFAULT NULL,
  "open_interest" int4 DEFAULT NULL
);