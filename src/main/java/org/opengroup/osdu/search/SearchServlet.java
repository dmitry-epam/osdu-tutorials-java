package org.opengroup.osdu.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SearchServlet extends HttpServlet {

    private static final String INDEX_SEARCH_PATH = "/indexSearch";

    private String searchApiUrl;

    private String requestBodyTemplate;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void init() {
        searchApiUrl = System.getenv("OSDU_API_BASE_URL") + INDEX_SEARCH_PATH;

        try {
            requestBodyTemplate = Files.readString(Paths.get(getClass().getClassLoader().getResource("index-search-template.json").toURI()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String wellName = req.getParameter("wellname");
        List<Object> wellData = searchWellData(wellName);
        Map<String, Object> groupedWellData = groupWellData(wellData);
        writeResponse(resp, groupedWellData);
    }

    private List<Object> searchWellData(String wellName) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(searchApiUrl);

            httpPost.setEntity(new StringEntity(
                    String.format(requestBodyTemplate, wellName)
            ));

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = client.execute(httpPost);

            String jsonResult = IOUtils.readInputStreamToString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8
            );

            return (List) mapper.readValue(jsonResult, Map.class)
                    .getOrDefault("results", Collections.emptyList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, Object> groupWellData(List<Object> wellData) {
        Map<String, Object> result = new HashMap<>();

        wellData.forEach(e -> {
            Map<String, Object> wdEntry = (Map) e;
            String resourceType = (String) wdEntry.get("resource_type");

            if (!result.containsKey(resourceType)) {
                result.put(resourceType, new ArrayList<>());
            }

            List<Object> existingFiles = (List) result.get(resourceType);
            existingFiles.addAll((List) wdEntry.get("files"));
        });

        return result;
    }

    private void writeResponse(HttpServletResponse resp, Map<String, Object> groupedWellData) {
        resp.setContentType("application/json");

        try {
            PrintWriter writer = resp.getWriter();
            String body = mapper.writeValueAsString(groupedWellData);
            writer.write(body);
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
