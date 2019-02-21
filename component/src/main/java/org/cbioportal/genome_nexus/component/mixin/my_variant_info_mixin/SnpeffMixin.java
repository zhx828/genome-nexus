package org.cbioportal.genome_nexus.component.mixin.my_variant_info_mixin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SnpeffMixin
{
    @JsonProperty(value = "license", required = true)
    private String license;

    // @JsonProperty(value = "ann", required = true)
    // private List<Ann> ann;
}
