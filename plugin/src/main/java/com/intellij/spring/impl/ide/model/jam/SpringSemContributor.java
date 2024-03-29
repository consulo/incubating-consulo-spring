package com.intellij.spring.impl.ide.model.jam;

import com.intellij.jam.JamService;
import com.intellij.jam.reflect.JamMemberMeta;
import com.intellij.java.language.codeInsight.AnnotationUtil;
import com.intellij.java.language.patterns.PsiClassPattern;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaSpringConfiguration;
import com.intellij.spring.impl.ide.model.jam.stereotype.*;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.psi.PsiElementRef;
import consulo.language.sem.SemContributor;
import consulo.language.sem.SemKey;
import consulo.language.sem.SemRegistrar;
import consulo.language.sem.SemService;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.spring.impl.boot.SpringBootModelProvider;
import jakarta.inject.Inject;

import java.util.List;
import java.util.function.Function;

import static com.intellij.java.language.patterns.PsiJavaPatterns.psiClass;

@ExtensionImpl
public class SpringSemContributor extends SemContributor {
  private static final SemKey<JamMemberMeta<PsiClass, CustomSpringComponent>> CUSTOM_COMPONENT_META_KEY =
    JamService.MEMBER_META_KEY.subKey("CustomSpringComponentMeta");
  public static final SemKey<CustomSpringComponent> CUSTOM_COMPONENT_JAM_KEY = JamService.JAM_ELEMENT_KEY.subKey("CustomSpringComponent");

  private final SemService mySemService;

  @Inject
  public SpringSemContributor(SemService semService) {
    mySemService = semService;
  }

  public void registerSemProviders(SemRegistrar registrar) {
    PsiClassPattern psiClassPattern = psiClass().nonAnnotationType();

    SpringBootModelProvider.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.SPRING_BOOT_APPLICATION));
    JavaConfigConfiguration.META.register(registrar,
                                          psiClassPattern.withAnnotation(SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION));
    JavaSpringConfiguration.META.register(registrar,
                                          psiClassPattern.withAnnotation(SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION));

    //JavaSpringConfiguration.BEANS_METHOD_META.register(registrar, PsiJavaPatterns.psiMethod().withAnnotation(SpringAnnotationsConstants.JAVA_SPRING_BEAN_ANNOTATION));

    SpringComponent.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.COMPONENT_ANNOTATION));
    SpringController.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.CONTROLLER_ANNOTATION));
    SpringService.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.SERVICE_ANNOTATION));
    SpringRepository.META.register(registrar, psiClassPattern.withAnnotation(SpringAnnotationsConstants.REPOSITORY_ANNOTATION));

    // register custom components

    registrar.registerSemElementProvider(CUSTOM_COMPONENT_META_KEY,
                                         psiClassPattern,
                                         new Function<PsiClass, JamMemberMeta<PsiClass, CustomSpringComponent>>() {
                                           public JamMemberMeta<PsiClass, CustomSpringComponent> apply(final PsiClass member) {
                                             return calcNamedWebBeanMeta(member);
                                           }
                                         });

    registrar.registerSemElementProvider(CUSTOM_COMPONENT_JAM_KEY,
                                         psiClassPattern,
                                         new Function<PsiClass, CustomSpringComponent>() {
                                           public CustomSpringComponent apply(PsiClass member) {
                                             final JamMemberMeta<PsiClass, CustomSpringComponent> memberMeta =
                                               mySemService.getSemElement(CUSTOM_COMPONENT_META_KEY, member);
                                             return memberMeta != null ? memberMeta.createJamElement(PsiElementRef.real(member)) : null;
                                           }
                                         });
  }


  private static JamMemberMeta<PsiClass, CustomSpringComponent> calcNamedWebBeanMeta(PsiClass psiClass) {
    if (psiClass.isAnnotationType()) return null;

    final Module module = ModuleUtilCore.findModuleForPsiElement(psiClass);
    if (module != null) {
      List<String> customComponentAnnotations = JamAnnotationTypeUtil.getUserDefinedCustomComponentAnnotations(module);

      for (String anno : customComponentAnnotations) {
        if (AnnotationUtil.isAnnotated(psiClass, anno, true)) {
          return createCustomSpringComponentJamMemberMeta(anno);
        }
      }
    }

    return null;
  }

  private static JamMemberMeta<PsiClass, CustomSpringComponent> createCustomSpringComponentJamMemberMeta(final String annotationFQN) {
    return new JamMemberMeta<PsiClass, CustomSpringComponent>(null, CustomSpringComponent.class, CUSTOM_COMPONENT_JAM_KEY) {
      @Override
      public CustomSpringComponent createJamElement(PsiElementRef<PsiClass> psiMemberPsiRef) {
        return new CustomSpringComponent(annotationFQN, psiMemberPsiRef.getPsiElement());
      }
    };
  }
}