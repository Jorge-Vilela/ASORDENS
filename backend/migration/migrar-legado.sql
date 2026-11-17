BEGIN;
ALTER TABLE tb_chamados ADD COLUMN IF NOT EXISTS solicitante_id bigint;
ALTER TABLE tb_chamados ADD COLUMN IF NOT EXISTS categoria_id bigint;
INSERT INTO categorias (nome, horas)
SELECT DISTINCT trim(categoria), 24 FROM tb_chamados c WHERE nullif(trim(categoria), '') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM categorias x WHERE lower(x.nome) = lower(trim(c.categoria)));
INSERT INTO usuarios (nome,email,senha,perfil,ativo)
SELECT n.nome, 'historico-' || md5(lower(n.nome)) || '@invalid.local', '!conta-desativada', n.perfil, false FROM (
 SELECT DISTINCT trim(solicitante) nome, 'USER' perfil FROM tb_chamados WHERE nullif(trim(solicitante),'') IS NOT NULL
 UNION SELECT DISTINCT trim(agente), 'AGENTE' FROM tb_chamados WHERE nullif(trim(agente),'') IS NOT NULL
) n WHERE NOT EXISTS (SELECT 1 FROM usuarios u WHERE lower(u.nome)=lower(n.nome)) ON CONFLICT (email) DO NOTHING;
UPDATE tb_chamados c SET solicitante_id = (SELECT min(id) FROM usuarios u WHERE lower(u.nome)=lower(trim(c.solicitante))) WHERE solicitante_id IS NULL;
UPDATE tb_chamados c SET agente_id = (SELECT min(id) FROM usuarios u WHERE lower(u.nome)=lower(trim(c.agente))) WHERE agente_id IS NULL AND nullif(trim(c.agente),'') IS NOT NULL;
UPDATE tb_chamados c SET categoria_id = (SELECT min(id) FROM categorias x WHERE lower(x.nome)=lower(trim(c.categoria))) WHERE categoria_id IS NULL;
UPDATE tb_chamados SET status=upper(status), prioridade=upper(prioridade);
ALTER TABLE tb_chamados ALTER COLUMN solicitante_id SET NOT NULL;
ALTER TABLE tb_chamados ALTER COLUMN categoria_id SET NOT NULL;
ALTER TABLE tb_chamados ADD CONSTRAINT fk_chamados_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuarios(id);
ALTER TABLE tb_chamados ADD CONSTRAINT fk_chamados_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id);
COMMIT;
