package com.vcredit.framework.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.plugins.*;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.layout.PatternSelector;
import org.apache.logging.log4j.core.pattern.RegexReplacement;
import org.apache.logging.log4j.util.Strings;

import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author guoxiaolong
 * @date 2019/1/25
 */
@Plugin(name = "JsonPatternLayout", category = Node.CATEGORY, elementType = Layout.ELEMENT_TYPE, printObject = true)
public class JsonPatternLayout extends AbstractStringLayout {
    private PatternLayout patternLayout;

    private String projectName;
    private String serviceName;
    private String profile;
    private String ip;

    private JsonPatternLayout(Configuration config, RegexReplacement replace, String eventPattern,
                              PatternSelector patternSelector, Charset charset, boolean alwaysWriteExceptions,
                              boolean noConsoleNoAnsi, String headerPattern, String footerPattern,
                              String projectName, String ip, String serviceName, String profile) {
        super(config, charset,
                PatternLayout.newSerializerBuilder()
                        .setAlwaysWriteExceptions(alwaysWriteExceptions)
                        .setNoConsoleNoAnsi(noConsoleNoAnsi)
                        .setConfiguration(config)
                        .setReplace(replace)
                        .setPatternSelector(patternSelector)
                        .setPattern(headerPattern)
                        .setPattern(footerPattern)
                        .build(),
                PatternLayout.newSerializerBuilder()
                        .setAlwaysWriteExceptions(alwaysWriteExceptions)
                        .setNoConsoleNoAnsi(noConsoleNoAnsi)
                        .setConfiguration(config)
                        .setReplace(replace)
                        .setPatternSelector(patternSelector)
                        .setPattern(headerPattern)
                        .setPattern(footerPattern)
                        .build()
        );

        this.projectName = projectName;
        this.serviceName = serviceName;
        this.profile = profile;
        this.ip = ip;
        this.patternLayout = PatternLayout.newBuilder()
                .withPattern(eventPattern)
                .withPatternSelector(patternSelector)
                .withConfiguration(config)
                .withRegexReplacement(replace)
                .withCharset(charset)
                .withAlwaysWriteExceptions(alwaysWriteExceptions)
                .withNoConsoleNoAnsi(noConsoleNoAnsi)
                .withHeader(headerPattern)
                .withFooter(footerPattern)
                .build();
    }

    @Override
    public String toSerializable(LogEvent logEvent) {
        String message = this.patternLayout.toSerializable(logEvent);
        String timestamp = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").format(
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(logEvent.getTimeMillis()),
                        ZoneId.systemDefault()));
        String jsonStr = new JsonLoggerInfo(
                this.projectName, message, logEvent.getLevel().name(),
                timestamp, this.serviceName,
                this.profile, this.ip, "", "", "", "",
                logEvent.getThreadId(), logEvent.getThreadName(), logEvent.getThreadPriority(), logEvent.getLoggerName()
        ).toString();
        return jsonStr + "\n";
    }

    @PluginFactory
    public static JsonPatternLayout createLayout(
            @PluginAttribute(value = "pattern", defaultString = PatternLayout.DEFAULT_CONVERSION_PATTERN) final String pattern,
            @PluginElement("PatternSelector") final PatternSelector patternSelector,
            @PluginConfiguration final Configuration config,
            @PluginElement("Replace") final RegexReplacement replace,
            @PluginAttribute(value = "charset") final Charset charset,
            @PluginAttribute(value = "alwaysWriteExceptions", defaultBoolean = true) final boolean alwaysWriteExceptions,
            @PluginAttribute(value = "noConsoleNoAnsi") final boolean noConsoleNoAnsi,
            @PluginAttribute("header") final String headerPattern,
            @PluginAttribute("footer") final String footerPattern,
            @PluginAttribute("projectName") final String projectName,
            @PluginAttribute("ip") final String ip,
            @PluginAttribute("serviceName") final String serviceName,
            @PluginAttribute("profile") final String profile) {


        return new JsonPatternLayout(config, replace, pattern, patternSelector, charset,
                alwaysWriteExceptions, noConsoleNoAnsi, headerPattern, footerPattern, projectName, ip, serviceName, profile);
    }

    static class JsonLoggerInfo {
        /**
         * 日志时间
         */
        private String timestamp;
        /**
         * 项目名
         */
        private String project;
        /**
         * 服务名
         */
        private String service;
        /**
         * 项目的profile
         */
        private String profile;
        /**
         * 主机ip
         */
        private String ip;
        /**
         * 日志级别
         */
        private String severity;
        /**
         * 当前的线程的id
         */
        private long pid;
        /**
         * 当前线程名
         */
        private String thread;
        /**
         * 当前线程名
         */
        private int threadPriority;
        /**
         * trace id
         */
        private String trace;
        /**
         * span id
         */
        private String span;
        /**
         *
         */
        private String parent;
        /**
         *
         */
        private String exportable;
        /**
         * 当前的类名
         */
        private String className;
        /**
         * 日志信息
         */
        private String msg;

        JsonLoggerInfo(String project, String msg, String severity,
                       String timestamp, String service, String profile, String ip,
                       String trace, String span, String parent, String exportable,
                       long pid, String thread, int threadPriority, String className
        ) {
            this.project = project;
            this.service = service;
            this.profile = profile;
            this.ip = ip;
            this.trace = trace;
            this.span = span;
            this.parent = parent;
            this.exportable = exportable;
            this.pid = pid;
            this.thread = thread;
            this.className = className;
            this.threadPriority = threadPriority;
            this.msg = msg;
            this.severity = severity;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            try {
                return new ObjectMapper().writeValueAsString(this);
            } catch (JsonProcessingException e) {
                System.err.println("Error when JSON serializing：" + e.getMessage());
            }
            return Strings.EMPTY;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public String getProject() {
            return project;
        }

        public String getService() {
            return service;
        }

        public String getProfile() {
            return profile;
        }

        public String getIp() {
            return ip;
        }

        public String getSeverity() {
            return severity;
        }

        public long getPid() {
            return pid;
        }

        public String getThread() {
            return thread;
        }

        public int getThreadPriority() {
            return threadPriority;
        }

        public String getTrace() {
            return trace;
        }

        public String getSpan() {
            return span;
        }

        public String getParent() {
            return parent;
        }

        public String getExportable() {
            return exportable;
        }

        public String getClassName() {
            return className;
        }

        public String getMsg() {
            return msg;
        }
    }
}
