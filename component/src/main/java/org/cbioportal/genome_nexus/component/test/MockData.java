package org.cbioportal.genome_nexus.component.test;

import java.io.IOException;
import java.util.Map;

public interface MockData<T>
{
    Map<String, T> generateData() throws IOException;
}
