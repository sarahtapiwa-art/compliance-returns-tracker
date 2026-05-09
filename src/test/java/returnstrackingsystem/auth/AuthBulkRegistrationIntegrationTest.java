package returnstrackingsystem.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.transaction.annotation.Transactional;
import returnstrackingsystem.auth.service.AuthService;
import returnstrackingsystem.domain.Department;
import returnstrackingsystem.domain.User;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.repository.DepartmentRepository;
import returnstrackingsystem.repository.UserRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthBulkRegistrationIntegrationTest {

        @Autowired
        private AuthService authService;

        @Autowired
        private DepartmentRepository departmentRepository;

        @Autowired
        private UserRepository userRepository;

//        @Test
        void testBulkRegister() throws IOException {
                // Setup: Create a department
                Department dept = departmentRepository.save(Department.builder()
                                .departmentName("IT Department")
                                .build());

                // Create Excel file
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("Users");
                Row header = sheet.createRow(0);
                header.createCell(0).setCellValue("Email");
                header.createCell(1).setCellValue("Role");
                header.createCell(2).setCellValue("Department");
                header.createCell(3).setCellValue("UserName");

                Row row1 = sheet.createRow(1);
                row1.createCell(0).setCellValue("user1@nbs.co.zw");
                row1.createCell(1).setCellValue("User"); // Test case-insensitivity/uppercase conversion
                row1.createCell(2).setCellValue("IT Department");
                row1.createCell(3).setCellValue("user1");

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                workbook.write(bos);
                byte[] bytes = bos.toByteArray();
                workbook.close();

                MockMultipartFile file = new MockMultipartFile("file", "users.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", bytes);

                // Mock logged in user (Super Admin)
                Authentication auth = new UsernamePasswordAuthenticationToken("admin", "pw",
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_SYSTEM_ADMIN")));

                // Execute
                BulkUploadResponse response = authService.bulkRegister(file, auth);

                // Verify
                assertEquals(1, response.getSuccessfulCount());
                assertEquals(0, response.getFailedCount());
                assertFalse(response.getSuccessfulIds().isEmpty());

                Optional<User> userOpt = userRepository.findByUsername("user1");
                assertTrue(userOpt.isPresent());
                User user = userOpt.get();
                assertEquals("user1@nbs.co.zw", user.getEmail());
                assertEquals(dept.getId(), user.getDepartment().getId());
                assertTrue(user.getRoles().size() >= 2);
        }
}
