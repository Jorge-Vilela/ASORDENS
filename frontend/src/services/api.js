import axios from 'axios'
import { ref } from 'vue'

export const erroApi = ref('')
export const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api', timeout: 60000 })

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const mensagem = error.response?.data?.detail || error.response?.data?.message
      || (error.response ? `A operação falhou (${error.response.status}).` : 'Não foi possível conectar à API. Tente novamente.')
    erroApi.value = mensagem
    return Promise.reject(new Error(mensagem))
  },
)
