/**
 * @file ComaErrorReportGenerator.java
 *
 * Create a user-friendly error report from validation errors in a coma file.
 *
 * @author Tommi A Pirinen <tommi.antero.pirinen@uni-hamburg.de>
 * @author HZSK
 */

package de.uni_hamburg.corpora.validation;


import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;

/**
 * XML schema validation error handler for coma file errors. This is specific
 * to HZSK use of coma file data, the class tries to turn XML Schema errors
 * into readable descriptions of what effect a validation error may have to
 * the deposited corpus and how to fix it.
 */
public class ComaErrorReportGenerator implements ErrorHandler {

    // store latest file name for laughs
    private String currentFileName;
    private List<ErrorMessage> errors;

    /**
     * Create an error report generator.
     */
    public ComaErrorReportGenerator() {
        super();
        errors = new ArrayList<ErrorMessage>();
    }

    public Collection<ErrorMessage> getErrors() {
        return errors;
    }

    /**
     * Classify XML schema validation errors into HZSK infos.
     */
    private void storeException(SAXParseException saxpe) {
        // yeah this hack relies on parsing the localised(?) messages...
        String msg = saxpe.getMessage();
        String idrefPattern = "IDREF [\"']([^\"']*)[\"']";
        Pattern idrefRE = Pattern.compile(idrefPattern);
        Matcher idrefm = idrefRE.matcher(msg);
        String idvaluePattern = "ID value [\"']([^\"']*)[\"']";
        Pattern idvalueRE = Pattern.compile(idvaluePattern);
        Matcher idvaluem = idvalueRE.matcher(msg);
        String elementPattern = "[Ee]lement [\"']([^\"']*)[\"']";
        Pattern elementRE = Pattern.compile(elementPattern);
        Matcher elementm = elementRE.matcher(msg);
        String attributePattern = "[Aa]ttribute [\"']([^\"']*)[\"']";
        Pattern attributeRE = Pattern.compile(attributePattern);
        Matcher attributem = attributeRE.matcher(msg);
        String valuePattern = "[Vv]alue [\"']([^\"']*)[\"']";
        Pattern valueRE = Pattern.compile(valuePattern);
        Matcher valuem = valueRE.matcher(msg);
        String value4Pattern = "[Vv]alue for [\"']([^\"']*)[\"']";
        Pattern value4RE = Pattern.compile(value4Pattern);
        Matcher value4m = value4RE.matcher(msg);
        // use error id as first narrow down:
        if (msg.contains("cvc-complex-type.4")) {
            if ((attributem.find() && elementm.find())) {
                if (attributem.group(1).equals("schemaVersion")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.NOTE, saxpe,
                            "schemaVersion is missing from coma file",
                            "This can be safely ignored."));
                } else if ((attributem.group(1).equals("Id")) &&
                        (elementm.group(1).equals("Recording"))) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "This recording is missing an ID.",
                        "Change Name into ID or create a new ID."));
                } else if ((attributem.group(1).equals("Id")) &&
                        (elementm.group(1).equals("Media"))) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "This media is missing an ID.",
                        "Create a new ID."));
                } else if ((attributem.group(1).equals("Id")) &&
                        (elementm.group(1).equals("File"))) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "This file is missing an ID.",
                        "Create a new ID."));
                } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "This " + elementm.group(1) +
                        " is missing " + attributem.group(1) + ".",
                        "See the original error message."));
                }
            } else if (elementm.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unknown error with missing attribute in " +
                        elementm.group(1),
                        "See the original error message."));
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unknown error with missing attribute",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-id.1")) {
            if (idrefm.find() && elementm.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "The " + elementm.group(1) + " with ID code " +
                        idrefm.group(1) + " is missing.",
                        "Add a new <" + elementm.group(1) + " Id="
                        + idrefm.group(1) + "'>."));
            } else if (elementm.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "An " + elementm.group(1) + " is missing.",
                        "Add a new <" + elementm.group(1) + " ...'>."));
            } else if (idrefm.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "A speaker, transcription or some file with ID " +
                        idrefm.group(1) + " is missing.",
                        "Add a new element with Id=" + idrefm.group(1) +
                        "'>."));
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "There is an unmatched ID here. Could be a speaker, " +
                        "transcription, or some other file.",
                        "Need to add something that would match the ID."));
            }
        } else if (msg.contains("cvc-id.2")) {
            if (idrefm.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "The Id code " +
                        idrefm.group(1) + " is defined in two or more places.",
                        "Remove all but one, or change ID's."));
            } else if (idvaluem.find()) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "The Id code " +
                        idvaluem.group(1) + " is defined in two or more places.",
                        "Remove all but one, or change ID's."));
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "There's a duplicate ID here. ",
                        "Check and remove duplicates, or change the ID's."));
            }
        } else if (msg.contains("cvc-type.3.1.3")) {
            if (valuem.find() && elementm.find()) {
                if (valuem.equals("")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        elementm.group(1) + " cannot be empty or undefined.",
                        "Fill in some value."));
                } else if (elementm.group(1).equals("PeriodStart")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                            "Period start is formatted wrong.",
                            "Reformat in YYYY-MM-DD format."));
                } else if (elementm.group(1).equals("Person")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                            "Person ID is formatted wrong.",
                            "Rewrite ID to contain only A-Z, 0-9, and hyphens."));
                } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                            elementm.group(1) + " has invalid value " +
                            valuem.group(1) + ".",
                            "See the original error message."));
                }
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Invalid value somewhere",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-datatype-valid.1.2.1")) {
            if (valuem.find() && value4m.find()) {
                if (valuem.group(1).equals("")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        value4m.group(1) + " cannot be empty or undefined. ",
                        "It needs to be filled in."));
                } else if (msg.contains("'' is not a valid value")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                            value4m.group(1) + " is empty when it shouldn't",
                            "It needs to be filled in"));
                } else if (value4m.group(1).equals("NCName")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                            "Name ID is not correct. ",
                            "It should be an ID of letters, dashes and numbers"));
                } else if (value4m.group(1).equals("dateTime")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                            "Date here is formatted wrong.",
                            "Write it as YYYY-MM-DD."));
                } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                            value4m.group(1) + " is not here",
                        "See the original error message."));
                }
            } else if (value4m.find()) {
                if (msg.contains("'' is not a valid value")) {
                    errors.add(new ErrorMessage(
                        ErrorMessage.Severity.CRITICAL, saxpe,
                        value4m.group(1) + " is empty when it shouldn't",
                        "It needs to be filled in"));
                } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Invalid value for " + value4m.group(1),
                        "See the original error message."));
                }
            } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Invalid value somewhere??",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-complex-type.2.3")) {
            errors.add(new ErrorMessage(
                        ErrorMessage.Severity.CRITICAL, saxpe,
                       "There's text in the file where there should be none. ",
                       "if you edited file in text editor, " +
                       "check for odd characters including special space marks,"
                       + "remove # signs outside the content and use <!-- --> "
                       + " comments."
                       ));
        } else if ((msg.contains("cvc-complex-type.2.4.a")) ||
                (msg.contains("cvc-complex-type.2.4.d"))) {
            if (elementm.find()) {
                if (elementm.group(1).equals("Description")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.NOTE, saxpe,
                        "Description elements are in wrong order here.",
                        "This can be ignored."));
                } else if (elementm.group(1).equals("DBNode")) {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.NOTE, saxpe,
                        "DBNode elements are in wrong order here.",
                        "This can be ignored"));
                } else if (elementm.group(1).equals("RecordingDuration")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                            "Recording duration cannot be figured out properly.",
                            "Are there more than one conflicting durations?"));
                } else if (elementm.group(1).equals("role")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE, saxpe,
                                "Roles have been deprecated.",
                                "You can remove it from the file, it is not used."
                                ));
                // FIXME: the File / Filename / NSLink /URI stuff...
                } else if (elementm.group(1).equals("AsocFile")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.WARNING, saxpe,
                                "AsocFile is not valid element type here.",
                                "AsocFile should be renamed to AssociatedFile?"
                                ));
                } else if (elementm.group(1).equals("NSLink")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.WARNING, saxpe,
                                "NSLink is not valid element type here.",
                                "<NSLink should be replaced with a <File... "
                                ));
                } else if (elementm.group(1).equals("Filename")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE, saxpe,
                                "Filename is somewhat out of order.",
                                "Probably doesn't matter if file was found."));
                } else if (elementm.group(1).equals("Name")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                                "Name value cannot be associated to correct entity.",
                                " It may be copy/pasted to a wrong place?"));
                } else if (elementm.group(1).equals("Key")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                                "Key value cannot be associated to correct entity.",
                                " It may be copy/pasted to a wrong place?"));
                } else if (elementm.group(1).equals("Postalcode")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE, saxpe,
                                "Postalcode comes in wrong order here",
                                "This can be safely ignored."));
                } else if (elementm.group(1).equals("Media")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE, saxpe,
                                "Media comes in wrong order here",
                                "This probably doesn't matter"));
                } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                            elementm.group(1) + " is in wrong place in the file.",
                            "This probably doesn't matter but might get misplaced"));
                }
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unexpected element error stuff",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-complex-type.2.4.b")) {
            if (elementm.find()) {
                if (elementm.group(1).equals("Language")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                            "Language elements are missing or partial here.",
                            "You should add or replace <Languages"));
                } else if (elementm.group(1).equals("File")) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.NOTE, saxpe,
                            "File information is missing or partial here.",
                            "You should add absolute path, URL or so."));
                } else {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.UNKNOWN, saxpe,
                                elementm.group(1) + " is missing some of needed data",
                                "See the original error message."));
                }
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unexpected element error stuff",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-complex-type.3.2.2")) {
            if (attributem.find() && elementm.find()) {
                if ((attributem.group(1).equals("Name")) &&
                        (elementm.group(1).equals("Recording"))) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.WARNING, saxpe,
                                "There's an extra Name in this Recording.",
                            "This name should probably be id... " +
                            "see for other warnings."));
                } else if ((attributem.group(1).equals("type")) &&
                        (elementm.group(1).equals("Location"))) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.WARNING, saxpe,
                                "There's an extraneous type= for this Location.",
                            "Location types may be ignored, when in attribute only."
                            ));
                } else if ((attributem.group(1).equals("type")) &&
                        (elementm.group(1).equals("Language"))) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.WARNING, saxpe,
                                "There's an extraneous type= for this Language.",
                            "Language types may be ignored, when in attribute only."
                            ));
                } else if ((attributem.group(1).equals("xsi")) ||
                        (attributem.group(1).equals("noNamespaceSchemaLocation"))) {
                    errors.add(new ErrorMessage(
                                ErrorMessage.Severity.CRITICAL, saxpe,
                            "The namespaces are missing from XML! ",
                            "This file may be really broken..."));
                } else {
                    errors.add(new ErrorMessage(ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unexpected attribute " + attributem.group(1) +
                        " in " + elementm.group(1),
                        "See the original error message."));
                }
            } else {
                errors.add(new ErrorMessage(ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Unexpected attribute error stuff",
                        "See the original error message."));
            }
        } else if (msg.contains("cvc-attribute.3")) {
            if (attributem.find() && valuem.find()) {
                if (attributem.group(1).equals("Id")) {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.CRITICAL, saxpe,
                        "This ID " + valuem.group(1) +
                        " doesn't validate (XML-wise)",
                        "May be still used if matches neatly for HZSK uses."));
                } else {
                    errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                            "This value " + valuem.group(1) +
                            " is invalid for " + attributem.group(1),
                            "May need manual corrections."));
                }
            } else {
                errors.add(new ErrorMessage(
                            ErrorMessage.Severity.UNKNOWN, saxpe,
                            " Attribute value error",
                            "See the original error message."));
            }
        } else {
            errors.add(new ErrorMessage(
                        ErrorMessage.Severity.UNKNOWN, saxpe,
                        "Totally unrecognised validation error.",
                        "See the original error message."));
        }
   }

   /**
    * Handle fatal XML schema validation error.
    * This handler ignores the severity of validation errors.
    */
    public void fatalError(SAXParseException saxpe) throws SAXException {
        storeException(saxpe);
    }

    /**
     * Handle XML schema validation error.
     * This handler ignores the severity of validation errors.
     */
    public void error(SAXParseException saxpe) throws SAXException {
        storeException(saxpe);
    }

    /**
     * Handle XML schema valdition warning
     * This handler ignores the severity of validation errors.
     */
    public void warning(SAXParseException saxpe) throws SAXException {
        storeException(saxpe);

    }
}

