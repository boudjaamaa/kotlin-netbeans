/*******************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************/
package org.jetbrains.kotlin.hints.intentions

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analyzer.AnalysisResult
import javax.swing.text.Document
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.kotlin.psi.psiUtil.getNonStrictParentOfType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult

class RemoveEmptyPrimaryConstructorIntention(doc: Document,
                                             analysisResult: AnalysisResult?,
                                             psi: PsiElement) : ApplicableIntention(doc, analysisResult, psi) {
    
    private var expression: KtPrimaryConstructor? = null
    
    override fun isApplicable(caretOffset: Int): Boolean {
        expression = psi.getNonStrictParentOfType(KtPrimaryConstructor::class.java) ?: return false
        val element = expression ?: return false
        
        return element.isApplicable()
    }

    override fun getDescription() = "Remove empty primary constructor"

    override fun implement() {
        val element = expression ?: return
        
        val startOffset = element.textRange.startOffset
        val lengthToDelete = element.textLength
        
        doc.remove(startOffset, lengthToDelete)
    }
}

class RemoveEmptyPrimaryConstructorInspection(val parserResult: KotlinParserResult,
                                              override val element: KtElement) : Inspection(element) {
    
    override val description = "Empty primary constructor"
    
    override fun isApplicable(): Boolean {
        if (element !is KtPrimaryConstructor) return false
        
        return element.isApplicable()
    }
    
}

private fun KtPrimaryConstructor.isApplicable() = when {
    valueParameters.isNotEmpty() -> false
    annotations.isNotEmpty() -> false
    modifierList?.text?.isBlank() == false -> false
    containingClass()?.getSecondaryConstructors()?.isNotEmpty() == true -> false
    else -> true
}