<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {  Back,List, Box } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const goBack = () => {
  router.push('/nav')   // 跳转到导航页
}
</script>

<template>
  <el-row class="product-page">
    <el-col :span="6" class="product-sidebar">
      <el-menu
          active-text-color="#ffd04b"
          background-color="#545c64"
          text-color="#fff"
          :default-active="activeMenu"
          class="el-menu-vertical-demo"
          router
      >

        <div class="sidebar-header">
          <div class="back-button" @click="goBack">
            <el-icon color="#fff"><Back /></el-icon>
          </div>
          <h1 class="personnel-title">产品管理系统</h1>
        </div>
        <el-menu-item index="/product/type">
          <el-icon>
            <svg t="1784880665712" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="1671" width="200" height="200"><path d="M293.65333333 70.4h-40.32C152.42666667 70.4 70.4 152.64 70.4 253.76v40.42666667c0 101.12 82.02666667 183.36 182.93333333 183.36h223.25333334V253.76c0-101.12-82.02666667-183.36-182.93333334-183.36z m128.21333334 183.36v168.96h-168.53333334c-70.72 0-128.21333333-57.70666667-128.21333333-128.53333333v-40.42666667c0-70.82666667 57.49333333-128.53333333 128.21333333-128.53333333h40.32c70.72 0 128.21333333 57.70666667 128.21333334 128.53333333z" fill="#ffffff" p-id="1672"></path><path d="M772.8 70.4h-40.32c-100.90666667 0-182.93333333 82.24-182.93333333 183.36v223.78666667h223.14666666c100.90666667 0 182.93333333-82.34666667 182.93333334-183.46666667v-40.32C955.73333333 152.64 873.70666667 70.4 772.8 70.4z m128.21333333 183.36v40.32c0 70.93333333-57.49333333 128.64-128.21333333 128.64H604.37333333V253.76c0-70.82666667 57.49333333-128.53333333 128.21333334-128.53333333h40.32c70.61333333 0 128.10666667 57.70666667 128.10666666 128.53333333z" fill="#ffffff" p-id="1673"></path><path d="M70.4 731.94666667v40.42666666C70.4 873.49333333 152.42666667 955.73333333 253.33333333 955.73333333h40.32c100.90666667 0 182.93333333-82.24 182.93333334-183.36V548.58666667H253.33333333C152.42666667 548.58666667 70.4 630.82666667 70.4 731.94666667z m54.72 0c0-70.93333333 57.49333333-128.53333333 128.21333333-128.53333334h168.53333334v168.96c0 70.93333333-57.49333333 128.53333333-128.21333334 128.53333334h-40.32c-70.72 0-128.21333333-57.70666667-128.21333333-128.53333334v-40.42666666zM772.8 548.58666667H549.65333333v223.78666666c0 101.12 82.02666667 183.36 182.93333334 183.36h40.32c100.90666667 0 182.93333333-82.24 182.93333333-183.36v-40.32c-0.10666667-101.22666667-82.13333333-183.46666667-183.04-183.46666666z m128.21333333 183.46666666v40.32c0 70.82666667-57.49333333 128.53333333-128.21333333 128.53333334h-40.32c-70.72 0-128.21333333-57.6-128.21333333-128.53333334V603.41333333h168.42666666c70.82666667 0 128.32 57.70666667 128.32 128.64z" fill="#ffffff" p-id="1674"></path></svg>
          </el-icon>
          <span>产品类型管理</span>
        </el-menu-item>
        <el-menu-item index="/product/model">
          <el-icon>
            <svg t="1784880862523" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5594" width="200" height="200"><path d="M918.738628 807.783682H696.881194a36.976239 36.976239 0 1 0 0 73.952478h221.857434a36.976239 36.976239 0 0 0 0-73.952478z m0-147.904955H696.881194a36.976239 36.976239 0 1 0 0 73.952477h221.857434a36.976239 36.976239 0 0 0 0-73.952477z m0-147.904956H696.881194a36.976239 36.976239 0 1 0 0 73.952478h221.857434a36.976239 36.976239 0 0 0 0-73.952478z" p-id="5595" fill="#ffffff"></path><path d="M937.226747 242.456653L530.488119 5.053846a37.424048 37.424048 0 0 0-37.360075 0L86.389417 242.456653a37.424048 37.424048 0 0 0-18.104284 31.986366v475.125477a36.52843 36.52843 0 0 0 18.48812 31.986366l406.738628 237.402807a37.424048 37.424048 0 0 0 37.360075 0l83.164551-48.427358a18.616065 18.616065 0 0 0 9.212073-15.92921 43.181594 43.181594 0 0 0-64.740404-37.360075l-9.59591 5.565628v-390.233663l332.658205-211.877687v80.605641a36.976239 36.976239 0 1 0 73.952477 0V274.2511a36.976239 36.976239 0 0 0-18.296201-31.794447zM475.023761 922.422817l-332.658204-194.093267V352.23386l332.658204 181.554613z m35.504866-453.310776l-343.853432-187.440104 345.45275-201.514104 318.392286 185.520921z" p-id="5596" fill="#ffffff"></path></svg>
          </el-icon>
          <span>产品型号管理</span>
        </el-menu-item>

        <el-menu-item index="/product/list">
          <el-icon ><svg t="1784881230347" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="9038" width="200" height="200"><path d="M800 64a32 32 0 0 1 32 32v192h32.768v128H832l0.064 512a32 32 0 0 1-26.24 31.488L800 960h-576a32 32 0 0 1-32-32v-512h-31.168v-128h31.104L192 96a32 32 0 0 1 26.24-31.488L224 64z m-32.128 511.36h-512L256 896h512V575.36z m-366.08 223.936v64h-64v-64h64z m142.976 0v64h-64v-64h64z m144 0v64h-64v-64h64z m-286.976-95.616v64h-64v-64h64z m142.976 0v64h-64v-64h64z m144 0v64h-64v-64h64zM401.728 608v64h-64v-64h64z m142.976 0v64h-64v-64h64z m144 0v64h-64v-64h64zM767.872 128h-512v383.36h512V128z" fill="#ffffff" p-id="9039"></path></svg></el-icon>
          <span>产品管理</span>
        </el-menu-item>
        <el-menu-item index="/product/inventory-records">
          <el-icon><Box /></el-icon>
          <span>出入库记录</span>
        </el-menu-item>
        <el-menu-item index="/product/production">
          <el-icon><List /></el-icon>
          <span>产量管理</span>
        </el-menu-item>
      </el-menu>
    </el-col>

    <el-col :span="18" class="product-content">
      <router-view />
    </el-col>
  </el-row>
</template>

<style scoped>
.el-menu--horizontal {
  --el-menu-horizontal-height: 100px;
}


.product-page {
  display: flex;
  flex-wrap: nowrap;
  margin: 0;
  width: 100%;
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.product-sidebar {
  height: 100%;
  min-height: 0;
  max-width: 280px;
  flex: 0 0 280px;
  border-right: 1px solid #e4e7ed;
  padding: 0;
  margin: 0;
  background-color: #fff;
}

.el-menu-vertical-demo {
  height: 100%;
  margin: 0;
}

.sidebar-header {
  display: flex;
  align-items: center;
  padding: 10px 16px;
  margin-bottom: 10px;
  background-color: #545c64;   /* 与菜单背景一致 */
}

/* 返回按钮样式 */
.back-button {
  font-size: 24px;
  margin-right: 8px;
  padding: 0;
  background-color: #545C64;
  border: none;
}


.personnel-title {
  margin: 0;
  font-size: 30px;
  font-weight: 600;
  color: white;
  flex: 1;
  text-align: left;
}

.product-content {
  flex: 1 1 auto;
  min-width: 0;
  max-width: none;
  min-height: 0;
  height: 100%;
  padding: 16px;
  background-color: #f5f7fa;
  overflow: hidden;
  box-sizing: border-box;
}
</style>
