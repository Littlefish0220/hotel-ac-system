<template>
  <!-- router-view 是路由出口，登录页会显示在这里 -->
  <router-view />
</template>

<script setup>
// 这里通常不需要写什么，除非有全局逻辑
</script>

<style>
/* 确保 html 和 body 占满全屏，防止出现白边 */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}
</style>
