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
package com.sun.tools.visualvm.truffle.heapwalker;

import java.util.Map;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.ui.swing.renderer.LabelRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.MultiRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.NormalBoldGrayRenderer;
import org.netbeans.lib.profiler.ui.swing.renderer.ProfilerRenderer;
import org.netbeans.modules.profiler.heapwalker.v2.HeapContext;
import org.netbeans.modules.profiler.heapwalker.v2.java.LocalObjectNode;
import org.netbeans.modules.profiler.heapwalker.v2.java.LocalObjectNodeRenderer;
import org.netbeans.modules.profiler.heapwalker.v2.java.InstanceReferenceNode;
import org.netbeans.modules.profiler.heapwalker.v2.java.InstanceReferenceNodeRenderer;
import org.netbeans.modules.profiler.heapwalker.v2.java.PrimitiveNode;
import org.netbeans.modules.profiler.heapwalker.v2.java.PrimitiveNodeRenderer;
import org.netbeans.modules.profiler.heapwalker.v2.java.StackFrameNode;
import org.netbeans.modules.profiler.heapwalker.v2.java.ThreadNode;
import org.netbeans.modules.profiler.heapwalker.v2.java.ThreadNodeRenderer;
import org.netbeans.modules.profiler.heapwalker.v2.model.DataType;
import org.netbeans.modules.profiler.heapwalker.v2.model.HeapWalkerNode;
import org.netbeans.modules.profiler.heapwalker.v2.ui.HeapWalkerRenderer;

/**
 *
 * @author Jiri Sedlacek
 */
public class TruffleNodesRendererProvider extends HeapWalkerRenderer.Provider {

    public void registerRenderers(Map<Class<? extends HeapWalkerNode>, HeapWalkerRenderer> renderers, HeapContext context) {

        Heap heap = context.getFragment().getHeap();
        
        // --- reused from Java ------------------------------------------------
        
        // primitive fields & items
        renderers.put(PrimitiveNode.class, new PrimitiveNodeRenderer());
        
        // object fields & items
        renderers.put(InstanceReferenceNode.class, new InstanceReferenceNodeRenderer(heap)); 
        
        // thread 
        renderers.put(ThreadNode.class, new RubyThreadNodeRenderer(heap));
        
        // local variables
        renderers.put(LocalObjectNode.class, new LocalObjectNodeRenderer(heap));
        
        // stack frames
        renderers.put(StackFrameNode.class, new TruffleStackFrameNodeRenderer());
    }
    
    
    // NOTE: temporary solution, should probably be implemented for each Truffle language separately
    private static class RubyThreadNodeRenderer extends ThreadNodeRenderer {
        
        public RubyThreadNodeRenderer(Heap heap) {
            super(heap);
        }
        
        public void setValue(Object value, int row) {
            HeapWalkerNode node = (HeapWalkerNode)value;
            String name = HeapWalkerNode.getValue(node, DataType.NAME, heap);
            int resIdx = name.indexOf('@');
            setText(resIdx == -1 ? name : name.substring(0, resIdx));
        }
        
        public String getShortName() {
            return getText();
        }
        
    }
    
    
    // NOTE: temporary solution, should probably be implemented for each Truffle language separately
    private static class TruffleStackFrameNodeRenderer extends MultiRenderer implements HeapWalkerRenderer {
    
        private final LabelRenderer atRenderer;
        private final NormalBoldGrayRenderer frameRenderer;
        private final ProfilerRenderer[] renderers;
        
        private String name1;
        private String name2;
        private String detail;


        public TruffleStackFrameNodeRenderer() {
            atRenderer = new LabelRenderer();
            atRenderer.setText("at");
            atRenderer.setMargin(3, 3, 3, 0);
            frameRenderer = new NormalBoldGrayRenderer() {
                public void setValue(Object value, int row) {
                    if (value == null) {
                        setNormalValue("");
                        setBoldValue("");
                        setGrayValue("");
                    } else {
                        setNormalValue(((Object[])value)[0].toString());
                        setBoldValue(((Object[])value)[1].toString());
                        setGrayValue(((Object[])value)[2].toString());
                    }
                }
            };
            renderers = new ProfilerRenderer[] { atRenderer, frameRenderer };
        }


        protected ProfilerRenderer[] valueRenderers() {
            return renderers;
        }


        public void setValue(Object value, int row) {
            String val = value.toString();
            
            int idx = val.indexOf('@');
            if (idx != -1) {
                // JavaScript Node@hashCode
                name2 = val.substring(0, idx);
                detail = " (" + val.substring(idx) + ")";
            } else {
                idx = val.indexOf(' ');
                if (idx != -1) {
                    // Ruby node with resource
                    name2 = val.substring(0, idx);
                    detail = val.substring(idx);
                    
                    idx = detail.lastIndexOf('/');
                    if (idx != -1) {
                        detail = detail.substring(idx + 1);
                    }
                    
                    if (!detail.startsWith(" (")) detail = " (" + detail + ")";
                } else {
                    // JavaScript function
                    name2 = val;
                    detail = " ()";
                }
            }
            
            idx = name2.lastIndexOf('.');
            if (idx != -1) {
                // JavaScript function in package
                name1 = name2.substring(0, idx + 1);
                name2 = name2.substring(idx + 1);
            } else {
                idx = name2.lastIndexOf('#');
                if (idx > 0) {
                    // Ruby method in module
                    name1 = name2.substring(0, idx);
                    name2 = name2.substring(idx);
                } else {
                    name1 = "";
                }
            }
            
            frameRenderer.setValue(new Object[] { name1, name2, detail }, row);
        }

        public String getShortName() {
            return "at " + name2 + " " + detail;
        }

    }
    
}
