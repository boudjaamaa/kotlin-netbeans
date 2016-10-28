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
package org.jetbrains.kotlin.installer

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.jetbrains.kotlin.statistics.KotlinStatisticsUpdater
import org.openide.filesystems.FileObject
import org.openide.loaders.DataObject
import org.openide.windows.TopComponent
import org.openide.windows.WindowManager
import java.util.HashSet

class KotlinInstaller : Yenta() {

    override fun friends() = setOf("org.netbeans.modules.maven",
                "org.netbeans.modules.maven.embedder",
                "org.netbeans.modules.jumpto",
                "org.netbeans.modules.debugger.jpda",
                "org.netbeans.modules.debugger.jpda.projects",
                "org.netbeans.modules.java.api.common")
    
    override fun restored() {        
        WindowManager.getDefault().invokeWhenUIReady { 
            WindowManager.getDefault().registry.addPropertyChangeListener listener@{
                if (it.propertyName.equals("opened")) {
                    if (KotlinStatisticsUpdater.updated) return@listener
                    
                    val newHashSet = it.newValue as HashSet<TopComponent>
                    val oldHashSet = it.oldValue as HashSet<TopComponent>
                    newHashSet.filter {!oldHashSet.contains(it)}
                            .forEach {
                        val dataObject = it.lookup.lookup(DataObject::class.java) ?: return@forEach
                        val currentFile = dataObject.primaryFile
                        if (currentFile != null && currentFile.mimeType.equals("text/x-kt")) {
                            KotlinStatisticsUpdater.updateStatistics()
                        }
                    }
                }
            }
        }
    }
    
}