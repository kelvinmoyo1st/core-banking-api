package com.kelvinmoyo.corebankingapi.dto;

import com.kelvinmoyo.corebankingapi.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);
}
