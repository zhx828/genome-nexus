package org.cbioportal.genome_nexus.service.internal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import org.cbioportal.genome_nexus.component.test.mock.VariantAnnotationMockData;
import org.cbioportal.genome_nexus.model.my_variant_info_model.MyVariantInfo;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.cbioportal.genome_nexus.service.cached.CachedMyVariantInfoFetcher;
import org.cbioportal.genome_nexus.service.exception.MyVariantInfoNotFoundException;
import org.cbioportal.genome_nexus.service.exception.MyVariantInfoWebServiceException;
import org.cbioportal.genome_nexus.service.exception.ResourceMappingException;
import org.cbioportal.genome_nexus.component.test.mock.MyVariantInfoMockData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MyVariantInfoServiceTest
{
    @InjectMocks
    private MyVariantInfoServiceImpl service;

    @Mock
    private CachedMyVariantInfoFetcher fetcher;

    private MyVariantInfoMockData myVariantInfoMockData = new MyVariantInfoMockData();
    private VariantAnnotationMockData variantAnnotationMockData = new VariantAnnotationMockData();

    @Test
    public void getMyVariantInfoByMyVariantInfoVariant()
        throws ResourceMappingException, MyVariantInfoWebServiceException, MyVariantInfoNotFoundException
    {
        Map<String, MyVariantInfo> mockData = this.myVariantInfoMockData.generateData();

        // mock methods in order to prevent hitting the live mutation assessor web API
        Mockito.when(fetcher.fetchAndCache("7:g.140453136A>T")).thenReturn(mockData.get("7:g.140453136A>T"));
        Mockito.when(fetcher.fetchAndCache("12:g.25398285C>A")).thenReturn(mockData.get("12:g.25398285C>A"));
        Mockito.when(fetcher.fetchAndCache("INVALID")).thenReturn(mockData.get("INVALID"));

        MyVariantInfo myVariantInfo1 = service.getMyVariantInfoByMyVariantInfoVariant("7:g.140453136A>T");
        assertEquals(myVariantInfo1.getVersion(), mockData.get("7:g.140453136A>T").getVersion());

        MyVariantInfo myVariantInfo2 = service.getMyVariantInfoByMyVariantInfoVariant("12:g.25398285C>A");
        assertEquals(myVariantInfo2.getVersion(), mockData.get("12:g.25398285C>A").getVersion());

        MyVariantInfo myVariantInfo3 = service.getMyVariantInfoByMyVariantInfoVariant("INVALID");
        assertEquals(myVariantInfo3.getVersion(), mockData.get("INVALID").getVersion());
    }

    @Test
    public void getMyVariantInfoByVariantAnnotation()
        throws ResourceMappingException, MyVariantInfoWebServiceException, MyVariantInfoNotFoundException,
        IOException
    {
        Map<String, MyVariantInfo> mviMockData = this.myVariantInfoMockData.generateData();
        Map<String, VariantAnnotation> variantMockData = this.variantAnnotationMockData.generateData();

        // mock methods in order to prevent hitting the live mutation assessor web API
        Mockito.when(fetcher.fetchAndCache("chr7:g.140453136A>T")).thenReturn(mviMockData.get("7:g.140453136A>T"));
        Mockito.when(fetcher.fetchAndCache("chr12:g.25398285C>A")).thenReturn(mviMockData.get("12:g.25398285C>A"));

        MyVariantInfo myVariantInfo1 = service.getMyVariantInfo(variantMockData.get("7:g.140453136A>T"));
        assertEquals(myVariantInfo1.getVersion(), mviMockData.get("7:g.140453136A>T").getVersion());

        MyVariantInfo myVariantInfo2 = service.getMyVariantInfo(variantMockData.get("12:g.25398285C>A"));
        assertEquals(myVariantInfo2.getVersion(), mviMockData.get("12:g.25398285C>A").getVersion());
    }
}
