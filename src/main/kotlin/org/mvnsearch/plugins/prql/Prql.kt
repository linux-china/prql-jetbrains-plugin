package org.mvnsearch.plugins.prql

import com.intellij.openapi.project.Project
import com.intellij.sql.dialects.SqlDialectMappings
import org.prql.prql4j.PrqlCompiler

object Prql {
    private val dialectMapping = mapOf(
        "PostgreSQL" to "postgres",
        "MySQL" to "postgres",
        "MariaDB" to "postgres",
        "GenericSQL" to "generic",
        "BigQuery" to "bigquery",
        "ClickHouse" to "clickhouse",
        "HiveQL" to "hive",
        "TSQL" to "mssql",
        "SQLite" to "sqlite",
        "Snowflake" to "snowflake",
    )

    @Throws(Exception::class)
    fun transformPrql(dialect: String?, prqlCode: String, format: Boolean, project: Project): String {
        val prqlTarget = getPrqlDatabaseTarget(dialect, project)
        var sql = if (prqlCode.contains(" ?")) {
            PrqlCompiler.toSql(prqlCode.replace(" ?", "$0"), "sql.${prqlTarget}", format, false)
        } else {
            PrqlCompiler.toSql(prqlCode, "sql.${prqlTarget}", format, false)
        }
        if (sql.contains(" $ ")) {
            sql = sql.replace(" $ ", " $")
        }
        return sql
    }

    @Throws(Exception::class)
    fun transformPrqlWithJdbc(dialect: String?, prqlCode: String, format: Boolean, project: Project): String {
        val prqlTarget = getPrqlDatabaseTarget(dialect, project)
        var sql = if (prqlCode.contains(" ?")) {
            PrqlCompiler.toSql(prqlCode.replace(" ?", "$0"), "sql.${prqlTarget}", format, false)
        } else {
            PrqlCompiler.toSql(prqlCode, "sql.${prqlTarget}", format, false)
        }
        if (sql.contains(" $0")) {
            sql = sql.replace(" $0", " ?")
        }
        if (sql.contains(" $")) {
            sql = sql.replace(" $", " :")
        }
        return sql
    }

    private fun getPrqlDatabaseTarget(dialect: String?, project: Project): String {
        return if (dialect != null) {
            dialectMapping[dialect]
        } else {
            val defaultDialect = SqlDialectMappings.getMapping(project, null)
            dialectMapping[defaultDialect.id]
        } ?: "generic"
    }
}