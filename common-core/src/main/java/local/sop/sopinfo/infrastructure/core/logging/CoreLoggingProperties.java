package local.sop.sopinfo.infrastructure.core.logging;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sopinfo.core.logging")
public class CoreLoggingProperties {
    /**
     * Enable core logging auto-configuration
     */
    private boolean enabled = true;

    /**
     * Debug mode for logging configuration
     */
    private boolean debugMode = false;

    /**
     * Console configuration
     */
    private Console console = new Console();

    /**
     * File configuration
     */
    private File file = new File();

    /**
     * Log levels
     */
    private Level level = new Level();

    // Getters and setters...

     public static class Console {
        /**
         * Only show ERROR level in console
         */
        private boolean errorOnly = true;

        public boolean isErrorOnly() {
            return errorOnly;
        }

        public void setErrorOnly(boolean errorOnly) {
            this.errorOnly = errorOnly;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public boolean isColorEnabled() {
            return colorEnabled;
        }

        public void setColorEnabled(boolean colorEnabled) {
            this.colorEnabled = colorEnabled;
        }

        /**
         * Console pattern
         */
        private String pattern = "%d{HH:mm:ss} %red(%highlight(%-5level)) [%logger{20}] %msg%n";

        /**
         * Enable colored output
         */
        private boolean colorEnabled = true;

        // Getters and setters...
        
    }

    public boolean isEnabled() {
        return enabled;
    }

     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }

     public boolean isDebugMode() {
         return debugMode;
     }

     public void setDebugMode(boolean debugMode) {
         this.debugMode = debugMode;
     }

     public Console getConsole() {
         return console;
     }

     public void setConsole(Console console) {
         this.console = console;
     }

     public File getFile() {
         return file;
     }

     public void setFile(File file) {
         this.file = file;
     }

     public Level getLevel() {
         return level;
     }

     public void setLevel(Level level) {
         this.level = level;
     }

    public static class File {
        /**
         * Enable file logging
         */
        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }

        public String getApplicationLog() {
            return applicationLog;
        }

        public void setApplicationLog(String applicationLog) {
            this.applicationLog = applicationLog;
        }

        public String getErrorLog() {
            return errorLog;
        }

        public void setErrorLog(String errorLog) {
            this.errorLog = errorLog;
        }

        public String getPattern() {
            return pattern;
        }

        public void setPattern(String pattern) {
            this.pattern = pattern;
        }

        public String getMaxFileSize() {
            return maxFileSize;
        }

        public void setMaxFileSize(String maxFileSize) {
            this.maxFileSize = maxFileSize;
        }

        public int getMaxHistory() {
            return maxHistory;
        }

        public void setMaxHistory(int maxHistory) {
            this.maxHistory = maxHistory;
        }

        public boolean isUseServiceName() {
            return useServiceName;
        }

        public void setUseServiceName(boolean useServiceName) {
            this.useServiceName = useServiceName;
        }

        /**
         * Base path for logs
         */
        private String basePath = "logs";

        /**
         * Application log file name
         */
        private String applicationLog = "application.log";

        /**
         * Error log file name
         */
        private String errorLog = "error.log";

        /**
         * File pattern
         */
        private String pattern = "%d{yyyy-MM-dd HH:mm:ss} %-5level [%logger{36}] - %msg%n";

        /**
         * Max file size
         */
        private String maxFileSize = "100MB";

        /**
         * Max history
         */
        private int maxHistory = 30;

        /**
         * Include service name in file path
         */
        private boolean useServiceName = true;

        // Getters and setters...
    }

    public static class Level {
        /**
         * Root log level
         */
        private String root = "INFO";

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public String getFramework() {
            return framework;
        }

        public void setFramework(String framework) {
            this.framework = framework;
        }

        public String getApplication() {
            return application;
        }

        public void setApplication(String application) {
            this.application = application;
        }

        public String getExternal() {
            return external;
        }

        public void setExternal(String external) {
            this.external = external;
        }

        /**
         * SOPINFO framework level
         */
        private String framework = "WARN";

        /**
         * Application level
         */
        private String application = "DEBUG";

        /**
         * External dependencies level
         */
        private String external = "WARN";

        // Getters and setters...
    }
}
