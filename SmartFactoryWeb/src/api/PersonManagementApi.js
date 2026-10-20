import request from "@/utils/request.js";

export const addDepartmentService = (data) =>
    request.post('/api/pm/addDepartment', data)

export const getDepartmentListService = (departmentId, departmentName, status, pageNum, pageSize, sort) =>
    request.get('/api/pm/getDepartmeList', { params: { departmentId, departmentName, status, pageNum, pageSize, sort } })

export const deleteDepartmentService = (departmentId) =>
    request.delete('/api/pm/deleteDepartment', { params: { departmentId } })

export const updateDepartmentService = (data) =>
    request.put('/api/pm/updateDepartment', data)

export const addPositionService = (data) =>
    request.post('/api/pm/addPosition', data)

export const getPositionListService = (positionId, positionName, pageNum, pageSize, sort) =>
    request.get('/api/pm/getPositionList', { params: { positionId, positionName, pageNum, pageSize, sort } })

export const deletePositionService = (positionId) =>
    request.delete('/api/pm/deletePosition', { params: { positionId } })

export const updatePositionService = (data) =>
    request.put('/api/pm/updatePosition', data)

export const addEmployeeService = (data) =>
    request.post('/api/pm/addEmployee', data)

export const employeeLoginService = (data) =>
    request.post('/api/pm/login', data)

export const getEmployeeListService = (pageNum, pageSize, keyword, departmentId, positionId, status) =>
    request.get('/api/pm/getEmployeeList', { params: { pageNum, pageSize, keyword, departmentId, positionId, status } })

export const getEmployeeInfoService = (employeeNumber) =>
    request.get('/api/pm/getEmployee', { params: { employeeNumber } })

export const updateEmployeeService = (data) =>
    request.put('/api/pm/updateEmployee', data)

export const updatePasswordService = (data) =>
    request.put('/api/pm/updatePassword', data)

export const deleteEmployeeService = (employeeId) =>
    request.delete('/api/pm/deleteEmployee', { params: { employeeId } })

export const addCheckRecordService = (data) =>
    request.post('/api/pm/addCheckRecord', data)

export const updateCheckRecordService = (data) =>
    request.put('/api/pm/updateCheckRecord', data)

export const getCheckRecordListService = (pageNum, pageSize, employeeNumber, date, checkinTime, checkoutTime, status, workHours) =>
    request.get('/api/pm/getCheckRecordList', { params: { pageNum, pageSize, employeeNumber, date, checkinTime, checkoutTime, status, workHours } })

export const deleteCheckRecordService = (attendanceID) =>
    request.delete('/api/pm/delCheckRecord', { params: { attendanceID } })

export const addLeaveRecordService = (data) =>
    request.post('/api/pm/addLeaveRecord', data)

export const updateLeaveRecordService = (data) =>
    request.put('/api/pm/updateLeaveRecord', data)

export const getLeaveListService = (pageNum, pageSize, employeeNumber, leaveType, status, startDate, endDate) =>
    request.get('/api/pm/getLeaveList', { params: { pageNum, pageSize, employeeNumber, leaveType, status, startDate, endDate } })

export const deleteLeaveRecordService = (leaveID) =>
    request.delete('/api/pm/deleteLeave', { params: { leaveID } })
