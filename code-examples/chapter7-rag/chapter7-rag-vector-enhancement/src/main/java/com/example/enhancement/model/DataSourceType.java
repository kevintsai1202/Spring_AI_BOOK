package com.example.enhancement.model;

/**
 * 資料來源類型枚舉
 *
 * 定義支援的各種資料來源類型
 */
public enum DataSourceType {
    /**
     * 關聯式資料庫（PostgreSQL, MySQL, SQL Server 等）
     */
    RELATIONAL_DATABASE,

    /**
     * NoSQL 資料庫（MongoDB, Cassandra 等）
     */
    NOSQL_DATABASE,

    /**
     * REST API
     */
    REST_API,

    /**
     * 文件系統
     */
    FILE_SYSTEM,

    /**
     * 訊息佇列（Kafka, RabbitMQ 等）
     */
    MESSAGE_QUEUE,

    /**
     * 雲端存儲（S3, Azure Blob 等）
     */
    CLOUD_STORAGE
}
