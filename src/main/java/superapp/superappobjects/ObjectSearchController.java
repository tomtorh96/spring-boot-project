package superapp.superappobjects;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping(path = { "/superapp/objects/search" })
public class ObjectSearchController {
	private EnhancedObjectService objectService;

	public ObjectSearchController(EnhancedObjectService objectService) {
		this.objectService = objectService;

	}

	@GetMapping(path = { "/byType/{type}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ObjectBoundary[] searchByType(@PathVariable("type") String type,
			@RequestParam(name = "userSuperapp") String superapp, @RequestParam(name = "userEmail") String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.objectService.searchByType(type, size, superapp, email, page).toArray(new ObjectBoundary[0]);
	}

	@GetMapping(path = { "/byAlias/{alias}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ObjectBoundary[] searchByAlias(@PathVariable("alias") String alias,
			@RequestParam(name = "userSuperapp") String superapp, @RequestParam(name = "userEmail") String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.objectService.searchByAlias(alias, superapp, email, size, page).toArray(new ObjectBoundary[0]);
	}

	@GetMapping(path = { "/byAliasPattern/{pattern}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ObjectBoundary[] searchByAliaPattern(@PathVariable("pattern") String Pattern,
			@RequestParam(name = "userSuperapp") String superapp, @RequestParam(name = "userEmail") String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.objectService.searchByAliasPattern(Pattern, superapp, email, size, page)
				.toArray(new ObjectBoundary[0]);
	}

	@GetMapping(path = { "/byLocation/{lat}/{lng}/{distance}" }, produces = { MediaType.APPLICATION_JSON_VALUE })
	public ObjectBoundary[] searchBylocation(@PathVariable("lat") double lat, @PathVariable("lng") double lng,
			@PathVariable("distance") double distance,
			@RequestParam(name = "units", required = false, defaultValue = "neutral") String unit,
			@RequestParam(name = "userSuperapp") String superapp, 
			@RequestParam(name = "userEmail") String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.objectService.searchByLocation(lat, lng, distance, unit, superapp, email, size, page)
				.toArray(new ObjectBoundary[0]);
	}

}
