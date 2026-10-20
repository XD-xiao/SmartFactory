package com.example.smartfactoryservice.Controller.PersonManagementModule;

import com.example.smartfactoryservice.Domain.Vo.PageBean;
import com.example.smartfactoryservice.Domain.Vo.Result;
import com.example.smartfactoryservice.Service.PersonManagementModule.PositionService;
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
public class PositionController {

    private final PositionService positionService;

    @PostMapping("/addPosition")
    public Result<Void> addPosition(@RequestBody(required = false) Map<String, Object> position) {
        positionService.addPosition(position);
        return Result.success("增加职级成功", null);
    }

    @GetMapping("/getPositionList")
    public Result<PageBean> getPositionList(
            @RequestParam(required = false) Integer positionId,
            @RequestParam(required = false) String positionName,
            @RequestParam(required = false, defaultValue = "1") int pageNum,
            @RequestParam(required = false, defaultValue = "10") int pageSize,
            @RequestParam(required = false) Integer sort
    ) {
        return Result.success("获取职级列表成功",
                positionService.getPositionList(positionId, positionName, pageNum, pageSize, sort));
    }

    @DeleteMapping("/deletePosition")
    public Result<Void> deletePosition(@RequestParam(required = false) Integer positionId) {
        positionService.deletePosition(positionId);
        return Result.success("删除职级成功", null);
    }

    @PutMapping("/updatePosition")
    public Result<Void> updatePosition(@RequestBody(required = false) Map<String, Object> position) {
        positionService.updatePosition(position);
        return Result.success("修改职级成功", null);
    }
}
