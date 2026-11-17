<script setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../services/api'
const route = useRoute()
const itens = ref([]), equipes = ref([]), editando = ref(null), nome = ref(''), descricao = ref(''), horas = ref(24), ativo = ref(true), erro = ref('')
const recurso = () => route.path.slice(1)
async function carregar() { try { const r = await api.get('/' + recurso(), { params: recurso() === 'usuarios' ? { size: 1000 } : {} }); itens.value = r.data.content || r.data; if (recurso() === 'usuarios') equipes.value = (await api.get('/equipes')).data } catch (e) { erro.value = e.message } }
watch(() => route.path, () => { editando.value = null; carregar() }, { immediate: true })
function editar(item) { editando.value = item.id; nome.value = item.nome; descricao.value = item.descricao || ''; horas.value = item.horas; ativo.value = item.ativo }
function limpar() { editando.value = null; nome.value = ''; descricao.value = ''; horas.value = 24; ativo.value = true }
async function salvar() { try { const dados = recurso() === 'categorias' ? { nome: nome.value, horas: Number(horas.value) } : { nome: nome.value, descricao: descricao.value, ativo: ativo.value }; if (editando.value) await api.put('/' + recurso() + '/' + editando.value, dados); else await api.post('/' + recurso(), dados); limpar(); await carregar() } catch (e) { erro.value = e.message } }
async function remover(item) { if (!confirm('Excluir ' + item.nome + '?')) return; try { await api.delete('/' + recurso() + '/' + item.id); await carregar() } catch (e) { erro.value = e.message } }
async function alterar(item, campo, valor) { try { await api.patch('/usuarios/' + item.id + '/' + campo, null, { params: { [campo === 'equipe' ? 'equipeId' : 'perfil']: valor || undefined } }); await carregar() } catch (e) { erro.value = e.message; await carregar() } }
</script>
<template>
  <div class="gestao">
    <section class="gestao-card">
      <div class="gestao-cabecalho"><div><span class="gestao-sobretitulo">GERENCIAMENTO</span><h2>{{ editando ? 'Editar' : 'Adicionar' }} {{ recurso() === 'categorias' ? 'categoria' : 'equipe' }}</h2><p>{{ recurso() === 'categorias' ? 'Organize os chamados e defina o prazo de atendimento.' : 'Mantenha suas equipes de atendimento organizadas.' }}</p></div></div>
      <p v-if="erro" class="gestao-erro" role="alert">{{ erro }}</p>
      <form class="gestao-form" @submit.prevent="salvar">
        <label class="gestao-campo"><span>Nome</span><input v-model="nome" required minlength="2" :placeholder="recurso() === 'categorias' ? 'Ex.: Rede' : 'Ex.: Suporte técnico'"></label>
        <label v-if="recurso() === 'categorias'" class="gestao-campo campo-curto"><span>SLA em horas</span><input v-model="horas" type="number" min="1" required></label>
        <template v-else><label class="gestao-campo"><span>Descrição</span><input v-model="descricao" placeholder="Descreva a equipe"></label><label class="gestao-check"><input v-model="ativo" type="checkbox"> Equipe ativa</label></template>
        <div class="gestao-acoes"><button class="gestao-botao primario">{{ editando ? 'Salvar alterações' : 'Adicionar' }}</button><button v-if="editando" type="button" class="gestao-botao secundario" @click="limpar">Cancelar</button></div>
      </form>
    </section>
    <section class="gestao-card gestao-tabela-card"><div class="gestao-cabecalho lista-cabecalho"><div><h2>{{ recurso() === 'categorias' ? 'Categorias cadastradas' : 'Equipes cadastradas' }}</h2><p>Consulte e atualize as informações cadastradas.</p></div><span class="gestao-contagem">{{ itens.length }} {{ itens.length === 1 ? 'registro' : 'registros' }}</span></div>
      <div class="gestao-rolagem"><table class="gestao-tabela"><thead><tr><th>Nome</th><th>{{ recurso() === 'categorias' ? 'SLA' : 'Descrição' }}</th><th v-if="recurso() === 'equipes'">Status</th><th>Ações</th></tr></thead><tbody><tr v-for="item in itens" :key="item.id"><td class="gestao-nome">{{ item.nome }}</td><td>{{ recurso() === 'categorias' ? `${item.horas} horas` : item.descricao || '—' }}</td><td v-if="recurso() === 'equipes'"><span class="gestao-status" :class="{ inativo: !item.ativo }">{{ item.ativo ? 'Ativa' : 'Inativa' }}</span></td><td class="gestao-linha-acoes"><button @click="editar(item)">Editar</button><button class="perigo" @click="remover(item)">Excluir</button></td></tr><tr v-if="!itens.length"><td :colspan="recurso() === 'equipes' ? 4 : 3" class="gestao-vazio">Nenhum registro encontrado.</td></tr></tbody></table></div>
    </section>
  </div>
</template>
