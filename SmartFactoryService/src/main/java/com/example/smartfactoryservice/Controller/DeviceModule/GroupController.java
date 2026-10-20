package com.example.smartfactoryservice.Controller.DeviceModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.DeviceModule.GroupService;
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
@RequestMapping("/api/device")
public class GroupController {

    private final GroupService groupService;

    @GetMapping("/getGroupList")
    public Result<PageBean> getGroupList(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer sort
    ) {
        return Result.success("获取分组列表成功",
                groupService.getGroupList(id, name, pageNum, pageSize, sort));
    }

    @PostMapping("/addGroup")
    public Result<Void> addGroup(@RequestBody(required = false) Map<String, Object> group) {
        groupService.addGroup(group);
        return Result.success("新建分组成功", null);
    }

    @PutMapping("/updateGroup")
    public Result<Void> updateGroup(
            @RequestParam(required = false) Integer id,
            @RequestBody(required = false) Map<String, Object> group) {
        groupService.updateGroup(id, group);
        return Result.success("修改分组成功", null);
    }

    @DeleteMapping("/deleteGroup")
    public Result<Void> deleteGroup(@RequestParam(required = false) String id) {
        groupService.deleteGroup(id);
        return Result.success("删除分组成功", null);
    }
}
