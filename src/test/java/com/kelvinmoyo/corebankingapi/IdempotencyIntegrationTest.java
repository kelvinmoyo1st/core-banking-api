package com.kelvinmoyo.corebankingapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kelvinmoyo.corebankingapi.entity.Account;
import com.kelvinmoyo.corebankingapi.entity.AccountType;
import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.repository.AccountRepository;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import com.kelvinmoyo.corebankingapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IdempotencyIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private final ObjectMapper mapper = new ObjectMapper();

    private String token;
    private Long accountId;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer("Idem", "Test", "idem-itest@test.com", passwordEncoder.encode("password123"));
        customerRepository.save(customer);
        token = jwtService.generateToken(customer.getEmail(), "CUSTOMER");

        Account account = new Account("ACC-ITEST01", customer, AccountType.CHECKING, BigDecimal.ZERO);
        accountRepository.save(account);
        accountId = account.getId();
    }

    @Test
    void duplicateIdempotencyKey_returnsIdenticalCachedResponse() throws Exception {
        String body = "{\"accountId\":" + accountId + ",\"amount\":100.00}";

        MvcResult first = mockMvc.perform(post("/accounts/deposit")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "itest-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        MvcResult second = mockMvc.perform(post("/accounts/deposit")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "itest-key-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        String firstBody = first.getResponse().getContentAsString();
        String secondBody = second.getResponse().getContentAsString();
        assertEquals(firstBody, secondBody);
    }

    @Test
    void differentIdempotencyKey_processesAsSeparateDeposit() throws Exception {
        String body = "{\"accountId\":" + accountId + ",\"amount\":50.00}";

        mockMvc.perform(post("/accounts/deposit")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "itest-key-a")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        MvcResult second = mockMvc.perform(post("/accounts/deposit")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", "itest-key-b")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = mapper.readTree(second.getResponse().getContentAsString());
        assertEquals(0, node.get("balanceAfter").decimalValue().compareTo(new BigDecimal("100.00")));
    }
}
