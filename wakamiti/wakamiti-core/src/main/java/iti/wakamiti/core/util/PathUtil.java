package iti.wakamiti.core.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PathUtil {

    private static final DateTimeFormatter YEAR_4 = DateTimeFormatter.ofPattern("yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter YEAR_2 = DateTimeFormatter.ofPattern("yy", Locale.ENGLISH);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MM", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("dd", Locale.ENGLISH);
    private static final DateTimeFormatter HOUR = DateTimeFormatter.ofPattern("HH", Locale.ENGLISH);
    private static final DateTimeFormatter MINUTE = DateTimeFormatter.ofPattern("mm", Locale.ENGLISH);
    private static final DateTimeFormatter SEC = DateTimeFormatter.ofPattern("ss", Locale.ENGLISH);
    private static final DateTimeFormatter MILLISEC = DateTimeFormatter.ofPattern("SSS", Locale.ENGLISH);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ENGLISH);
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HHmmssSSS", Locale.ENGLISH);


    public static Path replaceTemporalPlaceholders(Path path, LocalDateTime instant) {
        String pathString = path.toString();
        pathString = pathString.replace("%YYYY%", YEAR_4.format(instant));
        pathString = pathString.replace("%YY%",YEAR_2.format(instant));
        pathString = pathString.replace("%MM%",MONTH.format(instant));
        pathString = pathString.replace("%DD%",DAY.format(instant));
        pathString = pathString.replace("%hh%",HOUR.format(instant));
        pathString = pathString.replace("%mm%",MINUTE.format(instant));
        pathString = pathString.replace("%ss%",SEC.format(instant));
        pathString = pathString.replace("%sss%",MILLISEC.format(instant));
        pathString = pathString.replace("%DATE%", DATE.format(instant));
        pathString = pathString.replace("%TIME%", TIME.format(instant));
        return Path.of(pathString);
    }

}
