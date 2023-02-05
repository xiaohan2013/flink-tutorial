create table keyword_stats_2022 (
    stt DateTime,
    edt DateTime,
    keyword String ,
    source String ,
    ct UInt64 ,
    ts UInt64
)engine =ReplacingMergeTree( ts)
        partition by  toYYYYMMDD(stt)
        order by  ( stt,edt,keyword,source );

select keyword,sum(keyword_stats_2022.ct *
multiIf(
    source='SEARCH',10,
    source='ORDER',5,
    source='CART',2,
    source='CLICK',1,
    0
)) ct
from keyword_stats_2022 where toYYYYMMDD(stt)=20221216
group by keyword order by ct desc limit 5;
