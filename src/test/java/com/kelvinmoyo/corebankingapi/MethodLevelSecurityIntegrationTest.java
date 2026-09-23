package com.kelvinmoyo.corebankingapi;

import com.kelvinmoyo.corebankingapi.entity.Customer;
import com.kelvinmoyo.corebankingapi.entity.Role;
import com.kelvinmoyo.corebankingapi.repository.CustomerRepository;
import com.kelvinmoyo.corebankingapi.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MethodLevelSecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String tokenFor(String email, Role role) {
        Customer customer = new Customer("Test", "User", email, passwordEncoder.encode("password123"));
        ReflectionTestUtils.setField(customer, "role", role);
        customerRepository.save(customer);
        return jwtService.generateToken(email, role.name());
    }

    @Test
    void adminEndpoint_rejectsCustomerRole() throws Exception {
        String token = tokenFor("customer-sec-1@test.com", Role.CUSTOMER);

        mockMvc.perform(get("/accounts/admin/all-accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoint_allowsAdminRole() throws Exception {
        String token = tokenFor("admin-sec-1@test.com", Role.ADMIN);

        mockMvc.perform(get("/accounts/admin/all-accounts")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void selfProfileEndpoint_allowsCustomerRole() throws Exception {
        String token = tokenFor("customer-sec-2@test.com", Role.CUSTOMER);

        mockMvc.perform(get("/customers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void selfProfileEndpoint_allowsAdminRole() throws Exception {
        String token = tokenFor("admin-sec-2@test.com", Role.ADMIN);

        mockMvc.perform(get("/customers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_rejectsMissingToken() throws Exception {
        mockMvc.perform(get("/accounts/admin/all-accounts"))
                .andExpect(status().isForbidden());
    }
}
