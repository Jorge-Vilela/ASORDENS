<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
const router = useRouter()
function sair() { localStorage.removeItem('asordens_token'); localStorage.removeItem('asordens_usuario'); window.location.assign('/login') }
import { Bell, Inbox, LogOut } from 'lucide-vue-next'
import { chamados } from '../stores/dados'
import { usuarioAtual } from '../stores/dados'

const route = useRoute()

const dataHoje = computed(() => {
  const texto = new Date().toLocaleDateString('pt-BR', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
  })
  return `Hoje, ${texto}`
})

const minhaFila = computed(
  () =>
    chamados.filter(
      (c) =>
        c.agente === usuarioAtual.nome &&
        ['aberto', 'em_andamento'].includes(c.status),
    ).length,
)
</script>

<template>
  <header class="topbar">
    <div>
      <h1>{{ route.meta.title }}</h1>
      <p>{{ dataHoje }}</p>
    </div>

    <div class="acoes">
      <RouterLink to="/perfil" class="perfil-link"><span class="perfil-avatar">{{ (usuarioAtual.nome || '?').slice(0, 1).toUpperCase() }}</span><span class="perfil-texto"><strong>{{ usuarioAtual.nome }}</strong><small>{{ usuarioAtual.perfil }}</small></span></RouterLink>
      <button class="sair" aria-label="Sair da conta" title="Sair" @click="sair"><LogOut :size="17" /></button>
      <button class="sino" aria-label="Notificações">
        <Bell :size="18" />
        <span class="ponto"></span>
      </button>
      <RouterLink to="/minha-fila" class="btn-fila">
        <Inbox :size="16" />
        Fila
        <span class="contador">{{ minhaFila }}</span>
      </RouterLink>
    </div>
  </header>
</template>

<style scoped>
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg);
}

h1 {
  font-size: 20px;
  font-weight: 600;
  line-height: 1.2;
}

p {
  margin-top: 2px;
  font-size: 13px;
  color: var(--color-text-muted);
}

.acoes {
  display: flex;
  align-items: center;
  gap: 14px;
}
.perfil-link { display: flex; align-items: center; gap: 9px; color: var(--color-text); text-decoration: none; }
.perfil-avatar { display: grid; place-items: center; width: 35px; height: 35px; border-radius: 50%; background: var(--color-primary-soft); color: var(--color-primary); font-weight: 700; }
.perfil-texto { display: flex; flex-direction: column; gap: 2px; }.perfil-texto strong { font-size: 13px; }.perfil-texto small { color: var(--color-text-muted); font-size: 11px; }
.sair { display: grid; place-items: center; width: 36px; height: 36px; border: 1px solid var(--color-border); border-radius: 6px; background: white; color: #718096; }.sair:hover { color: var(--color-primary); background: var(--color-zebra); }
@media (max-width: 720px) { .perfil-texto { display: none; }.topbar { gap: 12px; padding: 14px 16px; }.acoes { gap: 8px; } }

.sino {
  position: relative;
  border: none;
  background: none;
  color: var(--color-text-muted);
  display: grid;
  place-items: center;
}

.ponto {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--color-accent);
}

.btn-fila {
  height: 40px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  background: #fff;
  font-size: 14px;
  font-weight: 500;
  color: var(--color-text);
  text-decoration: none;
}

.contador {
  min-width: 22px;
  height: 22px;
  border-radius: 11px;
  background: var(--color-primary);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: grid;
  place-items: center;
}
</style>
