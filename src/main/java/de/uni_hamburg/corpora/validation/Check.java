/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora.validation;

/**
 *
 * @author fsnv625
 */
public interface Check {

  public void check();
  public void fix();
  //Wenn es keine automatische Möglichkeit zum
  //fixen gibt, dann muss Erklärung in die ErrorMeldung
  
    
}
