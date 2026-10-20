import { defineStore } from 'pinia'
import { ref } from 'vue'

// 用户模块
export const useUserStore = defineStore(
    'user',
    () => {
        const token = ref('') // 定义 token
        const roles = ref([]) // 定义角色权限

        const setToken = (t) => (token.value = t) // 设置 token
        const setRoles = (roleList = []) => (roles.value = roleList)
        const setAuth = ({ token: newToken = '', roles: newRoles = [] } = {}) => {
            token.value = newToken
            roles.value = newRoles
        }
        const clearAuth = () => {
            token.value = ''
            roles.value = []
        }

        return { token, roles, setToken, setRoles, setAuth, clearAuth }
    },
    {
        persist: true // 持久化
    }
)
