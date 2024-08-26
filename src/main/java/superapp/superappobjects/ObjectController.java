package superapp.superappobjects;


import java.util.Optional;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import exception.NotFoundException;



@RestController
@RequestMapping(path = { "/superapp/objects" })
public class ObjectController {
	private EnhancedObjectService superAppService;
	
	public ObjectController(EnhancedObjectService superAppService) {
		this.superAppService = superAppService;
	}
	@PostMapping(
		consumes = MediaType.APPLICATION_JSON_VALUE, 
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectBoundary store(@RequestBody ObjectBoundary newSuperApp) {
		return this.superAppService
			.createAnObject(newSuperApp);
	}

	@GetMapping(
		path = { "/{superapp}/{id}"}, 
		produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectBoundary getSpecificObject(
			@PathVariable("superapp") String superapp,
			@PathVariable("id") String id,
			@RequestParam(name = "userSuperapp") String userSuperapp,
			@RequestParam(name = "userEmail") String email) {
		Optional<ObjectBoundary> demoOp = this.superAppService
			.retrieveObject(superapp,id,userSuperapp,email);
		
		if (demoOp.isPresent()) {
			return demoOp.get();
		}else {
			throw new NotFoundException("could not find object by id: " + id);
		}
	}
	@PutMapping(
			path = {"/{superapp}/{id}"},
			consumes = {MediaType.APPLICATION_JSON_VALUE})
		public void update (
				@PathVariable("superapp") String superApp,
				@PathVariable("id") String id,
				@RequestBody ObjectBoundary update,
				@RequestParam(name = "userSuperapp") String userSuperapp,
				@RequestParam(name = "userEmail") String email) {
			this.superAppService
				.updateAnObject(superApp, id, userSuperapp, email, update);
	
	}

	@GetMapping(
			produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectBoundary[] getAllObjects(
			@RequestParam(name = "userSuperapp") String userSuperapp,
			@RequestParam(name = "userEmail") String email,
			@RequestParam(name = "size", required = false, defaultValue = "10") int size,
			@RequestParam(name = "page", required = false, defaultValue = "0") int page) {
		return this.superAppService
			.getAllObjects(userSuperapp,email,size,page)
			.toArray(new ObjectBoundary[0]);
	}
	
}





