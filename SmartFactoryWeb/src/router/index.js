import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/index.js'

const routes = [
  {
    path: '/',
    redirect: '/nav',
  },
  {
    path: '/nav',
    name: 'navigation',
    component: () => import('@/views/NavigationPage.vue'),
    meta: {
      title: '导航页',
      public: true,
    },
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginPage.vue'),
    meta: {
      title: '登录页',
      public: true,
    },
  },
  {
    path: '/dashboard',
    name: 'dashboard',
    component: () => import('@/views/BigScreenPage.vue'),
    meta: {
      title: '大屏展示',
      public: true,
    },
  },
  {
    path: '/demo/websocket',
    name: 'demo-websocket',
    component: () => import('@/views/DemoWebSocket.vue'),
    meta: {
      title: 'WebSocket 测试页',
      public: true,
    },
  },
  {
    path: '/demo/mqtt',
    name: 'demo-mqtt',
    component: () => import('@/views/DemoMqtt.vue'),
    meta: {
      title: 'MQTT Demo',
      public: true,
    },
  },
  {
    path: '/device',
    name: 'device',
    component: () => import('@/views/device/DeviceManagementPage.vue'),
    redirect: '/device/sensors',
    meta: {
      title: '设备管理系统',
      requiresAuth: true,
      roles: ['admin', 'device_admin'],
    },
    children: [
      {
        path: 'sensors',
        name: 'sensor-management',
        component: () => import('@/views/device/SensorManagementPage.vue'),
        meta: {
          title: '传感器管理',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'monitoringrecord',
        name: 'monitoring-record',
        component: () => import('@/views/device/MonitoringRecordPage.vue'),
        meta: {
          title: '检测记录',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'plcControl',
        name: 'plc-control',
        component: () => import('@/views/device/PlcControlPage.vue'),
        meta: {
          title: 'PLC控制',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'plcWorkSituation',
        name: 'plc-worksituation',
        component: () => import('@/views/device/PlcWorkSituationPage.vue'),
        meta: {
          title: 'PLC运行状况',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'GroupManagement',
        name: 'groupemanagement',
        component: () => import('@/views/device/GroupManagementPage.vue'),
        meta: {
          title: '展区管理',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'MachineManagement',
        name: 'machinemanagement',
        component: () => import('@/views/device/MachineManagementPage.vue'),
        meta: {
          title: '机器管理',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
      {
        path: 'MaintenanceManagement',
        name: 'maintenancemanagement',
        component: () => import('@/views/device/MaintenanceManagementPage.vue'),
        meta: {
          title: '维修管理',
          requiresAuth: true,
          roles: ['admin', 'device_admin'],
        },
      },
    ],
  },
  {
    path: '/product',
    name: 'product',
    component: () => import('@/views/product/ProductManagementPage.vue'),
    redirect: '/product/list',
    meta: {
      title: '产品管理',
      requiresAuth: true,
      roles: ['admin', 'product_admin'],
    },
    children: [
      {
        path: 'type',
        name: 'product-type',
        component: () => import('@/views/product/ProductTypePage.vue'),
        meta: {
          title: '产品类型',
            requiresAuth: true,
            roles: ['admin', 'product_admin'],
        },
      },
      {
        path: 'model',
        name: 'product-model',
        component: () => import('@/views/product/ProductModelPage.vue'),
        meta: {
            title: '产品型号',
            requiresAuth: true,
            roles: ['admin', 'product_admin'],
        },
      },
      {
        path: 'list',
        name: 'product-list',
        component: () => import('@/views/product/ProductListPage.vue'),
        meta: {
          title: '产品列表',
          requiresAuth: true,
          roles: ['admin', 'product_admin'],
        },
      },
      {
        path: 'inventory-records',
        name: 'inventory-records',
        component: () => import('@/views/product/InventoryRecordsPage.vue'),
        meta: {
          title: '出入库记录',
          requiresAuth: true,
          roles: ['admin', 'product_admin'],
        },
      },
      {
        path: 'production',
        name: 'product-production',
        component: () => import('@/views/product/ProductProductionPage.vue'),
        meta: {
            title: '产量管理',
            requiresAuth: true,
            roles: ['admin', 'product_admin'],
        },
      },
    ],
  },
  {
    path: '/personnel',
    name: 'personnel',
    component: () => import('@/views/personnel/PersonnelManagementPage.vue'),
    redirect: '/personnel/staff',
    meta: {
      title: '人事管理系统',
      requiresAuth: true,
      roles: ['admin', 'hr_admin'],
    },
    children: [
      {
        path: 'department',
        name: 'department-management',
        component: () => import('@/views/personnel/DepartmentManagementPage.vue'),
        meta: {
          title: '部门管理',
          requiresAuth: true,
      roles: ['admin', 'hr_admin'],
        },
      },
      {
        path: 'position',
        name: 'position-management',
        component: () => import('@/views/personnel/PositionManagementPage.vue'),
        meta: {
          title: '职级管理',
          requiresAuth: true,
          roles: ['admin', 'hr_admin'],
        },
      },
      {
        path: 'staff',
        name: 'staff-management',
        component: () => import('@/views/personnel/StaffManagementPage.vue'),
        meta: {
          title: '人员管理',
          requiresAuth: true,
          roles: ['admin', 'hr_admin'],
        },
      },
      {
        path: 'check',
        name: 'check-management',
        component: () => import('@/views/personnel/CheckManagementPage.vue'),
        meta: {
            title: '考勤管理',
            requiresAuth: true,
            roles: ['admin', 'hr_admin'],
        },
      },
      {
        path: 'leave',
        name: 'leave-management',
        component: () => import('@/views/personnel/LeaveManagementPage.vue'),
        meta: {
          title: '请假管理',
          requiresAuth: true,
          roles: ['admin', 'hr_admin'],
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/nav',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

import { ElMessage } from 'element-plus'

const normalizeRoles = (roles) => {
    if (Array.isArray(roles)) {
        return roles.filter((role) => typeof role === 'string' && role.trim())
    }

    if (typeof roles === 'string' && roles.trim()) {
        return [roles.trim()]
    }

    return []
}

router.beforeEach(async (to) => {
    const userStore = useUserStore()
    const requiresAuth = to.matched.some((route) => route.meta.requiresAuth)

    // 1. 如果不需要鉴权，直接放行
    if (!requiresAuth) {
        return true
    }

    // 2. 检查 Token 是否存在
    if (!userStore.token) {
        ElMessage.error('登录已过期，请重新登录')

        // 使用 Promise 包装 setTimeout，实现阻塞等待 2 秒
        await new Promise((resolve) => {
            setTimeout(() => {
                resolve()
            }, 2000)
        })

        // 2秒后跳转到登录页，并携带重定向参数
        return {
            path: '/login',
            query: {
                redirect: to.fullPath,
            },
        }
    }

    // 3. 检查角色权限
    const userRoles = normalizeRoles(userStore.roles)
    const requiredRoles = [...new Set(to.matched.flatMap((route) => normalizeRoles(route.meta.roles)))]

    if (requiredRoles.length > 0 && !requiredRoles.some((role) => userRoles.includes(role))) {
        ElMessage.warning('您的权限不足，无法访问该页面')

        // 权限不足：仅提示，不跳转，取消本次导航
        return false
    }

    // 4. 鉴权通过，正常放行
    return true
})

export default router
