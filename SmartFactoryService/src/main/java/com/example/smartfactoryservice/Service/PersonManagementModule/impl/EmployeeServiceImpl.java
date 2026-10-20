package com.example.smartfactoryservice.Service.PersonManagementModule.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.AttendanceRecord;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Department;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Employee;
import com.example.smartfactoryservice.Domain.Entity.PersonManagementModule.Position;
import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Exception.BusinessException;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.AttendanceRecordMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.DepartmentMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.EmployeeMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.LeaveRecordMapper;
import com.example.smartfactoryservice.Mapper.PersonManagementModule.PositionMapper;
import com.example.smartfactoryservice.Service.PersonManagementModule.EmployeeService;
import com.example.smartfactoryservice.Utils.JwtUtil;
import com.example.smartfactoryservice.Utils.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl extends PersonManagementServiceSupport implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final DepartmentMapper departmentMapper;
    private final PositionMapper positionMapper;

    @Value("${admin.employee-number:adminadmin}")
    private String adminEmployeeNumber;

    @Value("${admin.password:}")
    private String adminPassword;

    @Override
    public String accessControl(Map<String, Object> accessInfo) {
        requireBody(accessInfo, "访问信息不能为空");
        String token = requireString(accessInfo, "token", "token");
        Map<String, Object> claims = JwtUtil.parseJWT(token);
        String departmentName = claims.get("departmentName").toString();
        return switch (departmentName) {
            case "信息部门" -> "admin";
            case "生产部门" -> "product_admin";
            case "人事部门" -> "hr_admin";
            case "后勤部门" -> "device_admin";
            default -> "other";
        };
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addEmployee(Map<String, Object> employee) {
        requireBody(employee, "员工信息不能为空");

        Integer departmentId = requireInteger(employee, "部门ID", "departmentId");
        Integer positionId = requireInteger(employee, "职位ID", "positionId");
        ensureDepartmentExists(departmentId);
        ensurePositionExists(positionId);

        Employee entity = new Employee();
        entity.setEmployeeNumber(resolveEmployeeNumber(employee));
        entity.setName(requireString(employee, "姓名", "name"));
        entity.setGender(requireString(employee, "性别", "gender"));
        entity.setBirthDate(requireLocalDate(employee, "出生日期", "birthDate"));
        entity.setPhone(requireString(employee, "手机号", "phone"));
        entity.setEmail(requireString(employee, "邮箱", "email"));
        entity.setAddress(requireString(employee, "地址", "address"));
        entity.setIdCard(requireString(employee, "身份证号", "idCard"));
        entity.setHireDate(getLocalDate(employee, "hireDate") == null ? LocalDate.now() : getLocalDate(employee, "hireDate"));
        entity.setDepartmentId(departmentId);
        entity.setPositionId(positionId);

        String rawPassword = requireString(employee, "密码", "password");
        String encodedPassword = PasswordUtil.encode(rawPassword);
        // String encodedPassword = rawPassword; // 调试时可改为明文保存
        entity.setPassword(encodedPassword);

        employeeMapper.insert(entity);
        return entity.getEmployeeNumber();
    }

    @Override
    public Map<String, Object> login(Map<String, Object> loginInfo) {
        requireBody(loginInfo, "登录信息不能为空");

        String employeeNumber = requireString(loginInfo, "工号", "employeeNumber");
        String password = requireString(loginInfo, "密码", "password");

        // 超级管理员账号（通过环境变量 ADMIN_EMPLOYEE_NUMBER / ADMIN_PASSWORD 配置，避免硬编码）
        if (adminPassword != null && !adminPassword.isBlank()
                && employeeNumber.equals(adminEmployeeNumber)
                && password.equals(adminPassword)) {
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("employeeId", -1);
            claims.put("employeeNumber", -1);
            claims.put("departmentName", "信息部门");
            String token = JwtUtil.getJWT(claims);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("token", token);
            result.put("roles", "admin");
            return result;
        }



        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("employee_number", employeeNumber)
                .eq("delete_flag", 0);
        Employee employee = employeeMapper.selectOne(wrapper);
        if (employee == null || !passwordMatched(password, employee.getPassword())) {
            throw new BusinessException(BAD_REQUEST_CODE, "工号或密码错误");
        }
        // 部门名称
        QueryWrapper<Department> deptWrapper = new QueryWrapper<>();
        deptWrapper.eq("department_id", employee.getDepartmentId())
                .eq("delete_flag", 0);
        Department dept = departmentMapper.selectOne(deptWrapper);
        String departmentName = dept == null ? null : dept.getDepartmentName();

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("employeeId", employee.getEmployeeId());
        claims.put("employeeNumber", employee.getEmployeeNumber());
        claims.put("departmentName", departmentName);
        String token = JwtUtil.getJWT(claims);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("token", token);
        switch (departmentName) {
            case "信息部门" -> result.put("roles", "admin");
            case "生产部门" -> result.put("roles", "product_admin");
            case "人事部门" -> result.put("roles", "hr_admin");
            case "后勤部门" -> result.put("roles", "device_admin");
            default -> result.put("roles", "other");
        };

        return result;
    }

    @Override
    public PageBean getEmployeeList(int pageNum, int pageSize, String keyword, Integer departmentId, Integer positionId, Integer status) {
        QueryWrapper<Employee> wrapper = new QueryWrapper<>();
        wrapper.eq("delete_flag", 0)
                .eq(departmentId != null, "department_id", departmentId)
                .eq(positionId != null, "position_id", positionId)
                .eq(status != null, "status", status)
                .orderByDesc("employee_id");

        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(query -> query.like("employee_number", keyword)
                    .or()
                    .like("name", keyword)
                    .or()
                    .like("phone", keyword));
        }

        List<Employee> employees = employeeMapper.selectList(wrapper);
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Employee employee : employees) {
            resultList.add(buildEmployeeDetail(employee));
        }
        return buildPageBean(resultList, pageNum, pageSize);
    }

    @Override
    public Map<String, Object> getEmployee(String employeeNumber) {
        Employee employee;
        if (employeeNumber != null && !employeeNumber.isBlank()) {
            QueryWrapper<Employee> wrapper = new QueryWrapper<>();
            wrapper.eq("employee_number", employeeNumber)
                    .eq("delete_flag", 0);
            employee = employeeMapper.selectOne(wrapper);
        } else {
            throw new BusinessException(BAD_REQUEST_CODE, "工号不能为空");
        }

        if (employee == null) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }
        return buildEmployeeDetail(employee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmployee(Map<String, Object> employee) {
        requireBody(employee, "员工信息不能为空");

        Integer employeeId = requireInteger(employee, "员工ID", "employeeId");
        Employee entity = employeeMapper.selectById(employeeId);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }

        String employeeNumber = getString(employee, "employeeNumber");
        if (employeeNumber != null) {
            entity.setEmployeeNumber(employeeNumber);
        }

        String name = getString(employee, "name");
        if (name != null) {
            entity.setName(name);
        }

        String gender = getString(employee, "gender");
        if (gender != null) {
            entity.setGender(gender);
        }

        LocalDate birthDate = getLocalDate(employee, "birthDate");
        if (birthDate != null) {
            entity.setBirthDate(birthDate);
        }

        String phone = getString(employee, "phone");
        if (phone != null) {
            entity.setPhone(phone);
        }

        String email = getString(employee, "email");
        if (email != null) {
            entity.setEmail(email);
        }

        String address = getString(employee, "address");
        if (address != null) {
            entity.setAddress(address);
        }

        String idCard = getString(employee, "idCard");
        if (idCard != null) {
            entity.setIdCard(idCard);
        }

        LocalDate hireDate = getLocalDate(employee, "hireDate");
        if (hireDate != null) {
            entity.setHireDate(hireDate);
        }

        Integer departmentId = getInteger(employee, "departmentId");
        if (departmentId != null) {
            ensureDepartmentExists(departmentId);
            entity.setDepartmentId(departmentId);
        }

        Integer positionId = getInteger(employee, "positionId");
        if (positionId != null) {
            ensurePositionExists(positionId);
            entity.setPositionId(positionId);
        }

        employeeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Map<String, Object> passwordInfo) {
        requireBody(passwordInfo, "密码信息不能为空");

        Integer employeeId = getInteger(passwordInfo, "employeeId", "empyloeeId");
        if (employeeId == null) {
            employeeId = currentEmployeeIdOrNull();
        }
        if (employeeId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "员工ID不能为空");
        }

        Employee entity = employeeMapper.selectById(employeeId);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }

        String oldPassword = requireString(passwordInfo, "旧密码", "oldPassword");
        String newPassword = requireString(passwordInfo, "新密码", "newPassword");
        if (!passwordMatched(oldPassword, entity.getPassword())) {
            throw new BusinessException(BAD_REQUEST_CODE, "旧密码不正确");
        }

        String encodedPassword = PasswordUtil.encode(newPassword);
        // String encodedPassword = newPassword; // 调试时可改为明文保存
        entity.setPassword(encodedPassword);
        employeeMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteEmployee(Integer employeeId) {
        if (employeeId == null) {
            throw new BusinessException(BAD_REQUEST_CODE, "员工ID不能为空");
        }
        Employee entity = employeeMapper.selectById(employeeId);
        if (entity == null || entity.isDeleteFlag()) {
            throw new BusinessException(NOT_FOUND_CODE, "员工不存在");
        }
        entity.setDeleteFlag(true);
        employeeMapper.updateById(entity);
    }

    private void ensureDepartmentExists(Integer departmentId) {
        QueryWrapper<Department> wrapper = new QueryWrapper<>();
        wrapper.eq("department_id", departmentId)
                .eq("delete_flag", 0);
        if (departmentMapper.selectOne(wrapper) == null) {
            throw new BusinessException(NOT_FOUND_CODE, "部门不存在");
        }
    }

    private void ensurePositionExists(Integer positionId) {
        QueryWrapper<Position> wrapper = new QueryWrapper<>();
        wrapper.eq("position_id", positionId)
                .eq("delete_flag", 0);
        if (positionMapper.selectOne(wrapper) == null) {
            throw new BusinessException(NOT_FOUND_CODE, "职级不存在");
        }
    }

    private String resolveEmployeeNumber(Map<String, Object> employee) {
        String employeeNumber = getString(employee, "employeeNumber");
        if (employeeNumber != null) {
            return employeeNumber;
        }
        return java.time.YearMonth.now().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }

    private boolean passwordMatched(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        if (rawPassword.equals(encodedPassword)) {
            return true;
        }
        try {
            return PasswordUtil.matches(rawPassword, encodedPassword);
        } catch (Exception ex) {
            return false;
        }
    }

    private Map<String, Object> buildEmployeeDetail(Employee employee) {
        Department department = null;
        if (employee.getDepartmentId() != null) {
            QueryWrapper<Department> deptWrapper = new QueryWrapper<>();
            deptWrapper.eq("department_id", employee.getDepartmentId())
                    .eq("delete_flag", 0);
            department = departmentMapper.selectOne(deptWrapper);
        }
        Position position = null;
        if (employee.getPositionId() != null) {
            QueryWrapper<Position> posWrapper = new QueryWrapper<>();
            posWrapper.eq("position_id", employee.getPositionId())
                    .eq("delete_flag", 0);
            position = positionMapper.selectOne(posWrapper);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("employeeId", employee.getEmployeeId());
        result.put("employeeNumber", employee.getEmployeeNumber());
        result.put("name", employee.getName());
        result.put("gender", employee.getGender());
        result.put("birthDate", employee.getBirthDate());
        result.put("phone", employee.getPhone());
        result.put("email", employee.getEmail());
        result.put("address", employee.getAddress());
        result.put("idCard", employee.getIdCard());
        result.put("hireDate", employee.getHireDate());
        result.put("departmentId", employee.getDepartmentId());
        result.put("departmentName", department == null ? null : department.getDepartmentName());
        result.put("positionId", employee.getPositionId());
        result.put("positionName", position == null ? null : position.getPositionName());
        result.put("status", employee.getStatus());
        result.put("deleteFlag", employee.isDeleteFlag());
        return result;
    }
}
