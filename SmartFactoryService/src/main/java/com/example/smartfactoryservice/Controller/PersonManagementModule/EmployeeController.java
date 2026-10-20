package com.example.smartfactoryservice.Controller.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.PersonManagementModule.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pm")
public class EmployeeController {

    private final EmployeeService employeeService;

//    @PostMapping("/accessControl")
//    public Result<Object> accessControl(@RequestBody(required = false) Map<String, Object> accessInfo) {
//        return Result.success("身份正确", employeeService.accessControl(accessInfo));
//    }

    @PostMapping("/addEmployee")
    public Result<String> addEmployee(@RequestBody(required = false) Map<String, Object> employee) {
        return Result.success("添加员工成功", employeeService.addEmployee(employee));
    }

    @PostMapping("/login")
    public Result<Object> login(@RequestBody(required = false) Map<String, Object> loginInfo) {

        return Result.success("员工登录成功", employeeService.login(loginInfo));
    }

    @GetMapping("/getEmployeeList")
    public Result<PageBean> getEmployeeList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Integer positionId,
            @RequestParam(required = false) Integer status
    ) {
        return Result.success("获取员工列表成功",
                employeeService.getEmployeeList(pageNum, pageSize, keyword, departmentId, positionId, status));
    }

    @GetMapping("/getEmployee")
    public Result<Object> getEmployee(
            @RequestParam(required = false) String employeeNumber
    ) {
        return Result.success("获取员工信息成功", employeeService.getEmployee(employeeNumber));
    }

    @PutMapping("/updateEmployee")
    public Result<Void> updateEmployee(@RequestBody(required = false) Map<String, Object> employee) {
        employeeService.updateEmployee(employee);
        return Result.success("修改员工信息成功", null);
    }

    @PutMapping("/updatePassword")
    public Result<Void> updatePassword(@RequestBody(required = false) Map<String, Object> passwordInfo) {
        employeeService.updatePassword(passwordInfo);
        return Result.success("修改员工密码成功", null);
    }

    @DeleteMapping("/deleteEmployee")
    public Result<Void> deleteEmployee(@RequestParam(required = false) Integer employeeId) {
        System.out.println("=====================");
        employeeService.deleteEmployee(employeeId);
        return Result.success("删除员工成功", null);
    }
}
