<script setup>
import { ref, onMounted } from 'vue'
import { api } from '../services/api'
import BaseModal from '../components/BaseModal.vue'
import { usuarioAtual } from '../stores/dados'
const usuarios = ref([]), equipes = ref([]), erro = ref(''), editando = ref(null), form = ref({})
const usuarioSelecionado = ref(null), acao = ref(''), executando = ref(false), erroAcao = ref('')
function limpar() { editando.value = null; form.value = { nome: '', email: '', senha: '', perfil: 'USER', equipeId: null, ativo: true } }
async function carregar() { try { const [u, e] = await Promise.all([api.get('/usuarios', { params: { size: 1000 } }), api.get('/equipes')]); usuarios.value = u.data.content; equipes.value = e.data } catch (e) { erro.value = e.message } }
function editar(u) { editando.value = u.id; form.value = { ...u, senha: '' } }
async function salvar() { try { const dados = { ...form.value, equipeId: form.value.equipeId || null, senha: form.value.senha || null }; if (editando.value) await api.put('/usuarios/' + editando.value, dados); else await api.post('/usuarios', dados); limpar(); await carregar() } catch (e) { erro.value = e.message } }
function confirmar(u, tipo) { usuarioSelecionado.value = u; acao.value = tipo; erroAcao.value = '' }
function fecharConfirmacao() { if (!executando.value) usuarioSelecionado.value = null }
async function executarAcao() {
  if (executando.value) return
  executando.value = true
  erroAcao.value = ''
  try {
    if (acao.value === 'excluir') await api.delete('/usuarios/' + usuarioSelecionado.value.id)
    else await api.patch('/usuarios/' + usuarioSelecionado.value.id + '/desativar')
    if (editando.value === usuarioSelecionado.value.id) limpar()
    usuarioSelecionado.value = null
    await carregar()
  } catch (e) { erroAcao.value = e.message }
  finally { executando.value = false }
}
limpar(); onMounted(carregar)
</script>
<template>
  <div class="gestao">
    <section class="gestao-card">
      <div class="gestao-cabecalho"><div><span class="gestao-sobretitulo">GERENCIAMENTO</span><h2>{{ editando ? 'Editar usuário' : 'Adicionar usuário' }}</h2><p>Cadastre pessoas e defina o acesso de cada uma.</p></div></div>
      <p v-if="erro" class="gestao-erro" role="alert">{{ erro }}</p>
      <form class="gestao-form" @submit.prevent="salvar">
        <label class="gestao-campo"><span>Nome completo</span><input v-model="form.nome" required minlength="3" placeholder="Nome do usuário"></label>
        <label class="gestao-campo"><span>E-mail</span><input v-model="form.email" required type="email" placeholder="email@exemplo.com"></label>
        <label class="gestao-campo"><span>{{ editando ? 'Nova senha (opcional)' : 'Senha' }}</span><input v-model="form.senha" :required="!editando" type="password" minlength="6" placeholder="Mínimo de 6 caracteres"></label>
        <label class="gestao-campo campo-curto"><span>Perfil</span><select v-model="form.perfil"><option value="USER">Usuário</option><option value="AGENTE">Agente</option><option value="ADMIN">Administrador</option></select></label>
        <label class="gestao-campo"><span>Equipe</span><select v-model="form.equipeId"><option :value="null">Sem equipe</option><option v-for="e in equipes" :key="e.id" :value="e.id">{{ e.nome }}</option></select></label>
        <label v-if="editando" class="gestao-check"><input v-model="form.ativo" type="checkbox"> Usuário ativo</label>
        <div class="gestao-acoes"><button class="gestao-botao primario">{{ editando ? 'Salvar alterações' : 'Adicionar usuário' }}</button><button v-if="editando" type="button" class="gestao-botao secundario" @click="limpar">Cancelar</button></div>
      </form>
    </section>
    <section class="gestao-card gestao-tabela-card"><div class="gestao-cabecalho lista-cabecalho"><div><h2>Usuários cadastrados</h2><p>Gerencie perfis, equipes e acesso à plataforma.</p></div><span class="gestao-contagem">{{ usuarios.length }} {{ usuarios.length === 1 ? 'usuário' : 'usuários' }}</span></div>
      <div class="gestao-rolagem"><table class="gestao-tabela"><thead><tr><th>Nome</th><th>E-mail</th><th>Perfil</th><th>Equipe</th><th>Status</th><th>Ações</th></tr></thead><tbody>
        <tr v-for="u in usuarios" :key="u.id"><td class="gestao-nome">{{ u.nome }}</td><td>{{ u.email }}</td><td><span class="gestao-perfil">{{ u.perfil }}</span></td><td>{{ u.equipeNome || '—' }}</td><td><span class="gestao-status" :class="{ inativo: !u.ativo }">{{ u.ativo ? 'Ativo' : 'Inativo' }}</span></td><td class="gestao-linha-acoes"><button @click="editar(u)">Editar</button><button v-if="u.ativo && u.id !== usuarioAtual.id" @click="confirmar(u, 'desativar')">Desativar</button><button v-if="u.id !== usuarioAtual.id" class="perigo" @click="confirmar(u, 'excluir')">Excluir</button></td></tr>
        <tr v-if="!usuarios.length"><td colspan="6" class="gestao-vazio">Nenhum usuário encontrado.</td></tr>
      </tbody></table></div>
    </section>
  </div>
  <BaseModal v-if="usuarioSelecionado" :titulo="acao === 'excluir' ? 'Excluir usuário' : 'Desativar usuário'" largura="460px" :escape-ativo="!executando" @fechar="fecharConfirmacao">
    <p class="confirmacao-texto">{{ acao === 'excluir' ? 'Deseja excluir permanentemente este usuário?' : 'Deseja desativar o acesso deste usuário?' }}</p>
    <p class="confirmacao-nome">{{ usuarioSelecionado.nome }}</p>
    <p class="confirmacao-email">{{ usuarioSelecionado.email }}</p>
    <p class="confirmacao-aviso">{{ acao === 'excluir' ? 'Usuários vinculados a chamados ou interações não podem ser excluídos. Nesse caso, você pode desativar a conta.' : 'O usuário não poderá acessar o sistema enquanto estiver desativado.' }}</p>
    <p v-if="erroAcao" class="confirmacao-erro" role="alert">{{ erroAcao }}</p>
    <template #rodape>
      <button class="gestao-botao secundario" :disabled="executando" @click="fecharConfirmacao">Cancelar</button>
      <button class="confirmacao-botao" :disabled="executando" @click="executarAcao">{{ executando ? 'Aguarde...' : acao === 'excluir' ? 'Sim, excluir' : 'Sim, desativar' }}</button>
    </template>
  </BaseModal>
</template>
<style scoped>
.confirmacao-texto { line-height: 1.5; }.confirmacao-nome { margin-top: 14px; color: var(--color-primary); font-weight: 600; }.confirmacao-email { margin-top: 4px; color: var(--color-text-muted); font-size: 13px; }.confirmacao-aviso { margin-top: 16px; color: var(--color-text-muted); font-size: 13px; line-height: 1.5; }.confirmacao-erro { margin-top: 14px; padding: 10px 12px; border-radius: 6px; background: #fff4f2; color: #a12619; font-size: 13px; line-height: 1.5; }.confirmacao-botao { min-height: 41px; padding: 0 16px; border: 1px solid #b83e36; border-radius: 6px; background: #b83e36; color: white; font-weight: 600; }.confirmacao-botao:disabled { opacity: .5; cursor: wait; }
</style>
