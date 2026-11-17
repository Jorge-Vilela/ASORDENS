import { reactive } from 'vue'

export const chamados = reactive([])
export const usuarioAtual = reactive({ nome: '', cargo: '', email: '', perfil: '', telefone: '', disponibilidade: 'online', ...JSON.parse(localStorage.getItem('asordens_usuario') || '{}') })
export const preferencias = reactive({})
