package org.cbioportal.genome_nexus.service.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cbioportal.genome_nexus.model.Hotspot;
import org.cbioportal.genome_nexus.model.IsoformOverride;
import org.cbioportal.genome_nexus.model.MutationAssessor;
import org.cbioportal.genome_nexus.model.my_variant_info_model.MyVariantInfo;
import org.cbioportal.genome_nexus.model.TranscriptConsequence;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.cbioportal.genome_nexus.service.IsoformOverrideService;
import org.cbioportal.genome_nexus.service.MutationAssessorService;
import org.cbioportal.genome_nexus.service.MyVariantInfoService;
import org.cbioportal.genome_nexus.component.annotation.NotationConverter;
import org.cbioportal.genome_nexus.service.cached.CachedVariantAnnotationFetcher;
import org.cbioportal.genome_nexus.service.cached.CachedVariantIdAnnotationFetcher;
import org.cbioportal.genome_nexus.service.cached.CachedVariantRegionAnnotationFetcher;
import org.cbioportal.genome_nexus.service.exception.CancerHotspotsWebServiceException;
import org.cbioportal.genome_nexus.service.exception.IsoformOverrideNotFoundException;
import org.cbioportal.genome_nexus.service.exception.MutationAssessorNotFoundException;
import org.cbioportal.genome_nexus.service.exception.MutationAssessorWebServiceException;
import org.cbioportal.genome_nexus.service.exception.MyVariantInfoNotFoundException;
import org.cbioportal.genome_nexus.service.exception.MyVariantInfoWebServiceException;
import org.cbioportal.genome_nexus.service.exception.ResourceMappingException;
import org.cbioportal.genome_nexus.service.exception.VariantAnnotationNotFoundException;
import org.cbioportal.genome_nexus.service.exception.VariantAnnotationWebServiceException;
import org.cbioportal.genome_nexus.service.mock.CancerHotspotMockData;
import org.cbioportal.genome_nexus.service.mock.IsoformOverrideMockData;
import org.cbioportal.genome_nexus.service.mock.MutationAssessorMockData;
import org.cbioportal.genome_nexus.service.mock.VariantAnnotationMockData;
import org.cbioportal.genome_nexus.service.mock.MyVariantInfoMockData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class VariantAnnotationServiceTest
{
    @InjectMocks
    private HgvsVariantAnnotationService variantAnnotationService;

    @Mock
    private CachedVariantAnnotationFetcher fetcher;

    @Mock
    private CachedVariantIdAnnotationFetcher idFetcher;

    @Mock
    private CachedVariantRegionAnnotationFetcher regionFetcher;

    @Mock
    private MutationAssessorService mutationAssessorService;

    @Mock
    private IsoformOverrideService isoformOverrideService;

    @Mock
    private CancerHotspotServiceImpl cancerHotspotService;

    @Mock
    private MyVariantInfoService myVariantInfoService;

    @Spy
    private NotationConverter notationConverter;


    private VariantAnnotationMockData variantAnnotationMockData = new VariantAnnotationMockData();
    private MutationAssessorMockData mutationAssessorMockData = new MutationAssessorMockData();
    private CancerHotspotMockData cancerHotspotMockData = new CancerHotspotMockData();
    private IsoformOverrideMockData isoformOverrideMockData = new IsoformOverrideMockData();
    private MyVariantInfoMockData myVariantInfoMockData = new MyVariantInfoMockData();

    @Test
    public void getAnnotationByVariantString()
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException,
        IOException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        this.mockVariantFetcherMethods(variantMockData);

        VariantAnnotation annotation1 = variantAnnotationService.getAnnotation("7:g.140453136A>T");
        assertEquals(variantMockData.get("7:g.140453136A>T").getStart(), annotation1.getStart());
        assertEquals(variantMockData.get("7:g.140453136A>T").getVariant(), annotation1.getVariant());

        VariantAnnotation annotation2 = variantAnnotationService.getAnnotation("12:g.25398285C>A");
        assertEquals(variantMockData.get("12:g.25398285C>A").getStart(), annotation2.getStart());
        assertEquals(variantMockData.get("12:g.25398285C>A").getVariant(), annotation2.getVariant());

        VariantAnnotation annotation3 = variantAnnotationService.getAnnotation("X:g.41242962_41242963insGA");
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getStart(), annotation3.getStart());
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getVariant(), annotation3.getVariant());

        // should convert chr prefix to 
        VariantAnnotation annotation4 = variantAnnotationService.getAnnotation("chr23:g.41242962_41242963insGA");
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getStart(), annotation4.getStart());
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getVariant(), annotation4.getVariant());

        VariantAnnotation annotation5 = variantAnnotationService.getAnnotation("chr24:g.41242962_41242963insGA");
        assertEquals(variantMockData.get("Y:g.41242962_41242963insGA").getStart(), annotation5.getStart());
        assertEquals(variantMockData.get("Y:g.41242962_41242963insGA").getVariant(), annotation5.getVariant());
    }

    @Test
    public void getAnnotationsByVariantString()
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException,
        IOException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        this.mockVariantFetcherMethods(variantMockData);

        List<String> variants = new ArrayList<>(4);
        variants.add("7:g.140453136A>T");
        variants.add("12:g.25398285C>A");
        variants.add("chr23:g.41242962_41242963insGA");
        variants.add("chr24:g.41242962_41242963insGA");

        List<VariantAnnotation> annotations = variantAnnotationService.getAnnotations(variants);
        assertEquals(variantMockData.get("7:g.140453136A>T").getStart(), annotations.get(0).getStart());
        assertEquals(variantMockData.get("7:g.140453136A>T").getVariant(), annotations.get(0).getVariant());
        assertEquals(variantMockData.get("12:g.25398285C>A").getStart(), annotations.get(1).getStart());
        assertEquals(variantMockData.get("12:g.25398285C>A").getVariant(), annotations.get(1).getVariant());
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getStart(), annotations.get(2).getStart());
        assertEquals(variantMockData.get("X:g.41242962_41242963insGA").getVariant(), annotations.get(2).getVariant());
        assertEquals(variantMockData.get("Y:g.41242962_41242963insGA").getStart(), annotations.get(3).getStart());
        assertEquals(variantMockData.get("Y:g.41242962_41242963insGA").getVariant(), annotations.get(3).getVariant());
    }

    @Test
    public void getMutationAssessorEnrichedAnnotationByVariantString()
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException,
        MutationAssessorWebServiceException, MutationAssessorNotFoundException, IsoformOverrideNotFoundException,
        IOException, MyVariantInfoWebServiceException, MyVariantInfoNotFoundException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        Map<String, MutationAssessor> maMockData = this.mutationAssessorMockData.generateData();
        Map<String, IsoformOverride> isoformOverrideMockData = this.isoformOverrideMockData.generateData();

        this.mockVariantFetcherMethods(variantMockData);
        this.mockMutationAssessorServiceMethods(variantMockData, maMockData);
        this.mockIsoformOverrideServiceMethods(isoformOverrideMockData);

        List<String> fields = new ArrayList<>(1);
        fields.add("mutation_assessor");

        VariantAnnotation annotation1 = variantAnnotationService.getAnnotation(
            "7:g.140453136A>T", null, fields);

        assertEquals(maMockData.get("7,140453136,A,T"),
            annotation1.getMutationAssessorAnnotation().getAnnotation());

        VariantAnnotation annotation2 = variantAnnotationService.getAnnotation(
            "12:g.25398285C>A", null, fields);

        assertEquals(maMockData.get("12,25398285,C,A"),
            annotation2.getMutationAssessorAnnotation().getAnnotation());
    }

    @Test
    public void getMyVariantInfoEnrichedAnnotationByVariantString()
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException,
        MyVariantInfoWebServiceException, MyVariantInfoNotFoundException, IsoformOverrideNotFoundException,
        IOException, MyVariantInfoWebServiceException, MyVariantInfoNotFoundException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        Map<String, MyVariantInfo> mviMockData = this.myVariantInfoMockData.generateData();
        Map<String, IsoformOverride> isoformOverrideMockData = this.isoformOverrideMockData.generateData();

        this.mockVariantFetcherMethods(variantMockData);
        this.mockMyVariantInfoServiceMethods(variantMockData, mviMockData);
        this.mockIsoformOverrideServiceMethods(isoformOverrideMockData);

        List<String> fields = new ArrayList<>(1);
        fields.add("my_variant_info");

        VariantAnnotation annotation1 = variantAnnotationService.getAnnotation(
            "7:g.140453136A>T", null, fields);

        assertEquals(mviMockData.get("7:g.140453136A>T"),
            annotation1.getMyVariantInfoAnnotation().getAnnotation());

        VariantAnnotation annotation2 = variantAnnotationService.getAnnotation(
            "12:g.25398285C>A", null, fields);

        assertEquals(mviMockData.get("12:g.25398285C>A"),
            annotation2.getMyVariantInfoAnnotation().getAnnotation());
    }

    @Test
    public void getHotspotEnrichedAnnotationByVariantString()
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException,
        CancerHotspotsWebServiceException, IsoformOverrideNotFoundException, IOException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        Map<String, List<Hotspot>> hotspotMockData = this.cancerHotspotMockData.generateData();
        Map<String, IsoformOverride> isoformOverrideMockData = this.isoformOverrideMockData.generateData();

        this.mockVariantFetcherMethods(variantMockData);
        this.mockHotspotServiceMethods(hotspotMockData);
        this.mockIsoformOverrideServiceMethods(isoformOverrideMockData);

        List<String> fields = new ArrayList<>(1);
        fields.add("hotspots");

        VariantAnnotation annotation1 = variantAnnotationService.getAnnotation(
            "7:g.140453136A>T", null, fields);

        assertEquals(hotspotMockData.get("ENST00000288602"),
            annotation1.getHotspotAnnotation().getAnnotation().get(0));

        VariantAnnotation annotation2 = variantAnnotationService.getAnnotation(
            "12:g.25398285C>A", null, fields);

        assertEquals(hotspotMockData.get("ENST00000256078"),
            annotation2.getHotspotAnnotation().getAnnotation().get(0));
    }

    @Test
    public void getIsorformOverrideEnrichedAnnotationByVariantString()
        throws VariantAnnotationWebServiceException, VariantAnnotationNotFoundException, ResourceMappingException,
        IsoformOverrideNotFoundException, IOException
    {
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();
        Map<String, IsoformOverride> isoformOverrideMockData = this.isoformOverrideMockData.generateData();

        this.mockVariantFetcherMethods(variantMockData);
        this.mockIsoformOverrideServiceMethods(isoformOverrideMockData);

        VariantAnnotation annotation1 = variantAnnotationService.getAnnotation(
            "7:g.140453136A>T", "mskcc", null);

        // first transcript of this annotation should be marked as canonical, the second one should NOT be marked
        assertEquals("1", annotation1.getTranscriptConsequences().get(0).getCanonical());
        assertEquals(null, annotation1.getTranscriptConsequences().get(1).getCanonical());

        VariantAnnotation annotation2 = variantAnnotationService.getAnnotation(
            "7:g.140453136A>T", "uniprot", null);

        // second transcript of this annotation should be marked as canonical, the first one should NOT be marked
        assertEquals(null, annotation2.getTranscriptConsequences().get(0).getCanonical());
        assertEquals("1", annotation2.getTranscriptConsequences().get(1).getCanonical());
    }

    private void mockVariantFetcherMethods(Map<String, VariantAnnotation> variantMockData)
        throws ResourceMappingException, VariantAnnotationWebServiceException, VariantAnnotationNotFoundException
    {
        // mock methods in order to prevent hitting the live VEP web API
        Mockito.when(this.fetcher.fetchAndCache("7:g.140453136A>T")).thenReturn(variantMockData.get("7:g.140453136A>T"));
        Mockito.when(this.fetcher.fetchAndCache("12:g.25398285C>A")).thenReturn(variantMockData.get("12:g.25398285C>A"));
        Mockito.when(this.fetcher.fetchAndCache("X:g.41242962_41242963insGA")).thenReturn(variantMockData.get("X:g.41242962_41242963insGA"));
        Mockito.when(this.fetcher.fetchAndCache("Y:g.41242962_41242963insGA")).thenReturn(variantMockData.get("Y:g.41242962_41242963insGA"));

        List<String> variants = new ArrayList<>(4);
        variants.add("7:g.140453136A>T");
        variants.add("12:g.25398285C>A");
        variants.add("X:g.41242962_41242963insGA");
        variants.add("Y:g.41242962_41242963insGA");

        List<VariantAnnotation> returnValue = new ArrayList<>(3);
        returnValue.add(variantMockData.get("7:g.140453136A>T"));
        returnValue.add(variantMockData.get("12:g.25398285C>A"));
        returnValue.add(variantMockData.get("X:g.41242962_41242963insGA"));
        returnValue.add(variantMockData.get("Y:g.41242962_41242963insGA"));

        Mockito.when(this.fetcher.fetchAndCache(variants)).thenReturn(returnValue);
    }

    private void mockMutationAssessorServiceMethods(Map<String, VariantAnnotation> variantMockData,
                                                    Map<String, MutationAssessor> maMockData)
        throws MutationAssessorWebServiceException, MutationAssessorNotFoundException
    {
        Mockito.when(this.mutationAssessorService.getMutationAssessor(
            variantMockData.get("7:g.140453136A>T"))).thenReturn(maMockData.get("7,140453136,A,T"));
        Mockito.when(this.mutationAssessorService.getMutationAssessor(
            variantMockData.get("12:g.25398285C>A"))).thenReturn(maMockData.get("12,25398285,C,A"));
    }

    private void mockMyVariantInfoServiceMethods(Map<String, VariantAnnotation> variantMockData,
            Map<String, MyVariantInfo> mviMockData)
            throws MyVariantInfoWebServiceException, MyVariantInfoNotFoundException {
        Mockito.when(this.myVariantInfoService.getMyVariantInfo(
            variantMockData.get("7:g.140453136A>T"))).thenReturn(mviMockData.get("7:g.140453136A>T"));
        Mockito.when(this.myVariantInfoService.getMyVariantInfo(
            variantMockData.get("12:g.25398285C>A"))).thenReturn(mviMockData.get("12:g.25398285C>A"));
    }

    private void mockHotspotServiceMethods(Map<String, List<Hotspot>> hotspotMockData)
        throws CancerHotspotsWebServiceException
    {
        // call the real getHotspots(TranscriptConsequence transcript) method when called with a transcript
        // it is the one calling the getHotspots(String hugoSymbol)
        Mockito.when(this.cancerHotspotService.getHotspots(
            Mockito.any(TranscriptConsequence.class), Mockito.any(VariantAnnotation.class))).thenCallRealMethod();

        Mockito.when(this.cancerHotspotService.filterHotspot(Mockito.any(Hotspot.class),
            Mockito.any(TranscriptConsequence.class),
            Mockito.any(VariantAnnotation.class))).thenReturn(true);

        Mockito.when(this.cancerHotspotService.getHotspots(
            "ENST00000288602")).thenReturn(hotspotMockData.get("ENST00000288602"));
        Mockito.when(this.cancerHotspotService.getHotspots(
            "ENST00000256078")).thenReturn(hotspotMockData.get("ENST00000256078"));
    }

    private void mockIsoformOverrideServiceMethods(Map<String, IsoformOverride> isoformOverrideMockData)
        throws IsoformOverrideNotFoundException
    {
        // false for null values
        Mockito.when(this.isoformOverrideService.hasData(null)).thenReturn(false);

        // true for mskcc and uniprot
        Mockito.when(this.isoformOverrideService.hasData("mskcc")).thenReturn(true);
        Mockito.when(this.isoformOverrideService.hasData("uniprot")).thenReturn(true);

        // when called for "mskcc" override, set first transcript "ENST00000288602" as canonical
        Mockito.when(this.isoformOverrideService.getIsoformOverride(
            "mskcc", "ENST00000288602")).thenReturn(isoformOverrideMockData.get("ENST00000288602"));
        Mockito.when(this.isoformOverrideService.getIsoformOverride(
            "uniprot", "ENST00000288602")).thenReturn(null);

        // when called for "uniprot" override, set second transcript "ENST00000479537" as canonical
        Mockito.when(this.isoformOverrideService.getIsoformOverride(
            "uniprot", "ENST00000479537")).thenReturn(isoformOverrideMockData.get("ENST00000479537"));
        Mockito.when(this.isoformOverrideService.getIsoformOverride(
            "mskcc", "ENST00000479537")).thenReturn(null);
    }
}
