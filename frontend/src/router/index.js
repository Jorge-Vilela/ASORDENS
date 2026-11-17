import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layouts/MainLayout.vue'
import { usuarioAtual } from '../stores/dados'
const equipes = () => import('../views/CadastrosView.vue')

const atendimento = () => import('../views/AtendimentoView.vue')
const dashboard = () => import('../views/DashboardView.vue')
const configuracoes = () => import('../views/ConfiguracoesView.vue')
const emConstrucao = () => import('../views/EmConstrucaoView.vue')
const chamados = () => import('../views/ChamadosView.vue')
const relatorios = () => import('../views/RelatoriosView.vue')
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: { render: () => null }, meta: { public: true } },
    {
      path: '/',
      component: MainLayout,
      children: [
        { path: '', component: dashboard, meta: { title: 'Dashboard' } },
        { path: 'fila', component: chamados, meta: { title: 'Fila não atribuída', filtro: { semAgente: true } } },
        { path: 'abertos', component: chamados, meta: { title: 'Chamados Abertos', filtro: { status: ['aberto'] } } },
        { path: 'em-andamento', component: chamados, meta: { title: 'Em Andamento', filtro: { status: ['em_andamento'] } } },
        { path: 'resolvidos', component: chamados, meta: { title: 'Resolvidos', filtro: { status: ['resolvido'] } } },
        { path: 'chamados', component: chamados, meta: { title: 'Todos os Chamados' } },
        { path: 'equipes', component: equipes, meta: { title: 'Equipes', roles: ['ADMIN'] } },
        { path: 'categorias', component: equipes, meta: { title: 'Categorias', roles: ['ADMIN'] } },
        { path: 'usuarios', component: () => import('../views/UsuariosView.vue'), meta: { title: 'Usuarios', roles: ['ADMIN'] } },
        { path: 'perfil', component: configuracoes, meta: { title: 'Perfil' } },
        { path: 'relatorios', component: relatorios, meta: { title: 'Relatórios' } },
        { path: 'configuracoes', component: configuracoes, meta: { title: 'Configurações' } },
        { path: 'minha-fila', component: chamados, meta: { title: 'Minha fila', filtro: () => ({ agente: usuarioAtual.nome, status: ['aberto', 'em_andamento'] }) } },
        { path: 'atendimento/:id', component: atendimento, meta: { title: 'Atendimento' } },
      ],
    },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('asordens_token')
  if (!to.meta.public && !token) return '/login'
  if (to.path === '/login' && token) return '/'
  if (to.meta.roles && !to.meta.roles.includes(usuarioAtual.perfil)) return '/'
})

export default router
