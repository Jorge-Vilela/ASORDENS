import { api } from './api'
import { chamados } from '../stores/dados'

export const normalizar = (c) => ({
  ...c,
  id: `CHM-${c.id}`,
  status: (c.status || 'aberto').toLowerCase(),
  solicitante: c.solicitanteNome || '',
  categoria: c.categoriaNome || '',
  agente: c.agenteNome || '',
  equipe: c.equipeNome || '',
  prioridade: c.prioridade || 'baixa',
  atualizadoEm: c.atualizadoEm || c.dataAbertura,
  anexos: c.anexos || [],
  mensagens: c.mensagens || [],
})
const idApi = (id) => encodeURIComponent(String(id).replace(/^CHM-/, ''))

async function anexosParaApi(anexos = []) {
  return Promise.all(anexos.map(async ({ arquivo, id, nome, tipo, tamanho, url }) => {
    if (arquivo) {
      url = await new Promise((resolve, reject) => {
        const leitor = new FileReader()
        leitor.onload = () => resolve(leitor.result)
        leitor.onerror = () => reject(new Error(`Não foi possível ler ${nome}.`))
        leitor.readAsDataURL(arquivo)
      })
    }
    return { id, nome, tipo: tipo || 'application/octet-stream', tamanho, url }
  }))
}

function guardar(dados) {
  const chamado = normalizar(dados)
  const indice = chamados.findIndex((c) => c.id === chamado.id)
  if (indice < 0) chamados.unshift(chamado)
  else chamados.splice(indice, 1, chamado)
  return chamado
}

export const chamadosService = {
  async listar(filtro = {}) {
    const itens = []
    let page = 0, data
    do {
      ;({ data } = await api.get('/chamados', { params: { page, size: 100 } }))
      itens.push(...data.content)
      page++
    } while (page < data.totalPages)
    chamados.splice(0, chamados.length, ...itens.map(normalizar))
    return chamados.filter((c) => {
      if (filtro.status && !filtro.status.includes(c.status)) return false
      if (filtro.semAgente && c.agente) return false
      if (filtro.agente && c.agente !== filtro.agente) return false
      return true
    })
  },
  async buscar(id) {
    const { data } = await api.get(`/chamados/${idApi(id)}`)
    return guardar(data)
  },
  async criar(dados) {
    const { data } = await api.post('/chamados', { ...dados, anexos: await anexosParaApi(dados.anexos) })
    return guardar(data)
  },
  async atualizar(id, dados) {
    const { data } = await api.put(`/chamados/${idApi(id)}`, { ...dados, anexos: await anexosParaApi(dados.anexos) })
    return guardar(data)
  },
  async puxar(id) {
    const { data } = await api.patch(`/chamados/${idApi(id)}/assumir`)
    return guardar(data)
  },
  async alterarStatus(id, status) {
    const { data } = await api.patch(`/chamados/${idApi(id)}/status`, { status })
    return guardar(data)
  },
  async finalizar(id) { const { data } = await api.patch(`/chamados/${idApi(id)}/finalizar`); return guardar(data) },
  async avaliar(id, dados) { const { data } = await api.post(`/chamados/${idApi(id)}/avaliacao`, dados); return guardar(data) },
  async excluir(id) { await api.delete(`/chamados/${idApi(id)}`); const i = chamados.findIndex(c => c.id === id); if (i >= 0) chamados.splice(i, 1) },
  async enviarMensagem(id, { texto }) {
    const { data } = await api.post(`/chamados/${idApi(id)}/interacoes`, { texto })
    return guardar(data)
  },
}
