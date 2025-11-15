<template>
  <section class="table-detail-content">
    <div class="detail-header">
      <h2 class="detail-title">{{ tableInfo.tableFullName || '加载中...' }}</h2>
      <p class="detail-subtitle">
        <span>别名：{{ tableInfo.tableAliasName }}</span>
        <span class="divider">|</span>
        <span>字段数：{{ tableInfo.fieldCount }}</span>
        <span class="divider">|</span>
        <span>创建时间：{{ formatTime(tableInfo.createTime) }}</span>
      </p>
    </div>

    <div class="table-structure-section">
      <h3 class="section-title">表结构</h3>
      <div v-if="loading" class="loading-state">加载中...</div>
      <div v-else-if="fields.length === 0" class="empty-state">暂无字段信息</div>
      <div v-else class="fields-grid">
        <div v-for="(field, index) in fields" :key="index" class="field-card">
          <div class="field-card-header">
            <span class="field-name">{{ field.fieldName }}</span>
            <span v-if="field.root" class="field-tag field-tag-admin">管理员</span>
            <span v-if="field.calc" class="field-tag field-tag-calc">计算字段</span>
          </div>
          <div class="field-card-footer">
            <span class="field-access">{{ field.root ? '仅管理员可操作' : '普通用户可操作' }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 用户得分统计（仅管理员可见） -->
    <div v-if="canViewStatistics" class="score-statistics-section">
      <div class="statistics-header">
        <h3 class="section-title">用户得分统计</h3>

        <!-- 统计时间区间筛选 -->
        <div class="statistics-filter">
          <label class="filter-label">统计时期：</label>
          <el-date-picker
              v-model="statisticsDateRange"
              type="monthrange"
              range-separator="至"
              start-placeholder="开始月份"
              end-placeholder="结束月份"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              style="width: 280px"
              @change="loadScoreStatistics"
          />
        </div>
      </div>

      <div v-if="statisticsLoading" class="loading-state">加载中...</div>
      <div v-else-if="filteredScoreStatistics && filteredScoreStatistics.userScores.length > 0"
           class="statistics-content">
        <!-- 整体统计卡片 -->
        <div class="overall-stats">
          <div class="stat-item">
            <span class="stat-label">总用户数</span>
            <span class="stat-value">{{ filteredScoreStatistics.totalUsers }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总数据条数</span>
            <span class="stat-value">{{ filteredScoreStatistics.totalDataCount }}</span>
          </div>
          <div class="stat-item">
            <span class="stat-label">总分数</span>
            <span class="stat-value highlight">{{ filteredScoreStatistics.totalScore.toFixed(2) }}</span>
          </div>
        </div>

        <!-- 用户得分列表 -->
        <div class="user-score-list">
          <div v-for="userScore in filteredScoreStatistics.userScores" :key="userScore.userId"
               class="user-score-item"
               :class="{ 'expanded': expandedUserId === userScore.userId }"
               @click="toggleUserDetail(userScore.userId)">
            <div class="user-score-header">
              <div class="user-info">
                <span class="user-rank">🏅</span>
                <span class="user-id">{{ userScore.userId }}</span>
                <span class="user-name">{{ userScore.username }}</span>
              </div>
              <div class="score-info">
                <span class="data-count">{{ userScore.dataCount }} 条数据</span>
                <span class="total-score">总分: {{ userScore.totalScore.toFixed(2) }}</span>
                <span class="avg-score">平均: {{ userScore.avgScore.toFixed(2) }}</span>
                <span class="expand-icon">{{ expandedUserId === userScore.userId ? '▼' : '▶' }}</span>
              </div>
            </div>

            <!-- 用户数据详情（展开时显示） -->
            <div v-if="expandedUserId === userScore.userId" class="user-data-detail" @click.stop>
              <div v-for="data in userScore.dataList" :key="data.id" class="data-detail-item">
                <div class="data-detail-header">
                  <span class="data-id">#{{ data.id }}</span>
                  <span class="data-date">{{ formatPeriod(data.submissionPeriod) }}</span>
                  <span v-if="data.score !== null" class="data-score-badge">{{ data.score }} 分</span>
                </div>
                <div class="data-detail-content">
                  <div v-for="(value, key) in data.dataContent" :key="key" class="data-field-row">
                    <span class="field-label">{{ key }}:</span>
                    <span class="field-value">{{ value }}</span>
                  </div>
                </div>
                <div v-if="data.reviewMaterial" class="data-detail-attachment">
                  <span class="attachment-icon">📎</span>
                  <span class="attachment-link" @click="openAttachment(data.reviewMaterial)">
                    {{ getAttachmentName(data.reviewMaterial) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="empty-state">暂无用户数据统计</div>
    </div>

    <div class="table-data-section">
      <div class="data-header">
        <h3 class="section-title">表数据</h3>
        <button class="btn-add" @click="openAddDialog">+ 填写申报表</button>
      </div>

      <!-- 筛选条件区域 -->
      <div class="filter-section">
        <div class="filter-row">
          <!-- 时间区间筛选 -->
          <div class="filter-item">
            <label class="filter-label">成果时期：</label>
            <el-date-picker
                v-model="filterDateRange"
                type="monthrange"
                range-separator="至"
                start-placeholder="开始月份"
                end-placeholder="结束月份"
                format="YYYY年MM月"
                value-format="YYYY-MM"
                style="width: 280px"
                @change="handleFilterChange"
            />
          </div>

          <!-- 状态筛选（仅管理员可见） -->
          <div v-if="isAdmin" class="filter-item">
            <label class="filter-label">审核状态：</label>
            <el-select
                v-model="filterStatus"
                placeholder="全部状态"
                style="width: 150px"
                clearable
                @change="handleFilterChange"
            >
              <el-option label="暂存" :value="0"/>
              <el-option label="待审核" :value="1"/>
              <el-option label="审核完成" :value="2"/>
              <el-option label="已退回" :value="3"/>
            </el-select>
          </div>

          <!-- 清空筛选按钮 -->
          <div class="filter-item">
            <button class="btn-clear-filter" @click="clearFilters">
              清空筛选
            </button>
          </div>
        </div>

        <!-- 筛选结果统计 -->
        <div v-if="hasActiveFilters" class="filter-result">
          <span class="filter-result-text">
            共找到 <strong>{{ filteredTableData.length }}</strong> 条数据
          </span>
        </div>
      </div>

      <div v-if="dataLoading" class="loading-state">加载中...</div>
      <div v-else-if="filteredTableData.length === 0" class="empty-state">
        <p>{{ hasActiveFilters ? '没有符合条件的数据' : '暂无数据记录' }}</p>
        <p class="empty-hint">{{ hasActiveFilters ? '请调整筛选条件' : '点击上方"添加数据"按钮开始录入' }}</p>
      </div>
      <div v-else class="data-table-container">
        <table class="data-table">
          <thead>
          <tr>
            <th class="col-index">序号</th>
            <th class="col-period">成果时期</th>
            <th v-for="field in fields" :key="field.fieldName" class="col-field">
              {{ field.fieldName }}
              <span v-if="field.root" class="lock-icon" title="管理员字段">🔒</span>
            </th>
            <!-- 管理员显示分数和状态列 -->
            <th v-if="isAdmin" class="col-status">审核状态</th>
            <th v-if="isAdmin" class="col-score">分数</th>
            <!-- 普通用户只显示状态列 -->
            <th v-else class="col-status">状态</th>
            <th class="col-material">审核材料</th>
            <th class="col-actions">操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(row, index) in filteredTableData" :key="row.id">
            <td class="col-index">{{ index + 1 }}</td>
            <td class="col-period">
              {{ row.submissionPeriod ? formatPeriod(row.submissionPeriod) : (row.status === 0 ? '未设置' : '-') }}
            </td>
            <td v-for="field in fields" :key="field.fieldName" class="col-field">
              {{ row.dataContent[field.fieldName] || '-' }}
            </td>
            <!-- 管理员显示状态和分数 -->
            <template v-if="isAdmin">
              <td class="col-status">
                <div class="status-container">
                    <span :class="['status-badge', getAdminStatusClass(row.status, row.score)]">
                      {{ getAdminStatusText(row.status, row.score) }}
                    </span>
                  <div v-if="row.status === 3 && row.rejectReason" class="reject-reason-hint" :title="row.rejectReason">
                    <span class="reject-icon">⚠</span>
                  </div>
                </div>
              </td>
              <td class="col-score">{{ row.score !== null ? row.score : '-' }}</td>
            </template>
            <!-- 普通用户只显示状态 -->
            <td v-else class="col-status">
              <div class="status-container">
                  <span :class="['status-badge', getStatusClass(row.status, row.score)]">
                    {{ getStatusText(row.status, row.score) }}
                  </span>
                <div v-if="row.status === 3 && row.rejectReason" class="reject-reason-hint" :title="row.rejectReason">
                  <span class="reject-icon">⚠</span>
                </div>
              </div>
            </td>
            <td class="col-material">
                <span v-if="row.reviewMaterial" class="attachment-link" @click="openAttachment(row.reviewMaterial)">
                  📎 {{ getAttachmentName(row.reviewMaterial) }}
                </span>
              <span v-else>-</span>
            </td>
            <td class="col-actions">
              <!-- 根据状态和角色显示不同的操作按钮 -->
              <div class="action-buttons">
                <button v-if="canEdit(row)" class="btn-edit" @click="openEditDialog(row)">
                  {{ row.id === -1 ? '继续编辑' : (row.status === 3 ? '重新提交' : '修改') }}
                </button>
                <!-- 管理员显示退回按钮，普通用户显示删除按钮 -->
                <button v-if="isAdmin && canReject(row)" class="btn-reject" @click="openRejectDialog(row)">
                  退回
                </button>
                <button v-else-if="canDelete(row)" class="btn-delete"
                        @click="row.id === -1 ? handleDeleteDraft() : handleDelete(row.id)">
                  {{ row.id === -1 ? '删除暂存' : '删除' }}
                </button>
                <span v-if="!canEdit(row) && !canDelete(row) && !canReject(row)" class="text-muted">审核通过</span>
              </div>
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 添加/编辑数据对话框 -->
    <div v-if="showDataDialog" class="dialog-overlay" @click.self="closeDataDialog">
      <div class="dialog-container">
        <div class="dialog-header">
          <h3>{{ editingData ? '编辑数据' : '添加数据' }}</h3>
          <button class="dialog-close" @click="closeDataDialog">×</button>
        </div>
        <div class="dialog-body">
          <div v-for="field in fields" :key="field.fieldName" class="form-group">
            <label class="form-label">
              {{ field.fieldName }}
              <span v-if="field.root && !canEditAdminField" class="lock-icon" title="无权限编辑管理员字段">🔒</span>
            </label>
            <input
                v-model="formData.dataContent[field.fieldName]"
                :disabled="field.root && !canEditAdminField"
                class="form-input"
                :class="{ 'input-disabled': field.root && !canEditAdminField }"
                :placeholder="`请输入${field.fieldName}`"
            />
          </div>
          <!-- 所属用户选择（仅管理员可见） -->
          <div v-if="canSelectUser" class="form-group">
            <label class="form-label">
              所属用户
              <span class="required-mark">*</span>
            </label>
            <div
                class="custom-select"
                :class="{ open: userSelectOpen, disabled: loadingUsers }"
                @click="!loadingUsers && (userSelectOpen = !userSelectOpen)"
            >
              <div class="select-value">
                {{ selectedUserDisplay }}
                <span class="select-arrow">⌄</span>
              </div>
              <ul v-if="userSelectOpen && !loadingUsers" class="custom-select-dropdown">
                <li
                    :class="{ selected: formData.userId === '' }"
                    @click.stop="selectUser('')"
                >
                  请选择用户
                </li>
                <li
                    v-for="user in normalUsers"
                    :key="user.id"
                    :class="{ selected: formData.userId === user.id }"
                    @click.stop="selectUser(user.id)"
                >
                  {{ user.id }} - {{ user.username }}
                </li>
              </ul>
            </div>
            <p v-if="loadingUsers" class="field-hint">加载用户列表中...</p>
          </div>

          <!-- 提交时期选择（年月选择器） -->
          <div class="form-group">
            <label class="form-label">
              成果日期
              <span class="required-mark">*</span>
            </label>
            <el-date-picker
                v-model="submissionPeriodDate"
                type="month"
                placeholder="请选择成果时期（年-月）"
                format="YYYY年MM月"
                value-format="YYYY-MM"
                style="width: 100%"
                :clearable="false"
                :editable="false"
                @change="handlePeriodChange"
            />
            <p class="field-hint">选择此次数据的所属时期</p>
          </div>

          <!-- 分数字段（仅管理员可见） -->
          <div v-if="canSetScore" class="form-group">
            <label class="form-label">
              分数
            </label>
            <input
                v-model.number="formData.score"
                type="number"
                step="0.01"
                class="form-input"
                placeholder="请输入分数"
            />
          </div>
          <div class="form-group">
            <label class="form-label">审核材料</label>
            <div class="attachment-section">
              <!-- 已上传的附件 -->
              <div v-if="formData.reviewMaterial" class="attachment-item">
                <span class="attachment-icon">📎</span>
                <span class="attachment-name" @click="openAttachment(formData.reviewMaterial)">
                  {{ getAttachmentName(formData.reviewMaterial) }}
                </span>
                <button class="btn-remove-attachment" @click="removeAttachment" type="button">×</button>
              </div>

              <!-- 上传按钮 -->
              <div v-else class="upload-attachment-area">
                <input
                    ref="fileInput"
                    type="file"
                    @change="handleFileSelect"
                    accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.jpg,.jpeg,.png,.gif,.bmp,.webp,.zip,.rar,.7z,.txt,.md"
                    style="display: none"
                />
                <button class="btn-upload-attachment" @click="triggerFileInput" type="button">
                  <span class="upload-icon">📁</span>
                  <span>选择文件上传</span>
                </button>
                <p class="upload-hint">支持 PDF、Word、Excel、PPT、图片、压缩包等，最大10MB</p>
              </div>

              <!-- 上传进度 -->
              <div v-if="uploadingAttachment" class="uploading-indicator">
                <span class="loading-spinner">⏳</span>
                <span>上传中...</span>
              </div>
            </div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeDataDialog">取消</button>
          <!-- 只有普通用户才显示暂存按钮 -->
          <button v-if="!isAdmin" class="btn-draft" @click="handleSaveDraft">暂存</button>
          <button class="btn-confirm" @click="handleSave">{{ isAdmin ? '保存' : '申报' }}</button>
        </div>
      </div>
    </div>

    <!-- 退回对话框 -->
    <div v-if="showRejectDialog" class="dialog-overlay" @click.self="closeRejectDialog">
      <div class="dialog-container dialog-small">
        <div class="dialog-header">
          <h3>退回数据</h3>
          <button class="dialog-close" @click="closeRejectDialog">×</button>
        </div>
        <div class="dialog-body">
          <div class="reject-info">
            <p><strong>用户：</strong>{{ rejectingData?.userId }}</p>
            <p><strong>提交时期：</strong>{{ rejectingData?.submissionPeriod || '未设置' }}</p>
          </div>
          <div class="form-group">
            <label class="form-label">
              退回原因
              <span class="required-mark">*</span>
            </label>
            <textarea
                v-model="rejectReason"
                class="form-textarea"
                placeholder="请详细说明退回原因，将通过邮件通知用户"
                rows="5"
            ></textarea>
            <p class="field-hint">退回原因将通过邮件发送给用户</p>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn-cancel" @click="closeRejectDialog">取消</button>
          <button class="btn-reject-confirm" @click="confirmReject">确认退回</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script lang="ts">
import {defineComponent} from 'vue';
import {ElDatePicker, ElMessage, ElMessageBox, ElOption, ElSelect} from 'element-plus';
import {getTableFields, getTableList, type TableListItem} from '@/api/table';
import {
  deleteTableData,
  getTableData,
  getTableScoreStatistics,
  rejectData,
  saveTableData,
  type TableDataItem,
  type TableScoreStatistics
} from '@/api/tableData';
import {uploadAttachment} from '@/api/attachment';
import {getNormalUsers, type SimpleUser} from '@/api/user';
import {useUserStore} from '@/store/user';

interface TableField {
  fieldName: string;
  root: boolean;
  calc: boolean;
}

interface FormData {
  id?: number;
  userId?: string;
  submissionPeriod: string;
  dataContent: Record<string, any>;
  score: number | null;
  reviewMaterial: string | null;
}

export default defineComponent({
  name: 'TableDetail',
  components: {
    ElDatePicker,
    ElSelect,
    ElOption
  },
  data() {
    return {
      loading: false,
      dataLoading: false,
      statisticsLoading: false,
      tableInfo: {} as TableListItem,
      fields: [] as TableField[],
      tableData: [] as TableDataItem[],
      scoreStatistics: null as TableScoreStatistics | null,
      expandedUserId: null as string | null,
      showDataDialog: false,
      editingData: null as TableDataItem | null,
      formData: {
        dataContent: {},
        userId: '',
        submissionPeriod: '',
        score: null,
        reviewMaterial: null
      } as FormData,
      submissionPeriodDate: '', // 用于 el-date-picker 的 v-model
      uploadingAttachment: false,
      normalUsers: [] as SimpleUser[],
      loadingUsers: false,
      userSelectOpen: false,
      // 筛选条件
      filterDateRange: null as [string, string] | null,
      filterStatus: null as number | null,
      // 统计时间区间
      statisticsDateRange: null as [string, string] | null,
      // 退回对话框相关
      showRejectDialog: false,
      rejectingData: null as TableDataItem | null,
      rejectReason: ''
    };
  },
  computed: {
    // 筛选后的表格数据
    filteredTableData(): TableDataItem[] {
      let result = this.tableData;
      console.log('原始数据条数:', result.length);
      console.log('原始数据中的暂存:', result.filter(item => item.status === 0).length);

      // 管理员默认不显示暂存数据（status=0）
      // 除非用户明确选择筛选暂存数据（filterStatus === 0）
      if (this.isAdmin && this.filterStatus !== 0) {
        result = result.filter(item => item.status !== 0);
        console.log('管理员过滤暂存数据后:', result.length);
      }

      // 时间区间筛选
      if (this.filterDateRange && this.filterDateRange.length === 2) {
        const [startMonth, endMonth] = this.filterDateRange;
        result = result.filter(item => {
          // 暂存数据即使没有时间也显示
          if (item.status === 0) return true;
          // 其他数据需要有时间且在区间内
          if (!item.submissionPeriod) return false;
          return item.submissionPeriod >= startMonth && item.submissionPeriod <= endMonth;
        });
        console.log('时间筛选后数据条数:', result.length);
      }

      // 状态筛选（管理员）
      if (this.filterStatus !== null && this.filterStatus !== undefined) {
        result = result.filter(item => {
          if (this.filterStatus === 0) {
            // 筛选暂存数据
            return item.status === 0;
          } else if (this.filterStatus === 1) {
            // 筛选待审核数据（已提交但未打分且未退回）
            return item.status !== 0 && item.status !== 3 && item.score === null;
          } else if (this.filterStatus === 2) {
            // 筛选审核完成数据（已打分）
            return item.score !== null;
          } else if (this.filterStatus === 3) {
            // 筛选已退回数据
            return item.status === 3;
          }
          return true;
        });
        console.log('状态筛选后数据条数:', result.length, '筛选状态:', this.filterStatus);
      }

      console.log('最终显示数据条数:', result.length);
      return result;
    },

    // 筛选后的统计数据
    filteredScoreStatistics(): any {
      if (!this.scoreStatistics) return null;

      // 如果没有时间筛选，返回原始统计
      if (!this.statisticsDateRange || this.statisticsDateRange.length !== 2) {
        return this.scoreStatistics;
      }

      const [startMonth, endMonth] = this.statisticsDateRange;

      // 筛选每个用户的数据
      const filteredUserScores = this.scoreStatistics.userScores.map(userScore => {
        // 筛选该用户在时间区间内的数据
        const filteredDataList = userScore.dataList.filter((data: any) => {
          if (!data.submissionPeriod) return false;
          return data.submissionPeriod >= startMonth && data.submissionPeriod <= endMonth;
        });

        if (filteredDataList.length === 0) return null;

        // 重新计算统计数据
        const totalScore = filteredDataList
            .filter((data: any) => data.score !== null)
            .reduce((sum: number, data: any) => sum + parseFloat(data.score), 0);

        const scoredCount = filteredDataList.filter((data: any) => data.score !== null).length;
        const avgScore = scoredCount > 0 ? totalScore / scoredCount : 0;

        return {
          ...userScore,
          dataCount: filteredDataList.length,
          totalScore,
          avgScore,
          dataList: filteredDataList
        };
      }).filter(item => item !== null);

      // 计算整体统计
      const totalUsers = filteredUserScores.length;
      const totalDataCount = filteredUserScores.reduce((sum, user) => sum + user.dataCount, 0);
      const totalScore = filteredUserScores.reduce((sum, user) => sum + user.totalScore, 0);

      return {
        tableId: this.scoreStatistics.tableId,
        tableName: this.scoreStatistics.tableName,
        totalUsers,
        totalDataCount,
        totalScore,
        userScores: filteredUserScores.sort((a, b) => b.totalScore - a.totalScore)
      };
    },

    // 是否有激活的筛选条件
    hasActiveFilters(): boolean {
      return (this.filterDateRange !== null && this.filterDateRange !== undefined) ||
          (this.filterStatus !== null && this.filterStatus !== undefined);
    },

    // 判断是否可以设置分数（基于权限标识）
    canSetScore(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:score');
    },
    // 判断是否可以编辑管理员字段（基于权限标识）
    canEditAdminField(): boolean {
      const userStore = useUserStore();
      return userStore.hasPermission('table:data:admin-field');
    },
    // 获取当前用户ID
    currentUserId(): string {
      const userStore = useUserStore();
      return userStore.userId;
    },
    // 判断是否可以选择用户（管理员权限）
    canSelectUser(): boolean {
      const userStore = useUserStore();
      return userStore.hasAnyPermission(['table:data:admin-field', 'table:data:score']);
    },
    // 获取选中用户的显示文本
    selectedUserDisplay(): string {
      if (!this.formData.userId) {
        return '请选择用户';
      }
      const user = this.normalUsers.find(u => u.id === this.formData.userId);
      return user ? `${user.id} - ${user.username}` : '请选择用户';
    },
    // 判断是否可以查看统计（管理员权限）
    canViewStatistics(): boolean {
      const userStore = useUserStore();
      return userStore.hasAnyPermission(['table:data:admin-field', 'table:data:score']);
    },
    // 判断是否是管理员
    isAdmin(): boolean {
      const userStore = useUserStore();
      return userStore.hasAnyPermission(['table:data:admin-field', 'table:data:score']);
    }
  },
  mounted() {
    this.loadTableDetail();
    this.loadNormalUsers();
  },
  methods: {
    async loadTableDetail() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) {
        ElMessage.error('表格ID无效');
        this.$router.push({name: 'HomeWelcome'});
        return;
      }

      this.loading = true;
      try {
        // 从表格列表中查找当前表格
        const tables = await getTableList();
        const table = tables.find(t => t.tableId === tableId);

        if (!table) {
          ElMessage.error('表格不存在');
          this.$router.push({name: 'HomeWelcome'});
          return;
        }

        this.tableInfo = table;

        // 获取表格字段详情
        const fields = await getTableFields(tableId);
        this.fields = fields.map(f => ({
          fieldName: f.fieldName,
          root: f.root,
          calc: f.calc
        }));

        // 加载表格数据
        await this.loadTableData();

        // 如果是管理员，加载用户得分统计
        if (this.canViewStatistics) {
          await this.loadScoreStatistics();
        }

      } catch (error) {
        console.error('加载表格详情失败:', error);
        ElMessage.error('加载表格详情失败');
      } finally {
        this.loading = false;
      }
    },

    async loadTableData() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      this.dataLoading = true;
      try {
        // 从后端加载数据，并过滤掉本地暂存记录（id=-1不会从服务器返回，但为了安全起见还是过滤一下）
        let serverData = await getTableData(tableId);
        console.log('从服务器加载的数据:', serverData);

        // 过滤掉所有 id 为负数的记录（本地暂存记录）
        serverData = serverData.filter(item => item.id > 0);

        // 从 localStorage 加载暂存数据
        const draftKey = `table_data_draft_${tableId}`;
        const savedDraft = localStorage.getItem(draftKey);

        if (savedDraft) {
          try {
            const draft = JSON.parse(savedDraft);
            // 检查暂存数据的时间戳
            const draftAge = Date.now() - (draft.timestamp || 0);
            const sevenDays = 7 * 24 * 60 * 60 * 1000;

            if (draftAge < sevenDays) {
              // 构造一个临时的暂存数据项
              const draftItem: TableDataItem = {
                id: -1, // 使用负数ID表示这是本地暂存
                tableId: tableId,
                userId: draft.userId || this.currentUserId,
                submissionPeriod: draft.submissionPeriod || null,
                dataContent: draft.dataContent || {},
                score: draft.score || null,
                reviewMaterial: draft.reviewMaterial || null,
                rejectReason: null, // 暂存数据无退回原因
                status: 0, // 暂存状态
                createdBy: this.currentUserId,
                updatedBy: null,
                createdAt: new Date(draft.timestamp).toISOString(),
                updatedAt: new Date(draft.timestamp).toISOString()
              };

              // 将暂存数据添加到列表最前面
              serverData = [draftItem, ...serverData];
              console.log('添加了本地暂存数据');
            } else {
              // 清除过期的暂存数据
              localStorage.removeItem(draftKey);
            }
          } catch (error) {
            console.error('解析暂存数据失败:', error);
          }
        }

        this.tableData = serverData;
        console.log('最终数据（包含暂存）:', this.tableData);
        console.log('暂存数据:', this.tableData.filter(item => item.status === 0));
      } catch (error) {
        console.error('加载表格数据失败:', error);
        ElMessage.error('加载表格数据失败');
      } finally {
        this.dataLoading = false;
      }
    },

    async loadNormalUsers() {
      if (!this.canSelectUser) return;

      this.loadingUsers = true;
      try {
        this.normalUsers = await getNormalUsers();
      } catch (error) {
        console.error('加载用户列表失败:', error);
        ElMessage.error('加载用户列表失败');
      } finally {
        this.loadingUsers = false;
      }
    },

    async loadScoreStatistics() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      this.statisticsLoading = true;
      try {
        this.scoreStatistics = await getTableScoreStatistics(tableId);
      } catch (error) {
        console.error('加载用户得分统计失败:', error);
        ElMessage.error('加载用户得分统计失败');
      } finally {
        this.statisticsLoading = false;
      }
    },

    toggleUserDetail(userId: string) {
      if (this.expandedUserId === userId) {
        this.expandedUserId = null;
      } else {
        this.expandedUserId = userId;
      }
    },

    // 选择用户
    selectUser(userId: string) {
      this.formData.userId = userId;
      this.userSelectOpen = false;
    },

    // 处理时期变化
    handlePeriodChange(value: string) {
      this.formData.submissionPeriod = value;
    },

    openAddDialog() {
      this.editingData = null;

      // 尝试从 localStorage 恢复暂存数据
      const tableId = Number(this.$route.params.id);
      const draftKey = `table_data_draft_${tableId}`;
      const savedDraft = localStorage.getItem(draftKey);

      if (savedDraft) {
        try {
          const draft = JSON.parse(savedDraft);
          // 检查暂存数据的时间戳，如果超过7天则清除
          const draftAge = Date.now() - (draft.timestamp || 0);
          const sevenDays = 7 * 24 * 60 * 60 * 1000;

          if (draftAge < sevenDays) {
            ElMessage.info('已恢复上次暂存的数据');
            this.formData = {
              dataContent: draft.dataContent || {},
              userId: draft.userId || '',
              submissionPeriod: draft.submissionPeriod || this.getCurrentYearMonth(),
              score: draft.score || null,
              reviewMaterial: draft.reviewMaterial || null
            };
            this.submissionPeriodDate = this.formData.submissionPeriod;
          } else {
            // 清除过期的暂存数据
            localStorage.removeItem(draftKey);
            this.initializeEmptyForm();
          }
        } catch (error) {
          console.error('恢复暂存数据失败:', error);
          this.initializeEmptyForm();
        }
      } else {
        this.initializeEmptyForm();
      }

      this.userSelectOpen = false;
      this.showDataDialog = true;
    },

    // 初始化空表单
    initializeEmptyForm() {
      const currentYearMonth = this.getCurrentYearMonth();
      this.formData = {
        dataContent: {},
        userId: '',
        submissionPeriod: currentYearMonth,
        score: null,
        reviewMaterial: null
      };
      this.submissionPeriodDate = currentYearMonth;
      // 初始化字段默认值
      this.fields.forEach(field => {
        this.formData.dataContent[field.fieldName] = '';
      });
    },

    openEditDialog(row: TableDataItem) {
      this.editingData = row;
      const period = row.submissionPeriod || this.getCurrentYearMonth();

      // 如果是本地暂存数据（id=-1），不传id，这样保存时会当做新增
      this.formData = {
        id: row.id === -1 ? undefined : row.id,
        userId: row.userId,
        submissionPeriod: period,
        dataContent: {...row.dataContent},
        score: this.canSetScore ? row.score : null,  // 只有管理员才保留分数，普通成员设为null
        reviewMaterial: row.reviewMaterial
      };
      this.submissionPeriodDate = period;
      this.userSelectOpen = false;
      this.showDataDialog = true;
    },

    // 获取当前年月（格式：YYYY-MM）
    getCurrentYearMonth(): string {
      const now = new Date();
      const year = now.getFullYear();
      const month = String(now.getMonth() + 1).padStart(2, '0');
      return `${year}-${month}`;
    },

    // 格式化时期显示
    formatPeriod(period: string | null): string {
      if (!period) return '-';
      const [year, month] = period.split('-');
      return `${year}年${month}月`;
    },

    // 处理筛选条件变化
    handleFilterChange() {
      // 筛选条件变化时，计算属性会自动更新 filteredTableData
      console.log('筛选条件变化:', {
        dateRange: this.filterDateRange,
        status: this.filterStatus
      });
    },

    // 清空所有筛选条件
    clearFilters() {
      this.filterDateRange = null;
      this.filterStatus = null;
      ElMessage.success('已清空筛选条件');
    },

    // 管理员端状态文本（根据分数判断）
    getAdminStatusText(status: number, score: number | null): string {
      if (status === 0) return '暂存';
      if (status === 3) return '已退回';  // 已退回状态
      if (score === null) return '待审核';  // 未打分都是待审核
      return '审核完成';  // 已打分就是审核完成
    },

    // 管理员端状态样式类
    getAdminStatusClass(status: number, score: number | null): string {
      if (status === 0) return 'status-draft';
      if (status === 3) return 'status-rejected';   // 已退回 - 红色
      if (score === null) return 'status-pending';  // 未打分都是待审核
      return 'status-completed';  // 已打分就是审核完成
    },

    closeDataDialog() {
      this.showDataDialog = false;
      this.editingData = null;
      this.userSelectOpen = false;
    },

    // 暂存数据（仅普通用户可用）
    async handleSaveDraft() {
      // 管理员不能使用暂存功能
      if (this.isAdmin) {
        ElMessage.warning('管理员不能使用暂存功能，请使用保存或退回功能');
        return;
      }

      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      // 检查是否有任何内容
      const hasAnyContent = Object.values(this.formData.dataContent).some(value => {
        return value && value.toString().trim() !== '';
      }) || this.formData.reviewMaterial;

      if (!hasAnyContent) {
        ElMessage.warning('请至少填写一个字段后再暂存');
        return;
      }

      try {
        // 普通用户只能暂存新增的数据到本地
        // 如果是编辑已有数据（包括暂存、已提交、已退回的数据），应该使用"申报"而不是"暂存"
        if (this.editingData && this.editingData.id > 0) {
          ElMessage.warning('编辑已有数据请使用"申报"按钮提交');
          return;
        }

        // 新增数据：保存到localStorage（本地暂存）
        const draftKey = `table_data_draft_${tableId}`;
        const draftData = {
          dataContent: this.formData.dataContent,
          userId: this.currentUserId,
          submissionPeriod: this.formData.submissionPeriod,
          reviewMaterial: this.formData.reviewMaterial,
          timestamp: Date.now()
        };

        localStorage.setItem(draftKey, JSON.stringify(draftData));
        ElMessage.success('数据已暂存到本地');

        // 关闭对话框
        this.closeDataDialog();

        // 刷新列表（会自动加载暂存数据）
        await this.loadTableData();
      } catch (error) {
        console.error('暂存数据失败:', error);
        ElMessage.error('暂存失败，请重试');
      }
    },

    async handleSave() {
      const tableId = Number(this.$route.params.id);
      if (!tableId) return;

      // 验证必填字段
      const hasEmptyField = this.fields.some(field => {
        if (field.root && !this.canEditAdminField) {
          return false;
        }
        const value = this.formData.dataContent[field.fieldName];
        return !value || value.toString().trim() === '';
      });

      if (hasEmptyField) {
        ElMessage.warning('请填写所有字段');
        return;
      }

      // 验证提交时期
      if (!this.formData.submissionPeriod) {
        ElMessage.warning('请选择提交时期');
        return;
      }

      // 如果是管理员且未选择用户，提示
      if (this.canSelectUser && !this.formData.userId) {
        ElMessage.warning('请选择所属用户');
        return;
      }

      try {
        // 确定状态
        let status: number;

        // 如果是编辑已有数据且原状态是暂存(0)或退回(3)，提交时改为已提交(1)
        // 如果管理员打了分，状态为2(审核通过)
        // 如果是新增数据，默认为1(已提交)
        if (this.editingData && this.editingData.id > 0) {
          // 编辑已有数据
          if (this.canSetScore && this.formData.score !== null && this.formData.score !== undefined) {
            // 管理员打了分，状态为2
            status = 2;
          } else if (this.editingData.status === 0 || this.editingData.status === 3) {
            // 从暂存或退回状态提交，改为已提交
            status = 1;
          } else {
            // 保持原状态
            status = this.editingData.status;
          }
        } else {
          // 新增数据
          status = (this.canSetScore && this.formData.score !== null && this.formData.score !== undefined) ? 2 : 1;
        }

        // 构建请求数据
        const requestData: any = {
          id: this.formData.id,
          tableId,
          userId: this.formData.userId,
          submissionPeriod: this.formData.submissionPeriod,
          dataContent: this.formData.dataContent,
          reviewMaterial: this.formData.reviewMaterial,
          status: status  // 传递状态参数
        };

        // 只有管理员才能设置分数
        if (this.canSetScore) {
          requestData.score = this.formData.score;
        }

        await saveTableData(requestData);

        ElMessage.success(this.editingData && this.editingData.id !== -1 ? '修改成功' : '提交成功');

        // 保存成功后清除暂存数据
        const draftKey = `table_data_draft_${tableId}`;
        localStorage.removeItem(draftKey);

        this.closeDataDialog();
        await this.loadTableData();

        // 如果是管理员，刷新统计
        if (this.canViewStatistics) {
          await this.loadScoreStatistics();
        }
      } catch (error) {
        console.error('保存数据失败:', error);
        // 错误信息已由 axios 拦截器处理
      }
    },

    async handleDelete(id: number) {
      try {
        await ElMessageBox.confirm('确定要删除这条数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        });

        await deleteTableData(id);
        ElMessage.success('删除成功');
        await this.loadTableData();

        // 如果是管理员，刷新统计
        if (this.canViewStatistics) {
          await this.loadScoreStatistics();
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除数据失败:', error);
        }
      }
    },

    // 删除本地暂存数据
    async handleDeleteDraft() {
      try {
        await ElMessageBox.confirm(
            '确定要删除这条暂存数据吗？',
            '删除确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        );

        const tableId = Number(this.$route.params.id);
        const draftKey = `table_data_draft_${tableId}`;
        localStorage.removeItem(draftKey);

        ElMessage.success('暂存数据已删除');

        // 刷新列表
        await this.loadTableData();
      } catch (error: any) {
        if (error !== 'cancel') {
          console.error('删除暂存失败:', error);
          ElMessage.error('删除失败');
        }
      }
    },

    // 打开退回对话框
    openRejectDialog(row: TableDataItem) {
      this.rejectingData = row;
      this.rejectReason = '';
      this.showRejectDialog = true;
    },

    // 关闭退回对话框
    closeRejectDialog() {
      this.showRejectDialog = false;
      this.rejectingData = null;
      this.rejectReason = '';
    },

    // 确认退回
    async confirmReject() {
      if (!this.rejectReason || this.rejectReason.trim() === '') {
        ElMessage.warning('请填写退回原因');
        return;
      }

      if (!this.rejectingData) return;

      try {
        await rejectData({
          id: this.rejectingData.id,
          rejectReason: this.rejectReason
        });
        ElMessage.success('退回成功，已发送邮件通知用户');
        this.closeRejectDialog();
        await this.loadTableData();

        // 刷新统计
        if (this.canViewStatistics) {
          await this.loadScoreStatistics();
        }
      } catch (error: any) {
        console.error('退回失败:', error);
        ElMessage.error(error.message || '退回失败');
      }
    },

    formatTime(time: string | undefined): string {
      if (!time) return '-';
      return new Date(time).toLocaleString('zh-CN');
    },

    // 触发文件选择
    triggerFileInput() {
      const fileInput = this.$refs.fileInput as HTMLInputElement;
      if (fileInput) {
        fileInput.click();
      }
    },

    // 处理文件选择
    async handleFileSelect(event: Event) {
      const target = event.target as HTMLInputElement;
      const file = target.files?.[0];

      if (!file) return;

      // 验证文件大小（10MB）
      if (file.size > 10 * 1024 * 1024) {
        ElMessage.error('文件大小不能超过10MB');
        target.value = '';
        return;
      }

      // 上传文件
      this.uploadingAttachment = true;
      try {
        const response: any = await uploadAttachment(file, 'table-data', String(this.editingData?.id || ''));

        // 保存附件路径到表单数据
        this.formData.reviewMaterial = response?.filePath || response?.fileUrl;

        ElMessage.success('附件上传成功');
      } catch (error: any) {
        console.error('附件上传失败:', error);
        ElMessage.error(error.message || '附件上传失败');
      } finally {
        this.uploadingAttachment = false;
        target.value = ''; // 清空input，允许重新选择同一文件
      }
    },

    // 移除附件
    removeAttachment() {
      this.formData.reviewMaterial = null;
      ElMessage.success('已移除附件');
    },

    // 打开附件
    openAttachment(filePath: string) {
      if (!filePath) return;

      // 构建完整URL
      const apiTarget = (import.meta as any).env?.VITE_ATTACHMENT_PATH;
      const attachmentPath = (import.meta as any).env?.VITE_ATTACHMENT_BASE_URL || 'attachment/';

      const baseUrl = `http://${apiTarget}/${attachmentPath.replace(/^\/|\/$/g, '')}`;
      const fullUrl = filePath.startsWith('http') ? filePath : `${baseUrl}${filePath}`;

      // 在新标签页打开
      window.open(fullUrl, '_blank');
    },

    // 获取附件名称
    getAttachmentName(filePath: string): string {
      if (!filePath) return '';

      // 从路径中提取文件名
      const parts = filePath.split('/');
      const filename = parts[parts.length - 1];

      if (!filename) return '';

      // 如果文件名包含UUID前缀，去掉它
      const match = filename.match(/^[a-f0-9]{32}_(.+)$/);
      return match && match[1] ? match[1] : filename;
    },

    // 获取状态文本
    getStatusText(status: number, score: number | null): string {
      if (status === 0) return '暂存';
      if (status === 3) return '已退回';  // 已退回状态
      if (score === null) return '待审核';  // 未打分都是待审核
      return '审核通过';  // 已打分就是审核通过
    },

    // 获取状态样式类
    getStatusClass(status: number, score: number | null): string {
      if (status === 0) return 'status-draft';      // 暂存 - 灰色
      if (status === 3) return 'status-rejected';   // 已退回 - 红色
      if (score === null) return 'status-pending';  // 待审核 - 橙色
      return 'status-completed';                     // 审核通过 - 绿色
    },

    // 判断是否可以编辑
    canEdit(data: TableDataItem): boolean {
      if (this.isAdmin) {
        return true; // 管理员可以编辑任何数据
      }
      // 普通用户可以编辑：暂存（0）、已提交但未打分（1且score=null）、已退回（3）的数据
      return data.status === 0 || data.status === 3 || (data.status === 1 && data.score === null);
    },

    // 判断是否可以删除
    canDelete(data: TableDataItem): boolean {
      if (this.isAdmin) {
        return true; // 管理员可以删除任何数据
      }
      // 普通用户只能删除未打分和未退回的数据
      return data.status !== 2 && data.status !== 3;
    },

    // 判断是否可以退回（仅管理员，且数据未退回）
    canReject(data: TableDataItem): boolean {
      // 只有管理员可以退回，且数据状态不是暂存（0）或已退回（3）
      return this.isAdmin && data.status !== 0 && data.status !== 3;
    },

    // 获取删除按钮文本
    getDeleteButtonText(): string {
      return '删除';
    }
  },
  watch: {
    '$route.params.id'(newId) {
      // 只有当路由参数变化且存在有效ID时才重新加载
      if (newId && this.$route.name === 'TableDetail') {
        this.loadTableDetail();
      }
    }
  }
});
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';
@import '@/styles/mixins.scss';

.table-detail-content {
  padding: $spacing-huge;
  max-width: 1400px;
  margin: 0 auto;
}

.detail-header {
  margin-bottom: $spacing-xxl;
  padding-bottom: $spacing-xl;
  border-bottom: 2px solid $border-color;
}

.detail-title {
  color: $primary-color;
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;
  margin-bottom: $spacing-sm;
}

.detail-subtitle {
  color: $text-secondary;
  font-size: $font-size-md;

  .divider {
    margin: 0 $spacing-md;
    color: $border-color;
  }
}

.section-title {
  color: $primary-color;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  margin-bottom: $spacing-lg;
  padding-bottom: $spacing-sm;
  border-bottom: 2px solid rgba($primary-color, 0.2);
}

.table-structure-section {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xxl;
  @include shadow(2);
}

// 用户得分统计部分
.score-statistics-section {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  margin-bottom: $spacing-xxl;
  @include shadow(2);
}

.statistics-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
  flex-wrap: wrap;
  gap: $spacing-md;
}

.statistics-filter {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.statistics-content {
  margin-top: $spacing-lg;
}

.overall-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: $spacing-lg;
  margin-bottom: $spacing-xxl;
}

.stat-item {
  background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, rgba($secondary-color, 0.1) 100%);
  border-radius: $border-radius;
  padding: $spacing-lg;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: $spacing-sm;
  border: 1px solid $border-color;
  transition: all 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba($primary-color, 0.15);
  }
}

.stat-label {
  color: $text-secondary;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
}

.stat-value {
  color: $primary-color;
  font-size: $font-size-xxxl;
  font-weight: $font-weight-bold;

  &.highlight {
    color: $secondary-color;
  }
}

.user-score-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-md;
}

.user-score-item {
  background: $background-secondary;
  border: 2px solid $border-color;
  border-radius: $border-radius-large;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba($primary-color, 0.1);
  }

  &.expanded {
    border-color: $secondary-color;
  }
}

.user-score-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg $spacing-xl;
  background: linear-gradient(135deg, rgba($primary-color, 0.05) 0%, rgba($secondary-color, 0.05) 100%);
  transition: background 0.3s;

  .user-score-item:hover & {
    background: linear-gradient(135deg, rgba($primary-color, 0.1) 0%, rgba($secondary-color, 0.1) 100%);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: $spacing-md;
}

.user-rank {
  font-size: $font-size-xl;
}

.user-id {
  color: $primary-color;
  font-weight: $font-weight-bold;
  font-size: $font-size-lg;
}

.user-name {
  color: $text-primary;
  font-size: $font-size-md;
  font-weight: $font-weight-medium;
}

.score-info {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
}

.data-count {
  color: $text-secondary;
  font-size: $font-size-sm;
  padding: $spacing-xs $spacing-sm;
  background: rgba($border-color, 0.3);
  border-radius: $border-radius-small;
}

.total-score {
  color: $secondary-color;
  font-weight: $font-weight-bold;
  font-size: $font-size-md;
}

.avg-score {
  color: $text-secondary;
  font-size: $font-size-sm;
}

.expand-icon {
  color: $primary-color;
  font-size: $font-size-sm;
  margin-left: $spacing-sm;
}

.user-data-detail {
  padding: $spacing-lg $spacing-xl;
  background: $background-primary;
  border-top: 1px solid $border-color;
}

.data-detail-item {
  background: $background-secondary;
  border: 1px solid $border-light;
  border-radius: $border-radius;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  transition: all 0.3s;

  &:last-child {
    margin-bottom: 0;
  }

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 2px 8px rgba($primary-color, 0.1);
  }
}

.data-detail-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  margin-bottom: $spacing-sm;
  padding-bottom: $spacing-sm;
  border-bottom: 1px solid $border-light;
}

.data-id {
  color: $primary-color;
  font-weight: $font-weight-bold;
  font-size: $font-size-sm;
}

.data-date {
  color: $text-muted;
  font-size: $font-size-xs;
}

.data-score-badge {
  margin-left: auto;
  background: linear-gradient(135deg, $secondary-color 0%, darken($secondary-color, 10%) 100%);
  color: $background-primary;
  padding: $spacing-xs $spacing-md;
  border-radius: $border-radius;
  font-weight: $font-weight-bold;
  font-size: $font-size-sm;
}

.data-detail-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
}

.data-field-row {
  display: flex;
  gap: $spacing-xs;
  font-size: $font-size-sm;
}

.data-detail-attachment {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding-top: $spacing-sm;
  border-top: 1px solid $border-light;

  .attachment-icon {
    font-size: $font-size-md;
  }

  .attachment-link {
    color: $primary-color;
    text-decoration: underline;
    cursor: pointer;
    font-size: $font-size-sm;
    transition: color 0.3s;

    &:hover {
      color: $secondary-color;
    }
  }
}

.fields-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-lg;
}

.field-card {
  background: $background-secondary;
  border: 2px solid $border-color;
  border-radius: $border-radius;
  padding: $spacing-lg;
  transition: all 0.3s;

  &:hover {
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba($primary-color, 0.1);
    transform: translateY(-2px);
  }
}

.field-card-header {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  margin-bottom: $spacing-md;
}

.field-name {
  color: $text-primary;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
}

.field-tag {
  padding: 2px $spacing-sm;
  border-radius: $border-radius-small;
  font-size: $font-size-xs;
  font-weight: $font-weight-medium;
}

.field-tag-admin {
  background: rgba($error-color, 0.1);
  color: $error-color;
}

.field-tag-calc {
  background: rgba($secondary-color, 0.1);
  color: $secondary-color;
}

.field-card-footer {
  color: $text-muted;
  font-size: $font-size-sm;
}

.field-access {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.table-data-section {
  background: $background-primary;
  border-radius: $border-radius-large;
  padding: $spacing-xxl;
  @include shadow(2);
}

// 筛选区域样式
.filter-section {
  background: $background-secondary;
  padding: $spacing-lg;
  border-radius: $border-radius;
  margin-bottom: $spacing-lg;
  border: 1px solid $border-color;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
}

.filter-label {
  font-size: $font-size-md;
  color: $text-primary;
  font-weight: $font-weight-medium;
  white-space: nowrap;
}

.btn-clear-filter {
  padding: $spacing-sm $spacing-lg;
  background: white;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  color: $text-primary;
  font-size: $font-size-md;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }
}

.filter-result {
  margin-top: $spacing-md;
  padding-top: $spacing-md;
  border-top: 1px solid $border-color;
}

.filter-result-text {
  font-size: $font-size-md;
  color: $text-secondary;

  strong {
    color: $primary-color;
    font-size: $font-size-lg;
    margin: 0 $spacing-xs;
  }
}

// Element Plus Select 样式定制
:deep(.el-select) {
  .el-input__wrapper {
    box-shadow: 0 0 0 1px $border-color inset;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px lighten($primary-color, 20%) inset;
    }
  }

  &.is-focus .el-input__wrapper {
    box-shadow: 0 0 0 1px $primary-color inset !important;
  }
}

.data-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-lg;
}

.btn-add {
  background: $primary-color;
  color: white;
  border: none;
  padding: $spacing-sm $spacing-lg;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.3s;

  &:hover {
    background: darken($primary-color, 10%);
    transform: translateY(-1px);
  }
}

.data-table-container {
  overflow-x: auto;
  margin-top: $spacing-lg;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: $font-size-md;

  thead {
    background: $background-secondary;

    th {
      padding: $spacing-md;
      text-align: left;
      font-weight: $font-weight-semibold;
      color: $text-primary;
      border-bottom: 2px solid $border-color;
      white-space: nowrap;
    }
  }

  tbody {
    tr {
      transition: background 0.2s;

      &:hover {
        background: rgba($primary-color, 0.05);
      }

      &:not(:last-child) {
        border-bottom: 1px solid $border-color;
      }
    }

    td {
      padding: $spacing-md;
      color: $text-primary;
    }
  }

  .col-index {
    width: 60px;
    text-align: center;
  }

  .col-period {
    min-width: 100px;
    text-align: center;
  }

  .col-field {
    min-width: 120px;
    max-width: 200px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .col-score {
    width: 100px;
    text-align: center;
    font-weight: $font-weight-semibold;
    color: $secondary-color;
  }

  .col-material {
    width: 150px;
  }

  .col-actions {
    width: 150px;
    text-align: center;
  }
}

.lock-icon {
  font-size: $font-size-sm;
  margin-left: $spacing-xs;
  opacity: 0.7;
}

.action-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.btn-edit,
.btn-delete {
  padding: 4px $spacing-sm;
  border: none;
  border-radius: $border-radius-small;
  cursor: pointer;
  font-size: $font-size-sm;
  margin: 0;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}

.btn-edit {
  background: $secondary-color;
  color: white;

  &:hover {
    background: darken($secondary-color, 10%);
  }
}

.btn-delete {
  background: $error-color;
  color: white;

  &:hover {
    background: darken($error-color, 10%);
  }
}

.btn-reject {
  background: #ff9800;
  color: white;
  padding: 4px $spacing-sm;
  border: none;
  border-radius: $border-radius-small;
  cursor: pointer;
  font-size: $font-size-sm;
  margin: 0;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;

  &:hover {
    background: darken(#ff9800, 10%);
  }
}

// 对话框样式
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.dialog-container {
  background: $background-primary;
  border-radius: $border-radius-large;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  @include shadow(3);
}

.dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: $spacing-lg $spacing-xl;
  border-bottom: 1px solid $border-color;

  h3 {
    margin: 0;
    color: $primary-color;
    font-size: $font-size-xl;
  }
}

.dialog-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: $text-secondary;
  line-height: 1;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s;

  &:hover {
    color: $error-color;
  }
}

.dialog-body {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-xl;
}

.form-group {
  margin-bottom: $spacing-lg;
}

.form-label {
  display: block;
  margin-bottom: $spacing-sm;
  color: $text-primary;
  font-weight: $font-weight-medium;
  font-size: $font-size-md;

  .required-mark {
    color: $error-color;
    margin-left: $spacing-xs;
  }
}

// Element Plus DatePicker 样式定制
:deep(.el-date-editor) {
  width: 100%;

  .el-input__wrapper {
    box-shadow: 0 0 0 1px $border-color inset;
    transition: all 0.3s;

    &:hover {
      box-shadow: 0 0 0 1px lighten($primary-color, 20%) inset;
    }
  }

  &.is-focus .el-input__wrapper {
    box-shadow: 0 0 0 1px $primary-color inset !important;
  }

  .el-input__inner {
    color: $text-primary;
    font-size: $font-size-md;
  }

  .el-input__prefix {
    color: $primary-color;
  }
}

.field-hint {
  margin-top: $spacing-xs;
  font-size: $font-size-sm;
  color: $text-secondary;
}

.form-input {
  width: 100%;
  padding: $spacing-md;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  font-size: $font-size-md;
  transition: border-color 0.3s;

  &:focus {
    outline: none;
    border-color: $primary-color;
  }

  &.input-disabled {
    background: $background-secondary;
    cursor: not-allowed;
    opacity: 0.6;
  }

}

// 自定义下拉框样式
.custom-select {
  position: relative;
  width: 100%;
  cursor: pointer;
  user-select: none;

  &.disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }

  .select-value {
    width: 100%;
    padding: $spacing-md;
    border: 1px solid $border-color;
    border-radius: $border-radius;
    font-size: $font-size-md;
    background: $background-primary;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s;
    color: $text-primary;

    .select-arrow {
      font-size: $font-size-xl;
      color: $text-secondary;
      transition: transform 0.3s;
    }
  }

  &.open .select-value {
    border-color: $primary-color;

    .select-arrow {
      transform: rotate(180deg);
    }
  }

  &:hover:not(.disabled) .select-value {
    border-color: $primary-color;
  }

  .custom-select-dropdown {
    position: absolute;
    left: 0;
    right: 0;
    top: calc(100% + 2px);
    z-index: 1600;
    background: $background-primary;
    border-radius: $border-radius-large;
    box-shadow: 0 2px 16px $shadow-medium;
    margin: 0;
    padding: 0;
    border: 1.5px solid $border-light;
    overflow: hidden;
    max-height: 300px;
    overflow-y: auto;

    li {
      display: block;
      padding: $spacing-lg $spacing-xl;
      font-size: $font-size-lg;
      color: $primary-color;
      background: transparent;
      cursor: pointer;
      border-bottom: 1px solid rgba($primary-color, 0.05);
      transition: all 0.15s;

      &:last-child {
        border-bottom: none;
      }

      &:hover,
      &.selected {
        background: rgba($secondary-color, 0.08);
        color: $secondary-color;
      }

      &.disabled {
        color: $text-muted;
        background: rgba($border-color, 0.1);
        cursor: not-allowed;
        opacity: 0.6;
      }
    }
  }
}

// 附件相关样式
.attachment-section {
  margin-top: $spacing-sm;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  background: $background-secondary;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  transition: all 0.3s;

  &:hover {
    background: lighten($primary-color, 45%);
    border-color: $primary-color;
  }
}

.attachment-icon {
  font-size: $font-size-lg;
}

.attachment-name {
  flex: 1;
  color: $primary-color;
  cursor: pointer;
  text-decoration: underline;
  font-size: $font-size-md;

  &:hover {
    color: darken($primary-color, 10%);
  }
}

.btn-remove-attachment {
  width: 24px;
  height: 24px;
  border: none;
  background: $error-color;
  color: $text-white;
  border-radius: 50%;
  cursor: pointer;
  font-size: $font-size-lg;
  line-height: 1;
  transition: all 0.3s;

  &:hover {
    background: darken($error-color, 10%);
    transform: scale(1.1);
  }
}

.upload-attachment-area {
  text-align: center;
  padding: $spacing-lg;
  border: 2px dashed $border-color;
  border-radius: $border-radius;
  transition: all 0.3s;

  &:hover {
    border-color: $primary-color;
    background: lighten($primary-color, 48%);
  }
}

.btn-upload-attachment {
  display: inline-flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-md $spacing-lg;
  background: $primary-color;
  color: $text-white;
  border: none;
  border-radius: $border-radius;
  font-size: $font-size-md;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    background: darken($primary-color, 10%);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba($primary-color, 0.3);
  }

  .upload-icon {
    font-size: $font-size-lg;
  }
}

.upload-hint {
  margin-top: $spacing-sm;
  color: $text-secondary;
  font-size: $font-size-sm;
}

.uploading-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  padding: $spacing-md;
  color: $primary-color;
  font-size: $font-size-md;

  .loading-spinner {
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.attachment-link {
  color: $primary-color;
  cursor: pointer;
  text-decoration: underline;
  transition: color 0.3s;

  &:hover {
    color: darken($primary-color, 15%);
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-md;
  padding: $spacing-lg $spacing-xl;
  border-top: 1px solid $border-color;
}

.btn-cancel,
.btn-draft,
.btn-confirm {
  padding: $spacing-sm $spacing-xl;
  border: none;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.3s;
}

.btn-cancel {
  background: $background-secondary;
  color: $text-primary;

  &:hover {
    background: darken($background-secondary, 5%);
  }
}

.btn-draft {
  background: #f39c12;
  color: white;

  &:hover {
    background: darken(#f39c12, 10%);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba(#f39c12, 0.3);
  }
}

.btn-confirm {
  background: $primary-color;
  color: white;

  &:hover {
    background: darken($primary-color, 10%);
    transform: translateY(-2px);
    box-shadow: 0 4px 8px rgba($primary-color, 0.3);
  }
}

// 状态标签样式
.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: $font-size-sm;
  font-weight: 500;
  text-align: center;
  white-space: nowrap;
  border: 1px solid transparent;
}

.status-draft {
  background: #f0f0f0;
  color: #666;
  border-color: #d0d0d0;
}

.status-pending {
  background: #fff3e0;
  color: #f57c00;
  border-color: #ffcc80;
}

.status-completed {
  background: #e8f5e9;
  color: #2e7d32;
  border-color: #a5d6a7;
}

.status-rejected {
  background: #ffebee;
  color: #c62828;
  border-color: #ef9a9a;
}

.status-submitted {
  background: #3498db;
  color: white;
}

.status-scored {
  background: #27ae60;
  color: white;
}

.text-muted {
  color: $text-muted;
  font-size: $font-size-sm;
}

.status-container {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
}

.reject-reason-hint {
  display: inline-flex;
  align-items: center;
  cursor: help;

  .reject-icon {
    color: #ff9800;
    font-size: 18px;
    animation: pulse 2s ease-in-out infinite;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.loading-state {
  text-align: center;
  padding: $spacing-huge;
  color: $text-secondary;
  font-size: $font-size-lg;
}

.empty-state {
  text-align: center;
  padding: $spacing-huge;
  color: $text-muted;

  .empty-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto $spacing-lg;
    opacity: 0.5;
  }

  p {
    margin: $spacing-sm 0;
    font-size: $font-size-lg;
  }

  .empty-hint {
    font-size: $font-size-sm;
    color: $text-secondary;
  }
}

// 退回对话框样式
.dialog-small {
  max-width: 500px;
}

.reject-info {
  background: #f5f5f5;
  padding: $spacing-md;
  border-radius: $border-radius;
  margin-bottom: $spacing-md;

  p {
    margin: $spacing-xs 0;
    color: $text-primary;
  }
}

.form-textarea {
  width: 100%;
  padding: $spacing-sm;
  border: 1px solid $border-color;
  border-radius: $border-radius;
  font-size: $font-size-md;
  font-family: inherit;
  resize: vertical;
  transition: border-color 0.2s;

  &:focus {
    outline: none;
    border-color: $primary-color;
  }

  &::placeholder {
    color: $text-muted;
  }
}

.btn-reject-confirm {
  background: $error-color;
  color: white;
  padding: $spacing-sm $spacing-lg;
  border: none;
  border-radius: $border-radius;
  cursor: pointer;
  font-size: $font-size-md;
  transition: all 0.2s;

  &:hover {
    background: darken($error-color, 10%);
  }
}

@media (max-width: 992px) {
  .table-detail-content {
    padding: $spacing-lg;
  }

  .fields-grid {
    grid-template-columns: 1fr;
  }

  .detail-title {
    font-size: $font-size-xxl;
  }

  .detail-subtitle {
    font-size: $font-size-sm;

    span {
      display: block;
      margin: $spacing-xs 0;
    }

    .divider {
      display: none;
    }
  }
}
</style>

