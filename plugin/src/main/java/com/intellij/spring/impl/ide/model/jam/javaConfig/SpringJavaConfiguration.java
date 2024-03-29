package com.intellij.spring.impl.ide.model.jam.javaConfig;

import com.intellij.jam.JamElement;
import com.intellij.jam.annotations.JamPsiConnector;
import com.intellij.jam.reflect.JamAnnotationMeta;
import com.intellij.java.language.psi.PsiAnnotation;
import com.intellij.java.language.psi.PsiClass;

import javax.annotation.Nonnull;
import java.util.List;

public abstract class SpringJavaConfiguration implements JamElement {

  private final JamAnnotationMeta myMeta;

  protected SpringJavaConfiguration(@Nonnull String annotation) {
    myMeta = new JamAnnotationMeta(annotation);
  }

  public PsiAnnotation getAnnotation() {
    return myMeta.getAnnotation(getPsiElement());
  }

  @Nonnull
  @JamPsiConnector
  public abstract PsiClass getPsiElement();

  public abstract List<? extends SpringJavaBean> getBeans();

  public PsiClass getPsiClass() {
    return getPsiElement();
  }
}
