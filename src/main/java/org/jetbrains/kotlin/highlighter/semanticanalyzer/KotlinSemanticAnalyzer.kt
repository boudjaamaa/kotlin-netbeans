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
package org.jetbrains.kotlin.highlighter.semanticanalyzer

import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParserResult
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.projectsextensions.KotlinProjectHelper.isScanning
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.language.Priorities
import org.netbeans.modules.csl.api.ColoringAttributes
import org.netbeans.modules.csl.api.OffsetRange
import org.netbeans.modules.csl.api.SemanticAnalyzer
import org.netbeans.modules.parsing.spi.Scheduler
import org.netbeans.modules.parsing.spi.SchedulerEvent

class KotlinSemanticAnalyzer : SemanticAnalyzer<KotlinParserResult>() {
    
    private var cancel = false
    private val highlighting = hashMapOf<OffsetRange, Set<ColoringAttributes>>()
    
    private fun highlightDeprecatedElements(bindingContext: BindingContext, ktFile: KtFile) = 
            bindingContext.diagnostics
                    .filter { it.factory == Errors.DEPRECATION }
                    .filter { it.psiFile == ktFile }
                    .map { OffsetRange(it.textRanges.first().startOffset,
                            it.textRanges.first().endOffset) }
                    .forEach { highlighting.put(it, KotlinHighlightingAttributes.DEPRECATED.styleKey) }
    
    override fun getPriority() = Priorities.SEMANTIC_ANALYZER_PRIORITY

    override fun getHighlights() = highlighting

    override fun run(result: KotlinParserResult?, event: SchedulerEvent?) {
        highlighting.clear()
        cancel = false
        
        if (result == null || result.project.isScanning()) return
          
        val analysisResult = result.analysisResult?.analysisResult ?: return
        
        highlight(analysisResult, result.ktFile)
    }

    fun highlight(analysisResult: AnalysisResult, ktFile: KtFile) {
        val highlightingVisitor = KotlinSemanticHighlightingVisitor(ktFile, analysisResult)
        highlighting.putAll(highlightingVisitor.computeHighlightingRanges())
        
        highlightDeprecatedElements(analysisResult.bindingContext, ktFile)
    }
    
    override fun cancel() {
        cancel = true
    }

    override fun getSchedulerClass() = Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER

}