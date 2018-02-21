/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

import de.uni_hamburg.corpora.CorpusData;

/**
 *
 * @author fsnv625
 */
public interface Check {

  public void check(CorpusData cd);
  //Wenn es keine automatische Möglichkeit zum
  //fixen gibt, dann muss Erklärung in die ErrorMeldung
  public void fix(CorpusData cd);

    
}
