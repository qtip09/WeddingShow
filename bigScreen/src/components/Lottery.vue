<!-- Lottery.vue -->
<template>
  <div class="lottery-container">
    <!-- 关闭按钮 -->
    <div class="close-btn" @click="closeLottery">
      <svg width="24" height="24" viewBox="0 0 24 24">
        <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z" fill="currentColor"/>
      </svg>
    </div>
    
    <!-- 奖品选择阶段 -->
    <div v-if="!selectedPrize" class="prize-selection">
      <h2 class="section-title">请选择要抽取的奖品</h2>
      <div class="prize-cards">
        <div 
          v-for="prize in prizes" 
          :key="prize.id" 
          class="prize-card"
          @click="selectPrize(prize)"
        >
          <div class="prize-image">
            <img :src="prize.filePath" :alt="prize.prizeName">
          </div>
          <div class="prize-info">
            <h3>{{ prize.prizeName }}</h3>
            <div class="prize-count">剩余: {{ prize.count || 0 }}份</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 抽奖阶段 -->
    <div v-else class="lottery-stage">
      <!-- 当前选中的奖品 -->
      <div class="current-prize">
        <h2 class="section-title">正在抽取: <span class="highlight">{{ selectedPrize.prizeName }}</span></h2>
        <div class="prize-image">
          <img :src="selectedPrize.filePath" :alt="selectedPrize.prizeName">
          <div class="prize-count-big">剩余: {{ selectedPrize.count || 0 }}份</div>
        </div>
      </div>
      
      <!-- 参与者头像轮播 -->
      <div class="participant-carousel">
        <div 
          class="participant-avatar"
          v-for="(participant, index) in visibleParticipants"
          :key="index"
        >
          <div class="avatar-frame">
            <img :src="participant.avatarUrl" :alt="participant.nickName">
          </div>
          <div class="name-tag">{{ participant.nickName }}</div>
        </div>
      </div>
      
      <!-- 抽奖按钮 -->
      <button 
        class="start-btn" 
        @click="startLottery" 
        :disabled="isRotating || participants.length === 0"
      >
        {{ isRotating ? '抽奖中...' : '开始抽奖' }}
      </button>
    </div>
    
    <!-- 中奖结果弹窗 -->
    <div v-if="winners.length > 0" class="winner-modal">
      <div class="winner-content">
        <h2>恭喜中奖!</h2>
        <div class="winner-list">
          <div 
            class="winner-item"
            v-for="(winner, index) in winners"
            :key="index"
          >
            <div class="winner-avatar">
              <img :src="winner.avatarUrl" :alt="winner.userName">
            </div>
            <div class="winner-details">
              <p><span class="winner-name">获奖者：</span>{{ winner.userName }}</p>
              <p><span class="prize-name">奖品：</span>{{ winner.prizeName }}</p>
            </div>
          </div>
        </div>
        <div class="remaining-count">
          <span>剩余数量：</span>{{ selectedPrize?.count || 0 }}份
        </div>
        <div class="action-buttons">
          <button class="confirm-btn" @click="closeModal">确定</button>
          <button 
            class="restart-btn" 
            @click="restartLottery"
            v-if="(selectedPrize?.count || 0) > 0"
          >
            继续抽奖
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { getPrizes, getParticipants, drawLottery } from '../request/lottery'
import listener2Callback from '../request/socket'

interface Prize {
  id: number
  prizeName: string
  filePath?: string
  count?: number
}

interface Participant {
  id: number
  nickName: string
  avatarUrl: string
}

interface Winner {
  userName: string
  avatarUrl: string
  prizeName: string
  prizeNum: number
}

interface ApiResponse<T> {
  data: T
  code: number
  message?: string
}

interface PrizeListResponse {
  activityPrizeDtoList: Prize[]
}

interface LotteryResultResponse {
  data: Winner[]
}

const prizes = ref<Prize[]>([])
const participants = ref<Participant[]>([])
const selectedPrize = ref<Prize | null>(null)
const winners = ref<Winner[]>([])
const isRotating = ref(false)
const rotateInterval = ref<ReturnType<typeof setTimeout> | null>(null)
const currentParticipantIndex = ref(0)
const emit = defineEmits(['close'])

// 计算当前显示的参与者
const visibleParticipants = computed(() => {
  if (!isRotating.value) {
    return participants.value.slice(0, 10)
  }
  
  const count = 10
  const start = currentParticipantIndex.value % participants.value.length
  const end = start + count
  
  if (end <= participants.value.length) {
    return participants.value.slice(start, end)
  } else {
    return [
      ...participants.value.slice(start),
      ...participants.value.slice(0, end - participants.value.length)
    ]
  }
})

// 初始化数据
const initData = async () => {
  try {
    const [prizesRes, participantsRes] = await Promise.all([
      getPrizes(),
      getParticipants()
    ])
    
    // 安全类型转换
    const response = prizesRes as unknown as ApiResponse<PrizeListResponse>
    prizes.value = response?.data?.activityPrizeDtoList || []
    participants.value = (participantsRes as ApiResponse<Participant[]>).data || []
  } catch (error) {
    console.error('初始化抽奖数据失败:', error)
  }
}

// 选择奖品
const selectPrize = (prize: Prize) => {
  if (!prize.count || prize.count <= 0) {
    // 使用您项目中的提示组件，例如：
    alert('该奖品已抽完')
    // 或者 alert('该奖品已抽完')
    console.warn('奖品已抽完:', prize.prizeName)
    return
  }
  selectedPrize.value = prize
  winners.value = []
}

// 开始抽奖
const startLottery = async () => {
  if (isRotating.value || participants.value.length === 0) return
  
  isRotating.value = true
  winners.value = []
  startParticipantRotation()
  
  try {
    const response = await drawLottery(selectedPrize.value?.id)
    
    // 类型安全的赋值
    if (Array.isArray(response.data)) {
      winners.value = response.data as Winner[]
    }
    
    // 更新奖品数量
    if (selectedPrize.value && typeof selectedPrize.value.count === 'number' && winners.value.length > 0) {
      selectedPrize.value.count -= winners.value.length
    }
  } catch (error) {
    console.error('抽奖失败:', error)
  } finally {
    if (rotateInterval.value !== null) {
      clearInterval(rotateInterval.value)
      rotateInterval.value = null
    }
    isRotating.value = false
  }
}

// 开始参与者头像轮播
const startParticipantRotation = () => {
  const rotationSpeed = 100
  
  rotateInterval.value = setInterval(() => {
    currentParticipantIndex.value = 
      (currentParticipantIndex.value + 1) % participants.value.length
  }, rotationSpeed)
}

// 关闭中奖弹窗
const closeModal = () => {
  winners.value = []
  if (selectedPrize.value && selectedPrize.value.count !== undefined && selectedPrize.value.count <= 0) {
    selectedPrize.value = null
  }
}

// 继续抽奖
const restartLottery = () => {
  winners.value = []
}

// 关闭抽奖页面
const closeLottery = () => {
  prizes.value = prizes.value.filter(prize => (prize.count || 0) > 0)
  emit('close')
}

onMounted(() => {
  initData()
  
  listener2Callback((data: any) => {
    if (data.type === 'lottery_result') {
      winners.value = data.payload
    }
  })
})
</script>

<style scoped>
/* 修改后的样式 - 去除背景 */
.lottery-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 999;
  font-family: 'Microsoft YaHei', sans-serif;
  /* 去除背景设置 */
}

.close-btn {
  position: absolute;
  top: 30px;
  right: 30px;
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s;
  z-index: 10;
  color: white;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: rotate(90deg);
}

.section-title {
  font-size: 32px;
  margin-bottom: 30px;
  color: white;
  text-align: center;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
}

.highlight {
  color: #ffd700;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.5);
}

.prize-selection {
  width: 90%;
  max-width: 1000px;
  text-align: center;
  position: relative;
  z-index: 2;
}

.prize-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.prize-card {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
  color: #333;
}

.prize-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.prize-image {
  height: 120px;
  overflow: hidden;
  position: relative;
}

.prize-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.prize-info {
  padding: 15px;
}

.prize-info h3 {
  margin: 0 0 5px 0;
  font-size: 16px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.prize-count {
  font-size: 14px;
  color: #c9184a;
  font-weight: bold;
}

.prize-count-big {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  text-align: center;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 0;
  font-size: 14px;
}

.lottery-stage {
  width: 90%;
  max-width: 800px;
  text-align: center;
  position: relative;
  z-index: 2;
}

.current-prize {
  margin-bottom: 30px;
}

.current-prize .prize-image {
  width: 200px;
  height: 200px;
  margin: 0 auto;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.3);
  border: 3px solid #ffd700;
}

.participant-carousel {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
  margin: 30px 0;
  width: 100%;
  max-width: 800px;
}

.participant-avatar {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.avatar-frame {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #ffd700;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.2);
}

.avatar-frame img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.name-tag {
  margin-top: 5px;
  font-size: 12px;
  background: rgba(255, 255, 255, 0.8);
  padding: 2px 8px;
  border-radius: 10px;
  max-width: 80px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #333;
}

.start-btn {
  padding: 12px 30px;
  background: linear-gradient(to right, #ff4d6d, #c9184a);
  color: white;
  border: none;
  border-radius: 30px;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 20px;
  min-width: 200px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.start-btn:hover:not(:disabled) {
  background: linear-gradient(to right, #c9184a, #a4133c);
  transform: translateY(-2px);
  box-shadow: 0 7px 20px rgba(0, 0, 0, 0.3);
}

.start-btn:disabled {
  background: #95a5a6;
  cursor: not-allowed;
}

/* 中奖弹窗样式调整 */
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
  background: #fff5f5; /* 浅粉色背景 */
  padding: 30px;
  border-radius: 10px;
  text-align: center;
  width: 80%;
  max-width: 500px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.winner-content h2 {
  color: #ff0000; /* 红色标题 */
  margin-bottom: 20px;
  font-size: 28px;
}

.winner-list {
  max-height: 300px;
  overflow-y: auto;
  margin: 20px 0;
}

.winner-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
}

.winner-avatar {
  width: 70px;
  height: 70px;
  margin-right: 15px;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid #ffd700; /* 黄色边框 */
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f0f0f0;
}

.winner-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.winner-details {
  text-align: left;
  flex: 1;
}

.winner-details p {
  margin: 8px 0;
  font-size: 16px;
}

.winner-name {
  font-weight: bold;
  color: #ff0000; /* 红色获奖者名称 */
}

.prize-name {
  font-weight: bold;
  color: #0000ff; /* 深蓝色奖品名称 */
}

.remaining-count {
  font-size: 16px;
  margin: 15px 0;
  font-weight: bold;
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.confirm-btn, .restart-btn {
  padding: 10px 25px;
  border: none;
  border-radius: 5px;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s;
  color: white;
}

.confirm-btn {
  background: linear-gradient(to right, #4a6bff, #3a56cc);
}

.confirm-btn:hover {
  background: linear-gradient(to right, #3a56cc, #2a42aa);
}

.restart-btn {
  background: linear-gradient(to right, #4caf50, #3d8b40);
}

.restart-btn:hover {
  background: linear-gradient(to right, #3d8b40, #2e6b31);
}
</style>