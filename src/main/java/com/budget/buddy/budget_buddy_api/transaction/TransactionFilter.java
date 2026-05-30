package com.budget.buddy.budget_buddy_api.transaction;

import lombok.Builder;
import lombok.With;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;
import java.util.UUID;

@With
@Builder
public record TransactionFilter(
    UUID ownerId,
    @Nullable UUID categoryId,
    @Nullable LocalDate start,
    @Nullable LocalDate end,
    @Nullable TransactionType type,
    @Nullable String query,
    @Nullable Long amountMin,
    @Nullable Long amountMax
) {}
