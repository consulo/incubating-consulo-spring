package consulo.spring.module.extension;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.*;

import com.intellij.spring.facet.SpringConfigurationTab;
import consulo.disposer.Disposable;
import consulo.module.extension.swing.SwingMutableModuleExtension;
import consulo.roots.ModuleRootLayer;
import consulo.ui.Component;
import consulo.ui.Label;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.layout.VerticalLayout;

/**
 * @author VISTALL
 * @since 14-Jan-17
 */
public class SpringMutableModuleExtensionImpl extends SpringModuleExtensionImpl implements SpringMutableModuleExtension, SwingMutableModuleExtension
{
	public SpringMutableModuleExtensionImpl(@Nonnull String id, @Nonnull ModuleRootLayer moduleRootLayer)
	{
		super(id, moduleRootLayer);
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public JComponent createConfigurablePanel(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		SpringConfigurationTab tab = new SpringConfigurationTab(this);
		return tab.createComponent();
	}

	@RequiredUIAccess
	@Nullable
	@Override
	public Component createConfigurationComponent(@Nonnull Disposable disposable, @Nonnull Runnable runnable)
	{
		return VerticalLayout.create().add(Label.create("Unsupported platform"));
	}

	@Override
	public void setEnabled(boolean b)
	{
		myIsEnabled = b;
	}

	@Override
	public boolean isModified(@Nonnull SpringModuleExtension springModuleExtension)
	{
		return myIsEnabled != springModuleExtension.isEnabled() || !myFileSets.equals(springModuleExtension.getFileSets());
	}
}
