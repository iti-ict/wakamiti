/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.database;


/** This class can be used to read sql files into an array of Strings, each * representing a single query terminated by ";" * Comments are filtered out. */
/* Based on code from https://coderanch.com/t/306966/databases/Execute-sql-file-java */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/* ATTENTION: SQL file must not contain column names, etc. including comment signs (#, --, /* etc.) *          like e.g. a.'#rows' etc. because every characters after # or -- in a line are filtered *          out of the query string the same is true for every characters surrounded by /* and */
/**/

public class SQLReader {

    private static final List<String> LINE_COMMENT_SYMBOLS = Arrays.asList("#", "--");

    private static UnaryOperator<String> removeEndOfLine = line -> line.replaceAll("(\\r|\\n)", "");


    private String filterLineComments(String line) {
        for (String commentSymbol : LINE_COMMENT_SYMBOLS) {
            int indexOfCommentSign = line.indexOf(commentSymbol);
            if (indexOfCommentSign != -1) {
                if (line.startsWith(commentSymbol)) {
                    line = "";
                } else {
                    line = line.substring(0, indexOfCommentSign - 1);
                }
            }
        }
        return line;
    }


    private String filterMultilineComments(String line, AtomicBoolean withinComment) {
        if (!withinComment.get()) {
            int indexOfOpenCommentSymbol = line.indexOf("/*");
            if (indexOfOpenCommentSymbol != -1) {
                withinComment.set(true);
                line = line.substring(0, indexOfOpenCommentSymbol - 1) +
                                filterMultilineComments(line.substring(indexOfOpenCommentSymbol), withinComment);

            }
        } else {
            int indexOfCloseCommentSymbol = line.indexOf("*/");
            if (indexOfCloseCommentSymbol != -1) {
                withinComment.set(false);
                line = filterMultilineComments(
                    line.substring(indexOfCloseCommentSymbol + 2),
                    withinComment
                );
            } else {
                line = "";
            }
        }
        return line;
    }


    private String readWithoutComments(Reader reader) throws IOException {
        StringBuilder script = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = null;
            AtomicBoolean withinComment = new AtomicBoolean(false);
            while ((line = bufferedReader.readLine()) != null) {
                if (!withinComment.get()) {
                    line = filterLineComments(line);
                }
                line = filterMultilineComments(line, withinComment);
                line = line.trim();
                if (!line.isEmpty()) {
                    script.append(line).append(" ");
                }
            }
            return script.toString();
        }
    }


    public List<String> parseStatements(Reader reader) throws IOException {
        return Stream.of(readWithoutComments(reader).split(";"))
            .map(removeEndOfLine)
            .map(String::trim)
            .filter(statement -> !statement.isEmpty())
            .collect(Collectors.toList());
    }

}