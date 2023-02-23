package org.mvnsearch.plugins.prql.lang.run

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.psi.PsiElement
import java.awt.datatransfer.StringSelection
import javax.swing.Icon

@Suppress("DialogTitleCapitalization")
class PrqlTranspileLineMarkerContributor : PrqlBaseLienMarkerContributor() {
    override fun getName(): String {
        return "prql-transpile-sql"
    }

    override fun getIcon(): Icon {
        return icons.DatabaseIcons.Sql
    }

    override fun getLineMarkerInfo(psiElement: PsiElement): LineMarkerInfo<*>? {
        if (isPrqlFromElement(psiElement)) {
            val text = psiElement.containingFile.text
            return LineMarkerInfo(
                psiElement,
                psiElement.textRange,
                icon,
                {
                    "Copy generated SQL to clipboard"
                },
                { e, elt ->
                    try {
                        var sqlOrError = transformPrql(null, text + "\n", elt.project).trim()
                        if (sqlOrError.contains("-- Generated by PRQL")) {
                            sqlOrError = sqlOrError.substring(0, sqlOrError.indexOf("-- Generated by PRQL")).trim()
                        }
                        if (sqlOrError.startsWith("ERROR:") || sqlOrError.startsWith("Error:")) {
                            raiseError(psiElement.project, "Error generating SQL!", sqlOrError)
                        } else {   // compiled successfully
                            CopyPasteManager.getInstance().setContents(StringSelection(sqlOrError))
                            val prqlNotificationGroup = NotificationGroupManager.getInstance().getNotificationGroup("PRQL Info")
                            prqlNotificationGroup.createNotification(
                                "SQL generated and copied to clipboard!",
                                sqlOrError, NotificationType.INFORMATION
                            ).notify(psiElement.project)
                        }
                    } catch (e: Exception) {
                        raiseError(psiElement.project, "Error generating SQL!", e.message!!)
                    }
                },
                GutterIconRenderer.Alignment.CENTER,
                { "Copy generated SQL to clipboard" }
            )
        }
        return null
    }
}
