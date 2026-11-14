import { chamadosService } from './chamadosService'
import { porCategoria, seriePorPeriodo } from './indicadores'

export const dashboardService = {
  async resumo() {
    const chamados = await chamadosService.listar()
    const contar = (fn) => chamados.filter(fn).length
    return {
      kpis: {
        abertos: contar((c) => c.status === 'aberto'),
        emAndamento: contar((c) => c.status === 'em_andamento'),
        resolvidos: contar((c) => c.status === 'resolvido'),
        altaPrioridade: contar((c) => c.prioridade === 'alta'),
      },
      porDia: seriePorPeriodo(chamados, 7),
      porCategoria: porCategoria(chamados),
      recentes: [...chamados].sort((a, b) => b.atualizadoEm.localeCompare(a.atualizadoEm)).slice(0, 6),
    }
  },
}
