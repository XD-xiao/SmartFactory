<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import {
  Box,
  Odometer,
  OfficeBuilding,
  ScaleToOriginal,
  Document,
  Setting,
  DataAnalysis,
  Monitor,
  Tools, Back
} from '@element-plus/icons-vue'
import router from "@/router/index.js";

const route = useRoute()

const activeMenu = computed(() => route.path)
const defaultOpeneds = ['/device', '/device/plc-management', '/device/machine-management', '/device/maintenance-management']

const handleOpen = (key, keyPath) => {
  console.log('open:', key, keyPath)
}

const handleClose = (key, keyPath) => {
  console.log('close:', key, keyPath)
}
const goBack = () => {
  router.push('/nav')   // 跳转到导航页
}
</script>

<template>
  <el-row class="device-page">
    <el-col :span="6" class="device-sidebar">
      <el-menu
        active-text-color="#ffd04b"
        background-color="#545c64"
        text-color="#fff"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        class="el-menu-vertical-demo"
        router
        @open="handleOpen"
        @close="handleClose"
      >

        <div class="sidebar-header">
          <div class="back-button" @click="goBack">
            <el-icon color="#fff"><Back /></el-icon>
          </div>
          <h1 class="device-title">设备管理系统</h1>
        </div>

        <el-sub-menu index="/device">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>试验箱模块管理</span>
          </template>
          <el-menu-item index="/device/sensors">
            <el-icon><Odometer /></el-icon>
            <span>传感器管理</span>
          </el-menu-item>
          <el-menu-item index="/device/monitoringrecord">
            <el-icon><Odometer /></el-icon>
            <span>监测记录</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/device/plc-management">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>PLC管理</span>
          </template>
          <el-menu-item index="/device/plcControl">
            <el-icon><Setting /></el-icon>
            <span>PLC控制</span>
          </el-menu-item>
          <el-menu-item index="/device/plcWorkSituation">
            <el-icon><DataAnalysis /></el-icon>
            <span>PLC运行时间</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/device/machine-management">
          <template #title>
            <el-icon><Monitor /></el-icon>
            <span>机器管理</span>
          </template>
          <el-menu-item index="/device/GroupManagement">
            <el-icon><OfficeBuilding /></el-icon>
            <span>展区管理</span>
          </el-menu-item>
          <el-menu-item index="/device/MachineManagement">
            <el-icon><Monitor /></el-icon>
            <span>设备管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="/device/maintenance-management">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>设备维修管理</span>
          </template>
          <el-menu-item index="/device/MaintenanceManagement">
            <el-icon><Tools /></el-icon>
            <span>维修管理</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-col>

    <el-col :span="18" class="device-content">
      <router-view />
    </el-col>
  </el-row>
</template>



<style scoped>
.el-menu--horizontal {
  --el-menu-horizontal-height: 100px;
}
.device-page {
  height: 100vh;
  display: flex;
  flex-wrap: nowrap;
  margin: 0;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 10px;
  background-color: #545c64;
}

.back-button {
  font-size: 24px;
  margin-right: 8px;
  padding: 0;
  background-color: #545C64;
  border: none;
}

.device-sidebar {
  min-height: 100vh;
  max-width: 280px;
  flex: 0 0 280px;
  border-right: 1px solid #e4e7ed;
  padding: 0;
  margin: 0;
  background-color: #fff;
  overflow: hidden;
}

.el-menu-vertical-demo {
  position: relative;
  min-height: 100vh;
  margin: 0;
}

.device-title {
  margin: 0;
  font-size: 30px;
  font-weight: 600;
  color: white;
  flex: 1;
  text-align: left;
}

.device-content {
  flex: 1 1 auto;
  min-width: 0;
  max-width: none;
  min-height: 100vh;
  padding: 24px;
  background-color: #f5f7fa;
  box-sizing: border-box;
  overflow: hidden;
}

.device-exit {
  position: absolute;
  left: 50%;
  bottom: 20px;
  transform: translateX(-50%);
  color: #000000;
  font-size: 16px;
  line-height: 1;
  text-decoration: underline;
  cursor: pointer;
}
.device-exit:hover {
  color: #ff0000;
}


</style>
