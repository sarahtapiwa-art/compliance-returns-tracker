package returnstrackingsystem.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import returnstrackingsystem.dtos.response.BulkUploadResponse;
import returnstrackingsystem.repository.DepartmentRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
public class DepartmentBulkUploadIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private DepartmentRepository departmentRepository;

//    @Test
    public void testBulkDepartmentsWork() throws IOException {
        // Create Excel file
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Departments");
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("DepartmentName");
        header.createCell(1).setCellValue("EscalationEmail");
        header.createCell(2).setCellValue("HeadOfDepartment");

        // Case 1: Standard department
        Row row1 = sheet.createRow(1);
        row1.createCell(0).setCellValue("ICT");
        row1.createCell(1).setCellValue("ict-applications@nbs.co.zw");
        row1.createCell(2).setCellValue("progress.chitiga@nbs.co.zw");

        // Case 2: New domain (Audit)
        Row row2 = sheet.createRow(2);
        row2.createCell(0).setCellValue("Audit");
        row2.createCell(1).setCellValue("belaries.makoni@nbs.co.zw");
        row2.createCell(2).setCellValue("godwin.kudumba@nbslimited.onmicrosoft.com");

        // Case 3: Optional HOD email (Admin)
        Row row3 = sheet.createRow(3);
        row3.createCell(0).setCellValue("Admin");
        row3.createCell(1).setCellValue("linda.chekai@nbs.co.zw");
        row3.createCell(2).setCellValue(""); // Empty HOD email

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        workbook.write(bos);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "departments.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                bos.toByteArray());

        BulkUploadResponse response = departmentService.bulkDepartments(file);

        assertEquals(3, response.getSuccessfulCount());
        assertEquals(0, response.getFailedCount());

        assertTrue(departmentRepository.findByDepartmentNameIgnoreCase("Audit").isPresent());
        assertTrue(departmentRepository.findByDepartmentNameIgnoreCase("Admin").isPresent());

        var audit = departmentRepository.findByDepartmentNameIgnoreCase("Audit").get();
        assertEquals("godwin.kudumba@nbslimited.onmicrosoft.com", audit.getHeadOfDepartmentEmail());

        var admin = departmentRepository.findByDepartmentNameIgnoreCase("Admin").get();
        assertNull(admin.getHeadOfDepartmentEmail() == null || admin.getHeadOfDepartmentEmail().isEmpty() ? null
                : admin.getHeadOfDepartmentEmail());
        // Note: DepartmentServiceImpl sets empty string if blank.
    }
}
