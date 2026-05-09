package returnstrackingsystem.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import returnstrackingsystem.domain.ReturnDefinition;
import returnstrackingsystem.domain.enums.Frequency;

public interface ReturnDefinitionRepository extends JpaRepository<ReturnDefinition, Long> {

    @Query("SELECT r FROM ReturnDefinition r" +
            " WHERE (:deleted IS NULL" +
            " OR r.deleted = :deleted)")
    Page<ReturnDefinition> findByDeletedFilter(@Param("deleted") Boolean deleted,
                                               Pageable pageable);


    @Query("SELECT rd FROM ReturnDefinition rd " +
            "JOIN FETCH rd.department d " +
            "WHERE 1=1 " +
            "AND (:search IS NULL OR LOWER(rd.title) " +
            "LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:frequency IS NULL OR rd.frequency = :frequency) " +
            "AND (:departmentName IS NULL OR LOWER(d.departmentName)" +
            " LIKE LOWER(CONCAT('%', :departmentName, '%'))) " +
            "AND (:isDeleted IS NULL OR rd.deleted = :isDeleted)")
    Page<ReturnDefinition> findAllWithFilters(
            @Param("search") String search,
            @Param("frequency") Frequency frequency,
            @Param("departmentName") String departmentName,
            @Param("isDeleted") Boolean isDeleted,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE ReturnDefinition rd SET rd.documentId = NULL WHERE rd.id = :id")
    void clearDocumentId(@Param("id") Long id);
}