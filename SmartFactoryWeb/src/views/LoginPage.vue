<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router"; // 引入路由，登录成功后通常需要跳转
import { ElMessage } from "element-plus"; // 引入 Element Plus 消息提示
import type { FormInstance, FormRules } from "element-plus"; // 引入表单类型

import { useUserStore } from "@/stores/modules/user";
import { employeeLoginService } from "@/api/PersonManagementApi";

const router = useRouter();
const userStore = useUserStore();

// 表单数据
const employeeNumber = ref('');
const password = ref('');

// 表单实例和验证规则
const formRef = ref<FormInstance>();
const rules = ref<FormRules>({
  employeeNumber: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
});

// 加载状态，防止重复点击
const loading = ref(false);

const login = async () => {
  if (!formRef.value) return;

  // 先触发表单验证
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true;
      try {
        const data = {
          employeeNumber: employeeNumber.value,
          password: password.value
        };
        const res = await employeeLoginService(data);
        console.log(res.data);

        // 保存 token
        userStore.setToken(res.data.token);
        userStore.setRoles(res.data.roles);

        ElMessage.success('登录成功');
        // 登录成功后跳转到首页（根据实际情况修改路径）
        await router.push('/');
      } catch (error: any) {
        // 简单的错误提示处理
        ElMessage.error(error?.message || '登录失败，请检查账号密码');
      } finally {
        loading.value = false;
      }
    }
  });
};
</script>

<template>
  <!-- 登录页面容器，使用 flex 布局居中 -->
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>系统登录</span>
        </div>
      </template>

      <el-form
          ref="formRef"
          :model="{ employeeNumber, password }"
          :rules="rules"
          label-width="0"
          size="large"
      >
        <el-form-item prop="employeeNumber">
          <el-input
              v-model="employeeNumber"
              placeholder="请输入工号"
              prefix-icon="User"
              clearable
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
              v-model="password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="Lock"
              show-password
              @keyup.enter="login"
          />
        </el-form-item>

        <el-form-item>
          <el-button
              class="login-btn"
              :loading="loading"
              @click="login"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>


<style scoped>.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(145deg, #f0f4f8 0%, #e2e8f0 100%);
  position: relative;
}


.login-container::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(84, 92, 100, 0.08) 0%, transparent 70%);
  top: 10%;
  right: 10%;
  pointer-events: none;
}

.login-card {
  width: 400px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.06);
  position: relative;
}

.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: 600;
  color: #545c64;
}


.login-card :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #545c64 inset, 0 0 16px rgba(84, 92, 100, 0.15) !important;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  background-color: #545c64;
  border-color: #545c64;
  color: #ffffff;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-btn:hover {
  background-color: #ffd04b;
  border-color: #ffd04b;
  color: #545c64;
  box-shadow: 0 8px 24px rgba(255, 208, 75, 0.35);
  transform: translateY(-2px);
}</style>