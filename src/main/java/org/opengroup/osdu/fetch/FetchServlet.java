package org.opengroup.osdu.fetch;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.nimbusds.jose.util.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static org.opengroup.osdu.utils.Singletons.OBJECT_MAPPER;

@WebServlet(name = "fetchServlet", urlPatterns = {"/fetch"})
public class FetchServlet extends HttpServlet {

    private static final String GET_RESOURCES_PATH = "/GetResources";

    private static final String FETCH_API_URL = System.getenv("OSDU_API_BASE_URL") + GET_RESOURCES_PATH;

    private String requestBodyTemplate;

    @Override
    public void init() {
        try {
            requestBodyTemplate = Files.readString(
                    Paths.get(getClass().getClassLoader().getResource("get-resource-template.json").toURI())
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String srn = req.getParameter("srn");
        Map<String, Object> fileLocation = fetchFileLocation(srn);
        BlobClient blobClient = prepareBlobClient(fileLocation);

        resp.setContentType("text/csv");
        blobClient.download(resp.getOutputStream());
    }

    private Map<String, Object> fetchFileLocation(String srn) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(FETCH_API_URL);

            httpPost.setEntity(new StringEntity(
                    String.format(requestBodyTemplate, srn)
            ));

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            CloseableHttpResponse response = client.execute(httpPost);

            String jsonResult = IOUtils.readInputStreamToString(
                    response.getEntity().getContent(),
                    StandardCharsets.UTF_8
            );

            Map<String, Object> resourcesResult = OBJECT_MAPPER.readValue(jsonResult, Map.class);

            return (Map) ((Map) ((List) resourcesResult.get("Result")).get(0)).get("FileLocation");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private BlobClient prepareBlobClient(Map<String, Object> fileLocation) {
        return new BlobClientBuilder()
                .endpoint((String) fileLocation.get("EndPoint"))
                .sasToken((String) ((Map) fileLocation.get("TemporaryCredentials")).get("SAS"))
                .containerName((String) fileLocation.get("Bucket"))
                .blobName((String) fileLocation.get("Key"))
                .buildClient();
    }
}
