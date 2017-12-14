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
 * Statistics report is a container class to facilitate building reports for
 * different validators and other file processors. The statistics consist of
 * "messages" that are singular events of success, failure or other notes,
 * categorised in named buckets. It's quite generic, the main point is to create
 * reports like:
 *
 * <pre>
 *   File "xyz.xml" has:
 *      1567 of things and stuff: 95 % done correctly,
 *      1 % missing, and 4 % with errors (see details here: ___)
 *      12400 of annotations: 100 % done correctly, 7 % with warnings.
 * </pre>
 */
public class StatisticsReport {


    /** the data structure holding all statistics. */
    private Map<String, Collection<StatisticsStuff>> statistics;

    /**
     * convenience function to create new statistic set if missing or get old.
     */
    private Collection<StatisticsStuff> getOrCreateStatistic(String statId) {
        if (!statistics.containsKey(statId)) {
            statistics.put(statId, new ArrayList<StatisticsStuff>());
        }
        return statistics.get(statId);
    }

    /**
     * Create empty report.
     */
    public StatisticsReport() {
        statistics = new HashMap<String, Collection<StatisticsStuff>>();
    }

    /**
     * Merge two error reports. Efficiently adds statistics from other report
     * to this one.
     */
    public void merge(StatisticsReport sr) {
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            if (statistics.containsKey(kv.getKey())) {
                Collection<StatisticsStuff> c =
                    statistics.get(kv.getKey());
                c.addAll(kv.getValue());
                statistics.put(kv.getKey(), c);
            } else {
                statistics.put(kv.getKey(), kv.getValue());
            }
        }
    }

    /**
     * Add a critical error in named statistics bucket.
     */
    public void addCritical(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CRITICAL,
                    description));
    }

    /**
     * Add a critical error in named statistics bucket.
     * @todo extrablah
     */
    public void addCritical(String statId, String description, String extraBlah) {
        addCritical(statId, description + extraBlah);
    }

    /**
     * Add a non-critical error in named statistics bucket.
     */
    public void addWarning(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.WARNING,
                    description));
    }

    /**
     * Add a non-critical error in named statistics bucket.
     * @todo extrablah
     */
    public void addWarning(String statId, String description, String extraBlah) {
        addWarning(statId, description + extraBlah);
    }

    /**
     * Add error about missing data in named statistics bucket.
     */
    public void addMissing(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.MISSING,
                    description));
    }

    /**
     * Add note for correctly formatted data in named statistics bucket.
     */
    public void addCorrect(String statId, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CORRECT,
                    description));
    }

    /**
     * Add error with throwable in statistics bucket. The exception provides
     * extra information about the error, ideally e.g. when parsing a file if
     * error comes in form of exception is a good idea to re-use the throwable
     * in statistics.
     */
    public void addException(String statId, Throwable e, String description) {
        Collection<StatisticsStuff> stat = getOrCreateStatistic(statId);
        stat.add(new StatisticsStuff(StatisticsStuff.Severity.CRITICAL,
                    e, description));
    }

    /**
     * Generate a one-line text-only message summarising the named bucket.
     */
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

    /**
     * Genereate summaries for all buckets.
     */
    public String getSummaryLines() {
        String rv = "Summaries:\n";
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            rv += getSummaryLine(kv.getKey());
        }
        return rv;
    }

    /**
     * Generate error report for given bucket. Includes only severe errors and
     * problems in detail.
     */
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

    /**
     * Generate error reports for all buckets.
     */
    public String getErrorReports() {
        String rv= "Errors:\n";
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            rv += getErrorReport(kv.getKey());
        }
        return rv;
    }

    /**
     * Generate verbose report for given bucket.
     */
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

    /**
     * Generate verbose reports for all buckets.
     */
    public String getFullReports() {
        String rv = "All reports\n";
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            rv += getFullReport(kv.getKey());
        }
        return rv;
    }

    /**
     * Get single collection of statistics.
     */
    public Collection<StatisticsStuff> getRawStatistics() {
        Collection<StatisticsStuff> allStats = new ArrayList<StatisticsStuff>();
        for (Map.Entry<String, Collection<StatisticsStuff>> kv :
                statistics.entrySet()) {
            allStats.addAll(kv.getValue());
        }
        return allStats;
    }
}
