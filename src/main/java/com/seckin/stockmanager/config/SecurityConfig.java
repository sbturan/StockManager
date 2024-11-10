package com.seckin.stockmanager.config;

import com.seckin.stockmanager.model.Customer;
import com.seckin.stockmanager.service.CustomerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomerService customerService;

    public SecurityConfig(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf((csrf) -> csrf.disable())
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers( "/register").permitAll()
                                .requestMatchers("/orders/match/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())  // HTTP Basic Authentication
                .headers((headers) ->
                        headers
                                .frameOptions((frameOptions) -> frameOptions.disable())
                );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Customer customer = customerService.getCustomer(username);
            if (customer == null) {
                throw new UsernameNotFoundException("Customer not found");
            }
            return User.builder()
                    .username(customer.getUsername())
                    .password(customer.getPassword())
                    .roles(customer.getRole().toString())
                    .build();
        };
    }
}