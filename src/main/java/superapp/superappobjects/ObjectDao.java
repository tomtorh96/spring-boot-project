package superapp.superappobjects;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.transaction.Transactional;

public interface ObjectDao extends JpaRepository<ObjectEntity, String> {
	public List<ObjectEntity> findAllByType(@Param("type") String type, Pageable pageable);

	public List<ObjectEntity> findAllByActive(@Param("active") boolean active, Pageable pageable);

	public List<ObjectEntity> findAllByTypeAndActive(@Param("type") String type, @Param("active") boolean active,
			Pageable pageable);

	public List<ObjectEntity> findAllByAlias(@Param("alias") String alias, Pageable pageable);

	public List<ObjectEntity> findAllByAliasAndActive(@Param("alias") String alias, @Param("active") boolean active,
			Pageable pageable);

	public List<ObjectEntity> findAllByAliasLike(@Param("aliasPatten") String aliasPatten, Pageable pageable);

	@Query(value = "SELECT * FROM object_table WHERE active = :active AND alias LIKE :aliasPatten", nativeQuery = true)
	public List<ObjectEntity> findAllByAliasLikeAndActive(@Param("aliasPatten") String aliasPatten,
			@Param("active") boolean active, Pageable pageable);
	// finds a distance between a earth curve with kilometer or miles
	@Query(value = "SELECT o FROM ObjectEntity o WHERE "
			+ "(:units * acos(cos(radians(:latitude)) * cos(radians(o.latitude))"
			+ " * cos(radians(o.longitude) - radians(:longitude)) + sin(radians(:latitude))"
			+ " * sin(radians(o.latitude)))) <= :distance")
	public List<ObjectEntity> findAllBylocationBetweenCurve(@Param("latitude") double lat,
			@Param("longitude") double lng, @Param("distance") double distance, @Param("units") double units,
			Pageable pageable);
	// finds a distance between a earth curve with kilometer or miles
	@Query(value = "SELECT o FROM ObjectEntity o WHERE "
			+ "(:units * acos(cos(radians(:latitude)) * cos(radians(o.latitude))"
			+ " * cos(radians(o.longitude) - radians(:longitude)) + sin(radians(:latitude))"
			+ " * sin(radians(o.latitude)))) <= :distance AND active = :active")
	public List<ObjectEntity> findAllBylocationBetweenCurveAndActive(@Param("latitude") double lat,
			@Param("longitude") double lng, @Param("distance") double distance, @Param("units") double units,
			@Param("active") boolean active, Pageable pageable);

	
	@Query(value = "SELECT o FROM ObjectEntity o WHERE "
			+ "POWER(o.latitude - :latitude, 2) + POWER(o.longitude - :longitude, 2)" + " <= POWER(:distance, 2)")
	public List<ObjectEntity> findAllBylocationBetween(@Param("latitude") double lat, @Param("longitude") double lng,
			@Param("distance") double distance, Pageable pageable);

	
	@Query(value = "SELECT o FROM ObjectEntity o WHERE "
			+ "POWER(o.latitude - :latitude, 2) + POWER(o.longitude - :longitude, 2)"
			+ " <= POWER(:distance, 2) AND active = :active")
	public List<ObjectEntity> findAllBylocationBetweenAndActive(@Param("latitude") double lat,
			@Param("longitude") double lng, @Param("distance") double distance, @Param("active") boolean active,
			Pageable pageable);

	public Optional<ObjectEntity> findByCreatedBy(@Param("createdBy") String createdBy);

	public Optional<ObjectEntity> findByTypeAndProjectNameAndActive(@Param("type") String type,
			@Param("projectName") String projectName, @Param("active") boolean active);

	public List<ObjectEntity> findAllByTypeAndProjectNameAndActive(@Param("type") String type,
			@Param("projectName") String projectName, @Param("active") boolean active, Pageable pageable);
	
	public List<ObjectEntity> findAllByEventDateBetweenAndActiveAndProjectName(
			@Param("minEventDate") Date startDate, @Param("maxEventDate") Date endDate, @Param("active") boolean active,
			@Param("projectName") String projectName);

	@Query(value = "SELECT * FROM object_table  WHERE type = :type And active = :active AND member_list LIKE :memberList", nativeQuery = true)
	public List<ObjectEntity> findAllByTypeAndActiveAndByMemberListContaining(@Param("type") String type,
			@Param("active") boolean active, @Param("memberList") String member);

	@Transactional
	@Modifying
	@Query("UPDATE ObjectEntity  SET memberList = REPLACE(memberList, :emailToRemove, '') "
			+ "WHERE memberList LIKE %:emailToRemove% AND type = :type AND active = :active "
			+ "AND projectName = :projectName")
	public void updateMemberList(@Param("type") String type, @Param("active") boolean active,
			@Param("projectName") String projectName, @Param("emailToRemove") String emailToRemove);

	@Query("SELECT e FROM ObjectEntity e "
			+ "WHERE EXTRACT(YEAR FROM e.eventDate) = EXTRACT(YEAR FROM CAST(:newDate AS timestamp)) "
			+ "AND EXTRACT(MONTH FROM e.eventDate) = EXTRACT(MONTH FROM CAST(:newDate AS timestamp)) "
			+ "AND EXTRACT(DAY FROM e.eventDate) = EXTRACT(DAY FROM CAST(:newDate AS timestamp)) "
			+ "AND e.active = :active " + "AND ((e.startTime < :endTime AND e.endTime >= :endTime) "
			+ "     OR (:startTime >= e.startTime AND :startTime < e.endTime) 	OR (:startTime < e.endTime AND :endTime >= e.endTime) OR (e.startTime >= :startTime AND e.startTime < :endTime) )")
	public List<ObjectEntity> checkEventConstraints(@Param("newDate") Date date, @Param("startTime") double startTime,
			@Param("endTime") double endTime, @Param("active") boolean active);
	
	@Query("SELECT e FROM ObjectEntity e "
			+ "WHERE EXTRACT(YEAR FROM e.eventDate) = EXTRACT(YEAR FROM CAST(:newDate AS timestamp)) "
			+ "AND EXTRACT(MONTH FROM e.eventDate) = EXTRACT(MONTH FROM CAST(:newDate AS timestamp)) "
			+ "AND EXTRACT(DAY FROM e.eventDate) = EXTRACT(DAY FROM CAST(:newDate AS timestamp)) "
			+ "AND e.active = :active " +"And e.id != :id "+ "AND ((e.startTime < :endTime AND e.endTime >= :endTime) "
			+ "     OR (:startTime >= e.startTime AND :startTime < e.endTime) 	OR (:startTime < e.endTime AND :endTime >= e.endTime) OR (e.startTime >= :startTime AND e.startTime < :endTime) )")
	public List<ObjectEntity> checkEventConstraints(@Param("newDate") Date date, @Param("startTime") double startTime,
			@Param("endTime") double endTime, @Param("active") boolean active, @Param("id") String id);
	
	@Modifying
	@Query("UPDATE ObjectEntity SET active = false WHERE projectName = :projectName")
	public void deactivateAllObjectRelatedToProjectName(@Param("projectName") String projectName);

	

}
