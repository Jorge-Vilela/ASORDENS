<script setup>
import BaseModal from './BaseModal.vue'
defineProps({ chamado: { type: Object, required: true }, excluindo: Boolean })
defineEmits(['cancelar', 'confirmar'])
</script>
<template>
  <BaseModal titulo="Excluir chamado" largura="460px" @fechar="$emit('cancelar')">
    <p>Deseja excluir o chamado <strong>{{ chamado.id }}</strong>?</p>
    <p class="titulo">{{ chamado.titulo }}</p>
    <p class="aviso">Esta ação não pode ser desfeita.</p>
    <template #rodape>
      <button class="botao cancelar" :disabled="excluindo" @click="$emit('cancelar')">Cancelar</button>
      <button class="botao excluir" :disabled="excluindo" @click="$emit('confirmar')">{{ excluindo ? 'Excluindo...' : 'Sim, excluir' }}</button>
    </template>
  </BaseModal>
</template>
<style scoped>
p { line-height: 1.55; }.titulo { margin-top: 10px; font-weight: 600; color: var(--color-primary); }.aviso { margin-top: 12px; color: var(--color-text-muted); font-size: 13px; }.botao { min-height: 40px; padding: 0 16px; border-radius: 6px; font-weight: 600; }.cancelar { background: white; border: 1px solid var(--color-border); color: var(--color-text); }.excluir { background: #b83e36; border: 1px solid #b83e36; color: white; }.botao:disabled { opacity: .5; cursor: wait; }
</style>
