SELECT
    o.id,
    o.nome,
    o.cnpj,
    SUM(d.despesa_hospitalar + d.despesa_ambulatorial) AS total_despesas,
    ROUND(SUM(d.despesa_hospitalar + d.despesa_ambulatorial) / 3, 2) AS media_mensal,
    o.uf
FROM operadora o
JOIN demonstracao_contabil d ON o.id = d.operadora_id
WHERE
d.periodo BETWEEN DATE_TRUNC('quarter', CURRENT_DATE) AND CURRENT_DATE
AND d.status = 'APROVADO'
GROUP BY o.id, o.nome, o.cnpj, o.uf
ORDER BY total_despesas DESC
    LIMIT 10;