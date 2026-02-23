<template>
  <div class="guest-view">
    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="map-card">
          <template #header>
            <span>区域地图 (800 x 600)</span>
          </template>
          <div class="map-container" ref="mapContainer">
            <div class="device-icon" 
                 v-for="station in chargingStations" 
                 :key="station.id"
                 :style="{ left: station.x + 'px', top: station.y + 'px' }"
                 @click="showStationDetail(station)">
              <div class="icon charging-station">⚡</div>
            </div>
            <div class="device-icon" 
                 v-for="car in cleaningCars" 
                 :key="car.id"
                 :style="{ left: car.currentX + 'px', top: car.currentY + 'px' }"
                 @click="showCarDetail(car)">
              <div class="icon cleaning-car">🚗</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="task-card">
          <template #header>
            <div style="display: flex; justify-content: space-between; align-items: center;">
              <span>任务列表</span>
              <el-button type="primary" size="small" @click="showCreateTaskDialog = true">
                新增任务
              </el-button>
            </div>
          </template>
          <el-tabs v-model="activeTaskTab" class="custom-el-tabs">
            <el-tab-pane name="pending" label="未完成">
              <el-table :data="pendingTasks" stripe style="width: 100%" max-height="200">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column label="坐标" width="100">
                  <template #default="{ row }">
                    ({{ row.x }}, {{ row.y }})
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150">
                  <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag type="warning">待处理</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane name="in-progress" label="进行中">
              <el-table :data="inProgressTasks" stripe style="width: 100%" max-height="200">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column label="坐标" width="100">
                  <template #default="{ row }">
                    ({{ row.x }}, {{ row.y }})
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150">
                  <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column label="小车" width="80">
                  <template #default="{ row }">
                    {{ getCarName(row.carId) }}
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag type="primary">进行中</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane name="completed" label="已完成">
              <el-table :data="completedTasks" stripe style="width: 100%" max-height="200">
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column label="坐标" width="100">
                  <template #default="{ row }">
                    ({{ row.x }}, {{ row.y }})
                  </template>
                </el-table-column>
                <el-table-column label="完成小车" width="80">
                  <template #default="{ row }">
                    {{ row.carId ? row.carId + '号小车' : '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" width="150">
                  <template #default="{ row }">
                    {{ formatTime(row.createTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="completeTime" label="完成时间" width="150">
                  <template #default="{ row }">
                    {{ formatTime(row.completeTime) }}
                  </template>
                </el-table-column>
                <el-table-column prop="duration" label="耗时(秒)" width="80" />
                <el-table-column label="状态" width="80">
                  <template #default="{ row }">
                    <el-tag type="success">已完成</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
          </el-tabs>
          <div class="clear-button-container">
            <el-button type="danger" size="small" @click="clearTasks">
              清零
            </el-button>
          </div>
        </el-card>
        <el-card class="list-card">
          <template #header>
            <span>设备列表</span>
          </template>
          <el-table :data="allDevices" stripe style="width: 100%" max-height="500">
            <el-table-column prop="code" label="编号" width="80" />
            <el-table-column label="类型" width="80">
              <template #default="{ row }">
                <el-tag :type="row.type === '充电桩' ? 'success' : 'primary'">
                  {{ row.type }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="name" label="名称">
              <template #default="{ row }">
                <el-link type="primary" @click="showDeviceDetail(row)">
                  {{ row.name }}
                </el-link>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showStationDialog" title="充电桩详情" width="500px">
      <el-descriptions :column="1" border v-if="currentStation">
        <el-descriptions-item label="编号">{{ currentStation.code }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ currentStation.name }}</el-descriptions-item>
        <el-descriptions-item label="坐标">({{ currentStation.x }}, {{ currentStation.y }})</el-descriptions-item>
        <el-descriptions-item label="使用次数">{{ currentStation.usageCount }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="currentStation.status === 'IDLE' ? 'success' : 'warning'">
            {{ currentStation.status === 'IDLE' ? '空闲' : '使用中' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="showCarDialog" title="智能小车详情" width="600px">
      <el-descriptions :column="1" border v-if="currentCar">
        <el-descriptions-item label="编号">{{ currentCar.code }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ currentCar.name }}</el-descriptions-item>
        <el-descriptions-item label="运行速度">{{ currentCar.speed }}</el-descriptions-item>
        <el-descriptions-item label="电池容量">{{ currentCar.batteryCapacity }}</el-descriptions-item>
        <el-descriptions-item label="充电速度">{{ currentCar.chargingSpeed }}</el-descriptions-item>
        <el-descriptions-item label="当前电量">{{ currentCar.currentBattery }}</el-descriptions-item>
        <el-descriptions-item label="充电次数">{{ currentCar.chargingCount }}</el-descriptions-item>
        <el-descriptions-item label="完成任务数">{{ currentCar.completedTaskCount }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentCar.status)">
            {{ getStatusText(currentCar.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="清理时间">
          {{ currentCar.cleaningTime > 0 ? `${currentCar.cleaningTime} 秒` : '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="当前坐标">({{ currentCar.currentX }}, {{ currentCar.currentY }})</el-descriptions-item>
        <el-descriptions-item label="目标坐标">
          {{ currentCar.targetX !== null ? `(${currentCar.targetX}, ${currentCar.targetY})` : '无' }}
        </el-descriptions-item>
        <el-descriptions-item label="任务编号">
          {{ currentCar.currentTaskId || '无' }}
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider>分配任务</el-divider>
      
      <el-form :model="taskForm" label-width="80px">
        <el-form-item label="X坐标">
          <el-input-number v-model="taskForm.x" :min="0" :max="800" placeholder="0-800" />
        </el-form-item>
        <el-form-item label="Y坐标">
          <el-input-number v-model="taskForm.y" :min="0" :max="600" placeholder="0-600" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="assignTask" :disabled="currentCar?.status !== 'IDLE'">
            分配任务
          </el-button>
          <el-button @click="showCarDialog = false">关闭</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>

    <el-dialog v-model="showCreateTaskDialog" title="新增任务" width="400px">
      <el-form :model="newTaskForm" label-width="80px">
        <el-form-item label="X坐标">
          <el-input-number v-model="newTaskForm.x" :min="0" :max="800" placeholder="0-800" />
        </el-form-item>
        <el-form-item label="Y坐标">
          <el-input-number v-model="newTaskForm.y" :min="0" :max="600" placeholder="0-600" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="createTask">创建任务</el-button>
          <el-button @click="showCreateTaskDialog = false">取消</el-button>
        </el-form-item>
      </el-form>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { chargingStationApi, cleaningCarApi, taskApi } from '@/api/index'

const chargingStations = ref([])
const cleaningCars = ref([])
const tasks = ref([])
const showStationDialog = ref(false)
const showCarDialog = ref(false)
const showCreateTaskDialog = ref(false)
const currentStation = ref(null)
const currentCar = ref(null)
const taskForm = ref({ x: 0, y: 0 })
const newTaskForm = ref({ x: 0, y: 0 })
const activeTaskTab = ref('pending')
let refreshInterval = null

const allDevices = computed(() => {
  const stations = chargingStations.value.map(s => ({
    ...s,
    type: '充电桩'
  }))
  const cars = cleaningCars.value.map(c => ({
    ...c,
    type: '智能小车'
  }))
  return [...stations, ...cars]
})

const pendingTasks = computed(() => {
  return tasks.value.filter(t => !t.carId && !t.completeTime)
})

const inProgressTasks = computed(() => {
  return tasks.value.filter(t => t.carId && !t.completeTime)
})

const completedTasks = computed(() => {
  return tasks.value.filter(t => t.completeTime)
})

const loadData = async () => {
  try {
    const [stationsRes, carsRes, tasksRes] = await Promise.all([
      chargingStationApi.getAll(),
      cleaningCarApi.getAll(),
      taskApi.getAll()
    ])
    chargingStations.value = stationsRes.data
    cleaningCars.value = carsRes.data
    tasks.value = tasksRes.data
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const showStationDetail = (station) => {
  currentStation.value = station
  showStationDialog.value = true
}

const showCarDetail = (car) => {
  currentCar.value = car
  taskForm.value = { x: 0, y: 0 }
  showCarDialog.value = true
}

const showDeviceDetail = (device) => {
  if (device.type === '充电桩') {
    showStationDetail(device)
  } else {
    showCarDetail(device)
  }
}

const getStatusType = (status) => {
  const typeMap = {
    'IDLE': 'success',
    'FAULT': 'danger',
    'MOVING': 'warning',
    'CLEANING': 'primary',
    'CHARGING': 'info',
    '待回收': 'danger'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'IDLE': '空闲',
    'FAULT': '故障',
    'MOVING': '在途',
    'CLEANING': '清理中',
    'CHARGING': '充电中',
    '待回收': '待回收'
  }
  return textMap[status] || status
}

const getCarName = (carId) => {
  if (!carId) return '无'
  const car = cleaningCars.value.find(c => c.id === carId)
  return car ? car.name : '未知'
}

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}:${date.getSeconds().toString().padStart(2, '0')}`
}

const assignTask = async () => {
  try {
    await cleaningCarApi.assignTask(currentCar.value.id, taskForm.value)
    ElMessage.success('任务分配成功')
    showCarDialog.value = false
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '任务分配失败')
  }
}

const createTask = async () => {
  try {
    await taskApi.add(newTaskForm.value)
    ElMessage.success('任务创建成功')
    showCreateTaskDialog.value = false
    newTaskForm.value = { x: 0, y: 0 }
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '任务创建失败')
  }
}

const clearTasks = async () => {
  try {
    switch (activeTaskTab.value) {
      case 'pending':
        await taskApi.deletePending()
        break
      case 'in-progress':
        await taskApi.deleteInProgress()
        break
      case 'completed':
        await taskApi.deleteCompleted()
        break
    }
    ElMessage.success('任务清零成功')
    loadData()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '任务清零失败')
  }
}

onMounted(() => {
  loadData()
  refreshInterval = setInterval(loadData, 1000)
})

onUnmounted(() => {
  if (refreshInterval) {
    clearInterval(refreshInterval)
  }
})
</script>

<style scoped>
.guest-view {
  padding: 20px;
}

.map-card, .list-card, .task-card {
  background: white;
  margin-bottom: 20px;
}

.task-card {
  position: relative;
}

.custom-el-tabs :deep(.el-tabs__header) {
  display: flex;
  align-items: center;
}

.custom-el-tabs :deep(.el-tabs__nav-wrap) {
  display: flex;
  flex: 1;
}

.custom-el-tabs :deep(.el-tabs__nav) {
  display: flex;
  flex: 1;
}

.custom-el-tabs :deep(.el-tabs__item) {
  flex: 1;
  text-align: center;
}

.clear-button-container {
  position: absolute;
  top: 60px;
  right: 20px;
  z-index: 10;
}

.map-container {
  width: 800px;
  height: 600px;
  background: linear-gradient(to bottom, #e0f7fa 0%, #b2ebf2 100%);
  border: 2px solid #00bcd4;
  position: relative;
  overflow: hidden;
}

.device-icon {
  position: absolute;
  transform: translate(-50%, -50%);
  cursor: pointer;
  transition: all 0.3s;
}

.device-icon:hover {
  transform: translate(-50%, -50%) scale(1.2);
  z-index: 10;
}

.icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.charging-station {
  background: #ff9800;
}

.cleaning-car {
  background: #2196f3;
}
</style>
