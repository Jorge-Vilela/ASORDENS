import { ref } from 'vue'const formAberto = ref(false)
const chamadoEmEdicao = ref(null)
const detalheId = ref(null)
const versao = ref(0)

export function useChamados() {
  return {
    formAberto,
    chamadoEmEdicao,
    detalheId,
    versao,
    abrirNovo() {
      chamadoEmEdicao.value = null
      formAberto.value = true
    },
    abrirEdicao(chamado) {
      chamadoEmEdicao.value = chamado
      formAberto.value = true
    },
    fecharForm() {
      formAberto.value = false
    },
    abrirDetalhe(id) {
      detalheId.value = id
    },
    fecharDetalhe() {
      detalheId.value = null
    },
    atualizarListas() {
      versao.value++
    },
  }
}