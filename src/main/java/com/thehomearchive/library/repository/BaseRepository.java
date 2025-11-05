package com.thehomearchive.library.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface that extends JpaRepository with additional common methods.
 * This interface provides a foundation for all entity repositories in the application.
 * 
 * @param <T> Entity type
 * @param <ID> Entity ID type
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    
    /**
     * Find entities by a list of IDs.
     * 
     * @param ids List of entity IDs
     * @return List of entities matching the IDs
     */
    List<T> findByIdIn(List<ID> ids);
    
    /**
     * Find entities by IDs with pagination.
     * 
     * @param ids List of entity IDs
     * @param pageable Pagination parameters
     * @return Page of entities matching the IDs
     */
    Page<T> findByIdIn(List<ID> ids, Pageable pageable);
    
    /**
     * Find entities by IDs with sorting.
     * 
     * @param ids List of entity IDs
     * @param sort Sorting parameters
     * @return List of entities matching the IDs with applied sorting
     */
    List<T> findByIdIn(List<ID> ids, Sort sort);
    
    /**
     * Check if an entity exists by ID without loading it.
     * This is more efficient than findById for existence checks.
     * 
     * @param id Entity ID
     * @return true if entity exists, false otherwise
     */
    @Override
    boolean existsById(@NonNull ID id);
    
    /**
     * Find an entity by ID with optional result.
     * 
     * @param id Entity ID
     * @return Optional containing the entity if found
     */
    @Override
    @NonNull
    Optional<T> findById(@NonNull ID id);
    
    /**
     * Get total count of entities in the repository.
     * 
     * @return Total count of entities
     */
    @Override
    long count();
    
    /**
     * Delete entity by ID.
     * 
     * @param id Entity ID to delete
     */
    @Override
    void deleteById(@NonNull ID id);
    
    /**
     * Delete entities by a list of IDs.
     * 
     * @param ids List of entity IDs to delete
     */
    void deleteByIdIn(List<ID> ids);
    
    /**
     * Save and flush entity immediately.
     * 
     * @param entity Entity to save
     * @return Saved entity
     */
    @Override
    @NonNull
    <S extends T> S saveAndFlush(@NonNull S entity);
    
    /**
     * Save all entities and flush immediately.
     * 
     * @param entities List of entities to save
     * @return List of saved entities
     */
    @Override
    @NonNull
    <S extends T> List<S> saveAllAndFlush(@NonNull Iterable<S> entities);
}