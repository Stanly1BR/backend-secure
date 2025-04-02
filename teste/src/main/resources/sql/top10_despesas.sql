-- Top 10 operadoras (último trimestre)
SELECT
    o.id,
    o.nome AS razao_social,
    SUM(d.despesa_hospitalar + d.despesa_ambulatorial + COALESCE(d.despesa_odontologica, 0)) AS total_despesas
FROM demonstracoes_contabeis d
JOIN operadoras o ON d.operadora_id = o.id
WHERE d.data BETWEEN CURRENT_DATE - INTERVAL '3 months' AND CURRENT_DATE
GROUP BY o.id, o.nome
ORDER BY total_despesas DESC
LIMIT 10;