<template>
  <div class="admin-view">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="充电桩管理" name="stations">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>充电桩管理</span>
              <el-button type="primary" @click="showAddStationDialog = true">新增充电桩</el-button>
            </div>
          </template>
          <el-input v-model="stationSearch" placeholder="输入名称搜索" clearable style="margin-bottom: 20px" @input="searchStations" />
          <el-table :data="filteredStations" stripe style="width: 100%">
            <el-table-column prop="code" label="编号" width="100" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column label="坐标" width="150">
              <template #default="{ row }">({{ row.x }}, {{ row.y }})</template>
            </el-table-column>
            <el-table-column prop="usageCount" label="使用次数" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.status)">
                  {{ getStatusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="deleteStation(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="智能小车管理" name="cars">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>智能小车管理</span>
              <el-button type="primary" @click="showAddCarDialog = true">新增智能小车</el-button>
            </div>
          </template>
          <el-input v-model="carSearch" placeholder="输入名称搜索" clearable style="margin-bottom: 20px" @input="searchCars" />
          <el-table :data="filteredCars" stripe style="width: 100%">
            <el-table-column prop="code" label="编号" width="100" />
            <el-table-column prop="name" label="名称" width="150" />
            <el-table-column prop="speed" label="运行速度" width="100" />
            <el-table-column prop="batteryCapacity" label="电池容量" width="100" />
            <el-table-column prop="chargingSpeed" label="充电速度" width="100" />
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button type="danger" size="small" @click="deleteCar(row.id)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="任务统计" name="tasks">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>任务完成情况</span>
              </template>
              <el-table :data="completedTasks" stripe style="width: 100%" max-height="400">
                <el-table-column prop="id" label="任务编号" width="80" />
                <el-table-column label="任务坐标" width="120">
                  <template #default="{ row }">({{ row.x }}, {{ row.y }})</template>
                </el-table-column>
                <el-table-column label="生成时间" width="150">
                  <template #default="{ row }">{{ formatDate(row.createTime) }}</template>
                </el-table-column>
                <el-table-column label="完成时间" width="150">
                  <template #default="{ row }">{{ formatDate(row.completeTime) }}</template>
                </el-table-column>
                <el-table-column prop="carId" label="智能小车编号" width="120" />
                <el-table-column label="总耗时" width="100">
                  <template #default="{ row }">{{ formatDuration(row.duration) }}</template>
                </el-table-column>
              </el-table>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card>
              <template #header>
                <span>智能小车电量</span>
              </template>
              <div v-for="car in cleaningCars" :key="car.id" style="margin-bottom: 15px">
                <div style="margin-bottom: 5px">{{ car.name }}: {{ car.currentBattery }} / {{ car.batteryCapacity }}</div>
                <el-progress :percentage="getBatteryPercentage(car)" :color="getBatteryColor(car)" />
              </div>
            </el-card>
          </el-col>
        </el-row>
        <el-row :gutter="20" style="margin-top: 20px">
          <el-col :span="24" style="display: flex; justify-content: center;">
            <el-card style="width: 820px; min-width: 820px;">
              <template #header>
                <span>完成任务数统计</span>
              </template>
              <div ref="chartRef" style="width: 800px; height: 400px; min-width: 400px; min-height: 300px; resize: both; overflow: hidden; border: 1px solid #dcdfe6;"></div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showAddStationDialog" title="新增充电桩" width="500px">
      <el-form :model="stationForm" label-width="80px">
        <el-form-item label="编号">
          <el-input v-model="stationForm.code" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="stationForm.name" placeholder="请输入名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddStationDialog = false">取消</el-button>
        <el-button type="primary" @click="addStation">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAddCarDialog" title="新增智能小车" width="500px">
      <el-form :model="carForm" label-width="100px">
        <el-form-item label="编号">
          <el-input v-model="carForm.code" placeholder="请输入编号" />
        </el-form-item>
        <el-form-item label="名称">
          <el-input v-model="carForm.name" placeholder="请输入名称" />
        </el-form-item>
        <el-form-item label="运行速度">
          <el-input-number v-model="carForm.speed" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="电池容量">
          <el-input-number v-model="carForm.batteryCapacity" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="充电速度">
          <el-input-number v-model="carForm.chargingSpeed" :min="1" :max="100" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddCarDialog = false">取消</el-button>
        <el-button type="primary" @click="addCar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { chargingStationApi, cleaningCarApi, taskApi } from '@/api/index'

const activeTab = ref('stations')
const chargingStations = ref([])
const cleaningCars = ref([])
const completedTasks = ref([])
const stationSearch = ref('')
const carSearch = ref('')
const showAddStationDialog = ref(false)
const showAddCarDialog = ref(false)
const stationForm = ref({ code: '', name: '' })
const carForm = ref({ code: '', name: '', speed: 10, batteryCapacity: 100, chargingSpeed: 5 })
const chartRef = ref(null)
let chart = null

const filteredStations = computed(() => {
  if (!stationSearch.value) return chargingStations.value
  return chargingStations.value.filter(s => s.name.includes(stationSearch.value))
})

const filteredCars = computed(() => {
  if (!carSearch.value) return cleaningCars.value
  return cleaningCars.value.filter(c => c.name.includes(carSearch.value))
})

const loadData = async () => {
  try {
    const [stationsRes, carsRes, tasksRes] = await Promise.all([
      chargingStationApi.getAll(),
      cleaningCarApi.getAll(),
      taskApi.getCompleted()
    ])
    chargingStations.value = stationsRes.data
    cleaningCars.value = carsRes.data
    completedTasks.value = tasksRes.data
    nextTick(() => {
      initChart()
    })
  } catch (error) {
    console.error('加载数据失败', error)
  }
}

const searchStations = () => {
}

const searchCars = () => {
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

const addStation = async () => {
  try {
    const randomX = Math.floor(Math.random() * 801)
    const randomY = Math.floor(Math.random() * 601)
    const isOnEdge = randomX <= 50 || randomX >= 750 || randomY <= 50 || randomY >= 550
    
    if (!isOnEdge) {
      ElMessage.warning('充电桩必须分布在区域四周')
      return
    }
    
    await chargingStationApi.add({
      ...stationForm.value,
      x: randomX,
      y: randomY,
      status: 'IDLE',
      usageCount: 0
    })
    ElMessage.success('添加成功')
    showAddStationDialog.value = false
    stationForm.value = { code: '', name: '' }
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteStation = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该充电桩吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await chargingStationApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const addCar = async () => {
  try {
    await cleaningCarApi.add(carForm.value)
    ElMessage.success('添加成功')
    showAddCarDialog.value = false
    carForm.value = { code: '', name: '', speed: 10, batteryCapacity: 100, chargingSpeed: 5 }
    loadData()
  } catch (error) {
    ElMessage.error('添加失败')
  }
}

const deleteCar = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该智能小车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cleaningCarApi.delete(id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hour = String(d.getHours()).padStart(2, '0')
  const minute = String(d.getMinutes()).padStart(2, '0')
  const second = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}-${minute}-${second}`
}

const formatDuration = (seconds) => {
  if (!seconds) return ''
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60
  return `${hours}时${minutes}分${secs}秒`
}

const getBatteryPercentage = (car) => {
  return Math.round((car.currentBattery / car.batteryCapacity) * 100)
}

const getBatteryColor = (car) => {
  const percentage = getBatteryPercentage(car)
  if (percentage > 50) return '#67c23a'
  if (percentage > 20) return '#e6a23c'
  return '#f56c6c'
}

const initChart = () => {
  if (!chartRef.value) return
  if (chart) chart.dispose()
  
  chart = echarts.init(chartRef.value)
  
  const option = {
    title: {
      text: '智能小车完成任务数统计',
      left: 'center',
      top: 10,
      textStyle: {
        fontSize: 18
      }
    },
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '5%',
      right: '5%',
      bottom: '10%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: cleaningCars.value.map(c => c.name),
      axisLabel: {
        interval: 0,
        rotate: 0,
        fontSize: 14,
        margin: 15
      },
      axisLine: {
        show: true,
        lineStyle: {
          fontSize: 14
        }
      },
      axisTick: {
        show: true,
        length: 5
      }
    },
    yAxis: {
      type: 'value',
      name: '完成任务数',
      nameTextStyle: {
        fontSize: 14,
        padding: [0, 0, 0, 20]
      },
      axisLabel: {
        show: true,
        fontSize: 14,
        margin: 15
      },
      axisLine: {
        show: true,
        lineStyle: {
          fontSize: 14
        }
      },
      axisTick: {
        show: true,
        length: 5
      },
      splitLine: {
        show: true
      }
    },
    series: [{
      name: '完成任务数',
      type: 'bar',
      data: cleaningCars.value.map(c => c.completedTaskCount),
      itemStyle: {
        color: '#409EFF'
      }
    }]
  }
  
  chart.setOption(option)
}

onMounted(() => {
  loadData()
  
  if (chartRef.value) {
    const resizeObserver = new ResizeObserver(() => {
      if (chart) {
        chart.resize()
      }
    })
    resizeObserver.observe(chartRef.value)
  }
})
</script>

<style scoped>
.admin-view {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
