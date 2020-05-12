package com.jy.yim;

public final class YIMConfig {

    public final long maxFreeTime;
    public final String ip;
    public final int port;
    public final long reconnectionTime;
    public final int connectTimeout;

    public YIMConfig(final Builder builder) {
        ip = builder.ip;
        port = builder.port;
        maxFreeTime = builder.maxFreeTime;
        connectTimeout = builder.connectTimeout;
        reconnectionTime = builder.reconnectionTime;
    }

    public static Builder beginBuilder() {
        return new Builder();
    }

    public static class Builder {
        private static final int DEFAULT_MAX_FREE_TIME = 20 * 1000; //默认最大空闲时间
        private static final int DEFAULT_RECONNECTION_TIME = 5 * 1000; //默认重连时间
        private static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000; //默认连接超时时间

        private long maxFreeTime = DEFAULT_MAX_FREE_TIME;
        private long reconnectionTime = DEFAULT_RECONNECTION_TIME;
        private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

        private String ip;
        private int port;


        public Builder setIp(String ip) {
            this.ip = ip;
            return this;
        }

        public Builder setPort(int port) {
            this.port = port;
            return this;
        }

        public Builder setMaxFreeTime(long maxFreeTime) {
            this.maxFreeTime = maxFreeTime;
            return this;
        }

        public Builder setConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder setReconnectionTime(long reconnectionTime) {
            this.reconnectionTime = reconnectionTime;
            return this;
        }

        public YIMConfig build() {
            return new YIMConfig(this);
        }
    }
}
