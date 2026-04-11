package com.budget.buddy.budget_buddy_api.base.crudl.base;

import java.io.Serializable;
import java.util.Objects;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Custom {@link Pageable} implementation that supports offset and limit as required by OpenAPI.
 */
public class OffsetPageRequest implements Pageable, Serializable {

  private final int limit;
  private final int offset;
  private final Sort sort;

  public OffsetPageRequest(int offset, int limit) {
    this(offset, limit, Sort.unsorted());
  }

  public OffsetPageRequest(int offset, int limit, Sort sort) {
    if (offset < 0) {
      throw new IllegalArgumentException("Offset index must not be less than zero!");
    }
    if (limit < 1) {
      throw new IllegalArgumentException("Limit must not be less than one!");
    }
    this.offset = offset;
    this.limit = limit;
    this.sort = sort;
  }

  @Override
  public int getPageNumber() {
    return offset / limit;
  }

  @Override
  public int getPageSize() {
    return limit;
  }

  @Override
  public long getOffset() {
    return offset;
  }

  @Override
  public Sort getSort() {
    return sort;
  }

  @Override
  public Pageable next() {
    return new OffsetPageRequest(offset + limit, limit, sort);
  }

  @Override
  public Pageable previousOrFirst() {
    return hasPrevious() ? new OffsetPageRequest(offset - limit, limit, sort) : first();
  }

  @Override
  public Pageable first() {
    return new OffsetPageRequest(0, limit, sort);
  }

  @Override
  public Pageable withPage(int pageNumber) {
    return new OffsetPageRequest(pageNumber * limit, limit, sort);
  }

  @Override
  public boolean hasPrevious() {
    return offset >= limit;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OffsetPageRequest that)) {
      return false;
    }
    return limit == that.limit && offset == that.offset && Objects.equals(sort, that.sort);
  }

  @Override
  public int hashCode() {
    return Objects.hash(limit, offset, sort);
  }
}
