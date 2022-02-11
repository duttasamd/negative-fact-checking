package org.dice.nfactcheck.proof.filter;

import java.util.*;

import org.aksw.defacto.evidence.ComplexProof;
import org.dice.nfactcheck.evidence.NComplexProof;
import org.dice.nfactcheck.evidence.NEvidence;

public class ProofFilter {
    public static void filterProofs(NEvidence evidence) {
        Set<ComplexProof> proofs = evidence.getComplexProofs();
        Set<ComplexProof> toRemoveProofs = new HashSet<>();

        for (ComplexProof complexProof : proofs) {
            NComplexProof nproof = (NComplexProof) complexProof;

            if(nproof.getIsToRemove()) {
                toRemoveProofs.add(complexProof);
            }
        }

        evidence.removeComplexProofs(toRemoveProofs);
    }
}
