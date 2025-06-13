<template>
  <div class="lottery-container">
    <!-- 关闭按钮 -->
    <div class="close-btn" @click="closeLottery">×</div>
    
    <!-- 抽奖转盘 -->
    <div 
      class="lottery-wheel" 
      :style="{ transform: `rotate(${rotateDeg}deg)` }"
      :class="{ rotating: isRotating }"
    >
      <div 
        v-for="(prize, index) in prizes" 
        :key="prize.id" 
        class="prize-item"
        :style="getPrizeItemStyle(index)"
      >
        <span class="prize-name">{{ prize.name }}</span>
      </div>
      <div class="lottery-pointer"></div>
    </div>
    
    <!-- 抽奖按钮 -->
    <button 
      class="start-btn" 
      @click="startLottery" 
      :disabled="isRotating || prizes.length === 0"
    >
      {{ isRotating ? '抽奖中...' : '开始抽奖' }}
    </button>
    
    <!-- 中奖结果弹窗 -->
    <div v-if="winner" class="winner-modal">
      <div class="winner-content">
        <div class="confetti"></div>
        <h2>恭喜中奖!</h2>
        <div class="winner-info">
          <p><span>获奖者：</span>{{ winner.participantName }}</p>
          <p><span>奖品：</span>{{ winner.prizeName }}</p>
        </div>
        <button class="confirm-btn" @click="closeModal">确定</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getPrizes, getParticipants, drawLottery } from '../request/lottery'
import listener2Callback from '../request/socket'

interface Prize {
  id: number
  name: string
  image?: string
}

interface Participant {
  id: number
  name: string
}

interface Winner {
  prizeId: number
  prizeName: string
  participantId: number
  participantName: string
}

const prizes = ref<Prize[]>([])
const participants = ref<Participant[]>([])
const winner = ref<Winner | null>(null)
const isRotating = ref(false)
const rotateDeg = ref(0)
const emit = defineEmits(['close'])

// 初始化数据
const initData = async () => {
  try {
    const [prizesRes, participantsRes] = await Promise.all([
      getPrizes(),
      getParticipants()
    ])
    prizes.value = prizesRes.data || []
    participants.value = participantsRes.data || []
  } catch (error) {
    console.error('初始化抽奖数据失败:', error)
  }
}

// 获取奖品格子样式
const getPrizeItemStyle = (index: number) => {
  const count = prizes.value.length
  if (count === 0) return {}
  
  const angle = 360 / count
  const skewAngle = 90 - angle
  return {
    transform: `rotate(${angle * index}deg) skewY(${skewAngle}deg)`,
    backgroundColor: `hsl(${(index * 360) / count}, 70%, 50%)`,
    borderColor: `hsl(${(index * 360) / count}, 70%, 35%)`
  }
}

// 开始抽奖
const startLottery = async () => {
  if (isRotating.value || prizes.value.length === 0) return
  
  isRotating.value = true
  winner.value = null
  
  // 旋转动画
  const duration = 5000 // 5秒
  const startTime = Date.now()
  const startRotation = rotateDeg.value
  
  const animate = () => {
    const elapsed = Date.now() - startTime
    const progress = Math.min(elapsed / duration, 1)
    
    // 缓动函数使旋转先快后慢
    rotateDeg.value = startRotation + 360 * 10 * easeOutCubic(progress)
    
    if (progress < 1) {
      requestAnimationFrame(animate)
    } else {
      finishLottery()
    }
  }
  
  animate()
}

// 抽奖结束
const finishLottery = async () => {
  try {
    const response = await drawLottery()
    winner.value = response.data
    
    // 定位到中奖奖品
    if (winner.value) {
      const prizeIndex = prizes.value.findIndex(p => p.id === winner.value?.prizeId)
      if (prizeIndex >= 0) {
        const angle = 360 / prizes.value.length
        const targetDeg = 360 * 10 - (angle * prizeIndex + angle / 2)
        rotateDeg.value = targetDeg
      }
    }
  } catch (error) {
    console.error('抽奖失败:', error)
  } finally {
    isRotating.value = false
  }
}

// 关闭中奖弹窗
const closeModal = () => {
  winner.value = null
}

// 关闭抽奖页面
const closeLottery = () => {
  emit('close')
}

// 缓动函数 - 三次方缓出
const easeOutCubic = (t: number): number => {
  return 1 - Math.pow(1 - t, 3)
}

// WebSocket监听中奖信息
onMounted(() => {
  initData()
  
  listener2Callback((data: any) => {
    if (data.type === 'lottery_result') {
      winner.value = data.payload
    }
  })
})
</script>

<style scoped>
.lottery-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 999;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 20px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 24px;
  cursor: pointer;
  transition: all 0.3s;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.lottery-wheel {
  position: relative;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  margin: 0 auto 30px;
  transition: transform 3s cubic-bezier(0.17, 0.67, 0.21, 0.99);
  transform-origin: center;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.lottery-wheel.rotating {
  transition-duration: 0.1s;
}

.prize-item {
  position: absolute;
  width: 50%;
  height: 50%;
  transform-origin: 100% 100%;
  left: 0;
  top: 0;
  border: 1px solid;
  box-sizing: border-box;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.prize-name {
  transform: skewY(30deg) rotate(15deg);
  color: white;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.5);
  text-align: center;
  width: 100%;
  padding: 0 10px;
}

.lottery-pointer {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 0;
  height: 0;
  border-left: 15px solid transparent;
  border-right: 15px solid transparent;
  border-top: 30px solid #ff0000;
  z-index: 10;
}

.start-btn {
  padding: 12px 30px;
  background: linear-gradient(135deg, #ff4d4d, #f9cb28);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  transition: all 0.3s;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(0, 0, 0, 0.3);
}

.start-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.winner-modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.winner-content {
  position: relative;
  background: white;
  padding: 40px;
  border-radius: 10px;
  text-align: center;
  max-width: 80%;
  width: 400px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
}

.winner-content h2 {
  color: #e74c3c;
  margin-bottom: 20px;
  font-size: 28px;
}

.winner-info {
  margin: 25px 0;
  font-size: 18px;
}

.winner-info p {
  margin: 10px 0;
}

.winner-info span {
  font-weight: bold;
  color: #e74c3c;
}

.confirm-btn {
  padding: 10px 25px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
}

.confirm-btn:hover {
  background: #2980b9;
}

/* 彩花效果 */
.confetti {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  z-index: -1;
  overflow: hidden;
}

.confetti:before {
  content: "";
  position: absolute;
  width: 10px;
  height: 10px;
  background: #f00;
  top: 20%;
  left: 50%;
  animation: confetti 5s ease-in-out infinite;
}

@keyframes confetti {
  0% {
    transform: translateY(0) rotate(0deg);
    opacity: 1;
  }
  100% {
    transform: translateY(1000px) rotate(720deg);
    opacity: 0;
  }
}
</style>