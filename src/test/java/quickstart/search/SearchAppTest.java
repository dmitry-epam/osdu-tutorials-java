package quickstart.search;

import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.Test;
import quickstart.auth.App;

class SearchAppTest {

	@Test
	void testFindAWell() {
		assertNotNull(SearchApp.findAWell(App.getBearerToken()));
	}

}
