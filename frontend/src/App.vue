<script setup>
import { ref, onMounted } from "vue";

const API_URL = "http://localhost:8080";
const chamados = ref([]);
const erro = ref("");

onMounted(async () => {
  try {
    const resposta = await fetch(`${API_URL}/chamados`);
    if (!resposta.ok) throw new Error(`Erro ${resposta.status}`);
    chamados.value = await resposta.json();
  } catch (e) {
    erro.value = e.message;
  }
});
</script>

<template>
  <main>
    <h1>Chamados</h1>
    <p v-if="erro">{{ erro }}</p>
    <ul>
      <li v-for="c in chamados" :key="c.id">{{ c }}</li>
    </ul>
  </main>
</template>