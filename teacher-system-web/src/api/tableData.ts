import {del, get, post} from '@/utils/request'

/**
 * 表格数据项
 */
export interface TableDataItem {
    id: number
    tableId: number
    userId: string                     // 所属用户工号
    submissionPeriod: string | null    // 提交时期（格式：YYYY-MM）
    dataContent: Record<string, any>  // 动态字段数据
    score: number | null               // 分数
    reviewMaterial: string | null      // 审核材料
    rejectReason: string | null        // 退回原因
    status: number                     // 数据状态：0=暂存，1=已提交，2=已打分，3=已退回
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
    userId?: string                    // 所属用户工号
    submissionPeriod?: string          // 提交时期（格式：YYYY-MM）
    dataContent: Record<string, any>
    score?: number | null
    reviewMaterial?: string | null
    status?: number                    // 数据状态：0=暂存，1=已提交，2=已打分
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

/**
 * 用户在表格中的得分信息
 */
export interface TableUserScore {
    userId: string
    username: string
    dataCount: number
    totalScore: number
    avgScore: number
    dataList: TableDataItem[]
}

/**
 * 表格得分统计（管理员视角）
 */
export interface TableScoreStatistics {
    tableId: number
    tableName: string
    totalUsers: number
    totalDataCount: number
    totalScore: number
    userScores: TableUserScore[]
}

/**
 * 获取表格的用户得分统计（管理员功能）
 */
export function getTableScoreStatistics(tableId: number): Promise<TableScoreStatistics> {
    return get(`/table/${tableId}/score-statistics`)
}

/**
 * 暂存数据（普通用户功能）
 */
export function saveDraft(data: SaveTableDataRequest): Promise<number> {
    return post('/table/data/draft', data)
}

/**
 * 提交数据（普通用户功能）
 */
export function submitData(data: SaveTableDataRequest): Promise<number> {
    return post('/table/data/submit', data)
}

/**
 * 退回数据请求
 */
export interface RejectDataRequest {
    id: number           // 数据ID
    rejectReason: string // 退回原因
}

/**
 * 退回数据（管理员功能）
 */
export function rejectData(data: RejectDataRequest): Promise<string> {
    return post('/table/data/reject', data)
}

/**
 * 全局数据统计
 */
export interface GlobalDataStatistics {
    totalCount: number     // 总数据量
    pendingCount: number   // 待审核（status=1）
    scoredCount: number    // 已打分（status=2）
}

/**
 * 用户数据按状态统计
 */
export interface UserStatusStatistics {
    pendingCount: number    // 待审核（status=1）
    scoredCount: number     // 已打分（status=2）
    rejectedCount: number   // 已退回（status=3）
}

/**
 * 获取全局数据统计（管理员功能）
 */
export function getGlobalStatistics(): Promise<GlobalDataStatistics> {
    return get('/table/data/global-statistics')
}

/**
 * 获取当前用户的数据按状态分类统计
 */
export function getMyStatusStatistics(): Promise<UserStatusStatistics> {
    return get('/table/data/my-status-statistics')
}

