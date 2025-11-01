import { post } from '@/utils/request'

/**
 * 表格字段DTO
 */
export interface TableFieldDTO {
    root: boolean      // 是否为管理员操作字段
    fieldName: string  // 字段名称
    calc: boolean      // 是否为计算字段
}

/**
 * 创建表格DTO
 */
export interface TableDTO {
    tableFullName: string       // 表格全称
    tableAliasName: string      // 表格别称
    tableFields: TableFieldDTO[] // 字段列表
}

/**
 * 创建表格
 */
export function createTable(data: TableDTO): Promise<string> {
    return post('/table/create-table', data)
}

