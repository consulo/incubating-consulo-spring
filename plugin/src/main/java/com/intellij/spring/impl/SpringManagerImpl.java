/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */

package com.intellij.spring.impl;

import com.intellij.java.impl.util.xml.converters.values.GenericDomValueConvertersRegistry;
import com.intellij.java.language.psi.PsiType;
import com.intellij.spring.impl.ide.SpringManager;
import com.intellij.spring.impl.ide.SpringModel;
import com.intellij.spring.impl.ide.SpringModelProvider;
import com.intellij.spring.impl.ide.facet.SpringFileSet;
import com.intellij.spring.impl.ide.model.converters.CustomConverterRegistry;
import com.intellij.spring.impl.ide.model.values.converters.*;
import com.intellij.spring.impl.ide.model.xml.beans.Beans;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ServiceImpl;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.project.Project;
import consulo.spring.impl.boot.AnnotationSpringModel;
import consulo.spring.impl.boot.SpringBootFileSet;
import consulo.spring.impl.dom.SpringDomUtil;
import consulo.spring.impl.model.CompositeSpringModel;
import consulo.spring.impl.module.extension.SpringModuleExtension;
import consulo.util.collection.SmartList;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.util.xml.DomFileElement;
import consulo.xml.util.xml.DomManager;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Dmitry Avdeev
 */
@Singleton
@ServiceImpl
public class SpringManagerImpl extends SpringManager {

  @Deprecated
  private final SpringModelFactory myModelFactory;
  private final GenericDomValueConvertersRegistry myValueProvidersRegistry;
  private final CustomConverterRegistry myCustomConvertersRegistry;

  @Inject
  public SpringManagerImpl(Project project) {
    myModelFactory = new SpringModelFactory(project);
    myValueProvidersRegistry = new GenericDomValueConvertersRegistry();
    myCustomConvertersRegistry = new CustomConverterRegistry();

    registerValueConverters();
  }

  private void registerValueConverters() {
    myValueProvidersRegistry
      .registerConverter(new PlaceholderPropertiesConverter(), new PlaceholderPropertiesConverter.PlaceholderPropertiesCondition());

    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(false), PsiType.BOOLEAN);
    myValueProvidersRegistry.registerConverter(new SpringBooleanValueConverter(true), Boolean.class);

    myValueProvidersRegistry.registerNumberValueConverters();
    myValueProvidersRegistry.registerCharacterConverter();

    myValueProvidersRegistry.registerClassValueConverters();

    myValueProvidersRegistry.registerConverter(new ResourceValueConverter(), new ResourceValueConverter.ResourceValueConverterCondition());
    myValueProvidersRegistry.registerConverter(new FieldRetrievingFactoryBeanConverter(),
                                               new FieldRetrievingFactoryBeanConverter.FactoryClassAndPropertyCondition());
    myValueProvidersRegistry.registerConverter(new EnumValueConverter(), new EnumValueConverter.TypeCondition());
  }

  @Override
  @Nonnull
  @RequiredReadAction
  public List<SpringModel> getAllModels(@Nonnull consulo.module.Module module) {
    SpringModuleExtension extension = ModuleUtilCore.getExtension(module, SpringModuleExtension.class);
    if (extension == null) {
      return Collections.emptyList();
    }
    Set<SpringFileSet> allSets = getAllSets(extension);
    if (allSets.isEmpty()) {
      return Collections.emptyList();
    }

    List<SpringModel> list = new SmartList<>();
    for (SpringFileSet set : allSets) {
      if (set instanceof SpringBootFileSet) {
        list.add(new AnnotationSpringModel(module));
      }
      else {
        list.addAll(SpringDomUtil.createModels(set, module));
      }
    }
    return list;
  }


  @RequiredReadAction
  @Override
  @Nullable
  public SpringModel getCombinedModel(final Module module) {
    List<SpringModel> allModels = getAllModels(module);
    if (allModels.isEmpty()) {
      return null;
    }
    return new CompositeSpringModel(module, allModels);
  }

  @Override
  public GenericDomValueConvertersRegistry getValueProvidersRegistry() {
    return myValueProvidersRegistry;
  }

  @Override
  public CustomConverterRegistry getCustomConverterRegistry() {
    return myCustomConvertersRegistry;
  }

  @Override
  public boolean isSpringBeans(@Nonnull XmlFile file) {
    return DomManager.getDomManager(file.getProject()).getFileElement(file, Beans.class) != null;
  }

  @Override
  @Nullable
  public SpringModel getSpringModelByFile(@Nonnull XmlFile file) {
    return myModelFactory.getModelByConfigFile(file);
  }

  @Override
  @Nullable
  public SpringModel getLocalSpringModel(@Nonnull XmlFile file) {
    final DomFileElement<Beans> beans = myModelFactory.getDomRoot(file);
    return beans == null ? null : new DomSpringModelImpl(beans,
                                                         Collections.singleton(file),
                                                         ModuleUtilCore.findModuleForPsiElement(file),
                                                         null);
  }

  @Override
  @Nonnull
  @RequiredReadAction
  public List<SpringFileSet> getProvidedModels(@Nonnull SpringModuleExtension extension) {
    List<SpringFileSet> result = null;

    for (SpringModelProvider modelProvider : SpringModelProvider.EP_NAME.getExtensionList()) {
      final List<SpringFileSet> list = modelProvider.getFilesets(extension);
      if (list.size() > 0) {
        if (result == null) {
          result = list;
        }
        else {
          result.addAll(list);
        }
      }
    }
    return result == null ? Collections.<SpringFileSet>emptyList() : result;
  }

  @Override
  @Nonnull
  public Set<SpringFileSet> getAllSets(final @Nonnull SpringModuleExtension extension) {
    final Set<SpringFileSet> fileSets = new HashSet<SpringFileSet>(extension.getFileSets());
    final List<SpringFileSet> providedModels = getProvidedModels(extension);
    fileSets.addAll(providedModels);
    return fileSets;
  }
}
