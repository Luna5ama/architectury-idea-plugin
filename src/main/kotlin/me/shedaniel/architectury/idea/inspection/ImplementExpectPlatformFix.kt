package me.shedaniel.architectury.idea.inspection

import com.intellij.codeInsight.generation.GenerateMembersUtil
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.psi.JavaDirectoryService
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.ui.components.JBLabel
import me.shedaniel.architectury.idea.util.ArchitecturyBundle
import me.shedaniel.architectury.idea.util.EXPECT_PLATFORM
import me.shedaniel.architectury.idea.util.OLD_EXPECT_PLATFORM
import me.shedaniel.architectury.idea.util.Platform

object ImplementExpectPlatformFix : LocalQuickFix {
    override fun getFamilyName(): String = ArchitecturyBundle["inspection.implementExpectPlatform"]

    override fun applyFix(project: Project, descriptor: ProblemDescriptor) {
        val method = findMethod(descriptor.psiElement) ?: error("Could not find method from ${descriptor.psiElement}")
        val facade = JavaPsiFacade.getInstance(project)
        val elementFactory = JavaPsiFacade.getElementFactory(project)

        var generatedMethod: PsiMethod? = null
        val missingPlatforms = ArrayList<String>(2)

        for (platform in Platform.values()) {
            if (!platform.isIn(project)) continue

            val implClassName = platform.getImplementationName(method.containingClass ?: continue)
            val implClass = facade.findClass(implClassName, GlobalSearchScope.projectScope(project)) ?: run {
                val packageName = implClassName.substringBeforeLast('.')
                val pkg = facade.findPackage(packageName)

                if (pkg == null) {
                    missingPlatforms += packageName
                    return@run null
                }

                val dir = pkg.directories.first()
                JavaDirectoryService.getInstance().createClass(dir, implClassName.substringAfterLast('.'))
            } ?: continue

            val template = elementFactory.createMethod(method.name, method.returnType)

            // Add the different modifiers. The public modifier is first removed to correct its place.
            template.modifierList.setModifierProperty(PsiModifier.PUBLIC, false) // remove from list...
            GenerateMembersUtil.copyAnnotations(method, template, EXPECT_PLATFORM, OLD_EXPECT_PLATFORM)
            template.modifierList.setModifierProperty(PsiModifier.PUBLIC, true) // ... add to list
            template.modifierList.setModifierProperty(PsiModifier.STATIC, true)

            val added = GenerateMembersUtil.insert(implClass, template, implClass.methods.lastOrNull(), false)

            if (generatedMethod == null) {
                generatedMethod = added as? PsiMethod
            }
        }

        if (missingPlatforms.isEmpty()) {
            generatedMethod?.navigate(true)
        } else {
            val label = JBLabel(ArchitecturyBundle["fix.missingPlatformPackages", missingPlatforms.joinToString()])

            JBPopupFactory.getInstance()
                .createComponentPopupBuilder(label, null)
                .createPopup()
                .showInBestPositionFor(FileEditorManager.getInstance(project).selectedTextEditor!!)
        }
    }

    private tailrec fun findMethod(element: PsiElement): PsiMethod? =
        when {
            element is PsiMethod -> element
            element.parent != null -> findMethod(element.parent)
            else -> null
        }
}
