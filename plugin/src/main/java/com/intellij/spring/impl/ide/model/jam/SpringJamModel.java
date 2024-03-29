package com.intellij.spring.impl.ide.model.jam;

import com.intellij.jam.JamService;
import com.intellij.java.language.psi.PsiClass;
import com.intellij.spring.impl.ide.constants.SpringAnnotationsConstants;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaConfigConfiguration;
import com.intellij.spring.impl.ide.model.jam.javaConfig.JavaSpringConfiguration;
import com.intellij.spring.impl.ide.model.jam.javaConfig.SpringJavaConfiguration;
import com.intellij.spring.impl.ide.model.jam.stereotype.*;
import com.intellij.spring.impl.ide.model.jam.utils.JamAnnotationTypeUtil;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.module.Module;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

@ServiceAPI(ComponentScope.MODULE)
@ServiceImpl
@Singleton
public class SpringJamModel {
  private final Module myModule;


  public static SpringJamModel getModel(@Nonnull Module module) {
    return module.getInstance(SpringJamModel.class);
  }

  @Inject
  public SpringJamModel(@Nonnull final Module module) {
    myModule = module;
  }

  public List<SpringJavaConfiguration> getConfigurations() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);
    List<SpringJavaConfiguration> configurations = new ArrayList<SpringJavaConfiguration>();

    configurations.addAll(service.getJamClassElements(JavaConfigConfiguration.META,
                                                      SpringAnnotationsConstants.JAVA_CONFIG_CONFIGURATION_ANNOTATION,
                                                      scope));
    configurations.addAll(service.getJamClassElements(JavaSpringConfiguration.META,
                                                      SpringAnnotationsConstants.JAVA_SPRING_CONFIGURATION_ANNOTATION,
                                                      scope));

    return configurations;
  }

  @Nonnull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents(@Nonnull PsiClass psiMember) {
    final JamService service = JamService.getJamService(myModule.getProject());

    return service
      .getAnnotatedMembersList(psiMember, true, true, false, true, SpringComponent.META, SpringController.META, SpringService.META,
                               SpringRepository.META);
  }

  @Nonnull
  public List<? extends SpringStereotypeElement> getAllStereotypeComponents() {
    List<SpringStereotypeElement> stereotypeElements = new ArrayList<SpringStereotypeElement>();

    stereotypeElements.addAll(getComponents());
    stereotypeElements.addAll(getControllers());
    stereotypeElements.addAll(getRepositories());
    stereotypeElements.addAll(getServices());
    stereotypeElements.addAll(getCustomStereotypeComponents());

    return stereotypeElements;
  }

  @Nonnull
  public List<? extends CustomSpringComponent> getCustomStereotypeComponents() {
    List<CustomSpringComponent> customSpringComponents = new ArrayList<CustomSpringComponent>();
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    for (String anno : JamAnnotationTypeUtil.getUserDefinedCustomComponentAnnotations(myModule)) {
      customSpringComponents.addAll(service.getJamClassElements(SpringSemContributor.CUSTOM_COMPONENT_JAM_KEY, anno, scope));
    }

    return customSpringComponents;
  }

  @Nonnull
  public List<? extends SpringComponent> getComponents() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringComponent.META, SpringAnnotationsConstants.COMPONENT_ANNOTATION, scope);
  }

  @Nonnull
  public List<? extends SpringController> getControllers() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringController.META, SpringAnnotationsConstants.CONTROLLER_ANNOTATION, scope);
  }


  @Nonnull
  public List<? extends SpringRepository> getRepositories() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringRepository.META, SpringAnnotationsConstants.REPOSITORY_ANNOTATION, scope);
  }


  @Nonnull
  public List<? extends SpringService> getServices() {
    final JamService service = JamService.getJamService(myModule.getProject());
    final GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(myModule);

    return service.getJamClassElements(SpringService.META, SpringAnnotationsConstants.SERVICE_ANNOTATION, scope);
  }

}