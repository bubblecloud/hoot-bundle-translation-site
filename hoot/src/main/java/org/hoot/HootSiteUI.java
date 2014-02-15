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

import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.vaadin.addons.sitekit.dao.CompanyDao;
import org.vaadin.addons.sitekit.model.Company;
import org.vaadin.addons.sitekit.site.*;
import org.vaadin.addons.sitekit.util.PersistenceUtil;
import org.vaadin.addons.sitekit.util.PropertiesUtil;
import org.vaadin.addons.sitekit.viewlet.administrator.company.CompanyFlowViewlet;
import org.vaadin.addons.sitekit.viewlet.administrator.customer.CustomerFlowViewlet;
import org.vaadin.addons.sitekit.viewlet.administrator.group.GroupFlowViewlet;
import org.vaadin.addons.sitekit.viewlet.administrator.user.UserFlowViewlet;
import org.vaadin.addons.sitekit.viewlet.anonymous.CompanyFooterViewlet;
import org.vaadin.addons.sitekit.viewlet.anonymous.EmailValidationViewlet;
import org.vaadin.addons.sitekit.viewlet.anonymous.ImageViewlet;
import org.vaadin.addons.sitekit.viewlet.anonymous.VerticalNavigationViewlet;
import org.vaadin.addons.sitekit.viewlet.anonymous.login.LoginFlowViewlet;
import org.vaadin.addons.sitekit.viewlet.user.AccountFlowViewlet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * BareSite UI.
 *
 * @author Tommi S.E. Laukkanen
 */
@SuppressWarnings({ "serial", "unchecked" })
@Theme("sitekit")
public final class HootSiteUI extends AbstractSiteUI implements ContentProvider {

    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger(HootSiteUI.class);
    /** The properties category used in instantiating default services. */
    private static final String PROPERTIES_CATEGORY = "hoot";
    /** The persistence unit to be used. */
    public static final String PERSISTENCE_UNIT = "hoot";

    /**
     * Main method for running HootSiteUI.
     * @param args the commandline arguments
     * @throws Exception if exception occurs in jetty startup.
     */
    public static void main(final String[] args) throws Exception {
        PropertiesUtil.setCategoryRedirection("site", PROPERTIES_CATEGORY);
        DOMConfigurator.configure("./log4j.xml");

        entityManagerFactory = PersistenceUtil.getEntityManagerFactory(PERSISTENCE_UNIT, PROPERTIES_CATEGORY);

        final String webappUrl = HootSiteUI.class.getClassLoader()
                .getResource("webapp/").toExternalForm();

        LOGGER.info("SMTP host: " + PropertiesUtil.getProperty(PROPERTIES_CATEGORY, "smtp-host"));

        final int port = Integer.parseInt(PropertiesUtil.getProperty(PROPERTIES_CATEGORY, "port"));
        final Server server = new Server(port);

        final WebAppContext context = new WebAppContext();
        context.setContextPath("/");
        context.setDescriptor(webappUrl + "/WEB-INF/web.xml");
        context.setResourceBase(webappUrl);
        context.setParentLoaderPriority(true);
        context.getSessionHandler().getSessionManager().setSessionCookie("HOOTSESSIONID");

        server.setHandler(context);
        server.start();

        final HootSynchronizer translationSynchronizer = new HootSynchronizer(
                entityManagerFactory.createEntityManager());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    translationSynchronizer.shutdown();
                } catch (final Throwable t) {
                    LOGGER.error("Error in synchronizer shutdown.", t);
                }
            }
        });

        server.join();
    }

    @Override
    protected Site constructSite(final VaadinRequest request) {

        final ContentProvider contentProvider = this;

        final LocalizationProvider localizationProvider =
                new LocalizationProviderBundleImpl(new String[] {"hoot-localization",
                        "bare-site-localization"});
        SiteFields.initialize(localizationProvider);
        HootFields.initialize(localizationProvider, getLocale());

        final SiteContext siteContext = new SiteContext();
        final EntityManager entityManager = entityManagerFactory.createEntityManager();
        siteContext.putObject(EntityManager.class, entityManager);

        Company company = CompanyDao.getCompany(entityManager,
                ((VaadinServletRequest) VaadinService.getCurrentRequest()).getHttpServletRequest().getServerName());
        if (company == null) {
            // If no exact host match exists then try to find global company marked with *.
            company = CompanyDao.getCompany(entityManager, "*");
        }
        siteContext.putObject(Company.class, company);

        final SecurityProviderSessionImpl securityProvider = new SecurityProviderSessionImpl(
                Arrays.asList("administrator", "translator", "user"));

        return new Site(SiteMode.PRODUCTION, contentProvider, localizationProvider, securityProvider, siteContext);
    }

    @Override
    public SiteDescriptor getSiteDescriptor() {
        final List<ViewDescriptor> viewDescriptors = new ArrayList<ViewDescriptor>();

        viewDescriptors.add(new ViewDescriptor("master", null, null, new ViewVersion(0, null, "Master", "",
                "This is a master view.", FixedWidthView.class.getCanonicalName(), new String[]{"admin"},
                Arrays.asList(
                        new ViewletDescriptor("logo", "Logo", "This is logo.", "logo.png",
                                ImageViewlet.class.getCanonicalName()),
                        new ViewletDescriptor("header", "Header", "This is header.", null,
                                ImageViewlet.class.getCanonicalName()),
                        new ViewletDescriptor("navigation", "NavigationDescriptor", "This is navigation.", null,
                                VerticalNavigationViewlet.class.getCanonicalName()),
                        new ViewletDescriptor("footer", "Footer", "This is footer.", null,
                                CompanyFooterViewlet.class.getCanonicalName())
                ))));

        viewDescriptors.add(new ViewDescriptor("dashboard", null, null, new ViewVersion(0, "master", "Dashboard", "",
                "This is entries view.", HootView.class.getCanonicalName(), new String[]{"translator", "administrator"},
                Arrays.asList(new ViewletDescriptor(
                        "content", "Entries Viewlet", "This is Entries viewlet.", null,
                        EntryFlowViewlet.class.getCanonicalName())
                ))));

        viewDescriptors.add(new ViewDescriptor("users", null, null, new ViewVersion(
                0, "master", "Users", "", "This is users page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"administrator"},
                Arrays.asList(new ViewletDescriptor(
                        "content", "Flowlet Sheet", "This is flow sheet.", null,
                        UserFlowViewlet.class.getCanonicalName())
                ))));
        viewDescriptors.add(new ViewDescriptor("groups", null, null, new ViewVersion(
                0, "master", "Groups", "", "This is groups page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"administrator"},
                Arrays.asList(new ViewletDescriptor(
                        "content", "Flowlet Sheet", "This is flow sheet.", null,
                        GroupFlowViewlet.class.getCanonicalName())
                ))));
        viewDescriptors.add(new ViewDescriptor("customers", null, null, new ViewVersion(
                0, "master", "Customers", "customers", "This is customers page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"administrator"},
                Arrays.asList(new ViewletDescriptor(
                        "content", "Flowlet Sheet", "This is flow sheet.", null,
                        CustomerFlowViewlet.class.getCanonicalName())
                ))));
        viewDescriptors.add(new ViewDescriptor("companies", null, null, new ViewVersion(
                0, "master", "Companies", "companies", "This is companies page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"administrator"},
                Arrays.asList(new ViewletDescriptor(
                        "content", "Flowlet Sheet", "This is flow sheet.", null,
                        CompanyFlowViewlet.class.getCanonicalName())
                ))));

        viewDescriptors.add(new ViewDescriptor("login", null, null, new ViewVersion(
                0, "master", "Login SiteView", "login page", "This is login page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"anonymous"},
                Arrays.asList(
                        new ViewletDescriptor(
                                "content", "Flowlet Sheet", "This is flow sheet.", null,
                                LoginFlowViewlet.class.getCanonicalName())
                ))));
        viewDescriptors.add(new ViewDescriptor("validate", null, null, new ViewVersion(
                0, "master", "Email Validation", "email validation page", "This is email validation page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"anonymous"},
                Arrays.asList(
                        new ViewletDescriptor(
                                "content", "Email Validation", "This is email validation flowlet.", null,
                                EmailValidationViewlet.class.getCanonicalName())
                ))));
        viewDescriptors.add(new ViewDescriptor("account", null, null, new ViewVersion(
                0, "master", "Groom / Account", "account page", "This is account page.",
                FixedWidthView.class.getCanonicalName(), new String[]{"user"},
                Arrays.asList(
                        new ViewletDescriptor(
                                "content", "Flowlet Sheet", "This is flow sheet.", null,
                                AccountFlowViewlet.class.getCanonicalName())
                ))));


        final NavigationDescriptor navigationDescriptor = new NavigationDescriptor("navigation", null, null,
                new NavigationVersion(0, "dashboard", "dashboard;account;users;groups;customers;companies;login", true));

        return new SiteDescriptor("Test site.", "test site", "This is a test site.",
                navigationDescriptor, viewDescriptors);

    }

    /** The entity manager factory for test. */
    private static EntityManagerFactory entityManagerFactory;

}
