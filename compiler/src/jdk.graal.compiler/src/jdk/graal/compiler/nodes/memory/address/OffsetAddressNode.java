/*
 * Copyright (c) 2015, 2022, Oracle and/or its affiliates. All rights reserved.
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
package jdk.graal.compiler.nodes.memory.address;

import org.graalvm.word.WordBase;

import jdk.graal.compiler.core.common.type.AbstractPointerStamp;
import jdk.graal.compiler.core.common.type.IntegerStamp;
import jdk.graal.compiler.core.common.type.PrimitiveStamp;
import jdk.graal.compiler.core.common.type.Stamp;
import jdk.graal.compiler.debug.Assertions;
import jdk.graal.compiler.graph.Node;
import jdk.graal.compiler.graph.NodeClass;
import jdk.graal.compiler.nodeinfo.InputType;
import jdk.graal.compiler.nodeinfo.NodeInfo;
import jdk.graal.compiler.nodes.ConstantNode;
import jdk.graal.compiler.nodes.NodeView;
import jdk.graal.compiler.nodes.ValueNode;
import jdk.graal.compiler.nodes.calc.AddNode;
import jdk.graal.compiler.nodes.calc.BinaryArithmeticNode;
import jdk.graal.compiler.nodes.spi.Canonicalizable;
import jdk.graal.compiler.nodes.spi.CanonicalizerTool;
import jdk.vm.ci.meta.JavaKind;

/**
 * Represents an address that is composed of a base and an offset. The base can be either a
 * {@link JavaKind#Object}, a word-sized integer or another pointer. The offset must be a word-sized
 * integer.
 */
@NodeInfo(allowedUsageTypes = InputType.Association)
public class OffsetAddressNode extends AddressNode implements Canonicalizable {
    public static final NodeClass<OffsetAddressNode> TYPE = NodeClass.create(OffsetAddressNode.class);

    @Node.Input ValueNode base;
    @Node.Input ValueNode offset;

    private static boolean assertOperands(ValueNode base, ValueNode offset) {
        assert base != null && offset != null;
        Stamp baseStamp = base.stamp(NodeView.DEFAULT);
        Stamp offsetStamp = offset.stamp(NodeView.DEFAULT);
        assert offsetStamp instanceof IntegerStamp : Assertions.errorMessageContext("base", base, "offset", offset);
        assert baseStamp instanceof AbstractPointerStamp || PrimitiveStamp.getBits(baseStamp) == PrimitiveStamp.getBits(offsetStamp)
                : Assertions.errorMessageContext("base", base, "offset", offset);
        return true;
    }

    public OffsetAddressNode(ValueNode base, ValueNode offset) {
        super(TYPE);
        this.base = base;
        this.offset = offset;
        assert assertOperands(base, offset);
    }

    public static OffsetAddressNode create(ValueNode base, JavaKind wordKind) {
        return new OffsetAddressNode(base, ConstantNode.forIntegerBits(wordKind.getBitCount(), 0));
    }

    @Override
    public ValueNode getBase() {
        return base;
    }

    public void setBase(ValueNode base) {
        updateUsages(this.base, base);
        this.base = base;
        assert assertOperands(base, offset);
    }

    public ValueNode getOffset() {
        return offset;
    }

    public void setOffset(ValueNode offset) {
        updateUsages(this.offset, offset);
        this.offset = offset;
        assert assertOperands(base, offset);
    }

    @Override
    public Node canonical(CanonicalizerTool tool) {
        if (base instanceof OffsetAddressNode) {
            NodeView view = NodeView.from(tool);
            // Rewrite (&base[offset1])[offset2] to base[offset1 + offset2].
            OffsetAddressNode b = (OffsetAddressNode) base;
            return new OffsetAddressNode(b.getBase(), BinaryArithmeticNode.add(b.getOffset(), this.getOffset(), view));
        } else if (base instanceof AddNode) {
            AddNode add = (AddNode) base;
            if (add.getY().isConstant()) {
                return new OffsetAddressNode(add.getX(), new AddNode(add.getY(), getOffset()));
            }
        }
        return this;
    }

    @Node.NodeIntrinsic
    public static native Address address(Object base, WordBase offset);

    @Override
    public long getMaxConstantDisplacement() {
        Stamp curStamp = offset.stamp(NodeView.DEFAULT);
        if (curStamp instanceof IntegerStamp) {
            IntegerStamp integerStamp = (IntegerStamp) curStamp;
            if (integerStamp.lowerBound() >= 0) {
                return integerStamp.upperBound();
            }
        }
        return Long.MAX_VALUE;
    }

    @Override
    public ValueNode getIndex() {
        return null;
    }
}
