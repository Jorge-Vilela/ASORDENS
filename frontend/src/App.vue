<script setup>
import { onMounted, ref } from 'vue'
import { configuracoesService } from './services/configuracoesService'
import { chamadosService } from './services/chamadosService'
import { erroApi } from './services/api'

const pronto = ref(false)
const carregando = ref(false)
async function carregar() {
  carregando.value = true
  erroApi.value = ''
  try {
    await Promise.all([configuracoesService.carregar(), chamadosService.listar()])
    pronto.value = true
  } catch { /* A mensagem da API é exibida abaixo, com opção de tentar novamente. */ }
  finally { carregando.value = false }
}
onMounted(carregar)
</script>

<template>
  <div v-if="erroApi" class="aviso-api" role="alert">
    {{ erroApi }}
    <button v-if="!pronto" :disabled="carregando" @click="carregar">Tentar novamente</button>
    <button v-else @click="erroApi = ''">Fechar</button>
  </div>
  <RouterView v-if="pronto" />
  <p v-else class="inicio-api">{{ carregando ? 'Conectando ao servidor...' : 'Não foi possível carregar os dados.' }}</p>
</template>

<style scoped>
.aviso-api { padding: 12px 20px; background: #fff0ed; color: #a12619; position: sticky; top: 0; z-index: 1000; }
.aviso-api button { margin-left: 16px; padding: 5px 12px; }
.inicio-api { padding: 40px; }
</style>
