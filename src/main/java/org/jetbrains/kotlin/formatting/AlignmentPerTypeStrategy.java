/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package org.jetbrains.kotlin.formatting;

import com.intellij.formatting.Alignment;
import com.intellij.psi.tree.IElementType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Alexander.Baratynski
 */
public class AlignmentPerTypeStrategy extends KotlinAlignmentStrategy {

    private final Collection<IElementType> targetElementTypes;
    private final IElementType myParentType;
    private final boolean myAllowBackwardShift;
    private final Alignment.Anchor anchor;
    private final Map<IElementType, Alignment> myAlignments = 
            new HashMap<>();
    
    public AlignmentPerTypeStrategy(Collection<IElementType> targetElementTypes,
            IElementType myParentType, boolean myAllowBackwardShift,
            Alignment.Anchor anchor) {
        this.targetElementTypes = targetElementTypes;
        this.myParentType = myParentType;
        this.myAllowBackwardShift = myAllowBackwardShift;
        this.anchor = anchor;
        
        for (IElementType elementType : targetElementTypes) {
            myAlignments.put(elementType, Alignment.createAlignment(myAllowBackwardShift, anchor));
        }
        
    }
    
    @Override
    public Alignment getAlignment(IElementType parentType, IElementType childType) {
        if (myParentType != null && parentType != null && myParentType != parentType) {
            return null;
        }
        
        return myAlignments.get(childType);
    }
    
    public void renewAlignment(IElementType elementType) {
        myAlignments.put(elementType, Alignment.createAlignment(myAllowBackwardShift));
    }
    
}
