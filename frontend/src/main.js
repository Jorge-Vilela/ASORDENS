import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import '@fontsource-variable/inter'
import './assets/styles/global.css'
import { erroApi } from './services/api'

const app = createApp(App)
app.config.errorHandler = (error) => { erroApi.value = error.message || 'Não foi possível concluir a operação.' }
app.use(router).mount('#app')
