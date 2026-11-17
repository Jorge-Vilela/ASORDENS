<script setup>
import { onMounted, ref } from 'vue'
import { configuracoesService } from './services/configuracoesService'
import { chamadosService } from './services/chamadosService'
import { api, erroApi } from './services/api'
import { usuarioAtual } from './stores/dados'
import { useRouter } from 'vue-router'
import { ArrowRight, Compass, Eye, EyeOff, LockKeyhole, Mail, ShieldCheck, CheckCircle2 } from 'lucide-vue-next'
const router = useRouter()
const autenticado = ref(!!localStorage.getItem('asordens_token'))
const email = ref('')
const senha = ref('')
const mostrarSenha = ref(false)
function sair() {
  localStorage.removeItem('asordens_token')
  localStorage.removeItem('asordens_usuario')
  window.location.assign('/login')
}
async function login() {
  carregando.value = true
  erroApi.value = ''
  try {
    const { data } = await api.post('/auth/login', { email: email.value, senha: senha.value })
    localStorage.setItem('asordens_token', data.token)
    Object.assign(usuarioAtual, { email: data.email, nome: data.email, perfil: data.perfil })
    localStorage.setItem('asordens_usuario', JSON.stringify(usuarioAtual))
    router.push('/')
    autenticado.value = true
    await carregar()
  } catch {} finally { carregando.value = false }
}

const pronto = ref(false)
const carregando = ref(false)
async function carregar() {
  carregando.value = true
  erroApi.value = ''
  try {
    const [, , usuarios] = await Promise.all([configuracoesService.carregar(), chamadosService.listar(), api.get('/usuarios', { params: { size: 1000 } })])
    const usuario = usuarios.data.content.find(u => u.email === usuarioAtual.email)
    if (usuario) Object.assign(usuarioAtual, usuario)
    localStorage.setItem('asordens_usuario', JSON.stringify(usuarioAtual))
    pronto.value = true
  } catch {  }
  finally { carregando.value = false }
}
onMounted(() => { if (autenticado.value) carregar() })
</script>

<template>
  <div v-if="erroApi && autenticado" class="aviso-api" role="alert">
    {{ erroApi }}
    <button v-if="autenticado && !pronto" :disabled="carregando" @click="carregar">Tentar novamente</button>
    <button v-if="autenticado && !pronto" @click="sair">Sair e entrar novamente</button>
    <button v-else @click="erroApi = ''">Fechar</button>
  </div>
  <main v-if="!autenticado" class="login">
    <section class="login-apresentacao">
      <div class="login-marca"><span class="marca-icone"><Compass :size="21" /></span><span>Às Ordens<small>GESTÃO DE CHAMADOS</small></span></div>
      <div class="apresentacao-conteudo">
        <span class="etiqueta"><span></span> ORGANIZAÇÃO EM CADA ATENDIMENTO</span>
        <h1>Um jeito melhor de <em>cuidar de cada chamado.</em></h1>
        <p>Mais clareza para sua equipe, do primeiro contato até a solução.</p>
        <div class="destaque"><span class="destaque-icone"><CheckCircle2 :size="20" /></span><div><strong>Tudo em um só lugar</strong><small>Acompanhe, organize e resolva com tranquilidade.</small></div></div>
      </div>
      <p class="apresentacao-rodape">Às Ordens <span>·</span> Feito para facilitar o dia a dia.</p>
    </section>
    <section class="login-acesso">
      <div class="login-mobile-marca"><span class="marca-icone"><Compass :size="19" /></span> Às Ordens</div>
      <div class="login-formulario">
        <div class="login-cabecalho"><span class="sobretitulo">BEM-VINDO DE VOLTA</span><h2>Entre na sua conta</h2><p>Acesse sua área para acompanhar os chamados.</p></div>
        <div v-if="erroApi" class="erro-login" role="alert">{{ erroApi }}</div>
        <form @submit.prevent="login">
          <label for="login-email">E-mail</label><div class="campo-login"><Mail :size="18" aria-hidden="true" /><input id="login-email" v-model="email" type="email" autocomplete="username" placeholder="seuemail@exemplo.com" required /></div>
          <label for="login-senha">Senha</label><div class="campo-login"><LockKeyhole :size="18" aria-hidden="true" /><input id="login-senha" v-model="senha" :type="mostrarSenha ? 'text' : 'password'" autocomplete="current-password" placeholder="Digite sua senha" required /><button class="mostrar-senha" type="button" :aria-label="mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'" @click="mostrarSenha = !mostrarSenha"><EyeOff v-if="mostrarSenha" :size="18" /><Eye v-else :size="18" /></button></div>
          <button class="entrar" type="submit" :disabled="carregando">{{ carregando ? 'Entrando...' : 'Entrar' }} <ArrowRight :size="18" /></button>
        </form>
        <div class="login-seguro"><ShieldCheck :size="16" /> Acesso seguro à plataforma</div>
      </div>
      <p class="login-rodape">Às Ordens <span>·</span> Gestão de chamados</p>
    </section>
  </main>
  <RouterView v-else-if="pronto" />
  <p v-else class="inicio-api">{{ carregando ? 'Conectando ao servidor...' : 'Não foi possível carregar os dados.' }}</p>
</template>

<style scoped>
.aviso-api { padding: 12px 20px; background: #fff0ed; color: #a12619; position: sticky; top: 0; z-index: 1000; }
.aviso-api button { margin-left: 16px; padding: 5px 12px; }
.inicio-api { padding: 40px; }
.login { min-height: 100vh; display: grid; grid-template-columns: minmax(0, 1fr) minmax(450px, 0.95fr); background: #fff; }
.login-apresentacao { position: relative; overflow: hidden; display: flex; flex-direction: column; min-height: 100vh; padding: 48px clamp(40px, 6vw, 96px); background: #0b1f4b; color: white; }
.login-apresentacao::after { content: ''; position: absolute; width: 470px; height: 470px; right: -250px; bottom: -210px; border: 1px solid rgba(201,153,58,.3); border-radius: 50%; box-shadow: 0 0 0 70px rgba(255,255,255,.025), 0 0 0 140px rgba(255,255,255,.025); pointer-events: none; }
.login-marca, .login-mobile-marca { display: flex; align-items: center; gap: 12px; font-size: 19px; font-weight: 700; letter-spacing: -.03em; }
.login-marca small { display: block; margin-top: 4px; font-size: 9px; letter-spacing: .17em; font-weight: 600; color: #b6c3df; }
.marca-icone { display: grid; place-items: center; width: 42px; height: 42px; border: 1px solid rgba(201,153,58,.4); border-radius: 10px; background: rgba(201,153,58,.12); color: #d7aa52; flex-shrink: 0; }
.apresentacao-conteudo { position: relative; z-index: 1; margin: auto 0; max-width: 540px; padding: 80px 0; }
.etiqueta { display: inline-flex; align-items: center; gap: 9px; color: #d9b86e; font-size: 10px; font-weight: 700; letter-spacing: .15em; }
.etiqueta span { width: 22px; height: 1px; background: #c9993a; }
.apresentacao-conteudo h1 { margin-top: 25px; font-size: clamp(38px, 4.3vw, 62px); line-height: 1.12; letter-spacing: -.045em; font-weight: 650; }
.apresentacao-conteudo h1 em { color: #dfbc78; font-style: normal; }
.apresentacao-conteudo > p { max-width: 380px; margin-top: 20px; color: #b7c4dd; font-size: 16px; line-height: 1.7; }
.destaque { display: flex; align-items: center; gap: 13px; margin-top: 52px; }
.destaque-icone { display: grid; place-items: center; width: 42px; height: 42px; border-radius: 10px; background: rgba(255,255,255,.08); color: #dfbc78; }
.destaque strong, .destaque small { display: block; }.destaque strong { font-size: 13px; }.destaque small { margin-top: 5px; color: #aab9d4; font-size: 12px; }
.apresentacao-rodape { position: relative; z-index: 1; color: #9dacc9; font-size: 12px; }.apresentacao-rodape span, .login-rodape span { color: #c9993a; padding: 0 4px; }
.login-acesso { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 50px 32px 30px; background: #fbfcfe; }
.login-formulario { width: min(100%, 390px); margin: auto 0; }
.sobretitulo { color: #a97e2f; font-size: 10px; font-weight: 700; letter-spacing: .16em; }.login-cabecalho h2 { margin-top: 13px; color: var(--color-primary); font-size: 31px; letter-spacing: -.04em; }.login-cabecalho p { margin-top: 10px; color: var(--color-text-muted); line-height: 1.6; }
.login-formulario form { margin-top: 34px; }.login-formulario label { display: block; margin: 20px 0 9px; color: #283751; font-size: 13px; font-weight: 600; }
.campo-login { display: flex; align-items: center; gap: 11px; height: 50px; padding: 0 14px; border: 1px solid #dce3ed; border-radius: 7px; background: white; color: #8491a5; transition: border-color .15s, box-shadow .15s; }.campo-login:focus-within { border-color: #47689e; box-shadow: 0 0 0 3px #e8edf7; }.campo-login input { width: 100%; min-width: 0; border: 0; outline: 0; background: transparent; font: inherit; color: var(--color-text); }.campo-login input::placeholder { color: #a2aab7; }.mostrar-senha { border: 0; background: transparent; color: #8491a5; display: grid; place-items: center; }
.entrar { width: 100%; height: 50px; display: flex; justify-content: center; align-items: center; gap: 10px; margin-top: 28px; border: 0; border-radius: 7px; background: var(--color-primary); color: white; font-size: 14px; font-weight: 650; }.entrar:hover:not(:disabled) { background: var(--color-primary-hover); }.entrar:disabled { opacity: .65; cursor: wait; }
.login-seguro { display: flex; justify-content: center; align-items: center; gap: 7px; margin-top: 25px; color: #8894a5; font-size: 12px; }.login-seguro svg { color: #ae883d; }.login-rodape { color: #a2aab6; font-size: 12px; }.login-mobile-marca { display: none; }.erro-login { margin-top: 22px; padding: 12px 14px; border: 1px solid #f1c9c4; border-radius: 7px; background: #fff4f2; color: #a12619; line-height: 1.5; }
@media (max-width: 800px) { .login { grid-template-columns: 1fr; }.login-apresentacao { display: none; }.login-acesso { min-height: 100vh; padding: 32px 24px 24px; }.login-mobile-marca { display: flex; align-self: flex-start; color: var(--color-primary); }.login-formulario { margin: auto 0; padding: 50px 0; }.login-cabecalho h2 { font-size: 28px; } }
</style>
