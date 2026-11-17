import { api } from './api'
import { usuarioAtual, preferencias } from '../stores/dados'
import { categorias } from '../utils/chamados'

function aplicar(config) {
  const { categorias: nomes, preferencias: prefs, ...perfil } = config
  Object.assign(usuarioAtual, { telefone: perfil.telefone, disponibilidade: perfil.disponibilidade, cargo: perfil.cargo, equipe: perfil.equipe })
  Object.assign(preferencias, prefs)
  if (nomes) categorias.splice(0, categorias.length, ...nomes)
}

export const configuracoesService = {
  async carregar() { aplicar((await api.get('/configuracoes')).data) },
  async salvarPerfil(dados) { aplicar((await api.put('/me', dados)).data) },
  async salvarPreferencias(dados) { aplicar((await api.put('/me/preferencias', dados)).data) },
  async adicionarCategoria(nome) { aplicar((await api.post('/categorias', { nome })).data) },
  async removerCategoria(nome) { aplicar((await api.delete(`/categorias/${encodeURIComponent(nome)}`)).data) },
}
