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
package org.apache.isis.core.webapp;

import static org.apache.isis.commons.internal.base._With.acceptIfPresent;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Introduced to render web.xml listener configurations obsolete.
 * <p> 
 * Acts as the single application entry-point when running within a Servlet context.
 * Delegates the bootstrapping to appropriate 'bootstrappers' that are discovered 
 * on the class-path. 
 * </p>   
 *  
 * @since 2.0.0
 *
 */
@WebListener
public class IsisWebAppContextListener implements ServletContextListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(IsisWebAppContextListener.class);
    
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    // -- IMPLEMENTATION
    
    @Override
    public void contextInitialized(ServletContextEvent event) {
        
        final ServletContext context = event.getServletContext();
        
        WebModule.discoverWebModules()
        .filter(module->module.isAvailable(context))
        .forEach(module->addListener(context, module));
        
        activeListeners.forEach(listener->listener.contextInitialized(event));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        activeListeners.forEach(listener->shutdownListener(event, listener));
        activeListeners.clear();
    }
    
    // -- HELPER
    
    private void addListener(ServletContext context, WebModule provider) {
        LOG.info(String.format("ServletContext: adding '%s'", provider.getName()));
        try {
            acceptIfPresent(provider.init(context), activeListeners::add);
        } catch (ServletException e) {
            LOG.error(String.format("Failed to add '%s' to the ServletContext.", provider.getName()), e);
        }  
    }
    
    private void shutdownListener(ServletContextEvent event, ServletContextListener listener) {
        try {
            listener.contextDestroyed(event);
        } catch (Exception e) {
            LOG.error(String.format("Failed to shutdown WebListener '%s'.", listener.getClass().getName()), e);
        }
    }
    

}