<template>
  <div id="app">
    <el-container>
      <el-header>
        <h1>自动化清理系统</h1>
        <div class="header-actions">
          <template v-if="isAdmin">
            <el-button type="danger" @click="handleLogout">注销</el-button>
          </template>
          <template v-else>
            <el-button type="primary" @click="showLoginDialog = true">管理员登录</el-button>
          </template>
        </div>
      </el-header>
      <el-main>
        <GuestView v-if="!isAdmin" />
        <AdminView v-else />
      </el-main>
    </el-container>

    <el-dialog v-model="showLoginDialog" title="管理员登录" width="400px">
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showLoginDialog = false">取消</el-button>
        <el-button type="primary" @click="handleLogin">登录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/index'
import GuestView from './views/GuestView.vue'
import AdminView from './views/AdminView.vue'

const isAdmin = ref(false)
const showLoginDialog = ref(false)
const loginForm = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  try {
    const res = await adminApi.login(loginForm)
    if (res.code === 200) {
      isAdmin.value = true
      showLoginDialog.value = false
      ElMessage.success('登录成功')
      loginForm.username = ''
      loginForm.password = ''
    }
  } catch (error) {
    ElMessage.error('登录失败')
  }
}

const handleLogout = () => {
  isAdmin.value = false
  ElMessage.success('注销成功')
}
</script>

<style scoped>
#app {
  height: 100vh;
}

.el-header {
  background-color: #409EFF;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.el-header h1 {
  margin: 0;
  font-size: 24px;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.el-main {
  background-color: #f5f5f5;
  padding: 20px;
}
</style>
