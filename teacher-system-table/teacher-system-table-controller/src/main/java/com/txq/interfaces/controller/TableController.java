package com.txq.interfaces.controller;

import com.txq.application.service.ITableService;
import com.txq.common.annotation.ApiRequestMapping;
import com.txq.common.annotation.RequiresRole;
import com.txq.common.result.Response;
import com.txq.interfaces.converter.TableConverter;
import com.txq.interfaces.dto.TableDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 表格控制器
 */
@ApiRequestMapping("/table")
@RequiredArgsConstructor
public class TableController {

    private final ITableService tableService;

    /**
     * 创建表格
     * 只有超级管理员(roleId=1)才有权限
     */
    @PostMapping("/create-table")
    // @RequiresRole(1)  // TODO: 临时注释，测试完成后需要恢复
    public Response<String> createTable(@RequestBody TableDTO tableDTO) {
        tableService.createTable(
                TableConverter.INSTANCE.toQuery(tableDTO)
        );
        return Response.success("创建成功");
    }
}
