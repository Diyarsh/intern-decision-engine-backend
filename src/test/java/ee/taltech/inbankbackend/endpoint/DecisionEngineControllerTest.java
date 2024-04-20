package ee.taltech.inbankbackend.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.taltech.inbankbackend.exceptions.*;
import ee.taltech.inbankbackend.service.Decision;
import ee.taltech.inbankbackend.service.DecisionEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith({SpringExtension.class, MockitoExtension.class})
public class DecisionEngineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DecisionEngine decisionEngine;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void givenValidRequest_whenRequestDecision_thenReturnsExpectedResponse()
            throws Exception {
        Decision decision = new Decision(1000, 12, null);
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt())).thenReturn(decision);

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").value(1000))
                .andExpect(jsonPath("$.loanPeriod").value(12))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    public void givenInvalidPersonalCode_whenRequestDecision_thenReturnsBadRequest()
            throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidPersonalCodeException("Invalid personal code"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").doesNotExist())
                .andExpect(jsonPath("$.loanPeriod").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("Invalid personal code"));
    }

    @Test
    public void givenInvalidLoanAmount_whenRequestDecision_thenReturnsBadRequest()
            throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidLoanAmountException("Invalid loan amount"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").doesNotExist())
                .andExpect(jsonPath("$.loanPeriod").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan amount"));
    }

    @Test
    public void givenInvalidLoanPeriod_whenRequestDecision_thenReturnsBadRequest()
            throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new InvalidLoanPeriodException("Invalid loan period"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").doesNotExist())
                .andExpect(jsonPath("$.loanPeriod").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("Invalid loan period"));
    }

    @Test
    public void givenNoValidLoan_whenRequestDecision_thenReturnsBadRequest()
            throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new NoValidLoanException("No valid loan available"));

        DecisionRequest request = new DecisionRequest("1234", 1000L, 12);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").doesNotExist())
                .andExpect(jsonPath("$.loanPeriod").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("No valid loan available"));
    }

    @Test
    public void givenUnexpectedError_whenRequestDecision_thenReturnsInternalServerError()
            throws Exception {
        when(decisionEngine.calculateApprovedLoan(anyString(), anyLong(), anyInt()))
                .thenThrow(new RuntimeException("An unexpected error occurred"));

        DecisionRequest request = new DecisionRequest("1234", 10L, 10);

        mockMvc.perform(post("/loan/decision")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.loanAmount").doesNotExist())
                .andExpect(jsonPath("$.loanPeriod").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").value("An unexpected error occurred"));
    }
}