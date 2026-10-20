import request from "@/utils/request.js";

export const getEduBoxListService = ( sn, pageNum, pageSize , sort ) =>
    request.get('/api/experiment-boxes', { params: { sn, pageNum, pageSize , sort } })

export const getDeviceListService = (id, sn, type, state, pageNum, pageSize, sort) =>
    request.get('/api/device/getDeviceList', { params: { id, sn, type, state, pageNum, pageSize, sort } })

export const addDeviceService = (data) =>
    request.post('/api/device/addDevice', data)

export const updateDeviceService = (id, data) =>
    request.put('/api/device/updateDevice', data, { params: { id } })

export const deleteDeviceService = (id) =>
    request.delete('/api/device/deleteDevice', { params: { id } })

export const getGroupListService = (id, name, pageNum, pageSize, sort) =>
    request.get('/api/device/getGroupList', { params: { id, name, pageNum, pageSize, sort } })

export const addGroupService = (data) =>
    request.post('/api/device/addGroup', data)

export const updateGroupService = (id, data) =>
    request.put('/api/device/updateGroup', data, { params: { id } })

export const deleteGroupService = (id) =>
    request.delete('/api/device/deleteGroup', { params: { id } })

export const joinGroupService = (data) =>
    request.post('/api/device/joinGroup', data)

export const quitGroupService = (data) =>
    request.delete('/api/device/quitGroup', { data })

export const addMaintenanceService = (data) =>
    request.post('/api/device/addMaintenance', data)

export const getMaintenanceListService = (sn, reason, failureLevel, pageNum, pageSize, status) =>
    request.get('/api/device/getMaintenanceList', { params: { sn, reason, failureLevel, pageNum, pageSize, status } })

export const updateMaintenanceService = (id, data) =>
    request.put('/api/device/updateMaintenance', data, { params: { id } })

export const deleteMaintenanceService = (id) =>
    request.delete('/api/device/deleteMaintenance', { params: { id } })

// 传感器相关接口

// 传感器配置 CRUD
export const getSensorConfigByIdService = (id) =>
    request.get('/api/eduBox/getSensorConfig', { params: { id } })

export const getSensorConfigListService = (pageNum, pageSize, sn, sceneType, sensor) =>
    request.get('/api/eduBox/getSensorConfigList', { params: { pageNum, pageSize, sn, sceneType, sensor } })

export const addSensorConfigService = (data) =>
    request.post('/api/eduBox/addSensorConfig', data)

export const updateSensorConfigService = (data) =>
    request.put('/api/eduBox/updateSensorConfig', data)

export const deleteSensorConfigService = (id) =>
    request.delete('/api/eduBox/deleteSensorConfig', { params: { id } })

export const getSensorRecordListService = (params) =>
    request.get('/api/eduBox/getSensorRecordList', { params })


export const getPlcListService = (sn, seqNo, pageNum, pageSize) =>
    request.get('/api/device/getPlcList', { params: { sn, seqNo, pageNum, pageSize } })

export const controlPlcService = (data) =>
    request.post('/api/device/controlPlc', data)

// Agent 智能检测结果查询
export const getAgentDetectionListService = (status, pageNum, pageSize) =>
    request.get('/api/agent/getDetectionList', { params: { status, pageNum, pageSize } })
















