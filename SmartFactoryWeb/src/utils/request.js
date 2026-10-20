import { useUserStore } from '@/stores/index.js'
import axios from 'axios'
import router from '@/router'
import { ElMessage } from 'element-plus'
import { baseURL } from '@/config/runtimeConfig'

const instance = axios.create({
    baseURL,
    timeout: 100000
})

instance.interceptors.request.use(
    (config) => {
        const userStore = useUserStore()
        if (userStore.token) {
            config.headers.Authorization = userStore.token
        }
        return config
    },
    (err) => Promise.reject(err)
)

instance.interceptors.response.use(
    (res) => {
        if (res.data.code === 0 || res.data.code === 200) {
            return res.data
        }
        ElMessage({ message: res.data.message || '服务异常', type: 'error' })
        return Promise.reject(res.data)
    },
    (err) => {
        const userStore = useUserStore()
        ElMessage({ message: err.response?.data?.message || '服务异常', type: 'error' })
        console.log(err)
        if (err.response?.status === 401) {
            userStore.clearAuth()
            router.push('/login')
        }
        return Promise.reject(err)
    }
)

export default instance
export { baseURL }
