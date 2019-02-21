package org.cbioportal.genome_nexus.component.test.mock;

import org.cbioportal.genome_nexus.model.GeneXref;
import org.cbioportal.genome_nexus.model.VariantAnnotation;
import org.cbioportal.genome_nexus.component.config.ExternalResourceObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

public class JsonToObjectMapper
{
    private final ExternalResourceObjectMapper objectMapper;

    public JsonToObjectMapper()
    {
        this.objectMapper = new ExternalResourceObjectMapper();
    }

    public VariantAnnotation readVariantAnnotation(String resourceName) throws IOException
    {
        return this.objectMapper.readValue(
            new ClassPathResource("variant/" + resourceName).getInputStream(),
            VariantAnnotation.class
        );
    }

    public List<GeneXref> readGeneXrefs(String resourceName) throws IOException
    {
        return this.objectMapper.readValue(
            new ClassPathResource("xref/" + resourceName).getInputStream(),
            this.objectMapper.getTypeFactory().constructCollectionType(List.class, GeneXref.class)
        );
    }
}
