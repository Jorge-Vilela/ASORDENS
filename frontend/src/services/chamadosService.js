import { api } from './api'
import { chamados } from '../stores/dados'

const normalizar = (c) => ({
  ...c,
  id: `CHM-${c.id}`,
  status: (c.status || 'aberto').toLowerCase(),
  solicitante: c.solicitante || '',
  categoria: c.categoria || '',
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
    const { data } = await api.get('/chamados')
    chamados.splice(0, chamados.length, ...data.map(normalizar))
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
    const { data } = await api.post(`/chamados/${idApi(id)}/puxar`)
    return guardar(data)
  },
  async alterarStatus(id, status) {
    const { data } = await api.patch(`/chamados/${idApi(id)}/status`, { status })
    return guardar(data)
  },
  async enviarMensagem(id, { texto, anexos }) {
    const { data } = await api.post(`/chamados/${idApi(id)}/mensagens`, { texto, anexos: await anexosParaApi(anexos) })
    return guardar(data)
  },
}
