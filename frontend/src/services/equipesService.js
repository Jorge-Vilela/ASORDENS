import { api } from './api'
import { chamadosService } from './chamadosService'

export const equipesService = {
  async listar() {
    const [{ data: agentes }, chamados] = await Promise.all([api.get('/agentes'), chamadosService.listar()])
    return agentes.map((a) => {
      const meus = chamados.filter((c) => c.agente === a.nome)
      const contar = (status) => meus.filter((c) => c.status === status).length
      return { ...a, abertos: contar('aberto'), emAndamento: contar('em_andamento'),
        resolvidos: contar('resolvido'), naFila: contar('aberto') + contar('em_andamento') }
    })
  },
}
