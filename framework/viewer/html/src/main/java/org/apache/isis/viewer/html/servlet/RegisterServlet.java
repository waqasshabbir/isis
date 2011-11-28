/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.viewer.html.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.isis.core.runtime.authentication.standard.RegistrationDetailsPassword;
import org.apache.isis.runtimes.dflt.monitoring.servermonitor.Monitor;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategy;
import org.apache.isis.runtimes.dflt.webapp.auth.AuthenticationSessionLookupStrategyUtils;
import org.apache.isis.viewer.html.component.html.HtmlComponentFactory;
import org.apache.isis.viewer.html.component.html.LogonFormPage;
import org.apache.isis.viewer.html.component.html.RegisterFormPage;
import org.apache.log4j.Logger;

public class RegisterServlet extends AbstractHtmlViewerServlet {
    
    private static final long serialVersionUID = 1L;
    
    private static final Logger LOG = Logger.getLogger(RegisterServlet.class);
    
    private AuthenticationSessionLookupStrategy authenticationSessionLookupStrategy;

    @Override
    public void init() throws ServletException {
        authenticationSessionLookupStrategy = AuthenticationSessionLookupStrategyUtils.lookup(getServletConfig());
    }

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
        IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
        throws ServletException, IOException {

        // prompt
        final String user = request.getParameter("username");
        final String password = request.getParameter("password");
        final String password2 = request.getParameter("password2");
        if (user == null) {
            renderPrompt(response, "", "", "", "");
            return;
        }

        // register; re-prompt if required
        final boolean registered = register(user, password, password2);
        if (!registered) {
            renderPrompt(response, user, password, password2, "error");
            return;
        }

        // registered
        redirectToLogonPage(response, user);
    }

    private void redirectToLogonPage(final HttpServletResponse response, final String user) throws IOException {
        Monitor.addEvent("Web", "Logon - " + user);
        response.sendRedirect(pathTo(HtmlServletConstants.LOGON_PAGE));
    }

    private void renderPrompt(
            final HttpServletResponse response, 
            final String user, final String password, final String password2, 
            final String message) throws IOException {
        response.setContentType("text/html");
        final HtmlComponentFactory factory = new HtmlComponentFactory(getPathBuilder());
        final RegisterFormPage page = factory.createRegisterPage(user, password);
        page.write(response.getWriter());
    }

    private boolean register(final String user, final String password, final String password2) {
        return getAuthenticationManager().register(new RegistrationDetailsPassword(user, password));
    }


}
