package returnstrackingsystem.dtos.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.lang.Nullable;
import returnstrackingsystem.customvalidation.Email;


import java.util.List;



@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SendOutlookRequest {

    @JsonProperty("email")
    @Email
    @NonNull
    private String email;

    @JsonProperty("recipients")
    @Nullable
    private List<String> recipients;

    @JsonProperty("microsoftToken")
    @Nullable
    private String microsoftToken;
}
