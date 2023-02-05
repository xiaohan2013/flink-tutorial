create table product_stats_2022 (
   stt DateTime,
   edt DateTime,
   sku_id  UInt64,
   sku_name String,
   sku_price Decimal64(2),
   spu_id UInt64,
   spu_name String ,
   tm_id UInt64,
   tm_name String,
   category3_id UInt64,
   category3_name String ,
   display_ct UInt64,
   click_ct UInt64,
   favor_ct UInt64,
   cart_ct UInt64,
   order_sku_num UInt64,
   order_amount Decimal64(2),
   order_ct UInt64 ,
   payment_amount Decimal64(2),
   paid_order_ct UInt64,
   refund_order_ct UInt64,
   refund_amount Decimal64(2),
   comment_ct UInt64,
   good_comment_ct UInt64 ,
   ts UInt64
)engine =ReplacingMergeTree( ts)
        partition by  toYYYYMMDD(stt)
        order by   (stt,edt,sku_id );
