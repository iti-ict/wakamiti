package iti.kukumo.core.runner;

import iti.commons.configurer.Configuration;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static iti.kukumo.util.KukumoLogger.resultColor;

/**
 * @author ITI
 * Created by ITI on 26/08/19
 */
public class PlanNodeLogger {

    private final boolean showStepSource;
    private final boolean showElapsedTime;
    private final Logger logger;

    private long totalNumberTestCases;
    private long currentTestCaseNumber;

    public PlanNodeLogger(Logger logger, Configuration configuration, long totalNumberTestCases) {
        this.logger = logger;
        this.showStepSource = configuration
           .get(KukumoConfiguration.LOGS_SHOW_STEP_SOURCE,Boolean.class)
           .orElse(true);
        this.showElapsedTime = configuration
           .get(KukumoConfiguration.LOGS_SHOW_ELAPSED_TIME,Boolean.class)
           .orElse(true);
        this.totalNumberTestCases = totalNumberTestCases;
    }


    public void logTestPlanHeader(PlanNode plan) {
        if (logger.isInfoEnabled()) {
            logger.info("{message}","Running Test Plan with "+plan.numTestCases()+" Test Cases...");
        }
    }

    public void logTestPlanResult(PlanNode plan) {
        if (logger.isInfoEnabled()) {
            Result result = plan.computeResult().orElse(Result.ERROR);
            String extraInfo = result.isPassed() ? "" : "  ({} of {} test cases not passed)";
            logger.info("@|bold,"+resultColor(result)+" Test Plan {}"+extraInfo+"|@",
                result,
                plan.numTestCases() - plan.numTestCassesPassed(),
                plan.numTestCases()
            );
        }
    }



    public void logTestCaseHeader(PlanNode node) {
        currentTestCaseNumber++;
        if (logger.isInfoEnabled()) {
            StringJoiner name = new StringJoiner(" : ");
            if (node.keyword() != null) {
                name.add(node.keyword());
            }
            name.add(node.name());
            logger.info("@|bold,white {}|@", StringUtils.repeat("-",name.length()+4));
            logger.info("@|bold,white | {} ||@  (Test Case {}/{})", name, currentTestCaseNumber, totalNumberTestCases);
            logger.info("@|bold,white {}|@", StringUtils.repeat("-",name.length()+4));
        }
    }

    public void logStepResult(PlanStep step) {
        if (step.isVoid()) {
            return;
        }
        logger.info(buildMessage(step),buildMessageArgs(step));
        step.getError().ifPresent(error->logger.debug("stack trace:", error));
    }


    private String buildMessage(PlanStep step) {
        String messageColor = resultColor(step.getResult());
        StringBuilder message = new StringBuilder();
        message.append("@|bold,white [|@@|bold,"+messageColor+" {}|@@|bold,white ]|@ ");
        if (showStepSource) {
            message.append("@|faint {}|@ :");
        }
        message.append(" @|bold,cyan {}|@ {}");
        if (showElapsedTime) {
            message.append(" @|faint {}|@ ");
        }
        message.append("@|"+messageColor+" {}|@");
        return message.toString();
    }


    private Object[] buildMessageArgs(PlanStep step) {
        List<Object> args = new ArrayList<>();
        args.add(step.getResult());
        if (showStepSource) {
            args.add(step.source());
        }
        args.add(emptyIfNull(step.keyword()));
        args.add(step.name());
        if (showElapsedTime) {
            String duration = (
                    step.getResult() == Result.SKIPPED ? "" :
                            "("+ String.valueOf(Duration.between(step.getStartInstant(),step.getFinishInstant()).toMillis() / 1000f) + ")"
            );
            args.add(duration);
        }
        args.add(step.getError().map(Throwable::getLocalizedMessage).orElse(""));
        return args.toArray();
    }



    private static Object emptyIfNull(Object value) {
        return value == null ? "" : value;
    }



}
