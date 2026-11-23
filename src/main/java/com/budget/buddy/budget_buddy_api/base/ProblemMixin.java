package com.budget.buddy.budget_buddy_api.base;

import com.budget.buddy.budget_buddy_contracts.generated.model.FieldError;
import com.budget.buddy.budget_buddy_contracts.generated.model.Problem;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import org.springframework.boot.jackson.JacksonMixin;

@JacksonMixin(Problem.class)
interface ProblemMixin {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  List<FieldError> errors();
}
