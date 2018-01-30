/**
 * @file StringChecker.java
 *
 * A corpus processor interface that can check raw string data.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.StatisticsReport;

/**
 * A HZSK validator that can check data from a string. Most validators should
 * start with this. The data is typically file contents as string, but not
 * necessarily. The validator should create a statistics report while checking.
 *
 * @sa de.uni_hamburg.corpora.utilities.TypeConverter
 *
 */
public interface StringChecker {

    /**
     * Check string data and create a report.
     */
    public StatisticsReport check(String data);


}

