<template>
  <div class="nav-container">

    <div class="nav-left">
      <h1 class="main-title">SmartFactory 慧控工场安全监测智能控制平台</h1>
      <p class="sub-description">
        本作品基于国产操作系统，构建智慧工厂安全监测控制平台。平台以传感器融合感知为神经末梢，以可视化大屏为决策中枢，实现对生产环境、安全态势、物流运输、产线节拍的全方位监测与远程协同管控。系统集成设备状态监控、环境智能调节、安全预警响应、视觉质量检测等功能模块，打通数据孤岛，让工厂运营更透明、控制更精准、安全更可靠。立足国产技术底座，助力制造业数字化转型自主可控。
      </p>
    </div>

    <div class="divider"></div>

    <!-- 按钮区域 -->
    <div class="nav-right">
      <RouterLink to="/dashboard" class="nav-btn">大屏展示</RouterLink>
      <RouterLink to="/device" class="nav-btn">设备管理</RouterLink>
      <RouterLink to="/product" class="nav-btn">产品管理</RouterLink>
      <RouterLink to="/personnel" class="nav-btn">人事管理</RouterLink>
      <RouterLink to="/login" class="nav-btn">登录</RouterLink>
    </div>
  </div>
</template>

<style scoped>


.nav-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 60px 80px;
  background: #f5f7fa;
  box-sizing: border-box;
  gap:0;
}


.nav-left {
  flex: 0 0 80%;
  max-width: 750px;            /* 限制最大宽度 */
}

.main-title {
  font-size: 65px;
  font-weight: 700;
  color: #545c64;
  margin: 0 0 20px 0;
  letter-spacing: 1px;
}

.sub-description {
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
  max-width: 600px;
  margin: 0;
}


/* 竖线分隔符 */
.divider {
  width: 2px;
  height: 120px;
  background-color: #dcdfe6;
  flex-shrink: 0;
  margin: 0 40px;
}



.nav-right {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: flex-start;
}



.nav-btn {
  display: inline-block;
  padding: 14px 48px;
  background-color: #545c64;
  color: #ffffff;
  font-size: 18px;
  font-weight: 500;
  text-align: center;
  border-radius: 12px;
  border: none;
  text-decoration: none;
  transition: all 0.2s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  min-width: 160px;
}

.nav-btn:hover {
  background-color: #ffd04b;
  color: #545c64;
  border-color: #409eff;
  box-shadow: 0 8px 20px rgba(64, 158, 255, 0.3);
  transform: translateY(-2px);
}

/* 响应式：小屏幕时改为上下排列 */
@media (max-width: 768px) {
  .nav-container {
    flex-direction: column;
    justify-content: center;
    padding: 40px 24px;
    gap: 30px;
  }

  .main-title {
    font-size: 36px;
  }

  .sub-description {
    font-size: 16px;
    max-width: 100%;
  }

  .nav-right {
    width: 100%;
    align-items: stretch;
  }

  .nav-btn {
    text-align: center;
    width: 100%;
  }
}
</style>