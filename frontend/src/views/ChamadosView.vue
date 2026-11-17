<script setup>
import { ref, watch, onMounted } from 'vue'
import { usuarioAtual } from '../stores/dados'
import { useRoute } from 'vue-router'
import { api } from '../services/api'
import { chamadosService, normalizar } from '../services/chamadosService'
import { useChamados } from '../composables/useChamados'
import { statusLabel, prioridadeLabel, formatarData } from '../utils/chamados'
import { Search } from 'lucide-vue-next'
import StatusBadge from '../components/StatusBadge.vue'
import ConfirmarExclusao from '../components/ConfirmarExclusao.vue'
const chamadoParaExcluir = ref(null), excluindo = ref(false)
async function excluirChamado() { if (excluindo.value) return; excluindo.value = true; try { await chamadosService.excluir(chamadoParaExcluir.value.id); chamadoParaExcluir.value = null; if (itens.value.length === 1 && page.value > 0) page.value--; else await carregar() } catch {} finally { excluindo.value = false } }
const route = useRoute(), { versao, abrirDetalhe } = useChamados()
const categorias = ref([]), categoriaId = ref('')
onMounted(async () => { try { categorias.value = (await api.get('/categorias')).data } catch {} })
const itens = ref([]), page = ref(0), totalPages = ref(0), total = ref(0), status = ref(''), prioridade = ref(''), busca = ref(''), carregando = ref(false), erro = ref('')
let sequencia = 0
async function carregar() { const n = ++sequencia; carregando.value = true; erro.value = ''; try { const params = { page: page.value, size: 10, sort: 'dataAbertura,desc' }; const filtro = typeof route.meta.filtro === 'function' ? route.meta.filtro() : route.meta.filtro; const st = status.value || (filtro?.status?.length === 1 ? filtro.status[0] : ''); if (st) params.status = st.toUpperCase(); if (prioridade.value) params.prioridade = prioridade.value.toUpperCase(); if (busca.value.trim()) params.titulo = busca.value.trim(); if (categoriaId.value) params.categoriaId = categoriaId.value; if (filtro?.semAgente) params.semAgente = true; if (filtro?.agente) params.agenteId = usuarioAtual.id; if (filtro?.status?.length > 1) params.somenteAtivos = true; const { data } = await api.get('/chamados', { params }); if (n !== sequencia) return; itens.value = data.content.map(normalizar); totalPages.value = data.totalPages; total.value = data.totalElements } catch(e) { if (n === sequencia) erro.value = e.message } finally { if (n === sequencia) carregando.value = false } }
let timer; watch([status, prioridade, busca, categoriaId], () => { page.value = 0; clearTimeout(timer); timer = setTimeout(carregar, 250) }); watch([() => route.path, versao], () => { page.value = 0; carregar() }, { immediate: true }); watch(page, carregar)
</script>
<template>
  <section class="gestao-card lista">
    <div class="gestao-cabecalho lista-cabecalho"><div><h2>Chamados</h2><p>Encontre e acompanhe os atendimentos.</p></div><span class="gestao-contagem">{{ total }} {{ total === 1 ? 'chamado' : 'chamados' }}</span></div>
    <div class="gestao-filtros"><label class="busca"><Search :size="16" /><input v-model="busca" aria-label="Buscar por título" placeholder="Buscar por título..."></label><select v-model="status" aria-label="Filtrar por status"><option value="">Todos os status</option><option v-for="(label, value) in statusLabel" :key="value" :value="value">{{ label }}</option></select><select v-model="categoriaId" aria-label="Filtrar por categoria"><option value="">Todas as categorias</option><option v-for="c in categorias" :key="c.id" :value="c.id">{{ c.nome }}</option></select><select v-model="prioridade" aria-label="Filtrar por prioridade"><option value="">Todas as prioridades</option><option v-for="(label, value) in prioridadeLabel" :key="value" :value="value">{{ label }}</option></select></div>
    <p v-if="erro" class="gestao-erro" role="alert">{{ erro }}</p>
    <div class="gestao-rolagem"><table class="gestao-tabela"><thead><tr><th>ID</th><th>Título</th><th>Solicitante</th><th>Categoria</th><th>Prioridade</th><th>Status</th><th>Agente</th><th>Atualizado</th><th v-if="usuarioAtual.perfil === 'ADMIN'">Ações</th></tr></thead><tbody>
      <tr v-for="c in itens" v-show="!carregando" :key="c.id" class="linha" tabindex="0" @click="abrirDetalhe(c.id)" @keydown.enter.self="abrirDetalhe(c.id)"><td class="id">{{ c.id }}</td><td class="gestao-nome">{{ c.titulo }}</td><td>{{ c.solicitante }}</td><td><span class="categoria">{{ c.categoria }}</span></td><td><span class="prioridade" :class="`prioridade-${c.prioridade}`">{{ prioridadeLabel[c.prioridade] }}</span></td><td><StatusBadge :status="c.status" /></td><td>{{ c.agente || '—' }}</td><td>{{ formatarData(c.atualizadoEm) }}</td><td v-if="usuarioAtual.perfil === 'ADMIN'" class="gestao-linha-acoes"><button class="perigo" @click.stop="chamadoParaExcluir = c">Excluir</button></td></tr>
      <tr v-if="carregando || !itens.length"><td colspan="8" class="gestao-vazio">{{ carregando ? 'Carregando chamados...' : 'Nenhum chamado encontrado.' }}</td></tr>
    </tbody></table></div>
    <footer class="rodape"><span>Mostrando {{ itens.length }} de {{ total }} chamados</span><div class="paginacao"><button class="gestao-botao secundario" :disabled="page === 0 || carregando" @click="page--">Anterior</button><span>Página {{ page + 1 }} de {{ Math.max(1, totalPages) }}</span><button class="gestao-botao secundario" :disabled="page + 1 >= totalPages || carregando" @click="page++">Próxima</button></div></footer>
  </section>
  <ConfirmarExclusao v-if="chamadoParaExcluir" :chamado="chamadoParaExcluir" :excluindo="excluindo" @cancelar="chamadoParaExcluir = null" @confirmar="excluirChamado" />
</template>
<style scoped>
.gestao-filtros { display: flex; flex-wrap: wrap; gap: 10px; padding: 0 24px 20px; }.gestao-filtros select { width: auto; min-width: 155px; }.busca { flex: 1 1 220px; display: flex; align-items: center; gap: 9px; padding-left: 12px; border: 1px solid #dce3ed; border-radius: 6px; color: #8996a7; }.busca:focus-within { border-color: #5573a3; box-shadow: 0 0 0 3px var(--color-primary-soft); }.busca input { border: 0; box-shadow: none !important; }.linha { cursor: pointer; }.linha:focus-visible { outline: 2px solid #5573a3; outline-offset: -2px; }.id { color: #2563eb !important; font-weight: 600; }.categoria { padding: 4px 8px; border-radius: 4px; background: #eef2f7; color: #53647b; }.prioridade { font-weight: 600; }.prioridade-alta { color: #c44e48; }.prioridade-media { color: #bb861d; }.prioridade-baixa { color: #7b8797; }.rodape { display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap; padding: 14px 24px; border-top: 1px solid var(--color-border); background: #fafbfd; color: var(--color-text-muted); font-size: 13px; }.paginacao { display: flex; align-items: center; gap: 12px; }.paginacao .gestao-botao { min-height: 34px; }.gestao-erro { margin-bottom: 18px; }
@media (max-width: 650px) { .gestao-filtros { padding: 0 18px 18px; }.gestao-filtros select { flex: 1 1 140px; }.rodape { padding: 14px 18px; } }
</style>
