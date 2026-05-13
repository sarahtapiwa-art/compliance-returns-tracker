package returnstrackingsystem.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class RiskAssessmentService {

    @Value("${ml.service.url:http://localhost:5000}")
    private String mlServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, Object> assessRisk(String dueAt, String frequency, String status, double deptOverdueRate) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("dueAt", dueAt);
            payload.put("frequency", frequency);
            payload.put("status", status);
            payload.put("deptOverdueRate", deptOverdueRate);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    mlServiceUrl + "/predict/risk", request, String.class);

            JsonNode node = objectMapper.readTree(response.getBody());

            Map<String, Object> result = new HashMap<>();
            result.put("riskLevel", node.get("risk_level").asText());
            result.put("riskScore", node.get("risk_score").asDouble());
            result.put("color", node.get("color").asText());
            result.put("daysRemaining", node.get("daysRemaining").asInt());
            return result;

        } catch (Exception e) {
            log.warn("ML service unavailable, using default risk: {}", e.getMessage());
            Map<String, Object> defaultResult = new HashMap<>();
            defaultResult.put("riskLevel", "UNKNOWN");
            defaultResult.put("riskScore", 0.0);
            defaultResult.put("color", "#6b7280");
            defaultResult.put("daysRemaining", 0);
            return defaultResult;
        }
    }
}