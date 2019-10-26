package org.opengroup.osdu.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class Singletons {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final CloseableHttpClient HTTP_CLIENT = HttpClients.createDefault();

    private Singletons() {
        throw new RuntimeException("It's not allowed to create instances of this class");
    }
}
