import { api } from './api'
import { normalizar } from './chamadosService'
import { relatorioDosChamados } from './indicadores'
export const relatoriosService = {
  async gerar(dias, filtros = {}) {
    const fim = new Date(); const inicio = new Date(fim); inicio.setDate(inicio.getDate() - dias + 1)
    const iso = d => [d.getFullYear(), String(d.getMonth()+1).padStart(2,'0'), String(d.getDate()).padStart(2,'0')].join('-')
    const { data } = await api.get('/relatorios/chamados', { params: { inicio: filtros.inicio || iso(inicio), fim: filtros.fim || iso(fim), status: filtros.status || undefined, equipeId: filtros.equipeId || undefined } })
    return relatorioDosChamados(data.map(normalizar), dias)
  },
}
