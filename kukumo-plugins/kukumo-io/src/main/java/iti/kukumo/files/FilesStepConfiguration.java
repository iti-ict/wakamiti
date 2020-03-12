package iti.kukumo.files;

public class FilesStepConfiguration {

    public static String FILES_ACCESS_TIMEOUT = "files.timeout";
    public static String FILES_LINKS = "files.links";
    public static String FILES_ENABLE_CLEANUP_UPON_COMPLETION = "files.enableCleanupUponCompletion";

    private FilesStepConfiguration() {
        /* avoid instantiation */
    }

    public static class Defaults {

        public static Long DEFAULT_FILES_ACCESS_TIMEOUT = 60L;
        public static boolean DEFAULT_FILES_ENABLE_CLEANUP_UPON_COMPLETION = false;

        private Defaults() {
            /* avoid instantiation */
        }
    }
}
