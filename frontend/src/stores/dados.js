import { reactive } from 'vue'

export const chamados = reactive([])
export const usuarioAtual = reactive({ nome: '', cargo: '', email: '', telefone: '', disponibilidade: 'online' })
export const preferencias = reactive({})
