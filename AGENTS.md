# MapStruct & JsonNullable Conventions

## PATCH Operations
All PATCH operations use `BaseEntityMapper.patchEntity` which is configured to:
- Ignore fields that are not provided in the request (`null` or `undefined` JsonNullable).
- Propagate explicit `null` values to the entity when using `JsonNullable.of(null)`.

This is implemented using MapStruct `@Condition` and `NullValuePropertyMappingStrategy.IGNORE`.

### Mapper Configuration
- Mappers should extend `BaseEntityMapper`.
- `BaseEntityMapper` provides a generic `isPresent(Object)` condition that handles both `JsonNullable` and regular objects.
- The mapping from `JsonNullable<T>` to `T` is handled by `fromNullable(JsonNullable<T>)` in `BaseEntityMapper`.

