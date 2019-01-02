/**
 * Auxiliary data structure for exmaralda errors for xml.
 */
package de.uni_hamburg.corpora;

/**
 *
 * @author Ozzy
 */
public class ExmaErrorListItem {
    /** The name of the exb file on which error occurs*/
    private String fileName;
    /** ID of the tier that contains the error */
    private String tierID;
    /** Starting time of the event that has the error */
    private String eventStart;
    /** Flag for whether the error is fixed or not */
    private boolean done;
    /** Description of the error */
    private String description;

    public ExmaErrorListItem(String fileName, String tierID, String eventStart, boolean done, String description) {
        this.fileName = fileName;
        this.tierID = tierID;
        this.eventStart = eventStart;
        this.done = done;
        this.description = description;
    }
    
    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the tierID
     */
    public String getTierID() {
        return tierID;
    }

    /**
     * @return the eventStart
     */
    public String getEventStart() {
        return eventStart;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return done;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
