package org.cbioportal.genome_nexus.component.mixin.my_variant_info_mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public class GeneMixin
{
    @JsonProperty(value = "geneid", required = false)
    private String geneId;

    @JsonProperty(value = "symbol", required = false)
    private String symbol;
}
