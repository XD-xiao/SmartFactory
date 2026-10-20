package com.example.smartfactoryservice.Controller.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.PersonManagementModule.DepartmentService;
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
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping("/addDepartment")
    public Result<Void> addDepartment(@RequestBody(required = false) Map<String, Object> department) {
        departmentService.addDepartment(department);
        return Result.success("添加部门成功", null);
    }

    @GetMapping("/getDepartmeList")
    public Result<PageBean> getDepartmeList(
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) String departmentName,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer sort
    ) {
        return Result.success("获取部门列表成功",
                departmentService.getDepartmeList(pageNum, pageSize, departmentId, departmentName, status, sort));
    }

    @DeleteMapping("/deleteDepartment")
    public Result<Void> deleteDepartment(@RequestParam(required = false) Integer departmentId) {
        departmentService.deleteDepartment(departmentId);
        return Result.success("删除部门成功", null);
    }

    @PutMapping("/updateDepartment")
    public Result<Void> updateDepartment(@RequestBody(required = false) Map<String, Object> department) {
        departmentService.updateDepartment(department);
        return Result.success("修改部门成功", null);
    }
}
