package com.txq.interfaces.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 表格业务指标配置类
 * 用于收集和暴露表格相关的业务指标
 */
@Getter
@Component
public class TableMetricsConfig {

    private final MeterRegistry meterRegistry;
    
    // ========== 表格管理相关指标 ==========
    
    /** 表格创建总数计数器 */
    private final Counter tableCreatedCounter;
    
    /** 表格删除总数计数器 */
    private final Counter tableDeletedCounter;
    
    /** 表格查询总数计数器 */
    private final Counter tableQueryCounter;
    
    // ========== 表格数据相关指标 ==========
    
    /** 数据提交总数计数器 */
    private final Counter dataSubmittedCounter;
    
    /** 数据审核通过总数计数器 */
    private final Counter dataApprovedCounter;
    
    /** 数据驳回总数计数器 */
    private final Counter dataRejectedCounter;
    
    /** 数据删除总数计数器 */
    private final Counter dataDeletedCounter;
    
    /** 数据导出总数计数器 */
    private final Counter dataExportedCounter;
    
    // ========== 附件相关指标 ==========
    
    /** 附件上传总数计数器 */
    private final Counter attachmentUploadCounter;
    
    /** 附件下载总数计数器 */
    private final Counter attachmentDownloadCounter;
    
    /** 附件删除总数计数器 */
    private final Counter attachmentDeleteCounter;
    
    // ========== 统计指标（Gauge） ==========
    
    /** 当前活跃表格数量 */
    private final AtomicInteger activeTableCount;
    
    /** 待审核数据条数 */
    private final AtomicInteger pendingDataCount;

    public TableMetricsConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeTableCount = new AtomicInteger(0);
        this.pendingDataCount = new AtomicInteger(0);
        
        // 初始化表格管理指标
        this.tableCreatedCounter = Counter.builder("table.management.created")
                .description("表格创建总数")
                .tag("type", "table_management")
                .register(meterRegistry);
        
        this.tableDeletedCounter = Counter.builder("table.management.deleted")
                .description("表格删除总数")
                .tag("type", "table_management")
                .register(meterRegistry);
        
        this.tableQueryCounter = Counter.builder("table.management.query")
                .description("表格查询总数")
                .tag("type", "table_management")
                .register(meterRegistry);
        
        // 初始化表格数据指标
        this.dataSubmittedCounter = Counter.builder("table.data.submitted")
                .description("数据提交总数")
                .tag("type", "table_data")
                .register(meterRegistry);
        
        this.dataApprovedCounter = Counter.builder("table.data.approved")
                .description("数据审核通过总数")
                .tag("type", "table_data")
                .register(meterRegistry);
        
        this.dataRejectedCounter = Counter.builder("table.data.rejected")
                .description("数据驳回总数")
                .tag("type", "table_data")
                .register(meterRegistry);
        
        this.dataDeletedCounter = Counter.builder("table.data.deleted")
                .description("数据删除总数")
                .tag("type", "table_data")
                .register(meterRegistry);
        
        this.dataExportedCounter = Counter.builder("table.data.exported")
                .description("数据导出总数")
                .tag("type", "table_data")
                .register(meterRegistry);
        
        // 初始化附件指标
        this.attachmentUploadCounter = Counter.builder("table.attachment.upload")
                .description("附件上传总数")
                .tag("type", "attachment")
                .register(meterRegistry);
        
        this.attachmentDownloadCounter = Counter.builder("table.attachment.download")
                .description("附件下载总数")
                .tag("type", "attachment")
                .register(meterRegistry);
        
        this.attachmentDeleteCounter = Counter.builder("table.attachment.delete")
                .description("附件删除总数")
                .tag("type", "attachment")
                .register(meterRegistry);
        
        // 注册Gauge指标
        Gauge.builder("table.active.count", activeTableCount, AtomicInteger::get)
                .description("当前活跃表格数量")
                .tag("type", "statistics")
                .register(meterRegistry);
        
        Gauge.builder("table.data.pending.count", pendingDataCount, AtomicInteger::get)
                .description("待审核数据条数")
                .tag("type", "statistics")
                .register(meterRegistry);
    }
    
    // ========== 统计数据更新方法 ==========
    
    /**
     * 更新活跃表格数量
     */
    public void updateActiveTableCount(int count) {
        activeTableCount.set(count);
    }
    
    /**
     * 更新待审核数据条数
     */
    public void updatePendingDataCount(int count) {
        pendingDataCount.set(count);
    }
}

