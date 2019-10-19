package quickstart.auth;

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.Test;

class AppTest {


	@Test
	void testGetBearerToken() {	
		assertNotNull(App.getBearerToken());	
	}

}
