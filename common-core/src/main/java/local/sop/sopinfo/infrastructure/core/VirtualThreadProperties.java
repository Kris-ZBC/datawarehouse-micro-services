package local.sop.sopinfo.infrastructure.core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "common.threads.virtual")
    public class VirtualThreadProperties {
        private boolean enabled = false;
        private int tomcatThreadsMax = 200;
        private int maxConnections = 10000;
        private int connectionTimeout = 20000;
        
        // getters/setters
        // --- GETTERS ---
        public boolean isEnabled() {
            return enabled;
        }
        
        public int getTomcatThreadsMax() {
            return tomcatThreadsMax;
        }
        
        public int getMaxConnections() {
            return maxConnections;
        }
        
        public int getConnectionTimeout() {
            return connectionTimeout;
        }
        
        // --- SETTERS ---
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public void setTomcatThreadsMax(int tomcatThreadsMax) {
            this.tomcatThreadsMax = tomcatThreadsMax;
        }
        
        public void setMaxConnections(int maxConnections) {
            this.maxConnections = maxConnections;
        }
        
        public void setConnectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
    }