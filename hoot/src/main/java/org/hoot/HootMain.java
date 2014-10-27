/**
 * Copyright 2013 Tommi S.E. Laukkanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hoot;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.vaadin.addons.sitekit.jetty.DefaultJettyConfiguration;
import org.vaadin.addons.sitekit.site.*;
import org.vaadin.addons.sitekit.util.PropertiesUtil;

/**
 * Seed main class.
 *
 * @author Tommi S.E. Laukkanen
 */
public class HootMain {
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(HootMain.class);
    /** The persistence unit to be used. */
    public static final String PERSISTENCE_UNIT = "hoot";
    /** The localization bundle. */
    public static final String LOCALIZATION_BUNDLE = "hoot-localization";

    /**
     * Main method for tutorial site.
     *
     * @param args the commandline arguments
     * @throws Exception if exception occurs in jetty startup.
     */
    public static void main(final String[] args) throws Exception {
        PropertiesUtil.setCategoryRedirection("site", "hoot");

        // The default Jetty server configuration.
        final Server server = DefaultJettyConfiguration.configureServer(PERSISTENCE_UNIT, LOCALIZATION_BUNDLE);

        // Get default site descriptor.
        final SiteDescriptor siteDescriptor = DefaultSiteUI.getContentProvider().getSiteDescriptor();

        HootFields.initialize();

        final String dashboardPage = "dashboard";
        // Describe custom view.
        final ViewDescriptor commentView = new ViewDescriptor(dashboardPage, DefaultView.class);
        commentView.setViewerRoles("administrator");
        siteDescriptor.getViewDescriptors().add(commentView);
        // Place example viewlet to content slot in the view.
        commentView.setViewletClass(Slot.CONTENT, EntryFlowViewlet.class);

        // Add custom view to navigation.
        final NavigationVersion navigationVersion = siteDescriptor.getNavigationVersion();
        navigationVersion.setDefaultPageName(dashboardPage);
        navigationVersion.addRootPage(0, dashboardPage);

        // Start server.
        server.start();

        final HootSynchronizer translationSynchronizer = new HootSynchronizer(
                DefaultSiteUI.getEntityManagerFactory().createEntityManager());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    translationSynchronizer.shutdown();
                } catch (final Throwable t) {
                    LOGGER.error("Error in synchronizer shutdown.", t);
                }
            }
        });

        // Join this main thread to the Jetty server thread.
        server.join();
    }
}