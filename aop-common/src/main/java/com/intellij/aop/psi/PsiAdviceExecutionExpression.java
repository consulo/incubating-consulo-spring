/*
 * Copyright (c) 2000-2005 by JetBrains s.r.o. All Rights Reserved.
 * Use is subject to license terms.
 */
package com.intellij.aop.psi;

import com.intellij.java.language.psi.PsiMember;
import consulo.language.ast.ASTNode;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;

/**
 * @author peter
 */
public class PsiAdviceExecutionExpression extends AopElementBase implements PsiPointcutExpression{
  public PsiAdviceExecutionExpression(@Nonnull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "PsiAdviceExecutionExpression";
  }

  @Nonnull
  public PointcutMatchDegree acceptsSubject(final PointcutContext context, final PsiMember member) {
    return PointcutMatchDegree.FALSE;
  }

  @Nonnull
  public Collection<AopPsiTypePattern> getPatterns() {
    return Collections.emptyList();
  }
}
