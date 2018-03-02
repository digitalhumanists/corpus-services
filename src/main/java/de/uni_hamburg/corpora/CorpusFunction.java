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
    
   
public Collection<CorpusData> IsUsableFor();

public Report execute(CorpusData cd);

public Report execute(Corpus c);

public Report execute(Collection<CorpusData> cdc);

public Collection<CorpusData> getIsUsableFor();

public void setIsUsableFor(Collection<CorpusData> cdc);

}
