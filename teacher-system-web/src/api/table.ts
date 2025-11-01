import { get, post } from '@/utils/request'

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
 * 表格列表项
 */
export interface TableListItem {
    tableId: number           // 表格ID
    tableFullName: string     // 表格全称
    tableAliasName: string    // 表格别称
    fieldCount: number        // 字段数量
    createTime: string        // 创建时间
}

/**
 * 创建表格
 */
export function createTable(data: TableDTO): Promise<string> {
    return post('/table/create-table', data)
}

/**
 * 获取表格列表
 */
export function getTableList(): Promise<TableListItem[]> {
    return get('/table/list')
}

/**
 * 获取表格字段列表
 */
export function getTableFields(tableId: number): Promise<TableFieldDTO[]> {
    return get(`/table/${tableId}/fields`)
}

