package com.vcredit.framework.logging;

import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaManager;
import org.apache.logging.log4j.core.config.Node;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.layout.SerializedLayout;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

/**
 * Sends log events to an Apache Kafka topic.
 */
@Plugin(name = "VcreditKafkaAppender", category = Node.CATEGORY, elementType = Appender.ELEMENT_TYPE, printObject = true)
public final class VcreditKafkaAppender extends AbstractAppender {

    private final KafkaManager manager;
    private boolean enabled;

    private VcreditKafkaAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                                 final boolean ignoreExceptions, final KafkaManager manager, final Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.manager = Objects.requireNonNull(manager, "manager");
        this.enabled = Stream.of(properties)
                .anyMatch(x -> "enabled".equalsIgnoreCase(x.getName()) && "true".equalsIgnoreCase(x.getValue()));
    }

    /**
     * Creates a builder for a KafkaAppender.
     *
     * @return a builder for a KafkaAppender.
     */
    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }

    @Override
    public void append(final LogEvent event) {
        if (!enabled) {
            return;
        }
        if (event.getLoggerName() != null && event.getLoggerName().startsWith("org.apache.kafka")) {
            LOGGER.warn("Recursive logging from [{}] for appender [{}].", event.getLoggerName(), getName());
        } else {
            try {
                tryAppend(event);
            } catch (final Exception e) {
                error("Unable to write to Kafka in appender [" + getName() + "]", event, e);
            }
        }
    }

    private void tryAppend(final LogEvent event) throws ExecutionException, InterruptedException, TimeoutException {
        final Layout<? extends Serializable> layout = getLayout();
        byte[] data;
        if (layout instanceof SerializedLayout) {
            final byte[] header = layout.getHeader();
            final byte[] body = layout.toByteArray(event);
            data = new byte[header.length + body.length];
            System.arraycopy(header, 0, data, 0, header.length);
            System.arraycopy(body, 0, data, header.length, body.length);
        } else {
            data = layout.toByteArray(event);
        }
        manager.send(data);
    }

    @Override
    public void start() {
        super.start();
        if (!enabled) {
            return;
        }
        try {
            manager.startup();
        } catch (Exception e) {
            enabled = false;
            LOGGER.catching(e);
            LOGGER.error("Cannot output logs to Kafka, will close KafkaAppender!");
        }
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        setStopping();
        boolean stopped = super.stop(timeout, timeUnit, false);
        stopped &= manager.stop(timeout, timeUnit);
        setStopped();
        return stopped;
    }

    @Override
    public String toString() {
        return "KafkaAppender{" +
                "name=" + getName() +
                ", state=" + getState() +
                ", topic=" + manager.getTopic() +
                '}';
    }

    /**
     * Builds KafkaAppender instances.
     *
     * @param <B> The type to build
     */
    public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<VcreditKafkaAppender> {

        @PluginAttribute("topic")
        private String topic;

        @PluginAttribute("key")
        private String key;

        @PluginAttribute(value = "syncSend", defaultBoolean = true)
        private boolean syncSend;

        @SuppressWarnings("resource")
        @Override
        public VcreditKafkaAppender build() {
            final Layout<? extends Serializable> layout = getLayout();
            if (layout == null) {
                AbstractLifeCycle.LOGGER.error("No layout provided for KafkaAppender");
                return null;
            }
            final KafkaManager kafkaManager = new KafkaManager(getConfiguration().getLoggerContext(), getName(), topic,
                    syncSend, getPropertyArray(), key);
            return new VcreditKafkaAppender(getName(), layout, getFilter(), isIgnoreExceptions(), kafkaManager,
                    getPropertyArray());
        }

        public String getTopic() {
            return topic;
        }

        public B setTopic(final String topic) {
            this.topic = topic;
            return asBuilder();
        }

        public boolean isSyncSend() {
            return syncSend;
        }

        public B setSyncSend(final boolean syncSend) {
            this.syncSend = syncSend;
            return asBuilder();
        }

    }
}
