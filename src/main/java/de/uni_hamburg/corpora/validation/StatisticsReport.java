/**
 * @file StatisticsReport.java
 *
 * Auxiliary data structure for user friendly validation reports. Bit like a
 * logger.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


/**
 * Error message class is meant to facilitate creating user friendly error
 * messages in HZSK validators. It kind of forces the programmer to at least
 * rephrase an exception to two messages describing the problem and suggested
 * solution. Can be used without exception as well.
 */
public class StatisticsReport {


    private Map<String, Collection<StatisticsStuff>> statistics;

    private Collection<StatisticsStuff> getOrCreateStatistic(String statId) {
        if (!statistics.containsKey(statId)) {
            statistics.put(statId, new ArrayList<StatisticsStuff>());
        }
        return statistics.get(statId);
    }

    public StatisticsReport() {
        statistics = new HashMap<String, Collection<StatisticsStuff>>();
    }

    public void addCritical(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CRITICAL,
                    description));
    }

    public void addWarning(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.WARNING,
                    description));
    }

    public void addMissing(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.MISSING,
                    description));
    }

    public void addCorrect(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CORRECT,
                    description));
    }

    public void addException(String statId, Throwable e, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CRITICAL,
                    e, description));
    }

    public String getSummaryLine(String statId) {
        int good = 0;
        int bad = 0;
        int unk = 0;
        Collection<StatisticsStuff> stats = statistics.get(statId);
        for (StatisticsStuff s : stats) {
            if (s.isBad()) {
                bad += 1;
            } else if (s.isGood()) {
                good += 1;
            } else {
                unk += 1;
            }
        }
        return MessageFormat.format("{1}: {2} % done: {3} OK + {4} bad + " +
                "{5} ??? = {6} items", statId, good / (good + bad + unk),
                good, bad, unk, good + bad + unk);
    }

    public String getErrorReport(String statId) {
        Collection<StatisticsStuff> stats = statistics.get(statId);
        String rv = MessageFormat.format("{1}:\n", statId);
        int suppressed = 0;
        for (StatisticsStuff s : stats) {
            if (s.isSevere()) {
                rv += s.toString() + "\n";
            } else {
                suppressed += 1;
            }
        }
        if (suppressed != 0) {
            rv += MessageFormat.format("\nand {1} warnings not included",
                    suppressed);
        }
        return rv;
    }


    public String getFullReport(String statId) {
        Collection<StatisticsStuff> stats = statistics.get(statId);
        String rv = MessageFormat.format("{1}:\n", statId);
        for (StatisticsStuff s : stats) {
            if (s.isGood()) {
                rv += s.toString() + "\n";
            }
        }
        for (StatisticsStuff s : stats) {
            if (s.isBad()) {
                rv += s.toString() + "\n";
            }
        }
        return rv;
    }

    public String getAllReports() {
        String rv = "All reports\n";
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            rv += getFullReport(kv.getKey());
        }
        return rv;
    }

    public Collection<StatisticsStuff> getRawStatistics() {
        Collection<StatisticsStuff> allStats = new ArrayList<StatisticsStuff>();
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            allStats.addAll(kv.getValue());
        }
        return allStats;
    }
}
