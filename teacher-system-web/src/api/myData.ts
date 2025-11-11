import {get} from '@/utils/request'

/**
 * 数据内容
 */
export interface DataContent {
  [key: string]: string
}

/**
 * 用户数据项
 */
export interface UserDataItem {
  id: number
  tableId: number
  tableName: string
  dataContent: DataContent
  score: number | null
  reviewMaterial: string | null
  createdAt: string
  updatedAt: string
}

/**
 * 按表格分组的数据
 */
export interface DataByTable {
  tableId: number
  tableName: string
  dataList: UserDataItem[]
}

/**
 * 用户数据统计
 */
export interface UserDataStatistics {
  totalCount: number      // 总数据条数
  tableCount: number       // 涉及表格数
  totalScore: number       // 总分数
  avgScore: number         // 平均分数
  dataByTable: DataByTable[]  // 按表格分组的数据
}

/**
 * 获取当前用户的所有数据统计
 */
export function getMyDataStatistics() {
  return get<UserDataStatistics>('/table/data/my-statistics')
}

