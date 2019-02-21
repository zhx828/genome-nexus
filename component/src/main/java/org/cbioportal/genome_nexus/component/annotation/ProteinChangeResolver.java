package org.cbioportal.genome_nexus.component.annotation;

import org.cbioportal.genome_nexus.model.TranscriptConsequence;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ProteinChangeResolver
{
    public static final String AA3TO1[][] = {
        {"Ala", "A"}, {"Arg", "R"}, {"Asn", "N"}, {"Asp", "D"}, {"Asx", "B"}, {"Cys", "C"},
        {"Glu", "E"}, {"Gln", "Q"}, {"Glx", "Z"}, {"Gly", "G"}, {"His", "H"}, {"Ile", "I"},
        {"Leu", "L"}, {"Lys", "K"}, {"Met", "M"}, {"Phe", "F"}, {"Pro", "P"}, {"Ser", "S"},
        {"Thr", "T"}, {"Trp", "W"}, {"Tyr", "Y"}, {"Val", "V"}, {"Xxx", "X"}, {"Ter", "*"}
    };

    public static final Set<String> SPLICE_SITE_VARIANTS = new HashSet<>(
        Arrays.asList("splice_acceptor_variant", "splice_donor_variant", "splice_region_variant")
    );

    private final CanonicalTranscriptResolver canonicalTranscriptResolver;
    private final VariantClassificationResolver variantClassificationResolver;
    private final Pattern cDnaExtractor;

    @Autowired
    public ProteinChangeResolver(CanonicalTranscriptResolver canonicalTranscriptResolver,
                                 VariantClassificationResolver variantClassificationResolver)
    {
        this.canonicalTranscriptResolver = canonicalTranscriptResolver;
        this.variantClassificationResolver = variantClassificationResolver;
        this.cDnaExtractor = Pattern.compile(".*[cn].-?\\*?(\\d+).*");
    }

    /**
     * Resolves to a single hgvsp short for the given variant annotation.
     *
     * @param variantAnnotation
     * @return
     */
    @Nullable
    public String resolveHgvspShort(VariantAnnotation variantAnnotation)
    {
        TranscriptConsequence canonicalTranscript = this.canonicalTranscriptResolver.resolve(variantAnnotation);

        // resolve for the canonical transcript
        return this.resolveHgvspShort(variantAnnotation, canonicalTranscript);
    }

    /**
     * Resolves to a single hgvsp short for the given transcript consequence and variant annotation.
     *
     * @param transcriptConsequence
     * @return
     */
    @Nullable
    public String resolveHgvspShort(VariantAnnotation variantAnnotation,
                                    TranscriptConsequence transcriptConsequence)
    {
        String hgvspShort = null;

        String hgvsp = this.resolveHgvsp(transcriptConsequence);
        String hgvsc = this.resolveHgvsc(transcriptConsequence);

        if (hgvsp != null) {
            hgvspShort = this.resolveHgvspShortFromHgvsp(hgvsp);
        }
        else if (hgvsc != null) {
            hgvspShort = this.resolveHgvspShortFromHgvsc(hgvsc, variantAnnotation, transcriptConsequence);
        }

        // if hgvspShort is still unresolved then
        // try to salvage using protein_start, amino_acids, and consequence_terms
        if (hgvspShort == null &&
            transcriptConsequence != null &&
            transcriptConsequence.getAminoAcids() != null)
        {
            hgvspShort = this.resolveHgvspShortFromAAs(transcriptConsequence);
        }

        return hgvspShort;
    }

    @Nullable
    private String resolveHgvspShortFromHgvsp(String hgvsp)
    {
        if (hgvsp == null) {
            return null;
        }

        String hgvspShort = hgvsp;

        for (int i = 0; i < 24; i++) {
            if (hgvsp.contains(AA3TO1[i][0])) {
                hgvspShort = hgvspShort.replaceAll(AA3TO1[i][0], AA3TO1[i][1]);
            }
        }

        return hgvspShort;
    }

    @Nullable
    private String resolveHgvspShortFromHgvsc(String hgvsc,
                                              VariantAnnotation variantAnnotation,
                                              TranscriptConsequence transcriptConsequence)
    {
        String hgvspShort = null;

        Integer cPos = 0;
        Integer pPos = 0;

        Matcher m = this.cDnaExtractor.matcher(hgvsc);

        if (m.matches())
        {
            cPos = Integer.parseInt(m.group(1));
            cPos = cPos < 1 ? 1 : cPos;
            pPos = (cPos + 2) / 3;

            if (SPLICE_SITE_VARIANTS.contains(transcriptConsequence.getConsequenceTerms().get(0)))
            {
                hgvspShort = "p.X" + String.valueOf(pPos) + "_splice";
            }
            else if (transcriptConsequence.getAminoAcids() == null)
            {
                String variantClassification = this.variantClassificationResolver.resolve(
                    variantAnnotation, transcriptConsequence);

                hgvspShort = "*" + String.valueOf(pPos);

                if (variantClassification != null && variantClassification.toLowerCase().startsWith("frame_shift")) {
                    hgvspShort += "fs*";
                }
                else {
                    hgvspShort += "*";
                }
            }
        }

        return hgvspShort;
    }

    @Nullable
    private String resolveHgvspShortFromAAs(TranscriptConsequence transcriptConsequence)
    {
        String hgvspShort = null;

        try {
            String[] aaParts = transcriptConsequence.getAminoAcids().split("/");

            // TODO: always using get(0) here, maybe better to get most
            // impactful consequence and use that?
            if (transcriptConsequence.getConsequenceTerms() != null &&
                transcriptConsequence.getConsequenceTerms().get(0).toLowerCase().contains("inframe_insertion"))
            {
                // to prevent IndexOutOfBoundsException's we check for 'dup' in the HGVSc field (ex: ENST00000357654.3:c.5266dupC)
                // since the 'amino_acids' may not always be provided in such a way where the reference allele is available
                // ex: with the reference aa 'N/KN' and without the reference aa '-/K'.
                // the second format will throw an IndexOutOfBoundsException when trying to access substring index 1
                if (transcriptConsequence.getHgvsc() != null &&
                    transcriptConsequence.getHgvsc().contains("dup"))
                {
                    hgvspShort = aaParts[1].substring(0,1) +
                        String.valueOf(transcriptConsequence.getProteinStart() - 1) +
                        "dup";
                }
                else
                {
                    hgvspShort = aaParts[1].substring(0,1) +
                        transcriptConsequence.getProteinStart() + "_" +
                        aaParts[1].substring(1, 2) + "ins" +
                        transcriptConsequence.getProteinEnd() +
                        aaParts[1].substring(2);
                }
            }
            else if (transcriptConsequence.getConsequenceTerms() != null &&
                transcriptConsequence.getConsequenceTerms().get(0).toLowerCase().contains("inframe_deletion"))
            {
                hgvspShort = aaParts[0] + "del";
            }
            else
            {
                hgvspShort = aaParts[0] + transcriptConsequence.getProteinStart();

                if (transcriptConsequence.getConsequenceTerms() != null &&
                    transcriptConsequence.getConsequenceTerms().get(0).toLowerCase().contains("frameshift_variant"))
                {
                    hgvspShort += "fs";
                }
                else
                {
                    hgvspShort += aaParts[1];
                }
            }
        } catch (NullPointerException e) {
            // LOG.debug("Failed to salvage HGVSp_Short from protein start, amino acids, and consequence terms");
        }

        return hgvspShort;
    }

    @Nullable
    public String resolveHgvsc(TranscriptConsequence transcriptConsequence)
    {
        String hgvsc = null;

        if (transcriptConsequence != null &&
            transcriptConsequence.getHgvsc() != null)
        {
            hgvsc = transcriptConsequence.getHgvsc();
        }

        return hgvsc;
    }

    @Nullable
    public String resolveHgvsp(TranscriptConsequence transcriptConsequence)
    {
        String hgvsp = null;

        String variantClassification = this.variantClassificationResolver.resolve(null, transcriptConsequence);

        // only use hgvsp if the most severe impact is not a splice variant
        if (transcriptConsequence != null &&
            transcriptConsequence.getHgvsp() != null &&
            !(variantClassification != null && variantClassification.toLowerCase().contains("splice"))
            )
        {
            hgvsp = this.normalizeHgvsp(transcriptConsequence.getHgvsp());
        }

        return hgvsp;
    }

    @Nullable
    private String normalizeHgvsp(String hgvsp)
    {
        if (hgvsp == null) {
            return null;
        }

        int index = hgvsp.indexOf(":");

        if (hgvsp.contains("(p.%3D)")) {
            return "p.=";
        }
        else {
            return hgvsp.substring(index+1);
        }
    }
}
