package com.kelvinmoyo.corebankingapi.dto;

import com.kelvinmoyo.corebankingapi.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "customer.id", target = "customerId")
    AccountResponse toResponse(Account account);
}
