/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.uni_hamburg.corpora;

import java.util.Collection;

/**
 *
 * @author fsnv625
 */
public interface CorpusFunction extends CommandLineable{
//Applicability (I already forgot what this was about...)

public Collection<CorpusData> IsUsableFor();

//does CorpusData need to be a field here too?? 
    
//WriteFormatBehaviour

//field: WriteLocationBeviour

public Report execute(CorpusData cd);
}
