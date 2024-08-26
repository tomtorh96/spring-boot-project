package superapp.command;


import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;





public interface CommandDao extends JpaRepository<CommandEntity, String>{
	
	public List<CommandEntity> findAllByMiniAppName(@Param("miniAppName") String miniAppName,Pageable pageable);

}