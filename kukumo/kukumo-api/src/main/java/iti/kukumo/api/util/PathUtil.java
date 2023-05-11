package iti.kukumo.api.util;

import iti.kukumo.api.plan.PlanNode;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

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


    public static Path replacePlaceholders(Path path, PlanNode planNode) {
        var instant = planNode.startInstant().orElseGet(Instant::now).atZone(ZoneId.systemDefault());
        var executionID = Objects.requireNonNullElse(planNode.executionID(),"");
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
        pathString = pathString.replace("%execID%", executionID);
        return Path.of(pathString);
    }

    public static Path replaceTemporalPlaceholders(Path path) {
        var instant = Instant.now().atZone(ZoneId.systemDefault());
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
