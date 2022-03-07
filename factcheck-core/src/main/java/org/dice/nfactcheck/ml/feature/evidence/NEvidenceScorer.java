package org.dice.nfactcheck.ml.feature.evidence;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;

import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class NEvidenceScorer {
    private Classifier classifier;
    private Instances trainingInstances;

    public NEvidenceScorer() {
        DataSource source = null;

        try {
            source = new DataSource(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "N_EVIDENCE_TRAINING_DATA_FILENAME"));
            trainingInstances = source.getDataSet();
            trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
        } catch(Exception ex) {
            System.out.println("ERROR : Loading evidence training data. " + ex.getMessage());
        }

        this.classifier = this.loadClassifier();
    }

    private Classifier loadClassifier() {
        // Classifier j48 = null;
        Classifier smo = null;
        try {
            // j48 = new J48();
            // trainingInstances.deleteStringAttributes();
            // j48.buildClassifier(trainingInstances);
            smo = new SMO();
            trainingInstances.setClassIndex(trainingInstances.numAttributes() - 1);
            ((SMO) smo).setBuildLogisticModels(true);
            smo.buildClassifier(trainingInstances);

            return smo;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load classifier : " + e.getMessage());
        }
    }

    public Double scoreEvidence(Evidence evidence) {

        try {
            trainingInstances.deleteStringAttributes();

            Instance newInstance = new Instance(evidence.getFeatures());
            // System.out.println(newInstance.toString());
            // newInstance.deleteAttributeAt(1);
            newInstance.setDataset(trainingInstances);
            evidence.setDeFactoScore(this.classifier.distributionForInstance(newInstance)[0]);
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        
        return evidence.getDeFactoScore();
    }
}
