

<template>
  <!-- 照片 -->
  <Images />
  <!-- 背景 -->
  <Background />
  <!-- 音乐组件 -->
  <!-- <Music /> -->
  <!-- 弹幕 -->
  <Bullet />
  
  <!-- 抽奖入口按钮 -->
  <div class="lottery-entry" @click="showLottery = true">
    <div class="lottery-btn">开始抽奖</div>
  </div>
  <!-- 抽奖组件 -->
  <Lottery v-if="showLottery" @close="showLottery = false" />
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Background from "./components/Background.vue";
import Images from "./components/Images.vue";
import Music from "./components/Music.vue";
import Bullet from "./components/Bullet.vue";
import Lottery from "./components/Lottery.vue";
const showLottery = ref(false)
</script>
<style scoped>
/* 抽奖入口按钮样式 */
.lottery-entry {
  position: fixed;
  right: 20px;
  bottom: 20px;
  z-index: 100;
  cursor: pointer;
}

.lottery-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #ff4d4d, #f9cb28);
  color: white;
  border-radius: 50px;
  font-size: 18px;
  font-weight: bold;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  animation: pulse 2s infinite;
}

.lottery-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(0, 0, 0, 0.3);
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(255, 77, 77, 0.7);
  }
  70% {
    box-shadow: 0 0 0 10px rgba(255, 77, 77, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(255, 77, 77, 0);
  }
}
</style>
