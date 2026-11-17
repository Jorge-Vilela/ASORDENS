import axios from 'axios'
import { ref } from 'vue'

export const erroApi = ref('')
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api', timeout: 60000 })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('asordens_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !error.config?.url?.includes('/auth/login')) {
      localStorage.removeItem('asordens_token')
      localStorage.removeItem('asordens_usuario')
      window.location.assign('/login')
    }
    const detalhes = error.response?.data?.detalhes
    const mensagemValidacao = Array.isArray(detalhes) ? detalhes.map((item) => item.mensagem).filter(Boolean).join(' ') : ''
    const mensagem = (error.config?.url?.includes('/auth/login') && [401, 403].includes(error.response?.status) ? 'E-mail ou senha inválidos.' : null) || mensagemValidacao || error.response?.data?.detail || error.response?.data?.message || error.response?.data?.erro
      || (error.response?.status === 403 ? 'Voce nao tem permissao para realizar esta acao.' : error.response ? `A operação falhou (${error.response.status}).` : 'Não foi possível conectar à API. Tente novamente.')
    erroApi.value = mensagem
    return Promise.reject(new Error(mensagem))
  },
)
