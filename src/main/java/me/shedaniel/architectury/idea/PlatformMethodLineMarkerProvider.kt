package me.shedaniel.architectury.idea

import com.intellij.codeInsight.daemon.DefaultGutterIconNavigationHandler
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import java.awt.event.MouseEvent
import javax.swing.Icon

class PlatformMethodLineMarkerProvider : LineMarkerProviderDescriptor(), GutterIconNavigationHandler<PsiIdentifier> {
    override fun getName(): String = "Platform implementation method line marker"
    override fun getIcon(): Icon = AllIcons.Gutter.ImplementingMethod

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiIdentifier>? {
        if (element is PsiIdentifier) {
            val parent = element.parent

            if (parent is PsiMethod && parent.nameIdentifier === element && parent.commonMethods.isNotEmpty()) {
                return LineMarkerInfo(
                    element,
                    element.textRange,
                    icon,
                    { "Go to common declaration" },
                    this,
                    GutterIconRenderer.Alignment.LEFT,
                    { "Go to common definition" }
                )
            }
        }

        return null
    }

    override fun navigate(e: MouseEvent, elt: PsiIdentifier) {
        val method = elt.parent as? PsiMethod ?: return
        val commonMethods = method.commonMethods

        if (commonMethods.isNotEmpty()) {
            DefaultGutterIconNavigationHandler<PsiIdentifier>(
                commonMethods,
                "Choose common declaration for ${method.name}"
            ).navigate(e, elt)
        }
    }
}
