package org.dice.nfactcheck.ml.feature.fact;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class NFactScorer {
    private Classifier classifier       = null;
    private Instances trainingInstances = null;

    public NFactScorer() {
        
        DataSource source = null;
        try {
            source = new DataSource(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "N_FACT_TRAINING_DATA_FILENAME"));
            trainingInstances = source.getDataSet();

            trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);

            if(trainingInstances == null) {
                System.out.println("No training data.");
            }

            System.out.println("Total training instances : " + Integer.toString(trainingInstances.numInstances()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.classifier = loadClassifier();
    }

    public void scoreEvidence(Evidence evidence) {

    	Instances instancesWithStringVector = new Instances(trainingInstances);
        instancesWithStringVector.setClassIndex(26);
    	
        for ( ComplexProof proof : evidence.getComplexProofs() ) {
        	
            try {
                Instance newInstance = new Instance(proof.getFeatures());
                newInstance.setDataset(trainingInstances);
                proof.setScore(this.classifier.distributionForInstance(newInstance)[0]);
            }
            catch (Exception e) {

                e.printStackTrace();
                System.exit(0);
            }
        }
        
        // set for each website the score by multiplying the proofs found on this site
        for ( WebSite website : evidence.getAllWebSites() ) {
            
            double score = 1D;
            
            for ( ComplexProof proof : evidence.getComplexProofs(website)) {

                score *= ( 1D - proof.getScore() );
            }
            website.setScore(1 - score);
        }
    }

    private Classifier loadClassifier() {

        Classifier smo = null;
        try {
            smo = new SMO();
            trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
            ((SMO) smo).setBuildLogisticModels(true);
            smo.buildClassifier(trainingInstances);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return smo; 
    }
}
