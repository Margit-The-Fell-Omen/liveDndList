\echo ''
\echo '========================================================'
\echo '   TRANSACTION BEHAVIOR DEMO - DATABASE STATE CHECK'
\echo '   Timestamp:' :TIMESTAMP
\echo '========================================================'
\echo ''

-- Summary table
\echo '📊 SUMMARY COMPARISON:'
\echo ''

SELECT 
    test_type,
    CASE WHEN char_count > 0 THEN '✅ EXISTS' ELSE '❌ NONE' END as character,
    char_count,
    equipment_count,
    CASE 
        WHEN test_type = 'WITH @Transactional' AND char_count = 0 
            THEN '✅ CORRECT (Full Rollback)'
        WHEN test_type = 'WITHOUT @Transactional' AND char_count > 0 
            THEN '⚠️  PROBLEM (Partial Data)'
        ELSE 'ℹ️  Check manually'
    END as status
FROM (
    SELECT 
        'WITH @Transactional' as test_type,
        (SELECT COUNT(*) FROM characters WHERE name LIKE '%FAIL_WITH_TRANSACTION%') as char_count,
        (SELECT COUNT(*) FROM equipment e 
         JOIN characters c ON e.character_id = c.id 
         WHERE c.name LIKE '%FAIL_WITH_TRANSACTION%') as equipment_count
    UNION ALL
    SELECT 
        'WITHOUT @Transactional',
        (SELECT COUNT(*) FROM characters WHERE name LIKE '%FAIL_NO_TRANSACTION%'),
        (SELECT COUNT(*) FROM equipment e 
         JOIN characters c ON e.character_id = c.id 
         WHERE c.name LIKE '%FAIL_NO_TRANSACTION%')
) as results;

\echo ''
\echo '📋 DETAILED VIEW:'
\echo ''

-- All FAIL characters
\echo '--- All FAIL Characters ---'
SELECT id, name, race, 
       (SELECT COUNT(*) FROM equipment WHERE character_id = characters.id) as equip_count,
       created_at
FROM characters 
WHERE name LIKE '%FAIL%'
ORDER BY created_at DESC;

-- Equipment details
\echo ''
\echo '--- Equipment for FAIL Characters ---'
SELECT c.id, c.name as character, e.name as equipment, e.type
FROM characters c
LEFT JOIN equipment e ON e.character_id = c.id
WHERE c.name LIKE '%FAIL%'
ORDER BY c.id, e.id;

\echo ''
\echo '========================================================'
\echo '   END OF REPORT'
\echo '========================================================'
