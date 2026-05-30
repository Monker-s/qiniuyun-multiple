package com.contentpublish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.contentpublish.common.Result;
import com.contentpublish.entity.UserPlatform;
import com.contentpublish.mapper.UserPlatformMapper;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class AccountController {

    private final UserPlatformMapper userPlatformMapper;

    public AccountController(UserPlatformMapper userPlatformMapper) {
        this.userPlatformMapper = userPlatformMapper;
    }

    @GetMapping("/platforms")
    public Result<List<UserPlatform>> list() {
        return Result.ok(userPlatformMapper.selectList(
            new LambdaQueryWrapper<UserPlatform>().eq(UserPlatform::getIsActive, true)));
    }

    @PostMapping("/platforms")
    public Result<UserPlatform> add(@RequestBody UserPlatform platform) {
        platform.setIsActive(true);
        platform.setId(null);
        userPlatformMapper.insert(platform);
        return Result.ok(platform);
    }

    @DeleteMapping("/platforms/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        UserPlatform p = userPlatformMapper.selectById(id);
        if (p != null && p.getIsBuiltin()) {
            throw new IllegalArgumentException("系统内置平台不可删除");
        }
        userPlatformMapper.deleteById(id);
        return Result.ok();
    }
}
