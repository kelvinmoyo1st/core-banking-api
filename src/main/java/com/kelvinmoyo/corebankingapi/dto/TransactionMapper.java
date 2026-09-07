package com.kelvinmoyo.corebankingapi.dto;

import com.kelvinmoyo.corebankingapi.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    TransactionResponse toResponse(Transaction transaction);
}
