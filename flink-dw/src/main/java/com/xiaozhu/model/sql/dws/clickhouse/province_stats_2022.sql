create table province_stats_2022 (
   stt DateTime,
   edt DateTime,
   province_id  UInt64,
   province_name String,
   area_code String ,
   iso_code String,
   iso_3166_2 String ,
   order_amount Decimal64(2),
   order_count UInt64 ,
   ts UInt64
)engine =ReplacingMergeTree(ts)
   partition by  toYYYYMMDD(stt)
   order by   (stt,edt,province_id );
