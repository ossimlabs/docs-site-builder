select
(select count(*) from information_schema.tables where table_name like 'isr%') as TABLES,
(select count(*) from information_schema.columns where table_name like 'isr%') as COLUMNS,
(select count(*) from information_schema.table_constraints where table_name like 'isr%') as CONSTRAINTS,
(select count(*) from pg_index c left join pg_class t
ON c.indrelid  = t.oid
LEFT JOIN pg_attribute a
ON a.attrelid = t.oid
AND a.attnum = ANY(indkey)
WHERE t.relname like 'isr%') as INDEXES;