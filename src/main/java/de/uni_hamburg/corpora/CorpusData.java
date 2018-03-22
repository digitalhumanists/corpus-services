/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.net.URL;

/**
 *
 * @author fsnv625
 */
public interface CorpusData {

    public URL getURL();

    public String toSaveableString();

    public String toUnformattedString();

}
