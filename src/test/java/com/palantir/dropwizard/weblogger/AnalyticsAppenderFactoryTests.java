/*
 * Copyright 2015 Palantir Technologies, Inc. All rights reserved.
 */

package com.palantir.dropwizard.weblogger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterReply;
import io.dropwizard.logging.DropwizardLayout;
import java.io.File;
import java.util.TimeZone;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.LoggerFactory;

/**
 * Tests for {@link AnalyticsAppenderFactory}.
 */
public final class AnalyticsAppenderFactoryTests {

    @Test
    public void testAnalyticsFilterPositive() {
        ILoggingEvent loggingEvent = Mockito.mock(ILoggingEvent.class);
        Mockito.when(loggingEvent.getLoggerName()).thenReturn("analytics");
        AnalyticsAppenderFactory.AnalyticsFilter analyticsFilter = new AnalyticsAppenderFactory.AnalyticsFilter();
        assertEquals(FilterReply.ACCEPT, analyticsFilter.decide(loggingEvent));
    }

    @Test
    public void testAnalyticsFilterNegative() {
        ILoggingEvent loggingEvent = Mockito.mock(ILoggingEvent.class);
        Mockito.when(loggingEvent.getLoggerName()).thenReturn("server.log");
        AnalyticsAppenderFactory.AnalyticsFilter analyticsFilter = new AnalyticsAppenderFactory.AnalyticsFilter();
        assertEquals(FilterReply.DENY, analyticsFilter.decide(loggingEvent));
    }

    @Test
    public void testBuild() {
        File file = new File("usage.json.log");
        try {
            Logger logger = (Logger) LoggerFactory.getLogger("analytics");
            Layout<ILoggingEvent> layout = Mockito.mock(Layout.class);
            AnalyticsAppenderFactory analyticsAppenderFactory = new AnalyticsAppenderFactory();
            analyticsAppenderFactory.setCurrentLogFilename(file.getAbsolutePath());
            analyticsAppenderFactory.setArchivedLogFilenamePattern("usage-%d.json.log");
            Appender<ILoggingEvent> appender = analyticsAppenderFactory.build(logger.getLoggerContext(),
                    "applicationName", layout);
            assertEquals("async-web-logger", appender.getName());
        } finally {
            if (file.exists()) {
                assertTrue(file.delete());
            }
        }
    }

    @Test
    public void testBuildLayout() {
        LoggerContext loggerContext = Mockito.mock(LoggerContext.class);
        TimeZone timeZone = Mockito.mock(TimeZone.class);
        Mockito.when(timeZone.getID()).thenReturn("UTC");
        AnalyticsAppenderFactory analyticsAppenderFactory = new AnalyticsAppenderFactory();
        DropwizardLayout layout = analyticsAppenderFactory.buildLayout(loggerContext, timeZone);
        String expectedPattern = "%m%n";
        assertEquals(expectedPattern, layout.getPattern());
    }
}
