const concluido = (c) => ['resolvido', 'fechado'].includes(c.status)

export function seriePorPeriodo(chamados, dias) {
  const agora = new Date()
  const inicio = new Date(agora.getFullYear(), agora.getMonth(), agora.getDate())
  inicio.setDate(inicio.getDate() - dias + 1)
  const quantidade = Math.min(dias, 7)
  const tamanho = Math.ceil(dias / quantidade)
  const serie = []
  for (let i = 0; i < dias; i += tamanho) {
    const de = new Date(inicio)
    de.setDate(de.getDate() + i)
    const ate = new Date(de)
    ate.setDate(ate.getDate() + Math.min(tamanho, dias - i))
    const dentro = (data) => data && new Date(data) >= de && new Date(data) < ate
    serie.push({
      dia: dias === 7 ? de.toLocaleDateString('pt-BR', { weekday: 'short' }) : de.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit' }),
      abertos: chamados.filter((c) => dentro(c.dataAbertura)).length,
      resolvidos: chamados.filter((c) => concluido(c) && dentro(c.resolvidoEm)).length,
    })
  }
  return serie
}

export function porCategoria(chamados) {
  const contagem = new Map()
  for (const c of chamados) {
    const categoria = c.categoria || 'Sem categoria'
    contagem.set(categoria, (contagem.get(categoria) || 0) + 1)
  }
  return [...contagem].map(([categoria, total]) => ({ categoria, total })).sort((a, b) => b.total - a.total)
}

export function relatorioDosChamados(chamados, dias) {
  const inicio = new Date()
  inicio.setHours(0, 0, 0, 0)
  inicio.setDate(inicio.getDate() - dias + 1)
  const doPeriodo = chamados.filter((c) => new Date(c.atualizadoEm) >= inicio)
  const finalizados = doPeriodo.filter(concluido)
  const duracoes = finalizados.filter((c) => c.resolvidoEm && c.dataAbertura)
    .map((c) => Math.max(0, (new Date(c.resolvidoEm) - new Date(c.dataAbertura)) / 3600000))
  const ranking = new Map()
  for (const c of doPeriodo) {
    if (!c.agente) continue
    if (!ranking.has(c.agente)) ranking.set(c.agente, { agente: c.agente, total: 0, concluidos: 0 })
    const agente = ranking.get(c.agente)
    agente.total++
    if (concluido(c)) agente.concluidos++
  }
  return {
    total: doPeriodo.length,
    altaPrioridade: doPeriodo.filter((c) => c.prioridade === 'alta').length,
    taxaResolucao: doPeriodo.length ? Math.round(finalizados.length / doPeriodo.length * 100) : 0,
    tempoMedioHoras: duracoes.length ? Math.round(duracoes.reduce((a, b) => a + b, 0) / duracoes.length * 10) / 10 : 0,
    serie: seriePorPeriodo(chamados, dias),
    porCategoria: porCategoria(doPeriodo),
    ranking: [...ranking.values()].sort((a, b) => b.concluidos - a.concluidos),
    chamados: doPeriodo,
  }
}
