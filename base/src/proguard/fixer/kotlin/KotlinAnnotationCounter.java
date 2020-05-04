/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2020 GuardSquare NV
 */

package proguard.fixer.kotlin;

import proguard.classfile.*;
import proguard.classfile.attribute.Attribute;
import proguard.classfile.attribute.annotation.*;
import proguard.classfile.attribute.annotation.visitor.*;
import proguard.classfile.attribute.visitor.*;
import proguard.classfile.kotlin.KotlinConstants;
import proguard.classfile.visitor.MemberVisitor;
import proguard.shrink.SimpleUsageMarker;

/**
 * @author james
 */
public class KotlinAnnotationCounter
implements   MemberVisitor,
             AttributeVisitor,
             AnnotationVisitor
{
    private int               count                    = 0;
    private int[]             parameterAnnotationCount = null;
    private SimpleUsageMarker usageMarker;


    public KotlinAnnotationCounter(SimpleUsageMarker javaUsageMarker)
    {
        this.usageMarker = javaUsageMarker;
    }


    public KotlinAnnotationCounter() {}


    /**
     * Returns the number of annotations excluding parameter annotations.
     *
     * @return the number of annotations.
     */
    public int getCount()
    {
        return count;
    }


    /**
     * Returns the number of annotations on param{index} or -1 if there is no parameter at that index.
     *
     * @param index parameter index.
     * @return the number of annotations or -1.
     */
    public int getParameterAnnotationCount(int index)
    {
        return parameterAnnotationCount != null &&
               parameterAnnotationCount.length > 0 && index <= parameterAnnotationCount.length
            ? parameterAnnotationCount[index]
            : -1;
    }


    public KotlinAnnotationCounter reset()
    {
        this.count = 0;
        this.parameterAnnotationCount = null;
        return this;
    }

    // Implementations for MemberVisitor.


    @Override
    public void visitAnyMember(Clazz clazz, Member member)
    {
        member.accept(clazz, new AllAttributeVisitor(this));
    }

    // Implementations for AttributeVisitor.


    @Override
    public void visitAnyAnnotationsAttribute(Clazz clazz, AnnotationsAttribute annotationsAttribute)
    {
        annotationsAttribute
            .annotationsAccept(clazz,
                               new AnnotationTypeFilter("!" + KotlinConstants.TYPE_KOTLIN_METADATA,
                                                        this));
    }


    @Override
    public void visitAnyParameterAnnotationsAttribute(Clazz                         clazz,
                                                      Method                        method,
                                                      ParameterAnnotationsAttribute parameterAnnotationsAttribute)
    {
        this.parameterAnnotationCount = new int[parameterAnnotationsAttribute.u1parametersCount];
        parameterAnnotationsAttribute
            .annotationsAccept(clazz,
                               method,
                               new AnnotationTypeFilter("!" + KotlinConstants.TYPE_KOTLIN_METADATA,
                                                        this));
    }


    @Override
    public void visitAnyAttribute(Clazz clazz, Attribute attribute) { }

    // Implementations for AnnotationVisitor.


    @Override
    public void visitAnnotation(Clazz clazz, Annotation annotation)
    {
        if (usageMarker == null || usageMarker.isUsed(annotation))
        {
            this.count++;
        }
    }


    @Override
    public void visitAnnotation(Clazz clazz, Method method, int parameterIndex, Annotation annotation)
    {
        if (usageMarker == null || usageMarker.isUsed(annotation))
        {
            this.parameterAnnotationCount[parameterIndex]++;
        }
    }
}