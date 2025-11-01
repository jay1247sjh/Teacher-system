import { get, post, del } from '@/utils/request'

/**
 * 表格数据项
 */
export interface TableDataItem {
    id: number
    tableId: number
    dataContent: Record<string, any>  // 动态字段数据
    score: number | null               // 分数
    reviewMaterial: string | null      // 审核材料
    createdBy: string
    updatedBy: string | null
    createdAt: string
    updatedAt: string
}

/**
 * 保存数据请求
 */
export interface SaveTableDataRequest {
    id?: number  // 有ID则更新，无ID则新增
    tableId: number
    dataContent: Record<string, any>
    score?: number | null
    reviewMaterial?: string | null
}

/**
 * 获取表格数据列表
 */
export function getTableData(tableId: number): Promise<TableDataItem[]> {
    return get(`/table/${tableId}/data`)
}

/**
 * 保存表格数据（新增或更新）
 */
export function saveTableData(data: SaveTableDataRequest): Promise<number> {
    return post('/table/data/save', data)
}

/**
 * 删除表格数据
 */
export function deleteTableData(id: number): Promise<string> {
    return del(`/table/data/${id}`)
}

/**
 * 批量删除表格数据
 */
export function batchDeleteTableData(ids: number[]): Promise<string> {
    return post('/table/data/batch-delete', ids)
}

