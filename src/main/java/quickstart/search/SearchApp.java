package quickstart.search;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * @author	Biswajeet.Basumatary
 * @date	Friday, 21 October 2097 20:11:12
 * @project	quickstart
 *
 */

public class SearchApp {

	private static final String resourceHost = "https://osdu-demo-portal-dev.azure-api.net";
	
	public SearchApp() {
	}

	/*
	 * bearerToken comes from the quickstart.auth.App class, getBearerToken method
	 */
	
	public static String findAWell(String bearerToken) {
		BufferedReader reader = null;
		String response = null;
		try {
			URL url = new URL(resourceHost + "/indexSearch");
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + bearerToken);
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.connect();

			// sending the JSON body
			OutputStream os = connection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");

			osw.write("{\r\n" + "	\"fulltext\": \"*\",\r\n" + "	\"aggregates_count\": 1000,\r\n"
					+ "	\"start\": 0,\r\n" + "	\"count\": 1,\r\n" + "	\"metadata\": {\r\n" + "		\"srn\": [\r\n"
					+ "		\"srn:master-data/Well:1016:\"\r\n" + "		]\r\n" + "	},\r\n" + "	\"facets\": [\r\n"
					+ "		\"resource_type\"\r\n" + "	],\r\n" + "	\"full_results\": true\r\n" + "}");
			osw.flush();
			osw.close();

			reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line = null;
			StringWriter out = new StringWriter(
					connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
			while ((line = reader.readLine()) != null) {
				out.append(line);
			}
			response = out.toString();
			// System.out.println(response);
		} catch (Exception e) {
			// handle exception
		}
		return response;
	}
}
