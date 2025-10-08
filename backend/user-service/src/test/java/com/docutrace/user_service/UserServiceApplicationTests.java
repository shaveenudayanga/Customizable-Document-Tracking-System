package com.docutrace.user_service;

import com.docutrace.user_service.dto.UpdateProfileRequest;
import com.docutrace.user_service.dto.UserResponse;
import com.docutrace.user_service.entity.Role;
import com.docutrace.user_service.entity.User;
import com.docutrace.user_service.repository.UserRepository;
import com.docutrace.user_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
	"security.jwt.secret=test-secret-key-for-tests-1234567890123456",
	"spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
class UserServiceApplicationTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserService userService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void updateProfileShouldPersistPositionAndSection() {
		User user = new User();
		user.setUsername("john.doe");
		user.setPassword("password");
		user.setEmail("john.doe@example.com");
		user.setRole(Role.USER);
		userRepository.save(user);

		UpdateProfileRequest request = new UpdateProfileRequest(
			"john.doe@example.com",
			"Operations Manager",
			"operations"
		);

		UserResponse response = userService.updateProfile("john.doe", request);

		assertThat(response.position()).isEqualTo("Operations Manager");
		assertThat(response.sectionId()).isEqualTo("operations");
		assertThat(response.email()).isEqualTo("john.doe@example.com");

		User reloaded = userRepository.findByUsername("john.doe").orElseThrow();
		assertThat(reloaded.getPosition()).isEqualTo("Operations Manager");
		assertThat(reloaded.getSectionId()).isEqualTo("operations");
	}

}
