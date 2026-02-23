# 自动化清理系统

基于Spring Boot + Vue 3的智能清理小车管理系统

## 项目简介

这是一个自动化清理系统，模拟智能小车在指定区域内进行清理任务的管理系统。系统包含充电桩、智能清理小车和任务管理功能，支持游客和管理员两种用户角色。

## 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Data JPA
- MySQL 8.0
- Lombok
- Maven

### 前端
- Vue 3
- Element Plus
- ECharts
- Axios
- Vite

## 功能特性

### 游客功能
1. **地图展示**
   - 实时显示充电桩和小车位置
   - 地图尺寸：800x600
   - 充电桩分布在地图边缘区域

2. **设备列表**
   - 显示所有充电桩和小车信息
   - 支持点击查看设备详情
   - 小车详情包含：位置、电量、速度、充电次数、完成任务数

3. **任务管理**
   - 显示未完成任务、进行中任务、已完成任务
   - 支持手动创建任务（输入坐标）
   - 显示任务创建时间、完成时间、耗时
   - 已完成任务显示完成小车编号
   - **任务清零功能**：支持按任务类型清零（未完成/进行中/已完成）
   - 清零按钮位于tab标签行最右边，根据当前选中的tab清除对应任务列表

4. **实时更新**
   - 每2秒自动刷新数据
   - 实时显示小车状态变化

### 管理员功能
1. **登录系统**
   - 默认账号：admin
   - 默认密码：admin123

2. **设备管理**
   - 充电桩管理：查看、添加、删除
   - 小车管理：查看、添加、删除
   - 支持搜索功能

3. **统计图表**
   - 任务完成数统计（支持拖动调整大小，初始尺寸800*400）
   - 充电次数统计
   - 使用次数统计
   - 数据可视化展示

## 智能决策算法

### 电量管理
- **移动消耗**：每移动1单位消耗0.05电量
- **清理消耗**：完成整个清理任务消耗1电量（不是按时间计算）
- **任务完成消耗**：完成一个任务消耗1电量
- **安全电量**：保留5%电量作为安全缓冲

### 充电策略
1. **电量过低充电**：当电量低于5%时，立即前往充电桩
2. **无任务充电**：当无可用任务且电量低于40%时，自动前往充电桩
3. **任务超出范围充电**：当任务成本超出可用电量且电量低于40%时，前往充电桩

### 任务选择策略
1. **距离优先**：优先选择距离最近的任务
2. **电量充足**：确保任务成本在可用电量范围内
3. **效率评估**：计算任务完成效率，低于15%不接取
4. **充电桩可达性**：确保完成任务后能返回充电桩

### 任务清零策略
1. **进行中任务清零**：删除进行中任务时，会自动释放关联的小车，使其回到IDLE状态，可以接取新任务
2. **分类清零**：根据当前选中的tab（未完成/进行中/已完成）清除对应类型的任务

## 系统参数（可调整）

### 地图参数
- 地图宽度：800
- 地图高度：600
- 充电桩数量：4个
- 小车数量：2个

### 小车参数
- 移动速度：10单位/秒
- 电池容量：100
- 充电速度：5/秒
- 清理时间：5-10秒（随机）

### 充电桩参数
- 充电距离限制：≤10单位
- 充电模式：独占式（一桩一车）
- 使用次数统计：自动累加

### 任务参数
- 自动生成间隔：30-60秒（随机）
- 任务坐标范围：0-800（X），0-600（Y）
- 清理时间：5-10秒（随机）

## 状态说明

### 小车状态
- **IDLE**：空闲状态，等待任务
- **MOVING**：移动中，前往任务或充电桩
- **CLEANING**：清理中，正在执行任务
- **CHARGING**：充电中，在充电桩充电
- **待回收**：电量耗尽，无法移动

### 充电桩状态
- **IDLE**：空闲，可使用
- **CHARGING**：使用中，正在充电

## 数据库设计

### 充电桩表（charging_station）
- id：主键
- code：编号
- name：名称
- x：X坐标
- y：Y坐标
- status：状态（IDLE/CHARGING）
- usage_count：使用次数
- current_car_id：当前充电小车ID

### 小车表（cleaning_car）
- id：主键
- code：编号
- name：名称
- x：当前X坐标
- y：当前Y坐标
- status：状态（IDLE/MOVING/CLEANING/CHARGING/待回收）
- battery_capacity：电池容量
- current_battery：当前电量
- speed：移动速度
- charging_speed：充电速度
- charging_count：充电次数
- completed_task_count：完成任务数
- cleaning_time：清理时间
- current_task_id：当前任务ID
- target_x：目标X坐标
- target_y：目标Y坐标
- target_type：目标类型（TASK/CHARGING）

### 任务表（task）
- id：主键
- x：X坐标
- y：Y坐标
- create_time：创建时间
- complete_time：完成时间
- duration：耗时（秒）
- car_id：完成小车ID

### 管理员表（admin）
- id：主键
- username：用户名
- password：密码

## 启动说明

### 后端启动
```bash
cd backend
mvn spring-boot:run
```
访问地址：http://localhost:9091

### 前端启动
```bash
cd frontend
npm install
npm run dev
```
访问地址：http://localhost:5173

### 数据库配置
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/student_management?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

## API接口

### 充电桩接口
- GET /api/charging-stations：获取所有充电桩
- POST /api/charging-stations：添加充电桩
- DELETE /api/charging-stations/{id}：删除充电桩

### 小车接口
- GET /api/cleaning-cars：获取所有小车
- POST /api/cleaning-cars：添加小车
- DELETE /api/cleaning-cars/{id}：删除小车

### 任务接口
- GET /api/tasks/pending：获取未完成任务
- GET /api/tasks/in-progress：获取进行中任务
- GET /api/tasks/completed：获取已完成任务
- POST /api/tasks：创建任务
- GET /api/tasks/{id}：获取任务详情
- DELETE /api/tasks/pending：删除未完成任务
- DELETE /api/tasks/in-progress：删除进行中任务（自动释放小车）
- DELETE /api/tasks/completed：删除已完成任务

### 管理员接口
- POST /api/admin/login：管理员登录
- GET /api/admin/stats：获取统计数据

## 系统特色

1. **智能决策**：小车根据电量、任务距离、充电桩位置自动决策
2. **自动任务生成**：系统每30-60秒自动生成新任务
3. **实时模拟**：小车位置、状态实时更新
4. **电量管理**：完善的电量消耗和充电策略
5. **任务追踪**：记录任务创建、完成、耗时信息
6. **任务清零**：支持按类型清零任务，清零进行中任务会自动释放小车
7. **可调整图表**：统计图支持拖动调整大小

## 注意事项

1. 确保MySQL服务已启动
2. 数据库字符集设置为utf8mb4
3. 后端端口9091，前端端口5173
4. 首次启动会自动初始化数据

## 开发者

基于Spring Boot + Vue 3开发的智能清理小车管理系统

## 更新日志

### v1.0.0 (2026-02-23)
- 初始版本发布
- 实现基本的小车、充电桩、任务管理功能
- 实现智能决策算法
- 实现自动任务生成
- 实现游客和管理员双角色系统
- 完善电量管理和充电策略
