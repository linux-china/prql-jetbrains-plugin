package org.mvnsearch.plugins.prql.lang.run

import com.intellij.execution.lineMarker.RunLineMarkerProvider
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.intellij.sql.dialects.SqlDialectMappings
import org.mvnsearch.plugins.prql.Prql
import org.mvnsearch.plugins.prql.lang.psi.PrqlTypes


open class PrqlBaseLienMarkerContributor : RunLineMarkerProvider() {

    fun transformPrql(prqlCode: String, project: Project): String {
        var prqlNewCode = prqlCode
        if (!prqlCode.contains("prql target:sql.")) {
            val defaultDialect = SqlDialectMappings.getMapping(project, null)
            val target = Prql.getTarget(defaultDialect.id)
            if (target != null) {
                prqlNewCode = "prql target:sql.${target}\n" + prqlCode
            }
        }
        val process = ProcessBuilder().command(Prql.getPrqlCompilerCmdAbsolutionPath(), "compile").start();
        process.outputStream.use {
            it.write(prqlNewCode.toByteArray())
        }
        process.waitFor()
        return if (process.exitValue() == 0) {
            process.inputStream.bufferedReader().readText()
        } else {
            process.errorStream.bufferedReader().readText()
        }
    }

    fun isPrqlFromElement(psiElement: PsiElement): Boolean {
        val elementType = psiElement.elementType
        return elementType == PrqlTypes.STMT_FROM || elementType == PrqlTypes.STMT_FROM_TEXT
    }

    fun raiseError(project: Project, title: String, message: String) {
        val prqlNotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("PRQL Error")
        prqlNotificationGroup.createNotification(title, message, NotificationType.ERROR).notify(project)
    }
}