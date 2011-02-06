package de.tud.stg.tigerseye.eclipse.core;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class TigerseyeCoreActivator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "de.tud.stg.tigerseye.eclipse.core"; //$NON-NLS-1$ // NO_UCD

    // The shared instance
    private static TigerseyeCoreActivator plugin;

    /**
     * The constructor
     */
    public TigerseyeCoreActivator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void start(BundleContext context) throws Exception {
	super.start(context);
	plugin = this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
	plugin = null;
	super.stop(context);
    }

    /**
     * @param imageName
     * @return predefined icon from the default image store.
     */
    public static ImageDescriptor getIcon(TigerseyeImage imageName) {
        String imagePath = "/icons/" + imageName.imageName;
        return getImageDescriptor(imagePath);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static TigerseyeCoreActivator getDefault() {
	return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
	return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
