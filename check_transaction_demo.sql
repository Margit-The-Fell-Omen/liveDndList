\echo ''
\echo '========================================================'
\echo '   TRANSACTION BEHAVIOR DEMO - BULK EQUIPMENT ADD'
\echo '========================================================'
\echo ''

-- Summary table
\echo '📊 SUMMARY COMPARISON:'
\echo ''

SELECT 
    test_type,
    CASE 
        WHEN test_type = 'WITH @Transactional' AND equipment_count = 0 
            THEN '✅ CORRECT (Full Rollback)'
        WHEN test_type = 'WITHOUT @Transactional' AND equipment_count > 0 
            THEN '⚠️  PROBLEM (Partial Data Saved)'
        ELSE 'ℹ️  Check manually'
    END as status,
    equipment_count as items_stuck_in_db
FROM (
    SELECT 
        'WITH @Transactional' as test_type,
        (SELECT COUNT(*) FROM equipment WHERE name LIKE 'TX-%') as equipment_count
    UNION ALL
    SELECT 
        'WITHOUT @Transactional',
        (SELECT COUNT(*) FROM equipment WHERE name LIKE 'NOTX-%')
) as results;

\echo ''
\echo '📋 DETAILED VIEW (Items actually saved to DB despite error):'
\echo ''

SELECT 
    c.name as character_name, 
    e.name as equipment_name, 
    e.type
FROM equipment e
JOIN characters c ON e.character_id = c.id
WHERE e.name LIKE 'TX-%' OR e.name LIKE 'NOTX-%'
ORDER BY e.name ASC;

\echo ''
\echo '========================================================'
\echo '   END OF REPORT'
\echo '========================================================'
