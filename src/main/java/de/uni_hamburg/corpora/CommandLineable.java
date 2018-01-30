/**
 * @file CommandLineable.java
 *
 * A corpus processor interface for command line available.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora;

import de.uni_hamburg.corpora.validation.StatisticsReport;

/**
 * A command-line interface for error-checking corpora files. This ensures that
 * a check has an entry point that can be called after construction. This is
 * basically a non-static @c main, but it can be called from other contexts too,
 * so it provides a neat entry point. A typical implementation goes like so:
 *
 * <pre>
 * public StatisticsReport doMain(String[] args) {
 *   settings.handleCommandLine(args, ...);
 *   for (File f : settings.getInputFiles()) {
 *     checker.check(f);
 *   }
 *
 *   public static void main(String[] args) {
 *     FooBarChecker checker = new FooBarChecker();
 *     checker.doMain(args);
 *   }
 * }
 * </pre>
 */
public interface CommandLineable {


    public StatisticsReport doMain(String[] args);


}

