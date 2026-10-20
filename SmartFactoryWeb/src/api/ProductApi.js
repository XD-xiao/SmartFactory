import request from '@/utils/request.js'

// ==================== 产品类型 ====================
export const addProductTypeService = (data) =>
    request.post('/api/product/addType', data)

export const getProductTypeListService = (pageNum, pageSize, name, code) =>
    request.get('/api/product/getTypeList', { params: { pageNum, pageSize, name, code } })

export const updateProductTypeService = (data) =>
    request.put('/api/product/updateType', data)

export const deleteProductTypeService = (id) =>
    request.delete('/api/product/deleteType', { params: { id } })

// ==================== 产品型号 ====================
export const addProductModelService = (data) =>
    request.post('/api/product/addModel', data)

export const getProductModelListService = (pageNum, pageSize, name, code) =>
    request.get('/api/product/getModelList', { params: { pageNum, pageSize, name, code } })

export const updateProductModelService = (data) =>
    request.put('/api/product/updateModel', data)

export const deleteProductModelService = (id) =>
    request.delete('/api/product/deleteModel', { params: { id } })

// ==================== 产品 ====================
export const addProductService = (data) =>
    request.post('/api/product/addProduct', data)

export const getProductListService = (pageNum, pageSize, barCode, typeCode, modelCode, productCode, deviceSn) =>
    request.get('/api/product/getProductList', { params: { pageNum, pageSize, barCode, typeCode, modelCode, productCode, deviceSn } })

export const getProductInfoService = (id, barCode) =>
    request.get('/api/product/getProduct', { params: { id, barCode } })

export const updateProductService = (data) =>
    request.put('/api/product/updateProduct', data)

export const deleteProductService = (id) =>
    request.delete('/api/product/deleteProduct', { params: { id } })

// ==================== 产品图片 ====================
export const uploadPictureService = (data) =>
    request.post('/api/product/uploadPicture', data)

export const getPictureService = (id) =>
    request.get('/api/product/getPicture', { params: { id } })

// ==================== 产品出入库 ====================
export const storeInService = (data) =>
    request.post('/api/product/storeIn', data)

export const storeOutService = (data) =>
    request.post('/api/product/storeOut', data)

export const getStoreListService = (pageNum, pageSize, barCode, action, groupId, employeeNumber, deviceSn) =>
    request.get('/api/product/getStoreList', { params: { pageNum, pageSize, barCode, action, groupId, employeeNumber, deviceSn } })

// ==================== 产品产量 ====================
export const setProductionService = (data) =>
    request.post('/api/product/setProduction', data)

export const updateProductionService = (data) =>
    request.put('/api/product/updateProduction', data)

export const getProductionListService = (pageNum, pageSize, barCode, planName) =>
    request.get('/api/product/getProductionList', { params: { pageNum, pageSize, barCode, planName } })

export const qualityCheckService = (data) =>
    request.post('/api/product/qualityCheck', data)
