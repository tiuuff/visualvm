/*
 * Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 * 
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.tools.visualvm.truffle.heapwalker.ruby;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.modules.profiler.heapwalk.details.api.DetailsSupport;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import org.netbeans.modules.profiler.heapwalker.v2.HeapContext;
import com.sun.tools.visualvm.truffle.heapwalker.DynamicObject;
import com.sun.tools.visualvm.truffle.heapwalker.TruffleLanguageHeapFragment;
import com.sun.tools.visualvm.truffle.heapwalker.TruffleLanguageSummaryView;
import org.netbeans.modules.profiler.heapwalker.v2.ui.HeapWalkerActions;
import org.netbeans.modules.profiler.heapwalker.v2.ui.HeapWalkerFeature;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Sedlacek
 */
public class RubyHeapSummaryView extends TruffleLanguageSummaryView {
    
    public RubyHeapSummaryView(HeapContext context) {
        super(RubySupport.createBadgedIcon(HeapWalkerIcons.PROPERTIES), context);
    }

    @Override
    protected String computeSummary(HeapContext context) {
        long rubyObjects = 0;
        long objectSize = 0;
        NumberFormat numberFormat = NumberFormat.getInstance();
        Map<Instance, String> shapes = new HashMap();
        Map<String, Object> types = new HashMap();
        List nodes = new ArrayList();
        TruffleLanguageHeapFragment fragment = (TruffleLanguageHeapFragment) context.getFragment();
        Heap heap = fragment.getHeap();
        Iterator<DynamicObject> instancesI = fragment.getDynamicObjectsIterator();

        while (instancesI.hasNext()) {
            DynamicObject dobject = instancesI.next();
            Instance shape = dobject.getShape();
            String type = shapes.get(shape);
            if (type == null) {
                type = DetailsSupport.getDetailsString(shape, heap);
                shapes.put(shape, type);
            }

            Object typeNode = types.get(type);
            if (typeNode == null) {
                String langid = dobject.getLanguageId().getName();
                if (RubyObjectsProvider.RUBY_LANG_ID.equals(langid)) {
                    typeNode = new Object();
                    nodes.add(typeNode);
                    types.put(type, typeNode);
                }
            }
            if (typeNode != null) {
                rubyObjects++;
                if (objectSize == 0) {
                    objectSize = dobject.getInstance().getSize();
                }
            }
        }
        String header = super.computeSummary(context);
        String bytes = LINE_PREFIX + "<b>Total bytes:&nbsp;</b>" + numberFormat.format(rubyObjects * objectSize) + "<br>"; // NOI18N
        String typesCount = LINE_PREFIX + "<b>Total types:&nbsp;</b>" + numberFormat.format(nodes.size()) + "<br>"; // NOI18N
        String robjects = LINE_PREFIX + "<b>Total objects:&nbsp;</b>" + numberFormat.format(rubyObjects) + "<br><br>"; // NOI18N
        return header + bytes + typesCount + robjects;
    }
    
    
    @ServiceProvider(service=HeapWalkerFeature.Provider.class)
    public static class Provider extends HeapWalkerFeature.Provider {

        public HeapWalkerFeature getFeature(HeapContext context, HeapWalkerActions actions) {
            if (RubyHeapFragment.isRubyHeap(context))
                return new RubyHeapSummaryView(context);
            
            return null;
        }

    }
    
}
