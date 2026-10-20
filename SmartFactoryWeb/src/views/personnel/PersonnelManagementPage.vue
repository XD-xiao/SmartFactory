<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {  Back,Calendar, OfficeBuilding, User, UserFilled } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

const activeMenu = computed(() => route.path)

const goBack = () => {
  router.push('/nav')   // 跳转到导航页
}
</script>

<template>
  <el-row class="personnel-page">
    <el-col :span="6" class="personnel-sidebar">
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
          <h1 class="personnel-title">人事管理系统</h1>
        </div>

        <el-menu-item index="/personnel/department">
          <el-icon><OfficeBuilding /></el-icon>
          <span>部门管理</span>
        </el-menu-item>
        <el-menu-item index="/personnel/position">
          <el-icon>
            <svg t="1784881272933" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="10100" width="200" height="200"><path d="M275 322.3c-15 0-26.4 11.4-26.4 26.4s11.4 26.4 26.4 26.4h477.5c15 0 26.4-11.4 26.4-26.4s-11.4-26.4-26.4-26.4H636.9V166.6C627 72.4 548.4 51.7 509.2 53.8c-79.2 0-114.1 73.5-122.1 112-0.7 1.4-0.7 2.9-0.7 5v151.3H275v0.2z m164.9-148.5c2.9-11.4 19.2-67.1 71.4-67.1 11.4-0.7 65.6 0.7 73.5 64.2v151.3H440.6V173.8h-0.7z" fill="#ffffff" p-id="10101"></path><path d="M487.7 185.9c0 13.6 11.4 25 25 25s25-11.4 25-25-11.4-25-25-25c-13.6 0.1-25 11.4-25 25z m360.4 334H616.9c-15 0-26.4 11.4-26.4 26.4 0 15 11.4 26.4 26.4 26.4h230.5c15 0 26.4-11.4 26.4-26.4 0-15-11.4-26.4-25.7-26.4zM875.2 677c0-15-11.4-26.4-26.4-26.4H617.5c-15 0-26.4 11.4-26.4 26.4 0 15 11.4 26.4 26.4 26.4H848c14.4 0 27.2-12.2 27.2-26.4zM617.5 777.6c-15 0-26.4 11.4-26.4 26.4s11.4 26.4 26.4 26.4h190.6c15 0 26.4-11.4 26.4-26.4s-11.4-26.4-26.4-26.4H617.5z m-463.2-32.9c0 54.9 44.2 99.9 99.9 99.9h167c54.9 0 99.2-44.2 99.2-99.9 0-46.4-32.2-85.6-74.9-95.7 17.1-21.4 27.8-48.5 27.8-78.6 0-70.6-57.1-128.5-128.5-128.5S216.3 499 216.3 570.4c0 28.5 9.3 54.9 25.7 76.4-49.1 5.9-87.7 47.3-87.7 97.9z m190.6-249.1c41.4 0 74.9 34.3 74.9 74.9s-34.3 74.9-74.9 74.9-75.7-33.6-75.7-74.9 33.6-74.9 75.7-74.9z m-90.6 203.5h167c25.7 0 46.4 20.7 46.4 46.4s-20.7 46.4-46.4 46.4h-167c-25.7 0-46.4-20.7-46.4-46.4s20-46.4 46.4-46.4z" fill="#ffffff" p-id="10102"></path><path d="M912.3 190.9H721c1.4 5 2.9 10.7 2.9 15.7V228c0 5.7-0.7 11.4-2.9 15.7h191.3c14.3 0 25.7 12.2 25.7 27.8v617.3c0 15.7-11.4 27.8-25.7 27.8H111.6c-14.3 0-25.7-12.2-25.7-27.8V272.1c0-15.7 11.4-27.8 25.7-27.8h191.3c-1.4-5-2.9-10.7-2.9-15.7v-21.4c0-5.7 0.7-11.4 2.9-15.7H111.6C68 191.7 33 228 33 272.2v617.3c0 44.2 35.7 80.7 78.6 80.7h800.8c43.5 0 78.6-36.4 78.6-80.7V272.2c-0.9-44.8-35.8-81.3-78.7-81.3z" fill="#ffffff" p-id="10103"></path><path d="M276.4 218.1c0 9.4 5 18.1 13.2 22.9 8.2 4.7 18.2 4.7 26.4 0s13.2-13.4 13.2-22.9-5-18.1-13.2-22.9c-8.2-4.7-18.2-4.7-26.4 0s-13.2 13.4-13.2 22.9M693.2 217.3c0 9.4 5 18.1 13.2 22.9 8.2 4.7 18.2 4.7 26.4 0s13.2-13.4 13.2-22.9c0-9.4-5-18.1-13.2-22.9-8.2-4.7-18.2-4.7-26.4 0-8.2 4.8-13.2 13.5-13.2 22.9" fill="#ffffff" p-id="10104"></path></svg>
          </el-icon>
          <span>职位管理</span>
        </el-menu-item>
        <el-menu-item index="/personnel/staff">
          <el-icon><User /></el-icon>
          <span>人员管理</span>
        </el-menu-item>
        <el-menu-item index="/personnel/check">
          <el-icon><Calendar /></el-icon>
          <span>考勤管理</span>
        </el-menu-item>
        <el-menu-item index="/personnel/leave">
          <el-icon>
            <svg t="1784881405135" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="13624" width="200" height="200"><path d="M512 0c282.766222 0 512 229.233778 512 512a28.444444 28.444444 0 1 1-56.888889 0c0-251.363556-203.747556-455.111111-455.111111-455.111111S56.888889 260.636444 56.888889 512s203.747556 455.111111 455.111111 455.111111a28.444444 28.444444 0 1 1 0 56.888889C229.233778 1024 0 794.766222 0 512S229.233778 0 512 0z m256 512a256 256 0 1 1 0 512 256 256 0 0 1 0-512z m0 56.888889a199.111111 199.111111 0 1 0 0 398.222222 199.111111 199.111111 0 0 0 0-398.222222z m113.777778 170.666667a28.444444 28.444444 0 0 1 5.12 56.433777L881.777778 796.444444h-213.333334a28.444444 28.444444 0 0 1-5.12-56.433777L668.444444 739.555556H881.777778zM512 175.36a28.444444 28.444444 0 0 1 27.989333 23.324444l0.455111 5.12V512a28.444444 28.444444 0 0 1-23.324444 27.989333L512 540.444444H227.555556a28.444444 28.444444 0 0 1-5.12-56.433777L227.555556 483.555556l256-0.056889V203.804444a28.444444 28.444444 0 0 1 23.324444-27.989333l5.12-0.455111z" fill="#ffffff" p-id="13625"></path></svg>
          </el-icon>
          <span>请假管理</span>
        </el-menu-item>
      </el-menu>
    </el-col>

    <el-col :span="18" class="personnel-content">
      <router-view />
    </el-col>
  </el-row>
</template>

<style scoped>
.el-menu--horizontal {
  --el-menu-horizontal-height: 100px;
}

.personnel-page {
  display: flex;
  flex-wrap: nowrap;
  margin: 0;
  width: 100%;
  height: 100vh;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.personnel-sidebar {
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

.personnel-content {
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
